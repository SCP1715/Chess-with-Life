#!/usr/bin/env python3
"""JSON-lines adapter between LifeChessProtocol v2 and the real UCI engine."""

import argparse
import copy
import json
import queue
import re
import subprocess
import sys
import threading

PROTOCOL = "lifechess"
VERSION = 2
PIECES = {"KING": "k", "QUEEN": "q", "ROOK": "r", "BISHOP": "b", "KNIGHT": "n", "PAWN": "p"}
FEN_PIECES = {"k": "k", "q": "q", "r": "r", "b": "b", "n": "n", "p": "p"}
PROMOTIONS = {"q": "QUEEN", "r": "ROOK", "b": "BISHOP", "n": "KNIGHT", "k": "KING"}
RESULT_NONE = "NONE"


class ProtocolError(Exception):
    def __init__(self, code, message):
        super().__init__(message)
        self.code = code


class UciEngine:
    def __init__(self, executable):
        self.process = subprocess.Popen(
            [executable], stdin=subprocess.PIPE, stdout=subprocess.PIPE,
            stderr=subprocess.PIPE, text=True, encoding="utf-8", bufsize=1,
        )
        self.read_timeout_seconds = 60
        self._stdout_queue = queue.Queue()
        self._stderr_lines = []
        self._stdout_thread = threading.Thread(target=self._read_stream,
                                               args=(self.process.stdout, self._stdout_queue), daemon=True)
        self._stderr_thread = threading.Thread(target=self._read_stream,
                                               args=(self.process.stderr, None), daemon=True)
        self._stdout_thread.start()
        self._stderr_thread.start()
        self._send("uci")
        options = []
        while True:
            line = self._read()
            if line == "uciok":
                break
            options.append(line)
        variant = next((line for line in options if line.startswith("option name UCI_Variant ")), "")
        if not re.search(r"\bvar lifechess\b", variant):
            self.close()
            raise ProtocolError("ENGINE_VARIANT_UNAVAILABLE", "Engine does not advertise UCI_Variant lifechess")
        required_options = {
            "UCI_Variant", "LifeChess Draw Decisions", "LifeChess Draw Offer Pending",
            "LifeChess Draw Claim Available", "LifeChess Draw Offer Sent",
        }
        advertised = set()
        for line in options:
            match = re.match(r"option name (.+?) type ", line)
            if match:
                advertised.add(match.group(1))
        missing_options = required_options - advertised
        if missing_options:
            self.close()
            raise ProtocolError("ENGINE_PROTOCOL_UNSUPPORTED",
                                "Engine lacks required LifeChess UCI extensions: "
                                + ", ".join(sorted(missing_options)))
        self._send("setoption name UCI_Variant value lifechess")
        self._send("setoption name Threads value 1")
        self._send("setoption name Hash value 16")
        self._send("setoption name Use NNUE value false")
        self._send("isready")
        while self._read() != "readyok":
            pass
        self._send("position startpos")
        self._send("d")
        fen_line = self._read_until_prefix("Fen: ")
        if " lc1:" not in fen_line:
            self.close()
            raise ProtocolError("ENGINE_VARIANT_REJECTED", "Engine did not load the LifeChess variant")

    def legal_moves(self, fen):
        self._send("position fen " + fen)
        self._send("go perft 1")
        moves = []
        while True:
            line = self._read()
            if line.startswith("Nodes searched:"):
                return moves
            match = re.match(r"^([a-h][1-8][a-h][1-8][qrbnk]?):\s*1$", line)
            if match:
                moves.append(match.group(1))

    def apply_move_and_get_fen(self, fen, uci_move):
        self._send("position fen " + fen + " moves " + uci_move)
        self._send("d")
        return self._read_until_prefix("Fen: ")[5:]

    def search(self, fen, depth, draw_context, position_command=None):
        self.last_search_reset_offer_latch = False
        self._send("setoption name LifeChess Draw Decisions value true")
        self._send("setoption name LifeChess Draw Offer Pending value " + str(draw_context["pending"]).lower())
        self._send("setoption name LifeChess Draw Claim Available value " + str(draw_context["claimable"]).lower())
        self._send("setoption name LifeChess Draw Claim Repetition value "
                   + str(draw_context["repetition_claim"]).lower())
        self._send("setoption name LifeChess Draw Claim Fifty Moves value "
                   + str(draw_context["fifty_moves_claim"]).lower())
        self._send("setoption name LifeChess Draw Offer Sent value " + str(draw_context["offered"]).lower())
        multipv = max(1, int(getattr(self, "search_multipv", 1)))
        self._send("setoption name MultiPV value " + str(multipv))
        override = getattr(self, "search_position_override", None)
        self._send(override or position_command or ("position fen " + fen))
        nodes = getattr(self, "search_node_budget", None)
        self._send("go nodes " + str(int(nodes)) if nodes is not None
                   else "go depth " + str(depth))
        lines = []
        decision = None
        reset_offer_latch = False
        while True:
            line = self._read()
            lines.append(line)
            if line.startswith("info string lifechess-action "):
                tokens = line.split()
                decision = tokens[3]
                reset_offer_latch = "reset-offer-latch" in tokens[4:]
            if line.startswith("bestmove "):
                self.last_search_reset_offer_latch = reset_offer_latch
                return line.split()[1], lines, decision, reset_offer_latch

    def _send(self, command):
        self.process.stdin.write(command + "\n")
        self.process.stdin.flush()

    def _read(self):
        try:
            line = self._stdout_queue.get(timeout=self.read_timeout_seconds)
        except queue.Empty as exc:
            raise ProtocolError("ENGINE_TIMEOUT", "Engine did not respond before the search timeout") from exc
        if line is None:
            error = "".join(self._stderr_lines[-50:])
            raise ProtocolError("ENGINE_EXITED", error or "Engine closed its output")
        return line.strip()

    def _read_stream(self, stream, destination):
        try:
            for line in stream:
                if destination is None:
                    self._stderr_lines.append(line)
                    del self._stderr_lines[:-100]
                else:
                    destination.put(line)
        finally:
            if destination is not None:
                destination.put(None)

    def new_game(self):
        self._send("ucinewgame")
        self._send("setoption name Clear Hash")
        self._send("isready")
        while self._read() != "readyok":
            pass

    def _read_until_prefix(self, prefix):
        while True:
            line = self._read()
            if line.startswith(prefix):
                return line

    def close(self):
        if self.process.poll() is None:
            try:
                self._send("quit")
                self.process.wait(timeout=3)
            except (OSError, subprocess.TimeoutExpired):
                self.process.kill()
                self.process.wait()


def square_name(index):
    if not isinstance(index, int) or not 0 <= index < 64:
        raise ProtocolError("INVALID_STATE", "Square index is outside the board")
    return chr(ord("a") + index % 8) + str(8 - index // 8)


def square_index(name):
    if not isinstance(name, str) or not re.fullmatch(r"[a-h][1-8]", name):
        raise ProtocolError("INVALID_ACTION", "Invalid square: " + repr(name))
    return (8 - int(name[1])) * 8 + ord(name[0]) - ord("a")


def validate_action(action):
    if not isinstance(action, dict):
        raise ProtocolError("INVALID_ACTION", "Action must be an object")
    action_type = action.get("type")
    if action_type == "MOVE":
        if set(action) != {"type", "from", "to", "moveKind", "promotion"}:
            raise ProtocolError("INVALID_ACTION", "Move must have exactly the v1 move fields")
        square_index(action["from"])
        square_index(action["to"])
        if action["moveKind"] not in {"NORMAL", "EN_PASSANT", "CASTLE_KING", "CASTLE_QUEEN", "CASTLE_VERTICAL"}:
            raise ProtocolError("INVALID_ACTION", "Unknown move kind")
        if action["promotion"] is not None and action["promotion"] not in PROMOTIONS.values():
            raise ProtocolError("INVALID_ACTION", "Unknown promotion type")
        return
    if action_type not in {
        "CLAIM_REPETITION", "CLAIM_FIFTY_MOVES", "CLAIM_BARE_KINGS",
        "OFFER_DRAW", "ACCEPT_DRAW", "DECLINE_DRAW"
    } or set(action) != {"type"}:
        raise ProtocolError("INVALID_ACTION", "Unknown or malformed procedural action")


def validate_request(request):
    if not isinstance(request, dict):
        raise ProtocolError("INVALID_REQUEST", "Request must be a JSON object")
    if set(request) != {
        "protocol", "version", "requestId", "stateRevision", "variant", "operation", "state", "action"
    }:
        raise ProtocolError("INVALID_REQUEST", "Request must contain exactly the v2 envelope fields")
    if request.get("protocol") != PROTOCOL or type(request.get("version")) is not int \
            or request.get("version") != VERSION:
        raise ProtocolError("UNSUPPORTED_PROTOCOL", "Unsupported protocol name or version")
    if request.get("variant") != PROTOCOL:
        raise ProtocolError("UNSUPPORTED_VARIANT", "Only the lifechess variant is accepted")
    request_id = request.get("requestId")
    if not isinstance(request_id, str) or not re.fullmatch(r"[A-Za-z0-9._-]{1,128}", request_id):
        raise ProtocolError("INVALID_REQUEST_ID", "requestId must contain 1-128 safe ASCII characters")
    revision = request.get("stateRevision")
    if type(revision) is not int or revision < 0:
        raise ProtocolError("INVALID_REVISION", "stateRevision must be a non-negative integer")
    if request.get("operation") not in {"LEGAL_ACTIONS", "APPLY_ACTION", "SEARCH", "EXPORT_STATE"}:
        raise ProtocolError("INVALID_OPERATION", "Unknown operation")
    state = request.get("state")
    validate_state(state)
    if (request["operation"] == "APPLY_ACTION") != (request.get("action") is not None):
        raise ProtocolError("INVALID_REQUEST", "Only APPLY_ACTION requires an action")
    if request.get("action") is not None:
        validate_action(request["action"])
    return request


def validate_state(state):
    if not isinstance(state, dict):
        raise ProtocolError("INVALID_STATE", "state must be an object")
    if set(state) != {
        "board", "toMove", "enPassant", "debtTargetKings", "halfMovesSinceCaptureOrPawn",
        "castlingRights", "result", "drawOfferBy", "drawOfferSentInCurrentNonWinningStretch",
        "repetitions", "history", "searchRootFen", "searchMoves",
    }:
        raise ProtocolError("INVALID_STATE", "State must contain exactly the v2 snapshot fields")
    board = state.get("board")
    if not isinstance(board, list) or len(board) != 64:
        raise ProtocolError("INVALID_STATE", "board must contain exactly 64 squares")
    for cell in board:
        if cell == ".":
            continue
        if not isinstance(cell, str) or not re.fullmatch(r"[wb][kqrbnp][01]", cell):
            raise ProtocolError("INVALID_STATE", "Invalid board square encoding")
    if state.get("toMove") not in {"WHITE", "BLACK"}:
        raise ProtocolError("INVALID_STATE", "Invalid side to move")
    if state.get("debtTargetKings") not in {None, "WHITE", "BLACK"}:
        raise ProtocolError("INVALID_STATE", "Invalid debt target")
    ep = state.get("enPassant")
    if ep is not None:
        square_index(ep)
    half = state.get("halfMovesSinceCaptureOrPawn")
    if type(half) is not int or half < 0:
        raise ProtocolError("INVALID_STATE", "Invalid half-move counter")
    rights = state.get("castlingRights")
    if not isinstance(rights, str) or not re.fullmatch(r"[01]{6}", rights):
        raise ProtocolError("INVALID_STATE", "castlingRights must be six 0/1 flags")
    if state.get("result") not in {"NONE", "WHITE_WIN", "BLACK_WIN", "DRAW_REPETITION", "DRAW_FIFTY_MOVES", "DRAW_AGREEMENT", "DRAW_BARE_KINGS"}:
        raise ProtocolError("INVALID_STATE", "Unknown game result")
    if state.get("drawOfferBy") not in {None, "WHITE", "BLACK"}:
        raise ProtocolError("INVALID_STATE", "Invalid draw offer owner")
    if not isinstance(state.get("drawOfferSentInCurrentNonWinningStretch"), bool):
        raise ProtocolError("INVALID_STATE", "Missing draw-offer cooldown flag")
    repetitions = state.get("repetitions")
    history = state.get("history")
    if not isinstance(repetitions, list) or not isinstance(history, list):
        raise ProtocolError("INVALID_STATE", "repetitions and history must be arrays")
    seen = set()
    for entry in repetitions:
        if (not isinstance(entry, dict) or set(entry) != {"key", "count"}
                or not isinstance(entry["key"], str) or type(entry["count"]) is not int
                or entry["count"] < 1 or entry["key"] in seen):
            raise ProtocolError("INVALID_STATE", "Malformed repetition entry")
        seen.add(entry["key"])
    for action in history:
        validate_action(action)
    root_fen = state.get("searchRootFen")
    search_moves = state.get("searchMoves")
    if not isinstance(root_fen, str) or " lc1:" not in root_fen or " lm1:" not in root_fen:
        raise ProtocolError("INVALID_STATE", "searchRootFen must be a complete LifeChess FEN")
    if not isinstance(search_moves, list) or any(
            not isinstance(move, str) or not re.fullmatch(r"[a-h][1-8][a-h][1-8][qrbnk]?", move)
            for move in search_moves):
        raise ProtocolError("INVALID_STATE", "searchMoves must contain UCI board moves")
    expected_moves = [move_to_uci(action) for action in history if action["type"] == "MOVE"]
    if search_moves != expected_moves:
        raise ProtocolError("INVALID_STATE", "searchMoves must exactly match board-changing history actions")


def state_fen(state):
    board = state["board"]
    ranks = []
    for row in range(8):
        encoded, empty = [], 0
        for col in range(8):
            cell = board[row * 8 + col]
            if cell == ".":
                empty += 1
                continue
            if empty:
                encoded.append(str(empty))
                empty = 0
            symbol = FEN_PIECES[cell[1]]
            encoded.append(symbol.upper() if cell[0] == "w" else symbol)
        if empty:
            encoded.append(str(empty))
        ranks.append("".join(encoded))
    rights_bits = state["castlingRights"]
    right_names = "KQVkqv"
    rights = "".join(name for name, enabled in zip(right_names, rights_bits) if enabled == "1") or "-"
    ep = state["enPassant"] or "-"
    debt = state["debtTargetKings"]
    debt_code = "-" if debt is None else ("w" if debt == "WHITE" else "b")
    moved_pawns = 0
    for index, cell in enumerate(board):
        if cell != "." and cell[1] == "p" and cell[2] == "1":
            moved_pawns |= 1 << ((7 - index // 8) * 8 + index % 8)
    return ("/".join(ranks) + " " + ("w" if state["toMove"] == "WHITE" else "b")
            + " " + rights + " " + ep + " " + str(state["halfMovesSinceCaptureOrPawn"])
            + " 1 lc1:" + debt_code + " lm1:" + format(moved_pawns, "016x"))


def parse_fen_state(fen, original):
    fields = fen.split()
    if len(fields) < 7 or not fields[6].startswith("lc1:"):
        raise ProtocolError("ENGINE_STATE_INVALID", "Engine FEN is missing LifeChess extension")
    board = ["."] * 64
    rank_strings = fields[0].split("/")
    if len(rank_strings) != 8:
        raise ProtocolError("ENGINE_STATE_INVALID", "Engine returned malformed board")
    code_by_piece = {value.lower(): key for key, value in PIECES.items()}
    for row, rank in enumerate(rank_strings):
        col = 0
        for char in rank:
            if char.isdigit():
                col += int(char)
            else:
                side = "w" if char.isupper() else "b"
                kind = code_by_piece.get(char.lower())
                if kind is None or col >= 8:
                    raise ProtocolError("ENGINE_STATE_INVALID", "Engine returned malformed piece placement")
                index = row * 8 + col
                old = original["board"][index]
                moved = old[2] if old != "." and old[:2] == side + PIECES[kind] else "1"
                board[index] = side + PIECES[kind] + moved
                col += 1
        if col != 8:
            raise ProtocolError("ENGINE_STATE_INVALID", "Engine returned an incomplete rank")

    rights = fields[2]
    state = copy.deepcopy(original)
    state["board"] = board
    state["toMove"] = "WHITE" if fields[1] == "w" else "BLACK"
    state["castlingRights"] = "".join("1" if right in rights else "0" for right in "KQVkqv")
    state["enPassant"] = None if fields[3] == "-" else fields[3]
    state["halfMovesSinceCaptureOrPawn"] = int(fields[4])
    debt = fields[6][4:]
    if debt not in {"-", "w", "b"}:
        raise ProtocolError("ENGINE_STATE_INVALID", "Engine returned malformed debt extension")
    state["debtTargetKings"] = None if debt == "-" else ("WHITE" if debt == "w" else "BLACK")
    moved_field = next((field[4:] for field in fields[7:] if field.startswith("lm1:")), None)
    if moved_field is None or not re.fullmatch(r"[0-9a-fA-F]{16}", moved_field):
        raise ProtocolError("ENGINE_STATE_INVALID", "Engine FEN is missing the moved-pawn mask")
    moved_pawns = int(moved_field, 16)
    for index, cell in enumerate(state["board"]):
        if cell != "." and cell[1] == "p":
            fen_square = (7 - index // 8) * 8 + index % 8
            state["board"][index] = cell[:2] + ("1" if moved_pawns & (1 << fen_square) else "0")
    return state


def uci_action(uci, state):
    if not re.fullmatch(r"[a-h][1-8][a-h][1-8][qrbnk]?", uci):
        raise ProtocolError("ENGINE_MOVE_INVALID", "Engine returned invalid move token: " + uci)
    source, target = uci[:2], uci[2:4]
    from_sq, to_sq = square_index(source), square_index(target)
    moving = state["board"][from_sq]
    kind = "NORMAL"
    if moving != "." and moving[1] == "k":
        castle_targets = {
            ("WHITE", "e1", "g1"): "CASTLE_KING", ("WHITE", "e1", "c1"): "CASTLE_QUEEN",
            ("WHITE", "e1", "e3"): "CASTLE_VERTICAL", ("BLACK", "e8", "g8"): "CASTLE_KING",
            ("BLACK", "e8", "c8"): "CASTLE_QUEEN", ("BLACK", "e8", "e6"): "CASTLE_VERTICAL",
        }
        kind = castle_targets.get((state["toMove"], source, target), kind)
    elif moving != "." and moving[1] == "p" and state["enPassant"] == target and state["board"][to_sq] == ".":
        kind = "EN_PASSANT"
    promotion = PROMOTIONS[uci[4]] if len(uci) == 5 else None
    result = {"type": "MOVE", "from": source, "to": target, "moveKind": kind, "promotion": promotion}
    return result


def same_action(left, right):
    return all(left.get(key) == right.get(key) for key in ("type", "from", "to", "moveKind", "promotion"))


def move_to_uci(action):
    if action.get("type") != "MOVE":
        raise ProtocolError("INVALID_ACTION", "Expected a move action")
    source, target = action.get("from"), action.get("to")
    square_index(source)
    square_index(target)
    promotion = action.get("promotion")
    suffix = {value: key for key, value in PROMOTIONS.items()}.get(promotion, "") if promotion is not None else ""
    if promotion is not None and not suffix:
        raise ProtocolError("INVALID_ACTION", "Unknown promotion choice")
    return source + target + suffix


def position_key(state):
    result = []
    home = {"WHITE": 6, "BLACK": 1}
    pawn_direction = {"WHITE": -1, "BLACK": 1}
    for index, cell in enumerate(state["board"]):
        if cell == ".":
            result.append(".")
        else:
            side = "WHITE" if cell[0] == "w" else "BLACK"
            ordinal = {"k": 0, "q": 1, "r": 2, "b": 3, "n": 4, "p": 5}[cell[1]]
            first = cell[1] == "p" and index // 8 == home[side] and cell[2] == "0"
            result.append(cell[0] + str(ordinal) + ("f" if first else "m"))
    turn = state["toMove"]
    debt = state["debtTargetKings"] or "null"
    rights = state["castlingRights"]
    result.append("|" + turn + "|" + debt + "|" + "".join("true" if b == "1" else "false" for b in rights))
    ep = state["enPassant"]
    if ep is not None:
        ep_index = square_index(ep)
        row, col = divmod(ep_index, 8)
        source_row = row - pawn_direction[turn]
        victim_index = ep_index - pawn_direction[turn] * 8
        victim_valid = 0 <= victim_index < 64 and state["board"][victim_index][:2] == (
            ("b" if turn == "WHITE" else "w") + "p"
        )
        capturable = any(
            0 <= source_row < 8 and 0 <= col + dc < 8
            and state["board"][source_row * 8 + col + dc][:2] == ("w" if turn == "WHITE" else "b") + "p"
            and victim_valid
            for dc in (-1, 1)
        )
        if not capturable:
            ep = None
    result.append("|" + str(-1 if ep is None else square_index(ep)))
    return "".join(result)


def procedural_actions(state):
    if state["result"] != RESULT_NONE:
        return []
    actions = []
    counts = {entry["key"]: entry["count"] for entry in state["repetitions"]}
    if counts.get(position_key(state), 0) >= 3:
        actions.append({"type": "CLAIM_REPETITION"})
    if state["halfMovesSinceCaptureOrPawn"] >= 100:
        actions.append({"type": "CLAIM_FIFTY_MOVES"})
    pieces = [(i, piece) for i, piece in enumerate(state["board"]) if piece != "."]
    kings = [(i, piece[0]) for i, piece in pieces if piece[1] == "k"]
    if state["debtTargetKings"] is None and len(pieces) == 2 and len(kings) == 2 \
            and {side for _, side in kings} == {"w", "b"}:
        distance = max(abs(kings[0][0] % 8 - kings[1][0] % 8),
                       abs(kings[0][0] // 8 - kings[1][0] // 8))
        if distance > 1:
            actions.append({"type": "CLAIM_BARE_KINGS"})
    if state["drawOfferBy"] is not None:
        if state["drawOfferBy"] == state["toMove"]:
            return actions
        actions.extend([{"type": "ACCEPT_DRAW"}, {"type": "DECLINE_DRAW"}])
    else:
        actions.append({"type": "OFFER_DRAW"})
    return actions


def update_repetitions(state):
    key = position_key(state)
    for item in state["repetitions"]:
        if item["key"] == key:
            item["count"] += 1
            break
    else:
            state["repetitions"].append({"key": key, "count": 1})


def mark_missing_turn_king_lost(state):
    updated = copy.deepcopy(state)
    side_code = "w" if updated["toMove"] == "WHITE" else "b"
    if any(cell != "." and cell[:2] == side_code + "k" for cell in updated["board"]):
        return None
    updated["result"] = "BLACK_WIN" if updated["toMove"] == "WHITE" else "WHITE_WIN"
    updated["drawOfferBy"] = None
    return updated


def apply_local_procedure(state, action):
    updated = copy.deepcopy(state)
    action_type = action["type"]
    if action_type == "OFFER_DRAW":
        updated["drawOfferBy"] = updated["toMove"]
        updated["drawOfferSentInCurrentNonWinningStretch"] = True
    elif action_type == "ACCEPT_DRAW":
        updated["result"] = "DRAW_AGREEMENT"
        updated["drawOfferBy"] = None
    elif action_type == "DECLINE_DRAW":
        updated["drawOfferBy"] = None
    elif action_type == "CLAIM_REPETITION":
        updated["result"] = "DRAW_REPETITION"
        updated["drawOfferBy"] = None
    elif action_type == "CLAIM_FIFTY_MOVES":
        updated["result"] = "DRAW_FIFTY_MOVES"
        updated["drawOfferBy"] = None
    elif action_type == "CLAIM_BARE_KINGS":
        updated["result"] = "DRAW_BARE_KINGS"
        updated["drawOfferBy"] = None
    updated["history"].append(copy.deepcopy(action))
    return updated


def apply_move_state(before, action, engine_fen):
    updated = parse_fen_state(engine_fen, before)
    from_index, to_index = square_index(action["from"]), square_index(action["to"])
    moving = before["board"][from_index]
    captured = before["board"][to_index]
    if moving == ".":
        raise ProtocolError("INVALID_ACTION", "Move source is empty")
    if action["moveKind"] == "EN_PASSANT":
        captured_index = to_index - (-1 if moving[0] == "w" else 1) * 8
        captured = before["board"][captured_index]
    else:
        captured_index = to_index

    expected = copy.deepcopy(before["board"])
    expected[from_index] = "."
    expected[captured_index] = "."
    if action["moveKind"].startswith("CASTLE_"):
        side = "WHITE" if moving[0] == "w" else "BLACK"
        rook_from, rook_to = {
            ("WHITE", "CASTLE_KING"): ("h1", "f1"), ("WHITE", "CASTLE_QUEEN"): ("a1", "d1"),
            ("WHITE", "CASTLE_VERTICAL"): ("e8", "e2"), ("BLACK", "CASTLE_KING"): ("h8", "f8"),
            ("BLACK", "CASTLE_QUEEN"): ("a8", "d8"), ("BLACK", "CASTLE_VERTICAL"): ("e1", "e7"),
        }[(side, action["moveKind"])]
        rook = expected[square_index(rook_from)]
        if rook == "." or rook[0] != moving[0] or rook[1] != "r":
            raise ProtocolError("ENGINE_STATE_INVALID", "Castling result has no matching rook")
        expected[square_index(rook_from)] = "."
        expected[square_index(rook_to)] = rook[0:2] + "1"

    promotion = action.get("promotion")
    if promotion is not None:
        expected[to_index] = moving[0] + PIECES[promotion] + "0"
    else:
        expected[to_index] = moving[0:2] + "1"
    if [piece[:2] if piece != "." else "." for piece in expected] != [piece[:2] if piece != "." else "." for piece in updated["board"]]:
        raise ProtocolError("ENGINE_STATE_MISMATCH", "C++ resulting board differs from action replay")
    updated["board"] = expected

    target_side = "BLACK" if moving[0] == "w" else "WHITE"
    next_side = target_side
    if captured != "." and captured[1] == "k" and sum(1 for p in updated["board"] if p != "." and p[:2] == ("w" if target_side == "WHITE" else "b") + "k") == 0:
        updated["result"] = "WHITE_WIN" if moving[0] == "w" else "BLACK_WIN"
    elif sum(1 for p in updated["board"] if p != "." and p[:2] == ("w" if next_side == "WHITE" else "b") + "k") == 0:
        updated["result"] = "WHITE_WIN" if moving[0] == "w" else "BLACK_WIN"
    if updated["result"] != "NONE":
        updated["drawOfferBy"] = None
    elif updated["drawOfferBy"] is not None:
        moving_side = "WHITE" if moving[0] == "w" else "BLACK"
        if updated["drawOfferBy"] != moving_side:
            updated["drawOfferBy"] = None
    updated["history"].append(copy.deepcopy(action))
    updated["searchMoves"].append(move_to_uci(action))
    update_repetitions(updated)
    return updated


def response(request, kind, state=None, actions=None, selected=None, error_code=None, message=None):
    request = request if isinstance(request, dict) else {}
    return {
        "protocol": PROTOCOL, "version": VERSION, "requestId": request.get("requestId", "invalid"),
        "stateRevision": request.get("stateRevision", 0), "variant": PROTOCOL,
        "kind": kind, "state": state, "legalActions": actions or [],
        "selectedAction": selected, "errorCode": error_code, "message": message,
    }


def handle(request, engine, depth):
    validate_request(request)
    state = request["state"]
    operation = request["operation"]
    if operation == "EXPORT_STATE":
        return response(request, "STATE_UPDATED", copy.deepcopy(state))
    if state["result"] != RESULT_NONE:
        if operation == "LEGAL_ACTIONS":
            return response(request, "LEGAL_ACTIONS", copy.deepcopy(state), [])
        return response(request, "GAME_OVER", copy.deepcopy(state))

    missing_king_result = mark_missing_turn_king_lost(state)
    if missing_king_result is not None:
        return response(request, "GAME_OVER", missing_king_result)

    fen = state_fen(state)
    uci_moves = engine.legal_moves(fen)
    engine_actions = [uci_action(move, state) for move in uci_moves]
    if operation == "LEGAL_ACTIONS":
        return response(request, "LEGAL_ACTIONS", copy.deepcopy(state), engine_actions + procedural_actions(state))

    if operation == "APPLY_ACTION":
        action = request["action"]
        candidates = engine_actions + procedural_actions(state)
        if not any(same_action(action, candidate) for candidate in candidates):
            return response(request, "ERROR", error_code="ILLEGAL_ACTION", message="Action is not legal in this state")
        if action["type"] != "MOVE":
            updated = apply_local_procedure(state, action)
        else:
            uci = move_to_uci(action)
            resulting_fen = engine.apply_move_and_get_fen(fen, uci)
            updated = apply_move_state(state, action, resulting_fen)
        return response(request, "STATE_UPDATED", updated, selected=action)

    if operation == "SEARCH":
        draw_context = {
            "pending": state["drawOfferBy"] is not None
                        and state["drawOfferBy"] != state["toMove"],
            "repetition_claim": any(action["type"] == "CLAIM_REPETITION"
                                     for action in procedural_actions(state)),
            "fifty_moves_claim": any(action["type"] == "CLAIM_FIFTY_MOVES"
                                      for action in procedural_actions(state)),
            "claimable": any(action["type"] in {"CLAIM_REPETITION", "CLAIM_FIFTY_MOVES", "CLAIM_BARE_KINGS"}
                             for action in procedural_actions(state)),
            "offered": getattr(engine, "draw_offer_sent_override",
                                state["drawOfferSentInCurrentNonWinningStretch"]),
        }
        replay = "position fen " + state["searchRootFen"]
        if state["searchMoves"]:
            replay += " moves " + " ".join(state["searchMoves"])
        bestmove, lines, draw_decision, reset_offer_latch = engine.search(
            fen, depth, draw_context, replay)
        if draw_decision in {"claim", "claim_repetition", "claim_fifty_moves",
                             "claim_bare_kings", "accept", "decline", "offer"}:
            if draw_decision in {"claim", "claim_repetition", "claim_fifty_moves", "claim_bare_kings"}:
                selected_type = {
                    "claim_bare_kings": "CLAIM_BARE_KINGS",
                    "claim_repetition": "CLAIM_REPETITION",
                    "claim_fifty_moves": "CLAIM_FIFTY_MOVES",
                }.get(draw_decision)
                action = next((a for a in procedural_actions(state)
                               if a["type"] == selected_type), None) if selected_type else None
            elif draw_decision == "accept":
                action = {"type": "ACCEPT_DRAW"}
            elif draw_decision == "decline":
                action = {"type": "DECLINE_DRAW"}
            else:
                action = {"type": "OFFER_DRAW"}
            if action is None:
                return response(request, "ERROR", error_code="DRAW_CONTEXT_MISMATCH",
                                message=("Engine selected " + draw_decision
                                         + " without a matching legal claim; legal="
                                         + repr(procedural_actions(state)) + "; search="
                                         + "\\n".join(lines)))
            candidates = procedural_actions(state)
            if not any(same_action(action, candidate) for candidate in candidates):
                return response(request, "ERROR", error_code="DRAW_ACTION_ILLEGAL",
                                message="Engine selected a draw action not legal in the supplied state")
            updated = apply_local_procedure(state, action)
            if draw_decision == "decline":
                updated["drawOfferBy"] = None
            if reset_offer_latch:
                updated["drawOfferSentInCurrentNonWinningStretch"] = False
            details = "\n".join(line for line in lines if line.startswith("info depth ")
                                  or line.startswith("info string lifechess-action "))
            return response(request, "DRAW_ACTION", updated, selected=action, message=details)
        if bestmove in {"(none)", "0000"}:
            return response(request, "ERROR", copy.deepcopy(state), error_code="NO_LEGAL_ACTIONS",
                            message="Engine returned no move although the side to move has a king")
        selected = uci_action(bestmove, state)
        if not any(same_action(selected, candidate) for candidate in engine_actions):
            return response(request, "ERROR", copy.deepcopy(state), error_code="ENGINE_ILLEGAL_MOVE",
                            message="Engine search returned an action outside the legal-action set")
        details = "\n".join(line for line in lines if line.startswith("info depth ")
                              or line.startswith("info string lifechess-action "))
        updated = copy.deepcopy(state)
        if reset_offer_latch:
            updated["drawOfferSentInCurrentNonWinningStretch"] = False
        return response(request, "SEARCH_RESULT", updated, selected=selected, message=details)
    raise ProtocolError("INVALID_OPERATION", "Unsupported operation")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--engine", required=True, help="Path to the built Fairy-Stockfish executable")
    parser.add_argument("--depth", type=int, default=5, help="Fixed search depth for SEARCH requests")
    args = parser.parse_args()
    if not 1 <= args.depth <= 64:
        parser.error("--depth must be between 1 and 64")

    for raw in sys.stdin:
        request = {}
        engine = None
        try:
            request = json.loads(raw)
            validate_request(request)
            engine = UciEngine(args.engine)
            result = handle(request, engine, args.depth)
        except ProtocolError as exc:
            result = response(request, "ERROR", error_code=exc.code, message=str(exc))
        except (ValueError, TypeError, KeyError, json.JSONDecodeError) as exc:
            result = response(request, "ERROR", error_code="INVALID_REQUEST", message=str(exc))
        except Exception as exc:  # Keep a malformed request from corrupting the JSON-lines stream.
            result = response(request, "ERROR", error_code="ADAPTER_FAILURE", message=str(exc))
        finally:
            if engine is not None:
                engine.close()
        print(json.dumps(result, separators=(",", ":"), ensure_ascii=True), flush=True)


if __name__ == "__main__":
    main()
