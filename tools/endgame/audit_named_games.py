"""Render primary-source facts for requested night-game episodes."""

import argparse
import gzip
import json
from pathlib import Path
import sys

if __package__ in (None, ""):
    sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
    from endgame.reference import Position, attacks, parse_square, square_name
    from endgame.table_io import load_tables, query
else:
    from .reference import Position, attacks, parse_square, square_name
    from .table_io import load_tables, query


TARGETS = {"A": {976: {201, 202, 203, 204, 205},
                  186: {113, 115, 122}},
           "B": {29: {48, 49, 55, 84, 85, 86}}}


def pieces(state):
    return [(square_name(i), cell[:2], cell[2:])
            for i, cell in enumerate(state["board"]) if cell != "."]


def king_threats(state):
    board = state["board"]
    cells = [None if cell == "." else (cell[0], cell[1].upper()) for cell in board]
    found = []
    for target, cell in enumerate(board):
        if cell == "." or cell[1] != "k":
            continue
        enemy = "b" if cell[0] == "w" else "w"
        for source, attacker in enumerate(board):
            if attacker == "." or attacker[0] != enemy:
                continue
            if attacks(cells, source, target, enemy, attacker[1].upper()):
                found.append({"king": square_name(target), "kingId": cell,
                              "attacker": square_name(source), "attackerId": attacker})
    return found


def capture_actions(record, target_color):
    target_color = {"WHITE": "w", "BLACK": "b"}.get(target_color, target_color)
    out = []
    for action in record.get("legalActions", []):
        if action.get("type") != "MOVE":
            continue
        target = parse_square(action["to"])
        cell = record["before"]["board"][target]
        if cell != "." and cell[0] == target_color and cell[1] == "k":
            out.append(action)
    return out


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("night_directory", type=Path)
    parser.add_argument("table_directory", type=Path)
    parser.add_argument("output_file", type=Path)
    args = parser.parse_args()
    tables = load_tables(args.table_directory)
    output = ["# Первичный аудит выбранных партий", "",
              "Таблицы дают доказательство только для загруженных беспешечных "
              "классов; позиции с большим числом фигур отмечены как UNKNOWN/OUT_OF_SCOPE.", ""]
    for profile, games in TARGETS.items():
        for game_id, target_plies in games.items():
            path = args.night_directory / profile / "games" / f"game-{game_id:06}.jsonl.gz"
            records = [json.loads(line) for line in gzip.open(path, "rt", encoding="utf-8")]
            start = next(r for r in records if r.get("record") == "START")
            end = next(r for r in records if r.get("record") == "END")
            actions = [r for r in records
                       if r.get("record") == "ACTION" and
                       (r["ply"] in target_plies or
                        (profile == "A" and game_id == 976 and r["action"].get("promotion") == "QUEEN"))]
            output += [f"## {profile}-{game_id}", "",
                       f"Лог: `{path}`; seed `{start.get('seed')}`; "
                       f"parent `{start.get('parentProfileGameId') or start.get('parentGameId')}`; "
                       f"итог `{end.get('result')}` / `{end.get('reason')}`, "
                       f"длина {end.get('plies')} игровых полуходов.", ""]
            for record in sorted(actions, key=lambda r: (r["ply"], r.get("actionNumber", 0))):
                ply = record["ply"]
                before = record["before"]
                after = record["after"]
                before_pos = Position.from_state(before)
                after_pos = Position.from_state(after)
                bresult = query(before_pos, tables)
                aresult = query(after_pos, tables)
                action = record.get("action", {})
                output += [f"### ply {ply}: {action}", "",
                           f"До ({sum(x != '.' for x in before['board'])} фигур): "
                           f"`{pieces(before)}`; ход `{before.get('toMove')}`, "
                           f"долг `{before.get('debtTargetKings')}`, "
                           f"полуходы `{before.get('halfMovesSinceCaptureOrPawn')}`, "
                           f"повторы `{len(before.get('repetitions', []))}`.", "",
                           f"После ({sum(x != '.' for x in after['board'])} фигур): "
                           f"`{pieces(after)}`; ход `{after.get('toMove')}`, "
                           f"долг `{after.get('debtTargetKings')}`, "
                           f"короли под боем `{king_threats(after)}`.", "",
                           f"Записанные легальные взятия короля по долгу: "
                           f"`{capture_actions(record, before.get('debtTargetKings', ''))}`.", "",
                           f"Результат таблицы до: `{bresult}`; после: `{aresult}` "
                           f"(это отдельные позиции, а не перенос оценки назад).", "",
                           "Записанная информация движка:", "",
                             "```text", record.get("searchInfo", ""), "```", "",
                             f"candidateMoves: `{record.get('search', {}).get('candidateMoves')}`; "
                             f"events: `{record.get('events')}`.", ""]
                if action.get("type") == "MOVE":
                    mover = "w" if record.get("side") == "WHITE" else "b"
                    king_squares = [square_name(i) for i, cell in enumerate(before["board"])
                                    if cell != "." and cell[0] == mover and cell[1] == "k"]
                    king_moves = [candidate for candidate in record.get("legalActions", [])
                                  if candidate.get("type") == "MOVE"
                                  and candidate.get("from") in king_squares]
                    output += [f"Полный набор ходов королём стороны хода: `{king_moves}`.", ""]
                if profile == "B" and game_id == 29 and ply == 55 and action.get("from") == "e1":
                    alternatives = [a for a in record.get("legalActions", [])
                                    if a.get("from") == "e1" and a.get("to") in ("d1", "d2")]
                    output += [f"B-29 сравнение ходов короля e1: `{alternatives}`.", ""]
                if profile == "A" and game_id == 186 and ply == 115:
                    alternatives = [a for a in record.get("legalActions", [])
                                    if a.get("from") == "d3" and a.get("to") in ("c2", "e2")]
                    output += [f"Проверка вариантов c2/e2 по legalActions: `{alternatives}`.", ""]
            if profile == "A" and game_id == 976:
                for record in actions:
                    if record["ply"] in (201, 204) and record["action"].get("promotion") == "KING":
                        ply = record["ply"]
                        candidates = [a for a in record.get("legalActions", [])
                                      if a.get("from") == record["action"].get("from")
                                      and a.get("to") == record["action"].get("to")]
                        output += [f"Для превращения на ply {ply} доступны варианты: "
                                   f"`{candidates}`.", ""]
            final_state = end.get("finalState")
            if final_state:
                final_kings = [(square_name(sq), cell) for sq, cell in enumerate(final_state["board"])
                               if cell != "." and cell[1] == "k"]
                output += [f"Финальная позиция: {sum(x != '.' for x in final_state['board'])} фигур; "
                           f"короли `{final_kings}`; toMove `{final_state.get('toMove')}`, "
                           f"result `{final_state.get('result')}`.", "",
                           f"King IDs/counters: `{end.get('finalKingIds')}` / "
                           f"`{end.get('finalKingCounters')}`.", ""]
    args.output_file.parent.mkdir(parents=True, exist_ok=True)
    args.output_file.write_text("\n".join(output), encoding="utf-8")
    print(f"Wrote {args.output_file}")


if __name__ == "__main__":
    main()
