"""Aggregate solved-state and DTLK counts without constructing board objects."""

import argparse
import json
import struct
from collections import Counter, defaultdict
from pathlib import Path

HEADER = struct.Struct("<8sIIQQBBBB24s")
ENTRY = struct.Struct("<BI")
LABELS = {1: "WIN", 2: "DRAW", 3: "LOSS"}


def summarize_kbkb_numpy(data, index_space, valid_states):
    """Vectorized count of the large KBKB file; NumPy remains optional."""
    import numpy as np

    values = np.frombuffer(data, dtype=np.dtype([("wdl", "u1"), ("dtlk", "<u4")]))

    def counts(codes):
        return {name: int(np.count_nonzero(codes == code))
                for code, name in LABELS.items()
                if np.count_nonzero(codes == code)}

    by_turn = {
        "WHITE": counts(values[0::2]["wdl"]),
        "BLACK": counts(values[1::2]["wdl"]),
    }
    relation_turn = {
        "SAME_COLOR": {"WHITE": Counter(), "BLACK": Counter()},
        "OPPOSITE_COLOR": {"WHITE": Counter(), "BLACK": Counter()},
    }
    relation_total = {"SAME_COLOR": Counter(), "OPPOSITE_COLOR": Counter()}
    chunk_size = 1_000_000
    for start in range(0, index_space, chunk_size):
        end = min(index_space, start + chunk_size)
        indices = np.arange(start, end, dtype=np.uint32)
        board_indices = indices >> 1
        black_bishop = board_indices & 63
        white_bishop = (board_indices >> 6) & 63
        white_parity = (((white_bishop >> 3) + (white_bishop & 7)) & 1)
        black_parity = (((black_bishop >> 3) + (black_bishop & 7)) & 1)
        same = white_parity == black_parity
        turns = indices & 1
        codes = values[start:end]["wdl"]
        for parity_name, mask in (("SAME_COLOR", same), ("OPPOSITE_COLOR", ~same)):
            for turn, turn_name in ((0, "WHITE"), (1, "BLACK")):
                selected = codes[mask & (turns == turn)]
                for code, label in LABELS.items():
                    amount = int(np.count_nonzero(selected == code))
                    if amount:
                        relation_turn[parity_name][turn_name][label] += amount
                        relation_total[parity_name][label] += amount

    dtlk_range = {}
    for code, label in ((1, "WIN"), (3, "LOSS")):
        distances = values[values["wdl"] == code]["dtlk"]
        if distances.size:
            dtlk_range[label] = {"min": int(distances.min()), "max": int(distances.max())}
    return {"validStates": valid_states, "indexSpace": index_space,
            "wdl": counts(values["wdl"]), "bySideToMove": by_turn,
            "byMinorSquareParity": {},
            "byBishopSquareRelation": {k: dict(v) for k, v in relation_total.items()},
            "byBishopSquareRelationAndTurn": {
                relation: {turn: dict(counter) for turn, counter in turns.items()}
                for relation, turns in relation_turn.items()},
            "dtlkRange": dtlk_range}


def summarize(path):
    with path.open("rb") as stream:
        raw = stream.read(HEADER.size)
        (magic, version, rules, index_space, valid, kind, minor, color,
         _, model) = HEADER.unpack(raw)
        data = stream.read()
    if (magic != b"LCTB1\0\0\0" or version != 1 or rules != 100
            or model.split(b"\0", 1)[0] != b"MODEL_UNBOUNDED"
            or len(data) != index_space * ENTRY.size):
        raise ValueError(f"invalid table file: {path}")
    if kind == 3:
        try:
            return summarize_kbkb_numpy(data, index_space, valid)
        except ImportError:
            pass
    minor_name = {1: "B", 2: "N", 3: "R", 4: "Q"}.get(minor, "UNKNOWN")
    name = ("KK" if kind == 1 else "KBKB" if kind == 3 else
            "K" + minor_name + "K-" + ("WHITE" if color == 1 else "BLACK"))
    groups = defaultdict(Counter)
    dtlk = defaultdict(lambda: {"min": None, "max": None})
    seen = 0
    for index in range(index_space):
        code, distance = ENTRY.unpack_from(data, index * ENTRY.size)
        if code == 255:
            continue
        seen += 1
        turn = "WHITE" if index % 2 == 0 else "BLACK"
        label = LABELS.get(code, f"INVALID_{code}")
        groups["all"][label] += 1
        groups["turn:" + turn][label] += 1
        if label != "DRAW":
            stat = dtlk[label]
            stat["min"] = distance if stat["min"] is None else min(stat["min"], distance)
            stat["max"] = distance if stat["max"] is None else max(stat["max"], distance)
        if kind == 2:
            minor_square = index // 2 % 64
            parity = "LIGHT_PARITY_0" if ((minor_square // 8 + minor_square % 8) % 2 == 0) else "LIGHT_PARITY_1"
            groups["minorSquareParity:" + parity][label] += 1
        elif kind == 3:
            board_index = index // 2
            black_bishop = board_index % 64
            white_bishop = board_index // 64 % 64
            white_parity = (white_bishop // 8 + white_bishop % 8) & 1
            black_parity = (black_bishop // 8 + black_bishop % 8) & 1
            relation = "SAME_COLOR" if white_parity == black_parity else "OPPOSITE_COLOR"
            groups["bishopSquareRelation:" + relation][label] += 1
            groups["bishopSquareRelationTurn:" + relation + ":" +
                   ("WHITE" if index % 2 == 0 else "BLACK")][label] += 1
    if seen != valid:
        raise ValueError(f"valid state count mismatch in {path}: {seen} != {valid}")
    return {"validStates": seen,
            "indexSpace": index_space,
            "wdl": dict(groups["all"]),
            "bySideToMove": {k.split(":", 1)[1]: dict(v) for k, v in groups.items()
                             if k.startswith("turn:")},
            "byMinorSquareParity": {k.split(":", 1)[1]: dict(v) for k, v in groups.items()
                                    if k.startswith("minorSquareParity:")},
            "byBishopSquareRelation": {k.split(":", 1)[1]: dict(v) for k, v in groups.items()
                                       if k.startswith("bishopSquareRelation:")},
            "byBishopSquareRelationAndTurn": {
                relation: {turn: dict(groups.get("bishopSquareRelationTurn:" + relation + ":" + turn, {}))
                           for turn in ("WHITE", "BLACK")}
                for relation in ("SAME_COLOR", "OPPOSITE_COLOR")
            },
            "dtlkRange": dict(dtlk)}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("table_directory", type=Path)
    parser.add_argument("output_file", type=Path)
    args = parser.parse_args()
    result = {path.stem: summarize(path) for path in sorted(args.table_directory.glob("*.lctb"))}
    args.output_file.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n",
                                encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
