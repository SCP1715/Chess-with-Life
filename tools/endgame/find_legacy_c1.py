"""Search the separate older v2 series for the exact C-1 board."""

import argparse
import gzip
import json
from pathlib import Path
import sys

if __package__ in (None, ""):
    sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
    from endgame.reference import Position, Wdl, parse_square, scope_result, Model
else:
    from .reference import Position, Wdl, parse_square, scope_result, Model


TARGET = ((parse_square("b7"), "w", "K"),
          (parse_square("f3"), "w", "B"),
          (parse_square("h2"), "b", "K"),
          (parse_square("a1"), "b", "B"))
TYPE_ORDINAL = {"k": 0, "q": 1, "r": 2, "b": 3, "n": 4, "p": 5}


def current_position_key(state):
    rows = []
    for square, cell in enumerate(state["board"]):
        if cell == ".":
            rows.append(".")
            continue
        color, kind, moved = cell[0], cell[1], cell[2:]
        row = square // 8
        pawn_home = (kind == "p" and row == (6 if color == "w" else 1)
                     and moved == "0")
        rows.append(color + str(TYPE_ORDINAL[kind]) + ("f" if pawn_home else "m"))
    rights = state.get("castlingRights", "000000")
    if isinstance(rights, str):
        rights = "".join("true" if value == "1" else "false" for value in rights)
    elif isinstance(rights, dict):
        keys = ("whiteKingSide", "whiteQueenSide", "whiteVertical",
                "blackKingSide", "blackQueenSide", "blackVertical")
        rights = "".join("true" if rights.get(k, False) else "false" for k in keys)
    ep = state.get("enPassant")
    ep = -1 if ep in (None, "-") else parse_square(ep) if isinstance(ep, str) else ep
    return ("".join(rows) + "|" + state["toMove"] + "|" +
            str(state.get("debtTargetKings") or "null") + "|" + rights + "|" + str(ep))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("series_directory", type=Path)
    parser.add_argument("output_file", type=Path)
    args = parser.parse_args()
    found = []
    expected = {parse_square("b7"): ("w", "k"),
                parse_square("f3"): ("w", "b"),
                parse_square("h2"): ("b", "k"),
                parse_square("a1"): ("b", "b")}
    paths = ([args.series_directory] if args.series_directory.is_file() else
             sorted(args.series_directory.rglob("game-*.jsonl.gz")))
    for path in paths:
        file_match_start = len(found)
        with gzip.open(path, "rt", encoding="utf-8") as stream:
            header = end = None
            for line_number, line in enumerate(stream, 1):
                row = json.loads(line)
                if row.get("record") == "START":
                    header = row
                elif row.get("record") == "END":
                    end = row
                for role in ("before", "after"):
                    state = row.get(role)
                    if not state or state.get("toMove") != "WHITE":
                        continue
                    board = state.get("board", [])
                    if len(board) != 64 or sum(cell != "." for cell in board) != 4:
                        continue
                    if any(board[square] == "." or
                           (board[square][0], board[square][1]) != piece
                           for square, piece in expected.items()):
                        continue
                    position = Position.from_state(state)
                    if tuple(sorted(position.pieces)) == tuple(sorted(TARGET)):
                        position_key = current_position_key(state)
                        current_repetitions = next((item["count"] for item in
                            state.get("repetitions", []) if item.get("key") == position_key), 0)
                        found.append({"file": str(path), "gameId": header.get("gameId") if header else None,
                                      "profile": header.get("profile") if header else "series-20261006-v2",
                                      "parentGameId": header.get("parentGameId") if header else None,
                                      "parentProfileGameId": header.get("parentProfileGameId") if header else None,
                                      "record": row.get("record"), "line": line_number,
                                      "ply": row.get("ply"), "role": role,
                                      "loggedAction": row.get("action"), "state": state,
                                      "gameResult": end.get("result") if end else "UNKNOWN",
                                      "gameReason": end.get("reason") if end else "missing END record",
                                      "gamePlies": end.get("plies") if end else None,
                                      "halfmoveClock": position.halfmove_clock,
                                      "currentRepetitionCount": current_repetitions,
                                      "claimFiftyAvailable": position.halfmove_clock >= 100,
                                      "claimRepetitionAvailable": current_repetitions >= 3,
                                      "repetitions": list(position.repetitions),
                                      "history": state.get("history"),
                                      "castlingRights": position.castling_rights,
                                      "enPassant": position.en_passant,
                                      "debtTarget": position.debt_target,
                                      "unbounded": scope_result(position, Model.UNBOUNDED).wdl.value,
                                       "historyModel": Wdl.UNKNOWN.value})
        for item in found[file_match_start:]:
            item["gameResult"] = end.get("result") if end else "UNKNOWN"
            item["gameReason"] = end.get("reason") if end else "missing END record"
            item["gamePlies"] = end.get("plies") if end else None
    args.output_file.parent.mkdir(parents=True, exist_ok=True)
    output = ["# Поиск точной позиции C-1", "",
              f"Источник поиска: `{args.series_directory}`; его результаты "
              "не объединялись с другими сериями. Искомая расстановка: "
              "White Kb7/Bf3, Black Kh2/Ba1, белые ходят.", "",
              f"Совпадений: {len(found)}.", ""]
    for item in found:
        output += [f"- `{item['file']}`: game `{item['gameId']}`, запись {item['line']}, "
                   f"ply {item['ply']} ({item['role']}); clock {item['halfmoveClock']}; "
                   f"current repetition count {item['currentRepetitionCount']}; "
                   f"Claim50 `{item['claimFiftyAvailable']}`; ClaimRep `{item['claimRepetitionAvailable']}`; "
                   f"repetition entries {len(item['repetitions'])}; "
                   f"rights `{item['castlingRights']}`; EP `{item['enPassant']}`; "
                   f"debt `{item['debtTarget']}`; parent `{item['parentProfileGameId'] or item['parentGameId']}`; "
                   f"result `{item['gameResult']}`/`{item['gameReason']}`."]
    json_path = args.output_file.with_suffix(".json")
    json_path.write_text(json.dumps(found, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    output += ["", "Таблица KBKB пока не вычислена, поэтому стратегический результат "
               "MODEL_UNBOUNDED и точный результат MODEL_HISTORY остаются UNKNOWN. "
               "Совпадение доски само по себе не переносит результат между моделями.", ""]
    args.output_file.write_text("\n".join(output), encoding="utf-8")
    print(f"matches={len(found)} wrote {args.output_file} and {json_path}")


if __name__ == "__main__":
    main()
