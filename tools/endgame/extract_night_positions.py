"""Stream night self-play logs and annotate every <=4-piece action endpoint."""

import argparse
import gzip
import hashlib
import json
from collections import Counter, defaultdict
from pathlib import Path
import sys
import tempfile

if __package__ in (None, ""):
    sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
    from endgame.reference import (Action, Model, Position, Wdl, legal_actions,
                                   parse_square, scope_result, table_class)
    from endgame.table_io import load_tables, optimal_actions, preserving_actions, query
else:
    from .reference import (Action, Model, Position, Wdl, legal_actions,
                            parse_square, scope_result, table_class)
    from .table_io import load_tables, optimal_actions, preserving_actions, query


def state_key(position):
    return (position.pieces, position.turn, position.debt_target,
            position.castling_rights, position.en_passant)


def material(position):
    counts = {"WHITE": Counter(), "BLACK": Counter()}
    for _, color, kind in position.pieces:
        counts["WHITE" if color == "w" else "BLACK"][kind] += 1
    return {side: dict(sorted(values.items())) for side, values in counts.items()}


def classify(state, tables):
    position = Position.from_state(state)
    key = state_key(position)
    result = query(position, tables)
    coverage = "SUPPORTED_COMPLETE" if result is not None else None
    if result is not None:
        wdl, dtlk = result
        preserve = preserving_actions(position, tables)
        optimal = optimal_actions(position, tables)
        exact = {"status": wdl.value, "dtlk": dtlk,
                 "wdlPreservingActions": [a.uci() for a in preserve],
                 "dtlkOptimalActions": [a.uci() for a in optimal]}
    else:
        scoped = scope_result(position, Model.UNBOUNDED)
        status = scoped.wdl.value
        if status == Wdl.UNKNOWN.value and not scoped.reason.startswith("already_terminal"):
            status = "UNKNOWN"
        coverage = ("NOT_SUPPORTED" if status in
                    (Wdl.OUT_OF_SCOPE.value, Wdl.INVALID_STATE.value) else "IN_PROGRESS")
        exact = {"status": status, "reason": scoped.reason,
                 "dtlk": None, "wdlPreservingActions": None,
                 "dtlkOptimalActions": None}
        if scoped.reason.startswith("already_terminal"):
            coverage = "TERMINAL_FACT"
    return position, key, coverage, exact


def selected_action_value(position, logged_action, tables):
    if logged_action.get("type") != "MOVE":
        return {"status": "UNKNOWN", "reason": "action is not a board move"}
    try:
        move = Action(parse_square(logged_action["from"]),
                      parse_square(logged_action["to"]))
    except (KeyError, ValueError):
        return {"status": "INVALID_STATE", "reason": "malformed logged move"}
    if move not in legal_actions(position):
        return {"status": "INVALID_STATE", "reason": "move absent from reference legal actions"}
    if query(position, tables) is None:
        return {"status": "UNKNOWN", "reason": "parent is outside solved table coverage"}
    target = position.board()[move.target]
    if target == ("b" if position.turn == "w" else "w", "K"):
        return {"status": "WIN", "dtlk": 1, "terminalLastKingCapture": True}
    cells = position.board()
    mover = cells[move.source]
    cells[move.source] = None
    cells[move.target] = mover
    child_position = Position(tuple((sq, cell[0], cell[1])
                                    for sq, cell in enumerate(cells) if cell),
                              "b" if position.turn == "w" else "w")
    child = tables.lookup(child_position)
    if child is None:
        return {"status": "UNKNOWN", "reason": "move left loaded table dependencies"}
    child_wdl, child_dtlk = child
    mover_wdl = {Wdl.WIN: Wdl.LOSS, Wdl.LOSS: Wdl.WIN}.get(child_wdl, child_wdl)
    return {"status": mover_wdl.value,
            "dtlk": child_dtlk + 1 if mover_wdl != Wdl.DRAW else 0,
            "terminalLastKingCapture": False}


def inspect_game(path, profile, tables, writer, stats, seen_states, cache):
    start = end = None
    action_count = 0
    qualified = 0
    with tempfile.TemporaryFile(mode="w+t", encoding="utf-8", newline="\n") as spool:
        with gzip.open(path, "rt", encoding="utf-8") as stream:
            for line_number, line in enumerate(stream, 1):
                record = json.loads(line)
                kind = record.get("record")
                if kind == "START":
                    start = record
                elif kind == "END":
                    end = record
                elif kind == "ACTION":
                    action_count += 1
                    before, after = record.get("before"), record.get("after")
                    before_count = sum(cell != "." for cell in before["board"])
                    after_count = sum(cell != "." for cell in after["board"])
                    if before_count > 4 and after_count > 4:
                        continue
                    row = {"profile": profile, "gameId": start["gameId"],
                           "parentGameId": start.get("parentGameId"),
                           "parentProfileGameId": start.get("parentProfileGameId"),
                           "prefixId": start.get("prefixId"), "sourceFile": str(path),
                           "sourceLine": line_number, "ply": record.get("ply"),
                           "actionNumber": record.get("actionNumber"),
                           "action": record.get("action"),
                           "beforePieceCount": before_count, "afterPieceCount": after_count,
                           "before": before, "after": after,
                           "seed": start.get("seed"), "search": record.get("search"),
                           "searchInfo": record.get("searchInfo"),
                           "drawPolicy": record.get("drawPolicy"),
                           "episodeKind": ("CONTINUATION" if start.get("parentProfileGameId") or start.get("parentGameId")
                                           else "DEBUT_PREFIX" if start.get("prefixId")
                                           else "INDEPENDENT_GAME")}
                    per_state = []
                    for role, state, count in (("before", before, before_count),
                                               ("after", after, after_count)):
                        if count > 4:
                            continue
                        position = Position.from_state(state)
                        key = state_key(position)
                        digest = hashlib.sha256(repr(key).encode("utf-8")).hexdigest()
                        cache_key = (Model.UNBOUNDED.value, digest)
                        cached = cache.get(cache_key)
                        if cached is None:
                            _, _, coverage, exact = classify(state, tables)
                            cache[cache_key] = (coverage, exact)
                            cached = (coverage, exact)
                        first = seen_states.setdefault(digest, {
                            "profile": profile, "gameId": start["gameId"],
                            "ply": record.get("ply"), "role": role,
                            "sourceFile": str(path)})
                        stats["position_occurrences"] += 1
                        stats["position_unique_keys"].add(digest)
                        stats["piece_count_occurrences"][str(count)] += 1
                        stats["status_occurrences"][cached[1]["status"]] += 1
                        model_status = (cached[1]["status"] if cached[0] == "TERMINAL_FACT"
                                        else "UNKNOWN")
                        stats["model_fifty_occurrences"][model_status] += 1
                        stats["model_history_occurrences"][model_status] += 1
                        material_key = json.dumps(material(position), sort_keys=True)
                        stats["materials"][material_key] += 1
                        class_value = table_class(position)
                        state_class = (class_value if isinstance(class_value, str)
                                       else "-".join(str(part) for part in class_value
                                                    if part is not None) if class_value
                                       else cached[1]["reason"])
                        stats["classes"][state_class] += 1
                        per_state.append({"role": role, "pieceCount": count,
                                          "materialBySide": material(position),
                                          "positionKeySha256": digest,
                                          "firstOccurrence": first, "coverage": cached[0],
                                          "MODEL_UNBOUNDED": cached[1],
                                          "MODEL_FIFTY": ({"status": model_status,
                                              "reason": "terminal game result is independent of draw claims"}
                                              if cached[0] == "TERMINAL_FACT" else
                                              {"status": "UNKNOWN",
                                               "reason": "claim-aware game graph not computed"}),
                                          "MODEL_HISTORY": ({"status": model_status,
                                              "reason": "terminal game result is independent of history"}
                                              if cached[0] == "TERMINAL_FACT" else
                                              {"status": "UNKNOWN",
                                               "reason": "history-sensitive game graph not computed"}),
                                          "independentObservation": not
                                              (profile == "C" and role == "before" and record.get("ply") == 301)})
                        if role == "before":
                            row["selectedActionValue"] = selected_action_value(
                                position, row["action"], tables)
                    row["states"] = per_state
                    spool.write(json.dumps(row, ensure_ascii=False, separators=(",", ":")) + "\n")
                    qualified += 1
        if start is None:
            raise ValueError(f"missing START record in game log {path}")
        if end is None:
            stats["incompleteGames"] += 1
            end = {"result": "UNKNOWN", "reason": "missing END record"}
        game_id = start["gameId"]
        spool.seek(0)
        for line in spool:
            row = json.loads(line)
            row["actualGameResult"] = end.get("result")
            row["actualGameReason"] = end.get("reason")
            writer.write(json.dumps(row, ensure_ascii=False, separators=(",", ":")) + "\n")
    stats["games"] += 1
    stats["actions"] += action_count
    stats["qualifyingActions"] += qualified
    stats["gameResults"][str(end.get("result"))] += 1
    stats["profiles"][profile]["games"] += 1
    stats["profiles"][profile]["actions"] += action_count
    stats["profiles"][profile]["qualifyingActions"] += qualified


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("night_directory", type=Path)
    parser.add_argument("table_directory", type=Path)
    parser.add_argument("output_directory", type=Path)
    args = parser.parse_args()
    tables = load_tables(args.table_directory)
    args.output_directory.mkdir(parents=True, exist_ok=True)
    stats = {"games": 0, "incompleteGames": 0, "actions": 0, "qualifyingActions": 0,
             "position_occurrences": 0, "position_unique_keys": set(),
             "piece_count_occurrences": Counter(), "status_occurrences": Counter(),
             "model_fifty_occurrences": Counter(), "model_history_occurrences": Counter(),
             "materials": Counter(), "classes": Counter(), "gameResults": Counter(),
             "profiles": defaultdict(lambda: Counter())}
    seen_states, cache = {}, {}
    with gzip.open(args.output_directory / "night-endgame-episodes.jsonl.gz",
                   "wt", encoding="utf-8", compresslevel=6) as writer:
        for profile in ("A", "B", "C"):
            game_dir = args.night_directory / profile / "games"
            files = sorted(game_dir.glob("game-*.jsonl.gz"))
            for index, path in enumerate(files, 1):
                inspect_game(path, profile, tables, writer, stats, seen_states, cache)
                if index % 250 == 0:
                    print(f"{profile}: {index}/{len(files)} game logs streamed", flush=True)
    summary = {k: v for k, v in stats.items() if k not in (
        "position_unique_keys", "profiles", "piece_count_occurrences",
        "status_occurrences", "model_fifty_occurrences", "model_history_occurrences",
        "materials", "classes", "gameResults")}
    summary.update({"uniquePositionKeys": len(stats["position_unique_keys"]),
                    "pieceCountOccurrences": dict(stats["piece_count_occurrences"]),
                    "MODEL_UNBOUNDED_statusOccurrences": dict(stats["status_occurrences"]),
                    "MODEL_FIFTY_statusOccurrences": dict(stats["model_fifty_occurrences"]),
                    "MODEL_HISTORY_statusOccurrences": dict(stats["model_history_occurrences"]),
                    "materialOccurrences": dict(stats["materials"]),
                    "classOccurrences": dict(stats["classes"]),
                    "gameResults": dict(stats["gameResults"]),
                    "profiles": {k: dict(v) for k, v in stats["profiles"].items()},
                    "source": "extracted night A/B/C game logs; source archives are audited, not reprocessed",
                    "models": [m.value for m in Model]})
    (args.output_directory / "night-endgame-coverage.json").write_text(
        json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({k: summary[k] for k in (
        "games", "incompleteGames", "actions", "qualifyingActions", "position_occurrences",
        "uniquePositionKeys", "pieceCountOccurrences",
        "MODEL_UNBOUNDED_statusOccurrences", "gameResults")},
        ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
