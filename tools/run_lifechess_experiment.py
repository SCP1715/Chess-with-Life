#!/usr/bin/env python3
"""Run a resumable, time-boxed A/B/C LifeChess self-play experiment."""

import argparse
import csv
import hashlib
import json
import pathlib
import platform
import random
import re
import shutil
import signal
import sys
import time
from datetime import datetime, timezone

from tools import lifechess_console as console
from tools import run_lifechess_selfplay as selfplay


STOP = False
WEIGHTS = {"A": 0.60, "B": 0.25, "C": 0.15}
NODE_BUDGETS = {"A": 10_000, "B": 100_000, "C": 10_000}


def request_stop(_signum, _frame):
    global STOP
    STOP = True


def read_attempts(path):
    if not path.exists():
        return []
    result = []
    with path.open(encoding="utf-8") as stream:
        for line in stream:
            if line.strip():
                result.append(attempt_summary(json.loads(line)))
    return result


def attempt_summary(item):
    return {key: value for key, value in item.items()
            if key not in {"finalState", "finalOfferLatches", "finalKingIds", "finalKingCounters"}}


def append_json(path, item):
    with path.open("a", encoding="utf-8", newline="\n") as stream:
        stream.write(json.dumps(item, ensure_ascii=False, separators=(",", ":")) + "\n")
        stream.flush()


def log_records(path):
    return selfplay.read_json_lines(path)


def extract_prefix(path, move_plies=16):
    actions = []
    moves = 0
    for row in log_records(path):
        if row.get("record") != "ACTION":
            continue
        actions.append(row["action"])
        moves += row["action"]["type"] == "MOVE"
        if moves >= move_plies:
            return actions
    return None


def sequence_hash(actions):
    encoded = json.dumps(actions, sort_keys=True, separators=(",", ":")).encode()
    return hashlib.sha256(encoded).hexdigest()


def load_prefixes(root, attempts):
    prefixes = []
    for item in attempts:
        if item.get("status") in {"ERROR", "IN_PROGRESS"} or item.get("logPath") is None:
            continue
        path = pathlib.Path(item["logPath"])
        if not path.is_absolute():
            path = root / path
        actions = extract_prefix(path)
        if actions is None:
            continue
        prefixes.append({"prefixId": f"A-{item['gameId']:06d}",
                         "sourceGameId": item["gameId"], "seed": item["seed"],
                         "actions": actions, "sha256": sequence_hash(actions),
                         "sourceLog": str(path)})
    return prefixes


def recover_orphan_logs(output, profile, attempts):
    """Register logs left between a move write and the matching attempts append."""
    attempts_file = output / profile / "attempts.jsonl"
    known = {str(pathlib.Path(row.get("logPath", "")).resolve())
             for row in attempts if row.get("logPath")}
    recovered = []
    for path in sorted((output / profile / "games").glob("*.jsonl.gz")):
        if str(path.resolve()) in known:
            continue
        try:
            log = selfplay.read_json_lines(path)
        except (OSError, EOFError, ValueError) as exc:
            append_json(output / "recovery-errors.jsonl",
                        {"profile": profile, "logPath": str(path),
                         "error": "UNREADABLE_OR_PARTIAL_GZIP", "message": str(exc)})
            continue  # preserve the damaged source log and never overwrite it
        start = next((row for row in log if row.get("record") == "START"), None)
        if start is None:
            append_json(output / "recovery-errors.jsonl",
                        {"profile": profile, "logPath": str(path), "error": "START_RECORD_MISSING"})
            continue
        end = next((row for row in reversed(log) if row.get("record") == "END"), None)
        parent_ref = start.get("parentProfileGameId")
        if profile == "C" and parent_ref is None:
            match = re.fullmatch(r"game-([AB])-(\d+)", path.stem.removesuffix(".jsonl"))
            if match:
                parent_ref = f"{match.group(1)}:{int(match.group(2))}"
        if end is not None:
            result = {key: value for key, value in end.items() if key != "record"}
        else:
            actions = [row for row in log if row.get("record") == "ACTION"]
            state = actions[-1]["after"] if actions else start["initialState"]
            ids = {int(square): king_id for square, king_id in
                   (start.get("initialKingIds") or selfplay.king_id_map(start["initialState"])[0]).items()}
            counters = dict(start.get("initialKingCounters") or selfplay.king_id_map(start["initialState"])[1])
            for row in actions:
                selfplay.advance_king_ids(row["before"], row["action"], row["after"], ids, counters)
            latches = start.get("engineOfferLatches", {"WHITE": False, "BLACK": False})
            for row in actions:
                latches = row.get("search", {}).get("offerLatchesAfter", latches)
            plies = sum(action["type"] == "MOVE" for action in state.get("history", []))
            result = {key: start.get(key) for key in ("gameId", "profile", "seed", "prefixId",
                                                        "parentGameId", "parentProfileGameId")}
            result.update(status="COMPLETED" if state.get("result") != "NONE" else "INTERRUPTED",
                          result=state.get("result") if state.get("result") != "NONE" else None,
                          reason=state.get("result") if state.get("result") != "NONE"
                          else "PROCESS_INTERRUPTED_RECOVERED", plies=plies,
                          randomizedPlies=[], finalState=state, finalOfferLatches=latches,
                          finalKingIds=ids, finalKingCounters=counters, recovered=True)
        if profile == "C" and result.get("parentProfileGameId") is None and parent_ref:
            result["parentProfileGameId"] = parent_ref
            selfplay.write_json_line(path, {"record": "END", **result})
        result["logPath"] = str(path.resolve())
        result.setdefault("elapsedSeconds", 0.0)
        result["recovered"] = True
        compact = attempt_summary(result)
        append_json(attempts_file, compact)
        attempts.append(compact)
        recovered.append(compact)
        print(f"Восстановлен журнал {profile}/{path.name}: {result.get('status')} "
              f"{result.get('reason')}; {result.get('plies', 0)} полуходов", flush=True)
    return recovered


def csv_report(root, profile, attempts):
    profile_root = root / profile
    fields = ["gameId", "parentGameId", "seed", "status", "reason", "result", "plies",
              "prefixId", "elapsedSeconds", "logPath"]
    with (profile_root / "games.csv").open("w", encoding="utf-8-sig", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=fields, extrasaction="ignore")
        writer.writeheader()
        writer.writerows(attempts)
    unfinished = [row for row in attempts if row.get("status") != "COMPLETED"]
    with (profile_root / "unfinished-games.csv").open("w", encoding="utf-8-sig", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=fields, extrasaction="ignore")
        writer.writeheader()
        writer.writerows(unfinished)
    done = sum(row.get("status") == "COMPLETED" for row in attempts)
    lines = [f"# Профиль {profile}", "", f"Попыток: {len(attempts)}; завершено: {done}; "
             f"остановлено/ошибки: {len(attempts) - done}.", "",
             "`games.csv` — все попытки, `unfinished-games.csv` — незавершённые. "
             "Полные JSONL gzip-журналы лежат в `games/`.", ""]
    (profile_root / "report.md").write_text("\n".join(lines), encoding="utf-8")
    summary = {"profile": profile, "attempts": len(attempts), "completed": done,
               "interrupted": sum(row.get("status") == "INTERRUPTED" for row in attempts),
               "errors": sum(row.get("status") == "ERROR" for row in attempts),
               "results": {kind: sum(row.get("result") == kind for row in attempts)
                           for kind in ("WHITE_WIN", "BLACK_WIN", "DRAW_AGREEMENT",
                                        "DRAW_FIFTY_MOVES", "DRAW_REPETITION")}}
    (profile_root / "summary.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n",
                                               encoding="utf-8")


def local_deadline(text):
    now = datetime.now().astimezone()
    hour, minute = map(int, text.split(":"))
    deadline = now.replace(hour=hour, minute=minute, second=0, microsecond=0)
    if deadline <= now:
        raise ValueError(f"локальный дедлайн {text} уже прошёл ({now.isoformat()})")
    return deadline


def main():
    global STOP
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--engine", type=pathlib.Path,
                        default=pathlib.Path("third_party/fairy-stockfish/src/stockfish.exe"))
    parser.add_argument("--output", type=pathlib.Path, required=True)
    parser.add_argument("--stop-at-local", required=True, help="локальное время HH:MM")
    parser.add_argument("--analysis-buffer-minutes", type=int, default=15)
    parser.add_argument("--pilot-games", type=int, default=4)
    parser.add_argument("--free-space-reserve-gb", type=int, default=2)
    args = parser.parse_args()
    root = pathlib.Path(__file__).resolve().parents[1]
    engine_path = args.engine.resolve()
    out = args.output.resolve()
    deadline = local_deadline(args.stop_at_local)
    play_stop = deadline.timestamp() - args.analysis_buffer_minutes * 60
    if args.pilot_games < 1 or args.analysis_buffer_minutes < 0:
        parser.error("pilot-games must be positive and analysis buffer non-negative")
    if not engine_path.is_file():
        parser.error("engine executable does not exist: " + str(engine_path))
    out.mkdir(parents=True, exist_ok=True)
    for name in ("pilot", "A", "B", "C"):
        (out / name / "games").mkdir(parents=True, exist_ok=True)
    manifest_path = out / "manifest.json"
    if manifest_path.exists():
        manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
        if manifest.get("engineSha256") != hashlib.sha256(engine_path.read_bytes()).hexdigest():
            raise RuntimeError("движок отличается от версии в manifest.json")
    else:
        manifest = {
            "createdUtc": datetime.now(timezone.utc).isoformat(),
            "localTimezone": str(datetime.now().astimezone().tzinfo),
            "enginePath": str(engine_path),
            "engineSha256": hashlib.sha256(engine_path.read_bytes()).hexdigest(),
            "engineRevision": selfplay.engine_revision(root),
            "engineWorkingTreePatchSha256": selfplay.engine_patch_hash(root),
            "variant": "lifechess", "pythonVersion": platform.python_version(),
            "build": {"compiler": "MSYS2 g++", "target": "x86_64 Windows",
                      "optimization": "-O3", "threads": 1, "hashMb": 16,
                      "nnue": False, "evaluation": "classical"},
            "workers": 1, "threadsPerEngine": 1,
            "memoryPlan": {"engineHashMb": 16, "workerMemoryLimit": "not set",
                           "peakWorkingSetMeasured": False},
            "profiles": {"A": {"nodesPerDecision": 10000, "maxPlies": 300},
                         "B": {"nodesPerDecision": 100000, "maxPlies": 300},
                         "C": {"nodesPerDecision": "inherited from parent A/B game",
                               "maxTotalPlies": 1200, "continuesOnlyNew300PlyGames": True}},
            "timeWeights": WEIGHTS, "opening": {"movePlies": 16, "multiPV": 5,
                                                   "centipawnWindow": 50, "seeded": True},
            "drawPolicy": "unchanged; offer latches are per-side and claims are independent",
            "deadlineLocal": args.stop_at_local,
            "playStopLocal": datetime.fromtimestamp(play_stop).astimezone().isoformat(),
            "analysisBufferMinutes": args.analysis_buffer_minutes,
            "dataFormat": "stream-written gzipped JSONL, before/action/after states",
        }
        manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n",
                                 encoding="utf-8")
    if not (out / "pilot" / "attempts.jsonl").exists():
        engine = console.UciEngine(str(engine_path))
        try:
            for game_id in range(1, args.pilot_games + 1):
                seed = selfplay.BASE_SEED + 700_000 + game_id
                log_path = out / "pilot" / "games" / f"pilot-{game_id:04d}.jsonl.gz"
                print(f"Пилот {game_id}/{args.pilot_games}: начинаю, seed={seed}", flush=True)
                started = time.monotonic()
                result = selfplay.game_loop(game_id, seed, engine, log_path, profile="A",
                                            node_budget=10_000, max_plies=32,
                                            opening_plies=16, opening_multipv=5)
                result.update(logPath=str(log_path), elapsedSeconds=round(time.monotonic() - started, 3))
                append_json(out / "pilot" / "attempts.jsonl", result)
                print(f"Пилот {game_id}: {result['status']} {result.get('reason')}; "
                      f"{result.get('plies')} полуходов", flush=True)
        finally:
            engine.close()
    pilot = read_attempts(out / "pilot" / "attempts.jsonl")
    pilot_prefixes = load_prefixes(out, pilot)
    append_json(out / "pilot" / "pilot-check.jsonl",
                {"attempts": len(pilot), "prefixesWith16Plies": len(pilot_prefixes),
                 "uniquePrefixes": len({item["sha256"] for item in pilot_prefixes})})
    print(f"Пилот проверен: {len(pilot_prefixes)} префиксов 16 полуходов, "
          f"{len({item['sha256'] for item in pilot_prefixes})} уникальных.", flush=True)

    for profile in "ABC":
        (out / profile / "games").mkdir(exist_ok=True)
    attempts = {p: read_attempts(out / p / "attempts.jsonl") for p in "ABC"}
    for profile in "ABC":
        recover_orphan_logs(out, profile, attempts[profile])
    elapsed = {p: sum(float(row.get("elapsedSeconds", 0)) for row in attempts[p]) for p in "ABC"}
    ids = {p: max((int(row["gameId"]) for row in attempts[p]), default=0) for p in "ABC"}
    prefixes = load_prefixes(out, attempts["A"])
    paired_prefixes = set()
    for row in attempts["B"]:
        if not row.get("prefixId") or row.get("status") == "ERROR" or not row.get("logPath"):
            continue
        b_path = pathlib.Path(row["logPath"])
        if not b_path.is_absolute():
            b_path = out / b_path
        source_prefix = next((item for item in prefixes if item["prefixId"] == row["prefixId"]), None)
        if (source_prefix is not None and b_path.exists()
                and extract_prefix(b_path) == source_prefix["actions"]):
            paired_prefixes.add(row["prefixId"])
    c_parents = {row.get("parentProfileGameId") for row in attempts["C"]}
    signal.signal(signal.SIGINT, request_stop)
    signal.signal(signal.SIGTERM, request_stop)
    engine = console.UciEngine(str(engine_path))
    try:
        while not STOP and time.time() < play_stop:
            free_gb = shutil.disk_usage(out).free / (1024 ** 3)
            if free_gb < args.free_space_reserve_gb:
                print(f"Остановка: свободно только {free_gb:.1f} GiB", flush=True)
                break
            available = [p for p in "ABC" if p != "B" or any(x["prefixId"] not in paired_prefixes for x in prefixes)]
            c_candidates = [row for p in "AB" for row in attempts[p]
                            if row.get("reason") == "MAX_PLIES" and row.get("logPath")
                            and f"{p}:{row['gameId']}" not in c_parents]
            if not c_candidates:
                available = [p for p in available if p != "C"]
            if not available:
                available = ["A"]
            profile = min(available, key=lambda p: elapsed[p] / WEIGHTS[p])
            ids[profile] += 1
            game_id = ids[profile]
            seed = selfplay.BASE_SEED + (ord(profile) - ord("A")) * 1_000_000 + game_id
            kwargs = {"profile": profile, "node_budget": NODE_BUDGETS[profile],
                      "max_plies": 1200 if profile == "C" else 300,
                      "opening_plies": 16 if profile in "AB" else 0,
                      "opening_multipv": 5}
            if profile == "B":
                prefix = next(x for x in prefixes if x["prefixId"] not in paired_prefixes)
                kwargs.update(fixed_prefix=prefix["actions"], prefix_id=prefix["prefixId"])
            elif profile == "C":
                parent = c_candidates[0]
                source_profile = parent["profile"]
                NODE_BUDGETS["C"] = NODE_BUDGETS[source_profile]
                ids["C"] = parent["gameId"]
                parent_log = selfplay.read_json_lines(pathlib.Path(parent["logPath"]))
                parent_end = next(row for row in reversed(parent_log) if row.get("record") == "END")
                kwargs.update(start_state=parent_end["finalState"],
                              start_latches=parent_end.get("finalOfferLatches"),
                              king_ids=parent_end.get("finalKingIds"),
                              king_counters=parent_end.get("finalKingCounters"),
                              parent_game_id=parent["gameId"],
                              parent_profile_game_id=f"{source_profile}:{parent['gameId']}")
            game_stem = (f"game-{source_profile}-{game_id:06d}" if profile == "C"
                         else f"game-{game_id:06d}")
            log_path = out / profile / "games" / f"{game_stem}.jsonl.gz"
            if log_path.exists():
                raise RuntimeError(f"отказ перезаписывать существующий лог {log_path}")
            print(f"{datetime.now().astimezone().isoformat(timespec='seconds')} "
                  f"Профиль {profile}, партия {game_id}: запуск", flush=True)
            started = time.monotonic()
            try:
                result = selfplay.game_loop(game_id, seed, engine, log_path,
                                            stop_requested=lambda: STOP or time.time() >= play_stop,
                                            **kwargs)
            except console.ProtocolError as exc:
                result = selfplay.recover_partial_attempt(game_id, seed, log_path,
                                                          exc.code, str(exc))
                result.update(profile=profile, prefixId=kwargs.get("prefix_id"),
                              parentGameId=kwargs.get("parent_game_id"))
                engine.close()
                engine = console.UciEngine(str(engine_path))
            except Exception as exc:
                result = selfplay.recover_partial_attempt(game_id, seed, log_path,
                                                          "UNEXPECTED_ERROR", repr(exc))
                result.update(profile=profile, prefixId=kwargs.get("prefix_id"),
                              parentGameId=kwargs.get("parent_game_id"))
                engine.close()
                engine = console.UciEngine(str(engine_path))
            duration = time.monotonic() - started
            result.update(logPath=str(log_path), elapsedSeconds=round(duration, 3))
            if profile == "C":
                result["parentProfileGameId"] = f"{source_profile}:{parent['gameId']}"
                result["parentResultAt300"] = parent.get("result", parent.get("reason"))
                c_parents.add(result["parentProfileGameId"])
            if profile == "A":
                prefix_actions = extract_prefix(log_path)
                if prefix_actions is not None:
                    prefixes.append({"prefixId": f"A-{game_id:06d}",
                                    "sourceGameId": game_id, "seed": seed,
                                    "actions": prefix_actions,
                                    "sha256": sequence_hash(prefix_actions),
                                    "sourceLog": str(log_path)})
            if profile == "B" and result.get("status") != "ERROR":
                paired_prefixes.add(result.get("prefixId"))
            compact_result = attempt_summary(result)
            attempts[profile].append(compact_result)
            elapsed[profile] += duration
            append_json(out / profile / "attempts.jsonl", compact_result)
            if profile == "B":
                prefix = next(x for x in prefixes if x["prefixId"] == result.get("prefixId"))
                b_actions = extract_prefix(log_path)
                if b_actions != prefix["actions"]:
                    raise RuntimeError("проверка общего префикса A/B не прошла")
            print(f"Профиль {profile}: {result['status']} {result.get('reason')}; "
                  f"plies={result.get('plies')}; {duration:.1f}s; "
                  f"накопленное время A/B/C={elapsed['A']:.0f}/"
                  f"{elapsed['B']:.0f}/{elapsed['C']:.0f}s", flush=True)
            if len(attempts[profile]) % 100 == 0:
                csv_report(out, profile, attempts[profile])
        if time.time() >= play_stop:
            STOP = True
    except KeyboardInterrupt:
        STOP = True
    finally:
        engine.close()
        for profile in "ABC":
            csv_report(out, profile, attempts[profile])
        (out / "scheduler.json").write_text(json.dumps({
            "updatedLocal": datetime.now().astimezone().isoformat(),
            "stopped": STOP, "elapsedSeconds": elapsed,
            "attempts": {p: len(attempts[p]) for p in "ABC"},
            "nextStep": "resume with the same --output and --stop-at-local",
        }, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Прогон остановлен/поставлен на паузу в {datetime.now().astimezone().isoformat()}.", flush=True)


if __name__ == "__main__":
    main()
