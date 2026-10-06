#!/usr/bin/env python3
"""Recompute reports and diagnostics from a saved A/B/C experiment."""

import argparse
import csv
import hashlib
import json
import pathlib
import shutil
import statistics
from collections import Counter, defaultdict
from datetime import datetime

from tools import lifechess_console as console
from tools import run_lifechess_selfplay as selfplay


def rows(path):
    if not path.exists():
        return
    with path.open(encoding="utf-8") as stream:
        for line in stream:
            if line.strip():
                yield json.loads(line)


def error_count(summary):
    """Counter-backed summaries omit keys whose count is zero."""
    return summary.get("errors", 0)


def records(path):
    return selfplay.read_json_lines(path) if path.exists() else []


def save_rows(path, objects):
    with path.open("w", encoding="utf-8", newline="\n") as stream:
        for item in objects:
            stream.write(json.dumps(item, ensure_ascii=False, separators=(",", ":")) + "\n")


def canonical_action(action):
    if action["type"] != "MOVE":
        return action
    return {key: action.get(key) for key in ("type", "from", "to", "moveKind", "promotion")}


def game_metrics(path):
    log = records(path)
    actions = [row for row in log if row.get("record") == "ACTION"]
    moves = [canonical_action(row["action"]) for row in actions if row["action"]["type"] == "MOVE"]
    digest = hashlib.sha256(json.dumps(moves, sort_keys=True, separators=(",", ":")).encode()).hexdigest()
    first16, count = [], 0
    for row in actions:
        first16.append(canonical_action(row["action"]))
        count += row["action"]["type"] == "MOVE"
        if count >= 16:
            break
    start = next((r.get("initialState") for r in log if r.get("record") == "START"), None)
    end = next((r for r in reversed(log) if r.get("record") == "END"), {})
    return log, actions, digest, first16 if count >= 16 else None, start, end


def first_move_plies(actions, target=16):
    selected, moves, state = [], 0, None
    for row in actions:
        selected.append(row)
        moves += row["action"]["type"] == "MOVE"
        state = row.get("after")
        if moves >= target:
            return selected, state
    return None, None


def write_interesting_episodes(root):
    wanted = {"KING_SELF_CAPTURE", "KING_PROMOTION", "DEBT_RESPONSE",
              "MISSED_LAST_KING_CAPTURE", "CASTLING", "KING_PROMOTION_AVAILABLE_OTHER_CHOSEN",
              "LAST_OPPONENT_KING_CAPTURED", "DRAW_CLAIM_AVAILABLE_UNUSED"}
    selected_by_type, signatures = defaultdict(list), set()
    for profile in "ABC":
        for attempt in rows(root / profile / "attempts.jsonl"):
            path = pathlib.Path(attempt.get("logPath", ""))
            if not path.exists():
                continue
            history = []
            for record in records(path):
                if record.get("record") != "ACTION":
                    continue
                action = record["action"]
                if action["type"] == "MOVE":
                    history.append(action)
                for event in record.get("events", []):
                    kind = event.get("type")
                    signature = (kind, profile, attempt.get("gameId"), event.get("ply"),
                                 event.get("from"), event.get("to"))
                    if (kind in wanted and signature not in signatures
                            and len(selected_by_type[kind]) < 3):
                        signatures.add(signature)
                        selected_by_type[kind].append({"profile": profile,
                                                       "gameId": attempt.get("gameId"),
                                                       "event": event, "before": record.get("before"),
                                                       "action": action, "after": record.get("after"),
                                                       "previousMoves": history[-10:]})
    selected = []
    event_types = sorted(selected_by_type)
    while event_types and len(selected) < 20:
        remaining = []
        for kind in event_types:
            if selected_by_type[kind]:
                selected.append(selected_by_type[kind].pop(0))
                if selected_by_type[kind]:
                    remaining.append(kind)
        event_types = remaining
    lines = ["# Выборка игровых эпизодов", "",
             "Эпизоды автоматически отобраны по типам событий из журналов; это примеры, "
             "а не независимые статистические наблюдения. Общий префикс A/B отмечается "
             "по ID партии и должен учитываться совместно.", ""]
    for index, item in enumerate(selected, 1):
        lines.extend([f"## {index}. {item['profile']} / партия {item['gameId']} — "
                      f"{item['event'].get('type')}", "",
                      f"Событие: `{json.dumps(item['event'], ensure_ascii=False)}`", "",
                      "Последние до 10 предшествующих ходов:", "", "```json",
                      json.dumps(item["previousMoves"], ensure_ascii=False, indent=2), "```", "",
                      "Полное состояние до действия:", "", "```json",
                      json.dumps(item["before"], ensure_ascii=False, separators=(",", ":")), "```", "",
                      "Действие и состояние после:", "", "```json",
                      json.dumps({"action": item["action"], "after": item["after"]},
                                 ensure_ascii=False, separators=(",", ":")), "```", ""])
    (root / "interesting-episodes.md").write_text("\n".join(lines), encoding="utf-8")


def immediate_last_king_captures(state, legal_actions, victim_side):
    victim = "w" if victim_side == "WHITE" else "b"
    return [action for action in legal_actions
            if action.get("type") == "MOVE"
            and state["board"][selfplay.board_index(action["to"])].startswith(victim + "k")]


def king_threat_continuations(root, engine_path):
    """Classify attacked-last-king positions by replaying legal continuations only."""
    counts = Counter()
    output_path = root / "king-threat-continuations.jsonl"
    engine = console.UciEngine(str(engine_path))
    try:
        with output_path.open("w", encoding="utf-8", newline="\n") as output:
          for profile in "ABC":
            for attempt in rows(root / profile / "attempts.jsonl"):
                path = pathlib.Path(attempt.get("logPath", ""))
                if not path.exists():
                    continue
                for record in records(path):
                    if record.get("record") != "ACTION" or not any(
                            event.get("type") == "KING_LEFT_UNDER_ATTACK"
                            for event in record.get("events", [])):
                        continue
                    before, selected = record["before"], record["action"]
                    side = before["toMove"]
                    after_selected = record["after"]
                    own_kings = [i for i, cell in enumerate(after_selected["board"])
                                 if cell != "." and cell.startswith(("w" if side == "WHITE" else "b") + "k")]
                    if len(own_kings) != 1 or not selfplay.attacked_king_squares(after_selected,
                                                                                  "w" if side == "WHITE" else "b"):
                        continue
                    safe_actions, continuations = [], []
                    selected_capture_available = None
                    for candidate in record.get("legalActions", []):
                        if candidate.get("type") != "MOVE" and candidate.get("type") not in {
                                "CLAIM_FIFTY_MOVES", "CLAIM_REPETITION", "ACCEPT_DRAW"}:
                            continue
                        applied = console.handle(selfplay.request("APPLY_ACTION", before, candidate), engine, 1)
                        if applied.get("kind") != "STATE_UPDATED":
                            continuations.append({"action": candidate, "legal": False,
                                                  "reason": applied.get("errorCode")})
                            continue
                        after = applied["state"]
                        captures = []
                        victim_prefix = "w" if side == "WHITE" else "b"
                        victim_kings = sum(cell.startswith(victim_prefix + "k")
                                           for cell in after["board"] if cell != ".")
                        own_win = after.get("result") == side + "_WIN"
                        draw_result = after.get("result", "").startswith("DRAW_")
                        opponent_win = after.get("result") in {"WHITE_WIN", "BLACK_WIN"} and not own_win
                        if (after.get("result") == "NONE" and after.get("toMove") != side
                                and victim_kings == 1):
                            reply = console.handle(selfplay.request("LEGAL_ACTIONS", after), engine, 1)
                            if reply.get("kind") == "LEGAL_ACTIONS":
                                captures = immediate_last_king_captures(after, reply["legalActions"], side)
                        if own_win or draw_result or (after.get("result") == "NONE" and victim_kings > 1):
                            safe = True
                        elif opponent_win or after.get("result") != "NONE" or victim_kings == 0:
                            safe = False
                        else:
                            safe = not captures
                        if safe:
                            safe_actions.append(candidate)
                        continuations.append({"action": candidate, "avoidsImmediateLastKingCapture": safe,
                                              "terminalResult": after.get("result"),
                                              "victimKingCountAfter": victim_kings,
                                              "opponentHasImmediateCapture": bool(captures),
                                              "immediateCaptures": captures})
                        if candidate == selected:
                            selected_capture_available = bool(captures)
                    if not safe_actions:
                        category = "NO_LEGAL_CONTINUATION_AVOIDS_IMMEDIATE_CAPTURE"
                    elif selected_capture_available:
                        category = "ENGINE_CHOSE_CAPTURE_EXPOSED_LINE_DESPITE_SAFE_CONTINUATION"
                    else:
                        category = "SAFE_CONTINUATION_CHOSEN_OR_NO_IMMEDIATE_CAPTURE"
                    result = {"profile": profile, "gameId": attempt.get("gameId"),
                              "ply": record.get("ply"), "side": side, "category": category,
                              "selectedAction": selected, "legalContinuationCount": len(continuations),
                              "safeContinuations": safe_actions,
                              "selectedLeavesImmediateCapture": selected_capture_available,
                              "continuations": continuations}
                    output.write(json.dumps(result, ensure_ascii=False, separators=(",", ":")) + "\n")
                    counts[category] += 1
    finally:
        engine.close()
    return counts


def state_summary(state):
    if not state:
        return {}
    pieces = {side: defaultdict(list) for side in ("WHITE", "BLACK")}
    for square, cell in enumerate(state["board"]):
        if cell == ".":
            continue
        side = "WHITE" if cell[0] == "w" else "BLACK"
        pieces[side][selfplay.piece_name(cell)].append(selfplay.console.square_name(square))
    return {"pieces": {side: dict(items) for side, items in pieces.items()},
            "toMove": state.get("toMove"), "debtTargetKings": state.get("debtTargetKings"),
            "castlingRights": state.get("castlingRights"), "enPassant": state.get("enPassant"),
            "halfMovesSinceCaptureOrPawn": state.get("halfMovesSinceCaptureOrPawn"),
            "repetitionCount": next((x.get("count", 0) for x in state.get("repetitions", [])
                                      if x.get("key") == selfplay.console.position_key(state)), 0),
            "drawOfferBy": state.get("drawOfferBy"),
            "drawOfferSentInCurrentNonWinningStretch": state.get("drawOfferSentInCurrentNonWinningStretch")}


def unfinished_row(item, log, path, final_claims=None):
    state = item.get("finalState")
    if state is None:
        end = next((r for r in reversed(log) if r.get("record") == "END"), {})
        state = end.get("finalState")
    actions = [r for r in log if r.get("record") == "ACTION"]
    latest = next((r.get("drawPolicy") for r in reversed(actions) if r.get("drawPolicy")), None)
    latest = latest or {}
    summary = state_summary(state)
    return {
        "gameId": item.get("gameId"), "profile": item.get("profile"), "seed": item.get("seed"),
        "status": item.get("status"), "reason": item.get("reason"), "plies": item.get("plies"),
        "parentGameId": item.get("parentProfileGameId"),
        "whitePieces": json.dumps(summary.get("pieces", {}).get("WHITE", {}), ensure_ascii=False),
        "blackPieces": json.dumps(summary.get("pieces", {}).get("BLACK", {}), ensure_ascii=False),
        "toMove": summary.get("toMove"), "debtTargetKings": summary.get("debtTargetKings"),
        "castlingRights": summary.get("castlingRights"), "enPassant": summary.get("enPassant"),
        "halfMovesSinceCaptureOrPawn": summary.get("halfMovesSinceCaptureOrPawn"),
        "currentPositionRepetitionCount": summary.get("repetitionCount"),
        "finalPositionClaimableReasons": json.dumps(final_claims, ensure_ascii=False),
        "latestEvaluationPositionPly": latest.get("positionPly"),
        "latestEvaluationPositionClaimableReasons": json.dumps(latest.get("claimableActions", []),
                                                                ensure_ascii=False),
        "latestEvaluationType": (latest.get("evaluation") or {}).get("type"),
        "latestEvaluationValue": (latest.get("evaluation") or {}).get("value"),
        "latestEvaluationPerspective": (latest.get("evaluation") or {}).get("perspective"),
        "materialPhase": latest.get("materialPhase"),
        "claimAndOfferThresholdCp": latest.get("claimAndOfferThresholdCp"),
        "drawOfferBy": summary.get("drawOfferBy"),
        "offerLatches": json.dumps(item.get("finalOfferLatches", {}), ensure_ascii=False),
        "lastActions": json.dumps([canonical_action(r["action"]) for r in actions[-8:]], ensure_ascii=False),
        "fullSnapshot": str(path),
    }


def analyze_profile(root, profile, attempts, engine):
    games_dir = root / profile / "games"
    event_counts, games_with_event = Counter(), defaultdict(set)
    sequences, openings = set(), set()
    unfinished_fields = ["gameId", "profile", "seed", "status", "reason", "plies", "parentGameId",
                         "whitePieces", "blackPieces", "toMove", "debtTargetKings", "castlingRights",
                         "enPassant", "halfMovesSinceCaptureOrPawn", "currentPositionRepetitionCount",
                         "finalPositionClaimableReasons", "latestEvaluationPositionPly",
                         "latestEvaluationPositionClaimableReasons", "latestEvaluationType", "latestEvaluationValue",
                         "latestEvaluationPerspective", "materialPhase", "claimAndOfferThresholdCp",
                         "drawOfferBy", "offerLatches", "lastActions", "fullSnapshot"]
    totals = Counter()
    move_results, parent_results, claim_positions = Counter(), Counter(), set()
    claim_decision_types, claim_score_bands = Counter(), Counter()
    pending_offer_decisions, own_latch_decisions = Counter(), Counter()
    completed_lengths = []
    pawn_episodes = pawn_long_refusals = 0
    with ((root / profile / "draw-decisions.jsonl").open("w", encoding="utf-8", newline="\n") as draw_file,
          (root / profile / "events-recomputed.jsonl").open("w", encoding="utf-8", newline="\n") as event_file,
          (root / profile / "pawn-episodes.jsonl").open("w", encoding="utf-8", newline="\n") as pawn_file,
          (root / profile / "unfinished-games.csv").open("w", encoding="utf-8-sig", newline="") as csv_file):
      writer = csv.DictWriter(csv_file, fieldnames=unfinished_fields)
      writer.writeheader()
      for item in attempts:
        totals["attempts"] += 1
        if item.get("status") == "COMPLETED":
            totals["completed"] += 1
            move_results[item.get("result")] += 1
            if isinstance(item.get("plies"), int):
                completed_lengths.append(item["plies"])
        elif item.get("status") == "INTERRUPTED":
            totals["interrupted"] += 1
        elif item.get("status") == "ERROR":
            totals["errors"] += 1
        if profile == "C":
            parent_results[item.get("parentResultAt300", "unknown")] += 1
        path = pathlib.Path(item.get("logPath", ""))
        if not path.is_absolute():
            path = root.parent.parent / path
        if not path.exists():
            path = games_dir / f"game-{int(item['gameId']):06d}.jsonl.gz"
        log, actions, digest, prefix, _start, _end = game_metrics(path) if path.exists() else ([], [], None, None, None, {})
        parent_ref = item.get("parentProfileGameId")
        if profile == "C" and parent_ref:
            parent_profile, parent_id = parent_ref.split(":", 1)
            parent_path = root / parent_profile / "games" / f"game-{int(parent_id):06d}.jsonl.gz"
            if parent_path.exists():
                parent_log = records(parent_path)
                parent_moves = [canonical_action(r["action"]) for r in parent_log
                                if r.get("record") == "ACTION" and r["action"]["type"] == "MOVE"]
                continuation_moves = [canonical_action(r["action"]) for r in actions
                                      if r["action"]["type"] == "MOVE"]
                digest = hashlib.sha256(json.dumps(parent_moves + continuation_moves,
                                                   sort_keys=True, separators=(",", ":")).encode()).hexdigest()
                parent_prefix, _ = first_move_plies([r for r in parent_log if r.get("record") == "ACTION"])
                if parent_prefix is not None:
                    prefix = [canonical_action(r["action"]) for r in parent_prefix]
        if digest:
            sequences.add(digest)
        if prefix is not None:
            openings.add(hashlib.sha256(json.dumps(prefix, sort_keys=True).encode()).hexdigest())
        for row in actions:
            action = row["action"]
            if action["type"] == "MOVE":
                totals["moveActions"] += 1
                totals["selectedAlternatives"] += bool(row.get("search", {}).get("selectedAlternative"))
                totals["randomizedMoves"] += bool(row.get("search", {}).get("randomized"))
                if item.get("status") == "COMPLETED":
                    totals["completedMovePlies"] += 1
            if row.get("drawPolicy"):
                before_state = row["before"]
                position_key = selfplay.console.position_key(before_state)
                repetition_count = next((x.get("count") for x in before_state.get("repetitions", [])
                                         if x.get("key") == position_key), 0)
                draw = {"profile": profile, "gameId": item.get("gameId"),
                        "actionNumber": row.get("actionNumber"), "ply": row.get("ply"),
                        "positionKey": (position_key
                                        + f"|half={before_state.get('halfMovesSinceCaptureOrPawn')}"
                                        + f"|rep={repetition_count}"),
                        "action": action, **row["drawPolicy"]}
                draw_file.write(json.dumps(draw, ensure_ascii=False, separators=(",", ":")) + "\n")
                totals["drawPolicyCalls"] += 1
                pending_offer_decisions["pending" if draw["opponentOfferPending"] else "none"] += 1
                own_latch_decisions["latched" if draw["offerLatchForSide"] else "clear"] += 1
                if draw["claimableActions"]:
                    totals["drawPolicyCallsWithClaimAvailable"] += 1
                    claim_positions.add((draw["gameId"], draw["positionKey"]))
                    claim_decision_types[action["type"]] += 1
                    evaluation = draw.get("evaluation")
                    if evaluation is None:
                        claim_score_bands["unavailable"] += 1
                    elif evaluation["type"] != "cp":
                        claim_score_bands["mate_score"] += 1
                    elif evaluation["value"] > draw["claimAndOfferThresholdCp"]:
                        claim_score_bands["above_threshold"] += 1
                    else:
                        claim_score_bands["at_or_below_threshold"] += 1
            for event in row.get("events", []):
                event_counts[event["type"]] += 1
                games_with_event[event["type"]].add(item.get("gameId"))
                event_file.write(json.dumps({"profile": profile, "gameId": item.get("gameId"), **event},
                                            ensure_ascii=False, separators=(",", ":")) + "\n")
        if log:
            pawn_source = log
            if profile == "C" and parent_ref:
                parent_profile, parent_id = parent_ref.split(":", 1)
                parent_path = root / parent_profile / "games" / f"game-{int(parent_id):06d}.jsonl.gz"
                if parent_path.exists():
                    parent_log = records(parent_path)
                    pawn_source = ([r for r in parent_log if r.get("record") != "END"]
                                   + [r for r in log if r.get("record") == "ACTION"]
                                   + [r for r in log if r.get("record") == "END"])
            episodes = selfplay.analyze_pawn_episodes(pawn_source, item.get("gameId"))
            pawn_episodes += len(episodes)
            pawn_long_refusals += sum(p["declinedAvailablePromotions"] >= 3 for p in episodes)
            for episode in episodes:
                pawn_file.write(json.dumps(episode, ensure_ascii=False, separators=(",", ":")) + "\n")
        if item.get("status") != "COMPLETED":
            state = item.get("finalState")
            if state is None:
                end = next((r for r in reversed(log) if r.get("record") == "END"), {})
                state = end.get("finalState")
            claim_reply = (console.handle(selfplay.request("LEGAL_ACTIONS", state,
                                                           request_id=f"analysis-{profile}-{item.get('gameId')}"),
                                          engine, 1) if state else {})
            claims = ([a["type"] for a in claim_reply.get("legalActions", [])
                       if a.get("type") in {"CLAIM_FIFTY_MOVES", "CLAIM_REPETITION"}]
                      if claim_reply.get("kind") == "LEGAL_ACTIONS" else None)
            writer.writerow(unfinished_row(item, log, path, claims))
    summary = {
        "profile": profile, **totals,
        "results": {str(k): v for k, v in move_results.items()},
        "medianCompletedPlies": statistics.median(completed_lengths) if completed_lengths else None,
        "parentResultsAt300": dict(parent_results),
        "uniqueMoveSequences": len(sequences), "uniqueFirst16PlyPrefixes": len(openings),
        "randomizedMoveCount": totals["randomizedMoves"],
        "selectedAlternativeCount": totals["selectedAlternatives"],
        "eventCounts": dict(event_counts),
        "gamesWithEvent": {k: len(v) for k, v in games_with_event.items()},
        "eventRatesPerAttemptPercent": {k: round(100 * len(v) / totals["attempts"], 3)
                                        for k, v in games_with_event.items() if totals["attempts"]},
        "eventsPer1000RecordedMovePlies": {k: round(1000 * count / totals["moveActions"], 3)
                                            for k, count in event_counts.items() if totals["moveActions"]},
        "uniqueGamePositionsWithClaimAvailable": len(claim_positions),
        "claimAvailableCallDecisions": dict(claim_decision_types),
        "claimAvailableEvaluationVsThreshold": dict(claim_score_bands),
        "drawPolicyCallsByIncomingOfferState": dict(pending_offer_decisions),
        "drawPolicyCallsByOwnOfferLatch": dict(own_latch_decisions),
        "drawClaimsActuallyMade": event_counts.get("DRAW_CLAIM_FIFTY_MOVES", 0)
                                  + event_counts.get("DRAW_CLAIM_REPETITION", 0),
        "pawnEpisodes": pawn_episodes,
        "pawnEpisodesWithThreeOrMoreDeclinedPromotions": pawn_long_refusals,
    }
    (root / profile / "summary.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n",
                                                   encoding="utf-8")
    lines = [f"# Профиль {profile}", "", f"Попыток: {summary['attempts']}; завершено: {summary['completed']}; "
             f"прервано: {summary['interrupted']}; технических ошибок: {error_count(summary)}.",
             f"Исходы завершённых партий: {json.dumps(summary['results'], ensure_ascii=False)}.",
             f"Медианная длина завершённой партии: {summary['medianCompletedPlies']} полуходов "
             f"(знаменатель {summary['completed']} завершённых партий).",
             f"Уникальных полных последовательностей: {len(sequences)}; уникальных 16-полуходовых префиксов: "
             f"{len(openings)}.", "", "Показатели событий указаны как количество событий; знаменатели — в summary.json.", ""]
    lines.extend([f"Вызовов draw-policy: {summary['drawPolicyCalls']}; с доступным Claim: "
                  f"{summary['drawPolicyCallsWithClaimAvailable']}; уникальных партий-позиций с Claim: "
                  f"{summary['uniqueGamePositionsWithClaimAvailable']}; фактических Claim: "
                  f"{summary['drawClaimsActuallyMade']}.",
                  f"Действия при доступном Claim: "
                  f"`{json.dumps(summary['claimAvailableCallDecisions'], ensure_ascii=False)}`; "
                  f"оценка относительно порога: "
                  f"`{json.dumps(summary['claimAvailableEvaluationVsThreshold'], ensure_ascii=False)}` "
                  "(это корреляция, не объяснение мотива движка).",
                  f"Входящее предложение / свой ограничитель: "
                  f"`{json.dumps(summary['drawPolicyCallsByIncomingOfferState'], ensure_ascii=False)}` / "
                  f"`{json.dumps(summary['drawPolicyCallsByOwnOfferLatch'], ensure_ascii=False)}`.",
                  f"Доля партий с событиями по типам: "
                  f"`{json.dumps(summary['eventRatesPerAttemptPercent'], ensure_ascii=False)}` "
                  "(% от всех попыток профиля).",
                  f"Событий на 1000 записанных игровых полуходов: "
                  f"`{json.dumps(summary['eventsPer1000RecordedMovePlies'], ensure_ascii=False)}`.", ""])
    (root / profile / "report.md").write_text("\n".join(lines), encoding="utf-8")
    return summary


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=pathlib.Path, required=True)
    args = parser.parse_args()
    root = args.output.resolve()
    manifest = json.loads((root / "manifest.json").read_text(encoding="utf-8"))
    summaries = {}
    engine = console.UciEngine(manifest["enginePath"])
    try:
        for profile in "ABC":
            attempts = rows(root / profile / "attempts.jsonl")
            summaries[profile] = analyze_profile(root, profile, attempts, engine)
    finally:
        engine.close()
    with (root / "events-recomputed.jsonl").open("w", encoding="utf-8", newline="\n") as target:
        for profile in "ABC":
            with (root / profile / "events-recomputed.jsonl").open(encoding="utf-8") as source:
                shutil.copyfileobj(source, target)
    write_interesting_episodes(root)
    threat_rows = king_threat_continuations(root, pathlib.Path(manifest["enginePath"]))
    a_by_id = {str(r["gameId"]): r.get("logPath") for r in rows(root / "A" / "attempts.jsonl")}
    pairs, mismatches = [], []
    for b in rows(root / "B" / "attempts.jsonl"):
        prefix_id = b.get("prefixId")
        a = a_by_id.get(prefix_id.removeprefix("A-").lstrip("0")) if prefix_id else None
        # Match by the persisted source ID, then compare the actual replayed actions.
        if a is None:
            continue
        apath = pathlib.Path(a)
        bpath = pathlib.Path(b["logPath"])
        alog, blog = records(apath), records(bpath)
        aa = [x for x in alog if x.get("record") == "ACTION"]
        ba = [x for x in blog if x.get("record") == "ACTION"]
        if not ba:
            continue  # failed before prefix replay; preserve it as a technical attempt, not a pair
        ap, astate = first_move_plies(aa)
        bp, bstate = first_move_plies(ba)
        a_prefix = [canonical_action(x["action"]) for x in ap] if ap else None
        b_prefix = [canonical_action(x["action"]) for x in bp] if bp else None
        a_latches = ap[-1].get("search", {}).get("offerLatchesAfter") if ap else None
        b_latches = bp[-1].get("search", {}).get("offerLatchesAfter") if bp else None
        valid = (a_prefix is not None and a_prefix == b_prefix and astate == bstate
                 and a_latches == b_latches)
        row = {"profileA": "A", "gameIdA": prefix_id, "gameIdB": b["gameId"],
               "prefixId": prefix_id, "prefixMatched": valid,
               "prefixActionsA": len(ap or []), "prefixActionsB": len(bp or []),
               "identicalAfterState": bool(astate == bstate),
               "identicalOfferLatches": bool(a_latches == b_latches)}
        pairs.append(row)
        if not valid:
            mismatches.append(row)
    with (root / "paired-openings.csv").open("w", encoding="utf-8-sig", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=["profileA", "gameIdA", "gameIdB", "prefixId",
                                                    "prefixMatched", "prefixActionsA", "prefixActionsB",
                                                    "identicalAfterState", "identicalOfferLatches"])
        writer.writeheader()
        writer.writerows(pairs)
    scheduler = {}
    if (root / "scheduler.json").exists():
        scheduler = json.loads((root / "scheduler.json").read_text(encoding="utf-8"))
    scheduled_stop = manifest.get("playStopLocal")
    if scheduled_stop and datetime.now().astimezone() >= datetime.fromisoformat(scheduled_stop):
        scheduler["stopped"] = True
        scheduler["stopReason"] = "SCHEDULED_LOCAL_STOP_TIME_REACHED"
        scheduler["playtestStoppedAtLocal"] = scheduler.get("updatedLocal")
        (root / "scheduler.json").write_text(json.dumps(scheduler, ensure_ascii=False, indent=2) + "\n",
                                             encoding="utf-8")
    lines = ["# Self-play по правилам «Шахмат с жизнями»", "",
             "Это описательный исследовательский прогон, не измерение Elo и не обучение NNUE.",
             "Старые серии не объединялись с этой выборкой. A/B-пары имеют общий дебютный префикс, "
             "поэтому префиксные события не являются независимыми наблюдениями.", "",
             f"Движок SHA-256: `{manifest.get('engineSha256')}`.",
             f"Фактически накопленное время A/B/C (сек): `{json.dumps(scheduler.get('elapsedSeconds', {}))}`.",
             f"A/B-префиксы: совпало {sum(p['prefixMatched'] for p in pairs)}/{len(pairs)}; "
             f"расхождений: {len(mismatches)}.",
             f"Проверка позиций с последним королём под атакой: {sum(threat_rows.values())}; "
             f"результаты в `king-threat-continuations.jsonl`.", ""]
    for profile in "ABC":
        s = summaries[profile]
        lines.extend([f"## {profile}", "", f"Попыток {s['attempts']}; завершено {s['completed']}; "
                      f"остановлено {s['interrupted']}; ошибок {error_count(s)}.",
                      f"Результаты: `{json.dumps(s['results'], ensure_ascii=False)}`.",
                      f"Медианная длина завершённой партии: {s['medianCompletedPlies']} полуходов "
                      f"(знаменатель: {s['completed']}).",
                      f"Уникальные полные последовательности: {s['uniqueMoveSequences']}; "
                      f"уникальные дебюты: {s['uniqueFirst16PlyPrefixes']}.",
                      f"Вызовов draw-policy: {s['drawPolicyCalls']}; позиций с доступным Claim: "
                      f"{s['uniqueGamePositionsWithClaimAvailable']}; фактических Claim: "
                      f"{s['drawClaimsActuallyMade']}.",
                      f"Действия при доступном Claim: "
                      f"`{json.dumps(s['claimAvailableCallDecisions'], ensure_ascii=False)}`; "
                      f"оценка относительно порога: "
                      f"`{json.dumps(s['claimAvailableEvaluationVsThreshold'], ensure_ascii=False)}`.",
                      f"Входящее предложение / свой ограничитель: "
                      f"`{json.dumps(s['drawPolicyCallsByIncomingOfferState'], ensure_ascii=False)}` / "
                      f"`{json.dumps(s['drawPolicyCallsByOwnOfferLatch'], ensure_ascii=False)}`.",
                      f"Доля партий с событиями (% от всех попыток): "
                      f"`{json.dumps(s['eventRatesPerAttemptPercent'], ensure_ascii=False)}`.",
                      f"Событий на 1000 записанных игровых полуходов: "
                      f"`{json.dumps(s['eventsPer1000RecordedMovePlies'], ensure_ascii=False)}`.",
                      f"События: `{json.dumps(s['eventCounts'], ensure_ascii=False)}`.",
                      ""])
        if profile == "C":
            lines.extend([f"Исходы исходных партий на 300 полуходах: "
                          f"`{json.dumps(s['parentResultsAt300'], ensure_ascii=False)}`; "
                          "исходы/остановки после продолжения указаны отдельно выше.", ""])
    lines.extend(["## Артефакты", "", "Для каждого профиля доступны полные сжатые журналы "
                  "`games/*.jsonl.gz`, сводка, CSV незавершённых партий, журнал событий, "
                  "диагностика решений ничьей и эпизоды пешек. `paired-openings.csv` "
                  "фиксирует проверку воспроизведения префиксов.", ""])
    (root / "report.md").write_text("\n".join(lines), encoding="utf-8")
    print(f"Анализ готов: {root / 'report.md'}; проверено A/B-пар: {len(pairs)}, "
          f"расхождений: {len(mismatches)}.")


if __name__ == "__main__":
    main()
