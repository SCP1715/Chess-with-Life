"""Small, deliberately simple LifeChess endgame reference model.

This module is not imported by the Android app or Fairy-Stockfish.  It is an
independent legality oracle for the no-pawn, no-rights endgame tables.
"""

from dataclasses import dataclass
from enum import Enum
from typing import Optional


class Model(str, Enum):
    UNBOUNDED = "MODEL_UNBOUNDED"
    FIFTY = "MODEL_FIFTY"
    HISTORY = "MODEL_HISTORY"


class Wdl(str, Enum):
    WIN = "WIN"
    DRAW = "DRAW"
    LOSS = "LOSS"
    UNKNOWN = "UNKNOWN"
    OUT_OF_SCOPE = "OUT_OF_SCOPE"
    INVALID_STATE = "INVALID_STATE"


@dataclass(frozen=True, order=True)
class Action:
    source: int
    target: int

    def uci(self):
        return square_name(self.source) + square_name(self.target)


@dataclass(frozen=True)
class Position:
    # (square, color, piece), with color w/b and piece K/Q/R/B/N/P.
    pieces: tuple
    turn: str
    debt_target: Optional[str] = None
    halfmove_clock: int = 0
    repetitions: tuple = ()
    castling_rights: str = "000000"
    en_passant: int = -1
    result: str = "NONE"

    @classmethod
    def from_state(cls, state):
        board = state.get("board")
        if not isinstance(board, list) or len(board) != 64:
            raise ValueError("board must contain 64 cells")
        pieces = []
        for sq, cell in enumerate(board):
            if cell == ".":
                continue
            if not isinstance(cell, str) or len(cell) < 3 or cell[0] not in "wb":
                raise ValueError(f"invalid piece on square {sq}: {cell!r}")
            kind = cell[1].upper()
            if kind not in "KQRBNP":
                raise ValueError(f"invalid piece type on square {sq}: {cell!r}")
            pieces.append((sq, cell[0], kind))
        debt = state.get("debtTargetKings")
        if debt not in (None, "WHITE", "BLACK"):
            raise ValueError("invalid debt target")
        turn_name = state.get("toMove")
        if turn_name not in ("WHITE", "BLACK"):
            raise ValueError("invalid side to move")
        rights = state.get("castlingRights", "000000")
        if isinstance(rights, str) and len(rights) == 6 and set(rights) <= {"0", "1"}:
            rights = rights
        elif isinstance(rights, dict):
            keys = ("whiteKingSide", "whiteQueenSide", "whiteVertical",
                    "blackKingSide", "blackQueenSide", "blackVertical")
            if any(k in rights and not isinstance(rights[k], bool) for k in keys):
                raise ValueError("castling right values must be booleans")
            rights = "".join("1" if rights.get(k, False) else "0" for k in keys)
        else:
            raise ValueError("invalid castling rights")
        ep = state.get("enPassant", -1)
        if ep is None or ep == "-":
            ep = -1
        elif isinstance(ep, str):
            ep = parse_square(ep)
        if isinstance(ep, bool) or not isinstance(ep, int) or not -1 <= ep < 64:
            raise ValueError("invalid en-passant square")
        halfmove = state.get("halfMovesSinceCaptureOrPawn", 0)
        if isinstance(halfmove, bool) or not isinstance(halfmove, int) or halfmove < 0:
            raise ValueError("invalid half-move clock")
        result = state.get("result", "NONE")
        if result not in ("NONE", "WHITE_WIN", "BLACK_WIN", "DRAW_REPETITION",
                          "DRAW_FIFTY_MOVES", "DRAW_AGREEMENT"):
            raise ValueError("invalid game result")
        return cls(tuple(pieces), "w" if turn_name == "WHITE" else "b", debt,
                   halfmove,
                   tuple(state.get("repetitions", ())), rights, ep,
                   result)

    def board(self):
        cells = [None] * 64
        for sq, color, kind in self.pieces:
            cells[sq] = (color, kind)
        return cells


@dataclass(frozen=True)
class QueryResult:
    model: Model
    wdl: Wdl
    exact: bool
    reason: str
    dtlk: Optional[int] = None


def square_name(sq):
    return chr(ord("a") + sq % 8) + str(8 - sq // 8)


def parse_square(value):
    if (not isinstance(value, str) or len(value) != 2
            or value[0] not in "abcdefgh" or value[1] not in "12345678"):
        raise ValueError(f"invalid square: {value!r}")
    return (8 - int(value[1])) * 8 + ord(value[0]) - ord("a")


def _inside(row, col):
    return 0 <= row < 8 and 0 <= col < 8


def _clear(cells, source, target):
    r, c = divmod(source, 8)
    tr, tc = divmod(target, 8)
    dr, dc = (tr > r) - (tr < r), (tc > c) - (tc < c)
    r, c = r + dr, c + dc
    while (r, c) != (tr, tc):
        if cells[r * 8 + c] is not None:
            return False
        r, c = r + dr, c + dc
    return True


def attacks(cells, source, target, color, kind):
    r, c = divmod(source, 8)
    tr, tc = divmod(target, 8)
    dr, dc = tr - r, tc - c
    adr, adc = abs(dr), abs(dc)
    if kind == "K":
        return max(adr, adc) == 1
    if kind == "N":
        return adr * adc == 2
    if kind == "B":
        return adr == adc and adr != 0 and _clear(cells, source, target)
    if kind == "R":
        return (dr == 0 or dc == 0) and dr != dc and _clear(cells, source, target)
    if kind == "Q":
        return ((dr == 0 or dc == 0 or adr == adc) and (dr != 0 or dc != 0)
                and _clear(cells, source, target))
    if kind == "P":
        return dr == (-1 if color == "w" else 1) and adc == 1
    return False


def legal_actions(position):
    """Generate geometry/occupancy actions; never applies king-safety filtering."""
    cells = position.board()
    actions = []
    for source, cell in enumerate(cells):
        if cell is None or cell[0] != position.turn:
            continue
        color, kind = cell
        for target, occupant in enumerate(cells):
            if target == source or (occupant is not None and occupant[0] == color and kind != "K"):
                continue
            if kind == "P":
                # Pawns are deliberately outside the current exact-table domain.
                continue
            if attacks(cells, source, target, color, kind):
                actions.append(Action(source, target))
    return tuple(actions)


def apply(position, action):
    cells = position.board()
    mover = cells[action.source]
    if mover is None or mover[0] != position.turn or action not in legal_actions(position):
        raise ValueError("illegal action")
    cells[action.source] = None
    cells[action.target] = mover
    pieces = tuple((sq, color, kind) for sq, cell in enumerate(cells) if cell
                   for color, kind in (cell,))
    return Position(pieces, "b" if position.turn == "w" else "w")


def table_class(position):
    """Return a complete supported class and color, or a non-table status."""
    cells = position.board()
    if len(position.pieces) == 4:
        if (sum(c == "w" and p == "K" for _, c, p in position.pieces) == 1
                and sum(c == "b" and p == "K" for _, c, p in position.pieces) == 1
                and sum(c == "w" and p == "B" for _, c, p in position.pieces) == 1
                and sum(c == "b" and p == "B" for _, c, p in position.pieces) == 1
                and position.debt_target is None
                and position.castling_rights == "000000"
                and position.en_passant < 0):
            return "KBKB", None
        return None
    if len(position.pieces) not in (2, 3):
        return None
    white_kings = sum(c == "w" and p == "K" for _, c, p in position.pieces)
    black_kings = sum(c == "b" and p == "K" for _, c, p in position.pieces)
    if white_kings != 1 or black_kings != 1:
        return None
    if position.debt_target is not None or position.castling_rights != "000000" or position.en_passant >= 0:
        return None
    others = [(c, p) for _, c, p in position.pieces if p != "K"]
    if not others:
        return "KK"
    if len(others) != 1 or others[0][1] not in ("B", "N", "R", "Q"):
        return None
    return ("K" + others[0][1] + "K", "WHITE" if others[0][0] == "w" else "BLACK")


def scope_result(position, model):
    """Never substitutes one draw model for another or a partial table for exact."""
    if model not in tuple(Model):
        return QueryResult(Model.UNBOUNDED, Wdl.INVALID_STATE, False, "invalid_draw_model")
    if position.turn not in ("w", "b"):
        return QueryResult(model, Wdl.INVALID_STATE, False, "invalid_side_to_move")
    if any(not isinstance(piece, tuple) or len(piece) != 3 for piece in position.pieces):
        return QueryResult(model, Wdl.INVALID_STATE, False, "malformed_piece_entry")
    squares = [sq for sq, _, _ in position.pieces]
    if (len(squares) != len(set(squares))
            or any(isinstance(sq, bool) or not isinstance(sq, int) or not 0 <= sq < 64
                   for sq in squares)
            or any(color not in ("w", "b") or not isinstance(kind, str)
                   or kind not in "KQRBNP"
                   for _, color, kind in position.pieces)):
        return QueryResult(model, Wdl.INVALID_STATE, False, "malformed_board")
    if (isinstance(position.halfmove_clock, bool)
            or not isinstance(position.halfmove_clock, int) or position.halfmove_clock < 0):
        return QueryResult(model, Wdl.INVALID_STATE, False, "negative_or_invalid_halfmove_clock")
    if (not isinstance(position.castling_rights, str) or len(position.castling_rights) != 6
            or set(position.castling_rights) - {"0", "1"}):
        return QueryResult(model, Wdl.INVALID_STATE, False, "invalid_castling_rights")
    if (isinstance(position.en_passant, bool) or not isinstance(position.en_passant, int)
            or not -1 <= position.en_passant < 64):
        return QueryResult(model, Wdl.INVALID_STATE, False, "invalid_en_passant_square")
    if position.debt_target not in (None, "WHITE", "BLACK"):
        return QueryResult(model, Wdl.INVALID_STATE, False, "invalid_debt_target")
    if position.result not in ("NONE", "WHITE_WIN", "BLACK_WIN", "DRAW_REPETITION",
                               "DRAW_FIFTY_MOVES", "DRAW_AGREEMENT"):
        return QueryResult(model, Wdl.INVALID_STATE, False, "invalid_game_result")
    if position.result != "NONE":
        if position.result.startswith("DRAW_"):
            return QueryResult(model, Wdl.DRAW, True, "already_terminal_draw")
        if position.result in ("WHITE_WIN", "BLACK_WIN"):
            won = "WHITE" if position.result == "WHITE_WIN" else "BLACK"
            current = "WHITE" if position.turn == "w" else "BLACK"
            return QueryResult(model, Wdl.WIN if won == current else Wdl.LOSS,
                               True, "already_terminal_result")
    own_kings = [1 for _, c, p in position.pieces if c == position.turn and p == "K"]
    enemy_kings = [1 for _, c, p in position.pieces if c != position.turn and p == "K"]
    if not own_kings and not enemy_kings:
        return QueryResult(model, Wdl.INVALID_STATE, False, "both_sides_missing_all_kings")
    if not own_kings:
        return QueryResult(model, Wdl.LOSS, True, "side_to_move_has_no_king")
    if not enemy_kings:
        return QueryResult(model, Wdl.WIN, True, "opponent_has_no_king")
    if position.debt_target is not None or position.castling_rights != "000000" or position.en_passant >= 0:
        return QueryResult(model, Wdl.OUT_OF_SCOPE, False, "special_rights_or_debt_not_in_table")
    if any(kind == "P" for _, _, kind in position.pieces):
        return QueryResult(model, Wdl.OUT_OF_SCOPE, False, "pawn_classes_not_yet_computed")
    if len(position.pieces) > 4:
        return QueryResult(model, Wdl.OUT_OF_SCOPE, False, "more_than_four_occupied_squares")
    if model != Model.UNBOUNDED:
        return QueryResult(model, Wdl.UNKNOWN, False,
                           "exact_solver_not_yet_implemented_for_this_draw_model")
    cls = table_class(position)
    if cls == "KK":
        return QueryResult(model, Wdl.UNKNOWN, False, "KK_table_not_loaded")
    if cls:
        return QueryResult(model, Wdl.UNKNOWN, False, f"{cls[0]}_{cls[1]}_table_not_loaded")
    return QueryResult(model, Wdl.UNKNOWN, False, "material_class_not_yet_computed")
