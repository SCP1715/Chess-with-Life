"""Reader and move selector for the standalone LifeChess table files."""

from dataclasses import dataclass
from pathlib import Path
import struct
from typing import Optional

from .reference import Action, Model, Position, QueryResult, Wdl, legal_actions

_HEADER = struct.Struct("<8sIIQQBBBB24s")
_ENTRY = struct.Struct("<BI")
_WDL = {0: Wdl.UNKNOWN, 1: Wdl.WIN, 2: Wdl.DRAW, 3: Wdl.LOSS,
        255: Wdl.INVALID_STATE}


@dataclass(frozen=True)
class Table:
    path: Path
    kind: str
    minor_color: Optional[str]
    entries: bytes
    valid_states: int

    def lookup(self, position: Position):
        cls = _class_for(position)
        if cls is None or cls[0] != self.kind:
            return None
        if self.kind not in ("KK", "KBKB") and cls[1] != self.minor_color:
            return None
        pieces = {square: (color, kind) for square, color, kind in position.pieces}
        wk = next(sq for sq, (color, kind) in pieces.items() if color == "w" and kind == "K")
        bk = next(sq for sq, (color, kind) in pieces.items() if color == "b" and kind == "K")
        turn = 0 if position.turn == "w" else 1
        if self.kind == "KK":
            index = (wk * 64 + bk) * 2 + turn
        elif self.kind == "KBKB":
            wb = next(sq for sq, (color, kind) in pieces.items()
                      if color == "w" and kind == "B")
            bb = next(sq for sq, (color, kind) in pieces.items()
                      if color == "b" and kind == "B")
            index = (((wk * 64 + bk) * 64 + wb) * 64 + bb) * 2 + turn
        else:
            minor = next(sq for sq, (color, kind) in pieces.items()
                         if kind in ("B", "N", "R", "Q"))
            index = ((wk * 64 + bk) * 64 + minor) * 2 + turn
        code, dtlk = _ENTRY.unpack_from(self.entries, index * _ENTRY.size)
        return _WDL[code], dtlk


@dataclass(frozen=True)
class TableSet:
    tables: dict

    def lookup(self, position):
        cls = _class_for(position)
        if cls is None:
            return None
        table = self.tables.get(cls)
        return None if table is None else table.lookup(position)


def _class_for(position):
    if (position.debt_target is not None or position.castling_rights != "000000"
            or position.en_passant >= 0 or position.result != "NONE"):
        return None
    pieces = position.pieces
    if len(pieces) == 2:
        if sum(c == "w" and p == "K" for _, c, p in pieces) == 1 \
                and sum(c == "b" and p == "K" for _, c, p in pieces) == 1:
            return "KK", None
        return None
    if len(pieces) == 4:
        if (sum(c == "w" and p == "K" for _, c, p in pieces) != 1
                or sum(c == "b" and p == "K" for _, c, p in pieces) != 1
                or sum(c == "w" and p == "B" for _, c, p in pieces) != 1
                or sum(c == "b" and p == "B" for _, c, p in pieces) != 1):
            return None
        return "KBKB", None
    if len(pieces) != 3:
        return None
    if (sum(c == "w" and p == "K" for _, c, p in pieces) != 1
            or sum(c == "b" and p == "K" for _, c, p in pieces) != 1):
        return None
    minors = [(c, p) for _, c, p in pieces if p != "K"]
    if len(minors) != 1 or minors[0][1] not in ("B", "N", "R", "Q"):
        return None
    return "K" + minors[0][1] + "K", "WHITE" if minors[0][0] == "w" else "BLACK"


def load_table(path):
    path = Path(path)
    with path.open("rb") as stream:
        raw = stream.read(_HEADER.size)
        if len(raw) != _HEADER.size:
            raise ValueError(f"truncated table header: {path}")
        magic, fmt, rules, index_space, valid, kind, minor, color, _, model = _HEADER.unpack(raw)
        if magic != b"LCTB1\0\0\0" or fmt != 1 or rules != 100:
            raise ValueError(f"unsupported table format or rules version: {path}")
        if model.split(b"\0", 1)[0] != b"MODEL_UNBOUNDED":
            raise ValueError(f"table is not MODEL_UNBOUNDED: {path}")
        minor_name = {1: "B", 2: "N", 3: "R", 4: "Q"}.get(minor)
        expected_kind = ("KK" if kind == 1 else "KBKB" if kind == 3 else
                         "K" + str(minor_name) + "K")
        expected_color = None if kind in (1, 3) else ("WHITE" if color == 1 else "BLACK")
        expected_space = 8192 if kind == 1 else 64 ** 4 * 2 if kind == 3 else 524288
        if index_space != expected_space:
            raise ValueError(f"unexpected table index space: {path}")
        data = stream.read()
    if len(data) != index_space * _ENTRY.size:
        raise ValueError(f"table data size mismatch: {path}")
    return Table(path, expected_kind, expected_color, data, valid)


def load_tables(directory):
    directory = Path(directory)
    tables = {}
    for path in directory.glob("*.lctb"):
        table = load_table(path)
        key = ((table.kind, table.minor_color)
               if table.kind not in ("KK", "KBKB") else (table.kind, None))
        if key in tables:
            raise ValueError(f"duplicate table class {key}")
        tables[key] = table
    return TableSet(tables)


def query(position, tables):
    cls = _class_for(position)
    return None if cls is None else tables.lookup(position)


def prove_draw_under_optional_claims(position, tables, model, pending_draw_offer=False):
    """Prove a claim-aware DRAW when the unbounded table already proves DRAW.

    This narrow transfer is valid only because claim/accept actions are
    optional terminal draws and do not remove ordinary board moves. It is not
    a general MODEL_FIFTY or MODEL_HISTORY solver.
    """
    if model not in (Model.FIFTY, Model.HISTORY) or pending_draw_offer:
        return None
    if query(position, tables) != (Wdl.DRAW, 0):
        return None
    return QueryResult(model, Wdl.DRAW, True,
                       "optional_draw_only_extension_of_proven_unbounded_draw", 0)


def optimal_actions(position, tables):
    """Return all WDL-preserving, DTLK-optimal actions for a covered state."""
    current = query(position, tables)
    if current is None:
        return None
    wdl, dtlk = current
    cells = position.board()
    selected = []
    for action in legal_actions(position):
        target = cells[action.target]
        if target is not None and target == ("b" if position.turn == "w" else "w", "K"):
            child_wdl, child_dtlk = Wdl.LOSS, 0
        else:
            child = tables.lookup(_apply_trusted(position, action))
            if child is None:
                raise ValueError("supported move left the loaded table dependency set")
            child_wdl, child_dtlk = child
        if wdl == Wdl.WIN and child_wdl == Wdl.LOSS and dtlk == child_dtlk + 1:
            selected.append(action)
        elif wdl == Wdl.LOSS and child_wdl == Wdl.WIN and dtlk == child_dtlk + 1:
            selected.append(action)
        elif wdl == Wdl.DRAW and child_wdl == Wdl.DRAW:
            selected.append(action)
    return tuple(selected)


def preserving_actions(position, tables):
    """Return every action that preserves the minimax WDL value."""
    current = query(position, tables)
    if current is None:
        return None
    wdl, _ = current
    cells = position.board()
    selected = []
    for action in legal_actions(position):
        target = cells[action.target]
        if target is not None and target == ("b" if position.turn == "w" else "w", "K"):
            child_wdl = Wdl.LOSS
        else:
            child = tables.lookup(_apply_trusted(position, action))
            if child is None:
                raise ValueError("supported move left the loaded table dependency set")
            child_wdl = child[0]
        if ((wdl == Wdl.WIN and child_wdl == Wdl.LOSS)
                or (wdl == Wdl.LOSS and child_wdl == Wdl.WIN)
                or (wdl == Wdl.DRAW and child_wdl == Wdl.DRAW)):
            selected.append(action)
    return tuple(selected)


def _apply_trusted(position, action):
    cells = position.board()
    mover = cells[action.source]
    cells[action.source] = None
    cells[action.target] = mover
    return Position(tuple((sq, cell[0], cell[1]) for sq, cell in enumerate(cells) if cell),
                    "b" if position.turn == "w" else "w")
