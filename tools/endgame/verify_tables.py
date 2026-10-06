"""Independent Bellman/DTLK certificate checker for generated LCTB tables.

The checker has its own board representation and move generator; it does not
import the reference model or the retrograde solver implementation.
"""

import argparse
import itertools
import struct
from pathlib import Path

HEADER = struct.Struct("<8sIIQQBBBB24s")
ENTRY = struct.Struct("<BI")
WIN, DRAW, LOSS, INVALID = 1, 2, 3, 255


class Reader:
    def __init__(self, path):
        self.path = Path(path)
        with self.path.open("rb") as f:
            raw = f.read(HEADER.size)
            (magic, fmt, rules, self.size, self.valid, kind, minor, color,
             _, model) = HEADER.unpack(raw)
            self.data = f.read()
        assert magic == b"LCTB1\0\0\0" and fmt == 1 and rules == 100
        assert model.split(b"\0", 1)[0] == b"MODEL_UNBOUNDED"
        assert len(self.data) == self.size * ENTRY.size
        self.name = ("KK" if kind == 1 else
                     "KBKB" if kind == 3 else
                     "K" + {1: "B", 2: "N", 3: "R", 4: "Q"}.get(minor, "?") + "K-" +
                     ("WHITE" if color == 1 else "BLACK"))

    def at(self, index):
        return ENTRY.unpack_from(self.data, index * ENTRY.size)


def index(spec, wk, bk, minor, turn):
    if spec == "KK":
        return (wk * 64 + bk) * 2 + turn
    if spec == "KBKB":
        wb, bb = minor
        return (((wk * 64 + bk) * 64 + wb) * 64 + bb) * 2 + turn
    return ((wk * 64 + bk) * 64 + minor) * 2 + turn


def destinations(board, source, color, kind):
    row, col = divmod(source, 8)
    if kind == "K":
        for dr in (-1, 0, 1):
            for dc in (-1, 0, 1):
                if dr or dc:
                    r, c = row + dr, col + dc
                    if 0 <= r < 8 and 0 <= c < 8:
                        yield r * 8 + c
    elif kind == "N":
        for dr, dc in ((-2, -1), (-2, 1), (-1, -2), (-1, 2),
                       (1, -2), (1, 2), (2, -1), (2, 1)):
            r, c = row + dr, col + dc
            if 0 <= r < 8 and 0 <= c < 8:
                yield r * 8 + c
    elif kind == "B":
        for dr, dc in ((-1, -1), (-1, 1), (1, -1), (1, 1)):
            r, c = row + dr, col + dc
            while 0 <= r < 8 and 0 <= c < 8:
                sq = r * 8 + c
                yield sq
                if board[sq] != -1:
                    break
                r += dr
                c += dc
    else:
        directions = ((-1, 0), (1, 0), (0, -1), (0, 1)) if kind == "R" else (
            (-1, 0), (1, 0), (0, -1), (0, 1),
            (-1, -1), (-1, 1), (1, -1), (1, 1))
        for dr, dc in directions:
            r, c = row + dr, col + dc
            while 0 <= r < 8 and 0 <= c < 8:
                sq = r * 8 + c
                yield sq
                if board[sq] != -1:
                    break
                r += dr
                c += dc


def children(wk, bk, minor, turn, minor_kind, minor_color):
    board = [-1] * 64
    board[wk], board[bk] = 0, 1
    if minor >= 0:
        board[minor] = 2 if minor_color == 0 else 3
    for src, piece in enumerate(board):
        if piece < 0:
            continue
        color = 0 if piece in (0, 2) else 1
        if color != turn:
            continue
        kind = "K" if piece < 2 else minor_kind
        for dst in destinations(board, src, color, kind):
            target = board[dst]
            if target >= 0 and piece >= 2 and (target in (0, 2) if color == 0 else target in (1, 3)):
                continue
            if target == (1 if turn == 0 else 0):
                yield None  # The moving side captures the last enemy king.
            elif target >= 0:
                if piece >= 2 or target not in (2, 3):
                    raise AssertionError("unexpected capture in supported material")
                new_wk = dst if src == wk else wk
                new_bk = dst if src == bk else bk
                yield ("KK", new_wk, new_bk, -1, 1 - turn)
            else:
                new_wk = dst if src == wk else wk
                new_bk = dst if src == bk else bk
                new_minor = dst if src == minor else minor
                yield (None, new_wk, new_bk, new_minor, 1 - turn)


def kbkb_children(wk, bk, wb, bb, turn):
    """Independent full-action generator for one bishop of each color."""
    board = [-1] * 64
    board[wk], board[bk], board[wb], board[bb] = 0, 1, 2, 3
    for src, piece in enumerate(board):
        if piece < 0:
            continue
        color = 0 if piece in (0, 2) else 1
        if color != turn:
            continue
        kind = "K" if piece < 2 else "B"
        for dst in destinations(board, src, color, kind):
            target = board[dst]
            if target >= 0 and piece >= 2 and (target in (0, 2) if color == 0 else target in (1, 3)):
                continue
            if target == (1 if turn == 0 else 0):
                yield None
                continue
            new_wk = dst if src == wk else wk
            new_bk = dst if src == bk else bk
            if target >= 0:
                new_wb = dst if src == wb else (wb if target != 2 else -1)
                new_bb = dst if src == bb else (bb if target != 3 else -1)
                if new_wb >= 0:
                    yield ("KBK-WHITE", new_wk, new_bk, new_wb, 1 - turn)
                else:
                    yield ("KBK-BLACK", new_wk, new_bk, new_bb, 1 - turn)
            else:
                new_wb = dst if src == wb else wb
                new_bb = dst if src == bb else bb
                yield ("KBKB", new_wk, new_bk, (new_wb, new_bb), 1 - turn)


def verify_kbkb(reader, all_tables):
    errors = checked = wins = draws = losses = 0
    space = reader.size
    for idx in range(space):
        actual, distance = reader.at(idx)
        if actual == INVALID:
            continue
        checked += 1
        board_id, turn = divmod(idx, 2)
        wk, rem = divmod(board_id, 64 * 64 * 64)
        bk, rem = divmod(rem, 64 * 64)
        wb, bb = divmod(rem, 64)
        nexts = []
        immediate = False
        for edge in kbkb_children(wk, bk, wb, bb, turn):
            if edge is None:
                immediate = True
                continue
            child_spec, cwk, cbk, minors, cturn = edge
            child_reader = all_tables[child_spec]
            child = child_reader.at(index(child_spec, cwk, cbk, minors, cturn))
            nexts.append(child)
        if immediate or any(child[0] == LOSS for child in nexts):
            expected = WIN
            expected_distance = min(([1] if immediate else []) +
                                    [d + 1 for w, d in nexts if w == LOSS])
        elif nexts and all(child[0] == WIN for child in nexts):
            expected = LOSS
            expected_distance = max(d + 1 for _, d in nexts)
        else:
            expected = DRAW
            expected_distance = 0
        if actual != expected or distance != expected_distance:
            errors += 1
            if errors <= 12:
                print(f"MISMATCH KBKB wk={wk} bk={bk} wb={wb} bb={bb} turn={turn} "
                      f"actual={actual}/{distance} expected={expected}/{expected_distance}")
        wins += actual == WIN
        draws += actual == DRAW
        losses += actual == LOSS
        if checked % 2_000_000 == 0:
            print(f"KBKB progress checked={checked:,}/{reader.valid:,} errors={errors}", flush=True)
    print(f"KBKB: checked={checked} W/D/L={wins}/{draws}/{losses} errors={errors}")
    return errors


def verify_one(reader, all_tables, spec, minor_kind="", minor_color=0):
    errors = 0
    checked = 0
    wins = draws = losses = 0
    for wk, bk in itertools.permutations(range(64), 2):
        for minor in ([-1] if spec == "KK" else
                      [s for s in range(64) if s != wk and s != bk]):
            for turn in (0, 1):
                idx = index(spec, wk, bk, minor, turn)
                actual, distance = reader.at(idx)
                if actual == INVALID:
                    continue
                checked += 1
                nexts = []
                immediate = False
                for edge in children(wk, bk, minor, turn, minor_kind, minor_color):
                    if edge is None:
                        immediate = True
                        continue
                    child_spec, cwk, cbk, cman, cturn = edge
                    if child_spec == "KK":
                        child_reader = all_tables["KK"]
                        child = child_reader.at(index("KK", cwk, cbk, -1, cturn))
                    else:
                        child = reader.at(index(spec, cwk, cbk, cman, cturn))
                    nexts.append(child)
                if immediate or any(child[0] == LOSS for child in nexts):
                    expected = WIN
                    win_distances = ([1] if immediate else []) + [d + 1 for w, d in nexts if w == LOSS]
                    expected_distance = min(win_distances)
                elif nexts and all(child[0] == WIN for child in nexts):
                    expected = LOSS
                    expected_distance = max(d + 1 for _, d in nexts)
                else:
                    expected = DRAW
                    expected_distance = 0
                if actual != expected or distance != expected_distance:
                    errors += 1
                    if errors <= 12:
                        print(f"MISMATCH {reader.name} wk={wk} bk={bk} minor={minor} turn={turn} "
                              f"actual={actual}/{distance} expected={expected}/{expected_distance}")
                wins += actual == WIN
                draws += actual == DRAW
                losses += actual == LOSS
    print(f"{reader.name}: checked={checked} W/D/L={wins}/{draws}/{losses} errors={errors}")
    return errors


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    parser.add_argument("--kbkb-python", action="store_true",
                        help="also run the readable but slower exhaustive Python KBKB verifier")
    args = parser.parse_args()
    readers = {p.stem: Reader(p) for p in args.directory.glob("*.lctb")}
    required = {"KK", "KBK-WHITE", "KBK-BLACK", "KNK-WHITE", "KNK-BLACK"}
    if not required.issubset(readers):
        raise SystemExit(f"missing required tables {sorted(required - readers.keys())}; found {sorted(readers)}")
    errors = 0
    errors += verify_one(readers["KK"], readers, "KK")
    for kind in ("B", "N", "R", "Q"):
        for color, ci in (("WHITE", 0), ("BLACK", 1)):
            name = f"K{kind}K-{color}"
            if name not in readers:
                continue
            errors += verify_one(readers[name], readers, name, kind, ci)
    if args.kbkb_python:
        if "KBKB" not in readers:
            raise SystemExit("--kbkb-python requested but KBKB.lctb is missing")
        errors += verify_kbkb(readers["KBKB"], readers)
    elif "KBKB" in readers:
        print("KBKB skipped by Python pass; run verify_kbkb.exe for the exhaustive certificate.")
    if errors:
        raise SystemExit(f"certificate verification failed: {errors} errors")
    print("All requested WDL and DTLK recurrences verified independently.")


if __name__ == "__main__":
    main()
