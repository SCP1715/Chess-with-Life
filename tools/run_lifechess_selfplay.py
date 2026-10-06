#!/usr/bin/env python3
"""Run reproducible LifeChess self-play games and save replayable JSONL logs."""

import argparse
import copy
import csv
import gzip
import hashlib
import json
import pathlib
import random
import re
import statistics
import subprocess
import sys
import time
import platform
from datetime import datetime, timezone

from tools import lifechess_console as console


FILES = "abcdefgh"
BASE_SEED = 20261006
NODES = 10000
MAX_PLIES = 300
MOVE_TIMEOUT_SECONDS = 30
OPENING_PLIES = 16
OPENING_MULTIPV = 5
OPENING_CP_WINDOW = 50


def board_index(square):
    return console.square_index(square)


def initial_state():
    board = ["."] * 64
    back = "rnbqkbnr"
    for col, symbol in enumerate(back):
        board[56 + col] = "w" + symbol.lower() + "0"
        board[48 + col] = "wp0"
        board[8 + col] = "bp0"
        board[col] = "b" + symbol + "0"
    state = {
        "board": board, "toMove": "WHITE", "enPassant": None,
        "debtTargetKings": None, "halfMovesSinceCaptureOrPawn": 0,
        "castlingRights": "111111", "result": "NONE", "drawOfferBy": None,
        "drawOfferSentInCurrentNonWinningStretch": False, "repetitions": [], "history": [],
        "searchMoves": [],
    }
    state["searchRootFen"] = console.state_fen(state)
    state["repetitions"] = [{"key": console.position_key(state), "count": 1}]
    return state


def request(operation, state, action=None, request_id="selfplay"):
    return {
        "protocol": "lifechess", "version": 2, "requestId": request_id,
        "stateRevision": len(state["history"]), "variant": "lifechess",
        "operation": operation, "state": state, "action": action,
    }


def move_history_position(state):
    moves = state.get("searchMoves", [])
    command = "position fen " + state["searchRootFen"]
    return command + (" moves " + " ".join(moves) if moves else "")


def parse_pvs(message):
    best_depth = -1
    candidates = {}
    pattern = re.compile(
        r"^info depth (\d+).*?multipv (\d+) score (cp|mate) (-?\d+).*? pv ([a-h][1-8][a-h][1-8][qrbnk]?)"
    )
    for line in message.splitlines():
        match = pattern.match(line)
        if not match:
            continue
        depth, index = int(match.group(1)), int(match.group(2))
        kind, raw_score, move = match.group(3), int(match.group(4)), match.group(5)
        if depth > best_depth:
            best_depth, candidates = depth, {}
        if depth == best_depth:
            candidates[move] = {"move": move, "multipv": index, "scoreType": kind,
                                "score": raw_score, "depth": depth}
    return sorted(candidates.values(), key=lambda item: item["multipv"])


def choose_opening_move(message, bestmove, rng):
    candidates = parse_pvs(message)
    if not candidates:
        return bestmove, {"candidateMoves": [bestmove], "randomized": False, "depth": None}
    best = candidates[0]
    if best["scoreType"] != "cp":
        eligible = [best]
    else:
        eligible = [item for item in candidates if item["scoreType"] == "cp"
                    and best["score"] - item["score"] <= OPENING_CP_WINDOW]
        if best not in eligible:
            eligible.insert(0, best)
    chosen = rng.choice(eligible)
    return chosen["move"], {
        "candidateMoves": [item["move"] for item in eligible],
        "candidateScores": [{"type": item["scoreType"], "value": item["score"]}
                            for item in eligible],
        "chosenScore": {"type": chosen["scoreType"], "value": chosen["score"]},
        "bestScore": {"type": best["scoreType"], "value": best["score"]},
        "depth": chosen["depth"], "randomized": len(eligible) > 1,
        "selectedAlternative": chosen["move"] != best["move"],
    }


def update_offer_latch(latches, side, reset, action):
    if reset:
        latches[side] = False
    if action == "OFFER_DRAW":
        latches[side] = True


def material_phase(state):
    values = {"q": 4, "r": 2, "b": 1, "n": 1}
    return min(24, sum(values.get(cell[1], 0) for cell in state["board"] if cell != "."))


def draw_policy_telemetry(state, legal_actions, lines, decision, offer_latches):
    pv = parse_pvs("\n".join(lines))
    score = pv[0] if pv else None
    phase = material_phase(state)
    threshold = -50 - (450 * phase + 12) // 24
    claims = [action["type"] for action in legal_actions
              if action["type"] in {"CLAIM_REPETITION", "CLAIM_FIFTY_MOVES"}]
    return {
        "positionPly": sum(action["type"] == "MOVE" for action in state["history"]),
        "side": state["toMove"],
        "claimableActions": claims,
        "opponentOfferPending": (state["drawOfferBy"] is not None
                                 and state["drawOfferBy"] != state["toMove"]),
        "offerLatchForSide": offer_latches[state["toMove"]],
        "decision": decision,
        "evaluation": ({"type": score["scoreType"], "value": score["score"],
                        "depth": score["depth"], "perspective": state["toMove"]}
                       if score else None),
        "materialPhase": phase,
        "claimAndOfferThresholdCp": threshold,
        "reason": None,
        "reasonAvailability": "not emitted by the engine protocol",
    }


def side_name(code):
    return "WHITE" if code == "w" else "BLACK"


def piece_name(cell):
    names = {"k": "KING", "q": "QUEEN", "r": "ROOK", "b": "BISHOP", "n": "KNIGHT", "p": "PAWN"}
    return names[cell[1]] if cell != "." else None


def attacks_square(state, target, attacker):
    board = state["board"]
    tr, tc = divmod(target, 8)
    for source, cell in enumerate(board):
        if cell == "." or cell[0] != attacker:
            continue
        sr, sc = divmod(source, 8)
        dr, dc = tr - sr, tc - sc
        kind = cell[1]
        if kind == "p":
            if dr == (-1 if attacker == "w" else 1) and abs(dc) == 1:
                return True
        elif kind == "n":
            if (abs(dr), abs(dc)) in {(1, 2), (2, 1)}:
                return True
        elif kind == "k":
            if max(abs(dr), abs(dc)) == 1:
                return True
        elif kind in "qrb":
            diagonal = abs(dr) == abs(dc) and dr != 0
            straight = (dr == 0) != (dc == 0)
            if (kind == "q" and (diagonal or straight)) or (kind == "r" and straight) or (kind == "b" and diagonal):
                step_r = (dr > 0) - (dr < 0)
                step_c = (dc > 0) - (dc < 0)
                row, col = sr + step_r, sc + step_c
                clear = True
                while (row, col) != (tr, tc):
                    if board[row * 8 + col] != ".":
                        clear = False
                        break
                    row += step_r
                    col += step_c
                if clear:
                    return True
    return False


def attacked_king_squares(state, side):
    attacker = "b" if side == "w" else "w"
    return [console.square_name(index) for index, cell in enumerate(state["board"])
            if cell != "." and cell[0] == side and cell[1] == "k"
            and attacks_square(state, index, attacker)]


def captured_piece(before, action):
    if action["type"] != "MOVE":
        return None, None
    target = board_index(action["to"])
    if action["moveKind"] == "EN_PASSANT":
        target += 8 if before["toMove"] == "WHITE" else -8
    return before["board"][target], target


def transition_events(before, action, after, legal_actions, king_details=None, king_ids_before=None):
    events = []
    if action["type"] != "MOVE":
        draw_types = {
            "CLAIM_FIFTY_MOVES": "DRAW_CLAIM_FIFTY_MOVES",
            "CLAIM_REPETITION": "DRAW_CLAIM_REPETITION",
            "OFFER_DRAW": "DRAW_OFFERED",
            "ACCEPT_DRAW": "DRAW_ACCEPTED",
            "DECLINE_DRAW": "DRAW_DECLINED",
        }
        if action["type"] in draw_types:
            events.append({"type": draw_types[action["type"]], "side": before["toMove"],
                           "ply": sum(a["type"] == "MOVE" for a in after["history"]),
                           "stateResult": after["result"]})
        return events
    moving = before["board"][board_index(action["from"])]
    captured, captured_square = captured_piece(before, action)
    mover = "w" if before["toMove"] == "WHITE" else "b"
    opposing = "b" if mover == "w" else "w"
    move_number = len([a for a in after["history"] if a["type"] == "MOVE"])
    ply = move_number
    if moving[1] == "k" and captured != "." and captured[0] == mover:
        safe_alternatives = []
        for candidate in legal_actions:
            if candidate["type"] != "MOVE" or candidate["from"] != action["from"]:
                continue
            to_index = board_index(candidate["to"])
            target = before["board"][to_index]
            if target != "." and target[0] == mover:
                continue
            if not attacks_square(before, to_index, opposing):
                safe_alternatives.append(candidate["to"])
        events.append({
            "type": "KING_SELF_CAPTURE", "side": before["toMove"], "ply": ply,
            "from": action["from"], "to": action["to"],
            "kingId": (king_details or {}).get("movingKingId"),
            "capturedType": piece_name(captured),
            "capturedPieceId": ((king_ids_before or {}).get(captured_square)
                                 if captured[1] == "k" else None),
            "kingAttackedBefore": bool(attacked_king_squares(before, mover)),
            "kingAttackedAfter": bool(attacked_king_squares(after, mover)),
            "safeNonSelfCaptureDestinations": safe_alternatives,
            "hasSafeNonSelfCapture": bool(safe_alternatives),
        })
    if action.get("promotion") is not None:
        events.append({"type": "PROMOTION", "side": before["toMove"], "ply": ply,
                       "from": action["from"], "to": action["to"],
                       "piece": action["promotion"]})
        if action["promotion"] == "KING":
            events.append({"type": "KING_PROMOTION", "side": before["toMove"], "ply": ply,
                           "square": action["to"], "debtCreated": after["debtTargetKings"] is not None,
                           "debtTarget": after["debtTargetKings"],
                           "newKingId": (king_details or {}).get("newKingId")})
        if action["promotion"] != "KING" and any(
                candidate["type"] == "MOVE" and candidate["from"] == action["from"]
                and candidate["promotion"] == "KING" for candidate in legal_actions):
            events.append({"type": "KING_PROMOTION_AVAILABLE_OTHER_CHOSEN", "side": before["toMove"],
                           "ply": ply, "square": action["to"], "chosenPromotion": action["promotion"]})
    if before["debtTargetKings"] is not None:
        target_color = "w" if before["debtTargetKings"] == "WHITE" else "b"
        possible = [candidate for candidate in legal_actions if candidate["type"] == "MOVE"
                    and before["board"][board_index(candidate["to"])] != "."
                    and before["board"][board_index(candidate["to"])][:2] == target_color + "k"]
        captured_king = captured != "." and captured[0] == target_color and captured[1] == "k"
        events.append({"type": "DEBT_RESPONSE", "side": before["toMove"], "ply": ply,
                       "targetSide": before["debtTargetKings"], "from": action["from"], "to": action["to"],
                       "targetKingIds": [king_id for square, king_id in (king_ids_before or {}).items()
                                         if before["board"][square] != "."
                                         and before["board"][square][:2] == target_color + "k"],
                       "legalTargetKingCaptures": len(possible),
                       "capturedTargetKing": captured_king,
                       "capturedKingSquare": action["to"] if captured_king else None,
                       "capturedKingId": (king_details or {}).get("capturedKingId") if captured_king else None,
                       "capturingPiece": piece_name(moving),
                       "capturerKingsAttackedBefore": bool(attacked_king_squares(before, mover)),
                       "capturerKingsAttackedAfter": bool(attacked_king_squares(after, mover)),
                       "chainContinues": action.get("promotion") == "KING" and after["debtTargetKings"] is not None})
    if action["moveKind"].startswith("CASTLE_"):
        source, dest = board_index(action["from"]), board_index(action["to"])
        sr, sc = divmod(source, 8)
        dr, dc = divmod(dest, 8)
        step_r = (dr > sr) - (dr < sr)
        step_c = (dc > sc) - (dc < sc)
        route = []
        row, col = sr + step_r, sc + step_c
        while (row, col) != (dr + step_r, dc + step_c):
            route.append(row * 8 + col)
            row += step_r
            col += step_c
        attacked = [attacks_square(before, sq, opposing) for sq in [source] + route]
        events.append({"type": "CASTLING", "side": before["toMove"], "ply": ply,
                       "kind": action["moveKind"], "fromAttacked": attacked[0],
                       "throughAttacked": any(attacked[1:-1]), "destinationAttacked": attacked[-1]})
    if captured != "." and captured[1] == "k":
        remaining = sum(1 for cell in after["board"] if cell != "." and cell[:2] == captured[:2])
        if remaining == 0:
            events.append({"type": "LAST_OPPONENT_KING_CAPTURED", "side": before["toMove"],
                           "ply": ply, "square": action["to"],
                           "kingId": (king_details or {}).get("capturedKingId"),
                           "result": after["result"]})
    king_attacks_before = attacked_king_squares(before, mover)
    king_attacks_after = attacked_king_squares(after, mover)
    if king_attacks_before and king_attacks_after:
        events.append({"type": "KING_LEFT_UNDER_ATTACK", "side": before["toMove"], "ply": ply,
                       "kingSquaresBefore": king_attacks_before, "kingSquaresAfter": king_attacks_after,
                       "createdDebt": before["debtTargetKings"] is None and after["debtTargetKings"] is not None,
                       "endedByLastKingCapture": after["result"] in {"WHITE_WIN", "BLACK_WIN"}})
    opponent_kings = [index for index, cell in enumerate(before["board"])
                      if cell != "." and cell[:2] == opposing + "k"]
    last_king_captures = [candidate for candidate in legal_actions if candidate["type"] == "MOVE"
                          and before["board"][board_index(candidate["to"])] != "."
                          and before["board"][board_index(candidate["to"])][:2] == opposing + "k"
                          and len(opponent_kings) == 1]
    if last_king_captures and not (captured != "." and captured[:2] == opposing + "k"):
        events.append({"type": "MISSED_LAST_KING_CAPTURE", "side": before["toMove"], "ply": ply,
                       "availableCaptures": last_king_captures})
    vertical_available = [candidate for candidate in legal_actions if candidate["type"] == "MOVE"
                          and candidate["moveKind"] == "CASTLE_VERTICAL"]
    if vertical_available:
        events.append({"type": "VERTICAL_CASTLING_AVAILABLE", "side": before["toMove"], "ply": ply,
                       "selected": action["moveKind"] == "CASTLE_VERTICAL"})
    non_king_material = [cell for cell in after["board"] if cell != "." and cell[1] not in {"k", "p"}]
    before_non_king_material = [cell for cell in before["board"] if cell != "." and cell[1] not in {"k", "p"}]
    if len(non_king_material) <= 3 and len(before_non_king_material) > 3:
        events.append({"type": "LOW_MATERIAL_POSITION", "side": after["toMove"], "ply": ply,
                       "countNonKingNonPawn": len(non_king_material),
                       "composition": {name: sum(piece_name(cell) == name for cell in non_king_material)
                                       for name in ("QUEEN", "ROOK", "BISHOP", "KNIGHT")},
                       "result": after["result"]})
    return events


def engine_revision(repo):
    return subprocess.check_output(["git", "-C", str(repo / "third_party" / "fairy-stockfish"),
                                    "rev-parse", "HEAD"], text=True).strip()


def engine_patch_hash(repo):
    engine_repo = repo / "third_party" / "fairy-stockfish"
    tracked = subprocess.check_output(["git", "-C", str(engine_repo), "diff", "HEAD", "--binary"])
    untracked = subprocess.check_output(
        ["git", "-C", str(engine_repo), "ls-files", "--others", "--exclude-standard"], text=True
    ).splitlines()
    digest = hashlib.sha256(tracked)
    for relative in sorted(untracked):
        path = engine_repo / relative
        if path.is_file():
            digest.update(relative.encode("utf-8"))
            digest.update(path.read_bytes())
    return digest.hexdigest()


def recover_partial_attempt(game_id, seed, log_path, reason, message):
    plies = 0
    if log_path.is_file():
        for record in read_json_lines(log_path):
            if record.get("record") == "ACTION":
                plies = max(plies, int(record.get("ply", 0)))
    result = {"gameId": game_id, "seed": seed,
              "status": "INTERRUPTED" if reason == "ENGINE_TIMEOUT" else "ERROR",
              "reason": reason, "message": message, "plies": plies}
    write_json_line(log_path, {"record": "END", **result})
    return result


def king_id_map(state):
    ids = {}
    counters = {"w": 0, "b": 0}
    for square, cell in enumerate(state["board"]):
        if cell != "." and cell[1] == "k":
            counters[cell[0]] += 1
            ids[square] = f"{cell[0].upper()}K{counters[cell[0]]}"
    return ids, counters


def advance_king_ids(before, action, after, ids, counters):
    if action["type"] != "MOVE":
        return None
    source, target = board_index(action["from"]), board_index(action["to"])
    captured, captured_square = captured_piece(before, action)
    moving = before["board"][source]
    moving_id = ids.pop(source, None) if moving[1] == "k" else None
    captured_id = ids.pop(captured_square, None) if captured != "." and captured[1] == "k" else None
    if moving_id is not None and action.get("promotion") != "KING":
        ids[target] = moving_id
    new_id = None
    if action.get("promotion") == "KING":
        counters[moving[0]] += 1
        new_id = f"{moving[0].upper()}K{counters[moving[0]]}"
        ids[target] = new_id
    return {"movingKingId": moving_id, "capturedKingId": captured_id, "newKingId": new_id}


def analyze_pawn_episodes(records, game_id):
    start = next((record["initialState"] for record in records if record.get("record") == "START"), None)
    if start is None:
        return []
    pawn_ids = {}
    counters = {"w": 0, "b": 0}
    for square, cell in enumerate(start["board"]):
        if cell != "." and cell[1] == "p":
            counters[cell[0]] += 1
            pawn_ids[square] = f"{cell[0].upper()}P{counters[cell[0]]}"
    episodes = {}
    completed = []
    current_ply = 0
    for record in records:
        if record.get("record") != "ACTION":
            continue
        before, after, action = record["before"], record["after"], record["action"]
        current_ply = record["ply"]
        for square, pawn_id in list(pawn_ids.items()):
            cell = before["board"][square]
            row = square // 8
            advanced = (cell != "." and cell[1] == "p"
                        and ((cell[0] == "w" and row == 1) or (cell[0] == "b" and row == 6)))
            if advanced and pawn_id not in episodes:
                episodes[pawn_id] = {"gameId": game_id, "pawnId": pawn_id,
                                     "side": side_name(cell[0]), "startPly": current_ply,
                                     "ownDecisions": 0, "availablePromotionDecisions": 0,
                                     "availableKingPromotionDecisions": 0, "declinedAvailablePromotions": 0,
                                     "unavailable": [], "finish": None}
            episode = episodes.get(pawn_id)
            if not advanced or episode is None or before["toMove"] != side_name(cell[0]):
                continue
            # A pawn decision is an executed game move, not a draw-procedure
            # action. Offers/rejections may revisit this same position.
            if action["type"] != "MOVE":
                continue
            episode["ownDecisions"] += 1
            promotion_actions = [candidate for candidate in record["legalActions"]
                                 if candidate["type"] == "MOVE" and candidate["from"] == console.square_name(square)
                                 and candidate["promotion"] is not None]
            if promotion_actions:
                episode["availablePromotionDecisions"] += 1
                if any(candidate["promotion"] == "KING" for candidate in promotion_actions):
                    episode["availableKingPromotionDecisions"] += 1
                if not (action.get("from") == console.square_name(square) and action.get("promotion") is not None):
                    episode["declinedAvailablePromotions"] += 1
            else:
                direction = -1 if cell[0] == "w" else 1
                forward = square + direction * 8
                reason = "DEBT" if before["debtTargetKings"] is not None else (
                    "BLOCKED" if 0 <= forward < 64 and before["board"][forward] != "." else "OTHER_RULE_OR_CAPTURE_ONLY")
                episode["unavailable"].append({"ply": current_ply, "reason": reason})

        moved_id = None
        if action["type"] == "MOVE":
            source = board_index(action["from"])
            target = board_index(action["to"])
            moving = before["board"][source]
            captured, captured_square = captured_piece(before, action)
            if captured != "." and captured[1] == "p":
                captured_id = pawn_ids.pop(captured_square, None)
                if captured_id in episodes:
                    episode = episodes.pop(captured_id)
                    episode.update(endPly=current_ply, finish="CAPTURED", durationPlies=current_ply - episode["startPly"])
                    completed.append(episode)
            if moving[1] == "p":
                moved_id = pawn_ids.pop(source, None)
                if action.get("promotion") is not None:
                    if moved_id in episodes:
                        episode = episodes.pop(moved_id)
                        episode.update(endPly=current_ply, finish="PROMOTED_" + action["promotion"],
                                       durationPlies=current_ply - episode["startPly"])
                        completed.append(episode)
                elif moved_id is not None:
                    pawn_ids[target] = moved_id
        for square, cell in enumerate(after["board"]):
            if cell == "." or cell[1] != "p" or square in pawn_ids:
                continue
            if (cell[0] == "w" and square // 8 == 1) or (cell[0] == "b" and square // 8 == 6):
                counters[cell[0]] += 1
                pawn_ids[square] = f"{cell[0].upper()}P{counters[cell[0]]}"
    end_record = next((record for record in reversed(records) if record.get("record") == "END"), {})
    finish = "GAME_END" if end_record.get("status") == "COMPLETED" else "GAME_STOPPED"
    for pawn_id, episode in episodes.items():
        episode.update(endPly=current_ply, finish=finish, durationPlies=current_ply - episode["startPly"])
        completed.append(episode)
    return completed


def write_json_line(path, value):
    opener = gzip.open if path.suffix == ".gz" else open
    with opener(path, "at", encoding="utf-8", newline="\n") as stream:
        stream.write(json.dumps(value, ensure_ascii=False, separators=(",", ":")) + "\n")


def read_json_lines(path):
    opener = gzip.open if path.suffix == ".gz" else open
    with opener(path, "rt", encoding="utf-8") as stream:
        return [json.loads(line) for line in stream if line.strip()]


def game_loop(game_id, seed, engine, log_path, *, profile="A", node_budget=NODES,
              max_plies=MAX_PLIES, opening_plies=OPENING_PLIES,
              opening_multipv=OPENING_MULTIPV, fixed_prefix=None,
              prefix_id=None, start_state=None, start_latches=None,
              king_ids=None, king_counters=None, stop_requested=None,
              parent_game_id=None, parent_profile_game_id=None):
    rng = random.Random(seed)
    state = copy.deepcopy(start_state) if start_state is not None else initial_state()
    if king_ids is None or king_counters is None:
        king_ids, king_counters = king_id_map(state)
    else:
        king_ids = {int(square): king_id for square, king_id in king_ids.items()}
        king_counters = dict(king_counters)
    offer_latches = dict(start_latches or {"WHITE": False, "BLACK": False})
    engine.new_game()
    engine.read_timeout_seconds = MOVE_TIMEOUT_SECONDS
    plies = sum(action["type"] == "MOVE" for action in state["history"])
    action_number = len(state["history"])
    randomized_plies = []
    prefix_index = 0
    attempt = {"gameId": game_id, "profile": profile, "seed": seed,
               "prefixId": prefix_id, "parentGameId": parent_game_id,
               "parentProfileGameId": parent_profile_game_id,
               "status": "IN_PROGRESS", "plies": plies}
    write_json_line(log_path, {"record": "START", **attempt, "initialState": state,
                               "engineOfferLatches": offer_latches,
                               "initialKingIds": king_ids,
                               "initialKingCounters": king_counters,
                               "openingPolicy": {"plies": opening_plies, "multiPV": opening_multipv,
                                                 "cpWindow": OPENING_CP_WINDOW},
                               "nodeBudget": node_budget, "maxPlies": max_plies})
    while state["result"] == "NONE":
        if plies >= max_plies:
            attempt.update(status="INTERRUPTED", reason="MAX_PLIES", plies=plies)
            break
        if stop_requested is not None and stop_requested():
            attempt.update(status="INTERRUPTED", reason="EXPERIMENT_STOP", plies=plies)
            break
        before = state
        engine.search_position_override = move_history_position(before)
        engine.search_node_budget = node_budget
        engine.search_multipv = opening_multipv if plies < opening_plies and profile in {"A", "B"} else 1
        side = before["toMove"]
        engine.draw_offer_sent_override = offer_latches[side]
        latches_before = dict(offer_latches)
        legal_reply = console.handle(request("LEGAL_ACTIONS", before,
                                             request_id=f"g{game_id}-l{action_number}"), engine, 1)
        if legal_reply["kind"] != "LEGAL_ACTIONS":
            attempt.update(status="ERROR", reason="LEGAL_ACTIONS_" + str(legal_reply["errorCode"]), plies=plies)
            break

        forced = fixed_prefix is not None and prefix_index < len(fixed_prefix)
        policy = None
        search_message = ""
        if forced:
            action = copy.deepcopy(fixed_prefix[prefix_index])
            applied = console.handle(request("APPLY_ACTION", before, action,
                                             request_id=f"g{game_id}-prefix{prefix_index}"), engine, 1)
            if applied["kind"] != "STATE_UPDATED":
                attempt.update(status="ERROR", reason="PREFIX_ILLEGAL_" + str(applied["errorCode"]),
                               message=applied["message"], plies=plies)
                break
            state = applied["state"]
            reset_offer_latch = False
            info = {"source": "SHARED_PREFIX", "prefixId": prefix_id,
                    "prefixActionIndex": prefix_index + 1, "budgetNodes": 0,
                    "offerLatchesBefore": latches_before}
            prefix_index += 1
            update_offer_latch(offer_latches, side, False, action["type"])
            info["offerLatchesAfter"] = dict(offer_latches)
            search_reply_kind = "PREFIX_ACTION"
        else:
            search_reply = console.handle(request("SEARCH", before,
                                                  request_id=f"g{game_id}-s{action_number}"), engine, 1)
            if search_reply["kind"] == "ERROR":
                code = search_reply["errorCode"] or "SEARCH_ERROR"
                attempt.update(status="INTERRUPTED" if code == "ENGINE_TIMEOUT" else "ERROR",
                               reason=code, message=search_reply["message"], plies=plies)
                break
            action = search_reply["selectedAction"]
            state = search_reply["state"]
            reset_offer_latch = getattr(engine, "last_search_reset_offer_latch", False)
            policy = draw_policy_telemetry(before, legal_reply["legalActions"],
                                           search_reply.get("message", "").splitlines(),
                                           action["type"], latches_before)
            search_message = search_reply.get("message", "")
            update_offer_latch(offer_latches, side, reset_offer_latch, action["type"])
            search_reply_kind = search_reply["kind"]

            if search_reply["kind"] == "DRAW_ACTION":
                action_number += 1
                events = transition_events(before, action, state, legal_reply["legalActions"],
                                           king_ids_before=dict(king_ids))
                write_json_line(log_path, {"record": "ACTION", "actionNumber": action_number,
                                           "ply": plies, "side": before["toMove"], "action": action,
                                           "before": before, "after": state,
                                           "legalActions": legal_reply["legalActions"],
                                           "search": {"kind": "DRAW_ACTION", "nodeBudget": node_budget,
                                                      "offerLatchesBefore": latches_before,
                                                      "offerLatchesAfter": dict(offer_latches)},
                                           "drawPolicy": policy, "searchInfo": search_message,
                                           "events": events})
                for event in events:
                    write_json_line(log_path.parent / "events.jsonl",
                                    {"gameId": game_id, "profile": profile, **event})
                continue

            bestmove = console.move_to_uci(action)
            info = {"source": "ENGINE_SEARCH", "randomized": False,
                    "candidateMoves": [bestmove], "budgetNodes": node_budget,
                    "positionCommand": engine.search_position_override,
                    "offerLatchesBefore": latches_before}
            if plies < opening_plies and profile in {"A", "B"}:
                chosen_uci, opening_info = choose_opening_move(search_message, bestmove, rng)
                info.update(opening_info)
                if info["randomized"]:
                    randomized_plies.append(plies + 1)
                action = console.uci_action(chosen_uci, before)
            applied = console.handle(request("APPLY_ACTION", state, action,
                                             request_id=f"g{game_id}-a{action_number}"), engine, 1)
            if applied["kind"] != "STATE_UPDATED":
                attempt.update(status="ERROR", reason="MOVE_APPLY_" + str(applied["errorCode"]),
                               message=applied["message"], plies=plies)
                break
            state = applied["state"]
            info["offerLatchesAfter"] = dict(offer_latches)

        king_ids_before = dict(king_ids)
        king_details = advance_king_ids(before, action, state, king_ids, king_counters)
        events = transition_events(before, action, state, legal_reply["legalActions"],
                                   king_details, king_ids_before)
        if action["type"] == "MOVE":
            unused_claims = [candidate["type"] for candidate in legal_reply["legalActions"]
                             if candidate["type"] in {"CLAIM_FIFTY_MOVES", "CLAIM_REPETITION"}]
            if unused_claims:
                events.append({"type": "DRAW_CLAIM_AVAILABLE_UNUSED", "side": before["toMove"],
                               "ply": plies + 1, "available": unused_claims})
            plies += 1
        action_number += 1
        info["profile"] = profile
        info["sharedPrefixAction"] = bool(forced)
        write_json_line(log_path, {"record": "ACTION", "actionNumber": action_number,
                                   "ply": plies, "side": before["toMove"], "action": action,
                                   "before": before, "after": state,
                                   "legalActions": legal_reply["legalActions"], "search": info,
                                   "drawPolicy": policy, "searchInfo": search_message,
                                   "events": events})
        for event in events:
            write_json_line(log_path.parent / "events.jsonl",
                            {"gameId": game_id, "profile": profile, **event})
    if state["result"] != "NONE":
        attempt.update(status="COMPLETED", result=state["result"], reason=state["result"], plies=plies)
    elif "status" not in attempt or attempt["status"] == "IN_PROGRESS":
        attempt.update(status="INTERRUPTED", reason="MAX_PLIES", plies=plies)
    attempt["randomizedPlies"] = randomized_plies
    attempt["finalState"] = state
    attempt["finalOfferLatches"] = offer_latches
    attempt["finalKingIds"] = king_ids
    attempt["finalKingCounters"] = king_counters
    write_json_line(log_path, {"record": "END", **attempt})
    return attempt


def summarize(attempts, output):
    completed = [item for item in attempts if item["status"] == "COMPLETED"]
    plies = [item["plies"] for item in completed]
    unique_sequences = set()
    unique_openings = set()
    all_events = []
    pawn_episodes = []
    completed_game_ids = {item["gameId"] for item in completed}
    for item in attempts:
        path = pathlib.Path(item["logPath"])
        records = read_json_lines(path)
        sequence = [record["action"] for record in records if record.get("record") == "ACTION"
                    and record["action"]["type"] == "MOVE"]
        unique_sequences.add(tuple((a["from"], a["to"], a["promotion"]) for a in sequence))
        unique_openings.add(tuple((a["from"], a["to"], a["promotion"]) for a in sequence[:10]))
        for record in records:
            if record.get("record") == "ACTION":
                all_events.extend({"gameId": item["gameId"], **event} for event in record.get("events", []))
        pawn_episodes.extend(analyze_pawn_episodes(records, item["gameId"]))
    counts = {}
    games_with_event = {}
    for event in all_events:
        counts[event["type"]] = counts.get(event["type"], 0) + 1
        games_with_event.setdefault(event["type"], set()).add(event["gameId"])
    completed_plies = sum(plies)
    self_captures = [event for event in all_events if event["type"] == "KING_SELF_CAPTURE"]
    victims = {}
    for event in self_captures:
        victims[event["capturedType"]] = victims.get(event["capturedType"], 0) + 1
    (output / "events.jsonl").write_text("".join(json.dumps(event, ensure_ascii=False, separators=(",", ":")) + "\n"
                                               for event in all_events), encoding="utf-8")
    (output / "pawn-episodes.jsonl").write_text("".join(json.dumps(episode, ensure_ascii=False, separators=(",", ":")) + "\n"
                                                       for episode in pawn_episodes), encoding="utf-8")
    return {
        "attempts": len(attempts), "completed": len(completed),
        "interrupted": sum(item["status"] == "INTERRUPTED" for item in attempts),
        "errors": sum(item["status"] == "ERROR" for item in attempts),
        "results": {name: sum(item.get("result") == name for item in completed)
                    for name in ("WHITE_WIN", "BLACK_WIN", "DRAW_AGREEMENT", "DRAW_FIFTY_MOVES", "DRAW_REPETITION")},
        "medianPlies": statistics.median(plies) if plies else None,
        "uniqueGames": len(unique_sequences), "uniqueFirstTenPlyOpenings": len(unique_openings),
        "randomizedPlyCount": sum(len(item.get("randomizedPlies", [])) for item in attempts),
        "selectedAlternativePlyCount": sum(
            sum(bool(record.get("search", {}).get("selectedAlternative")) for record in
                read_json_lines(pathlib.Path(item["logPath"]))
                if record.get("record") == "ACTION") for item in attempts),
        "eventCounts": counts,
        "partiesWithEvent": {kind: len(game_ids) for kind, game_ids in games_with_event.items()},
        "kingSelfCaptureVictims": victims,
        "kingSelfCapturesPer1000CompletedPlies": (1000 * len(self_captures) / completed_plies
                                                   if completed_plies else None),
        "pawnEpisodes": len(pawn_episodes),
        "pawnEpisodesWithThreeOrMoreDeclinedPromotions": sum(
            episode["declinedAvailablePromotions"] >= 3 for episode in pawn_episodes),
        "pawnEpisodeMaxDurationPlies": max((episode["durationPlies"] for episode in pawn_episodes), default=0),
        "completedPlies": completed_plies,
    }


def write_report(output, manifest, summary):
    def percent(numerator, denominator):
        return "n/a" if not denominator else f"{100 * numerator / denominator:.1f}% ({numerator}/{denominator})"
    results = summary["results"]
    lines = [
        "# LifeChess self-play: 100-game research run", "",
        "Цель — описательная проверка механик, не оценка Elo.", "",
        f"- Запрошено партий: {summary['attempts']}; завершено: {summary['completed']}; "
        f"прервано: {summary['interrupted']}; технических ошибок: {summary['errors']}",
        f"- Победы белых: {results['WHITE_WIN']}; победы чёрных: {results['BLACK_WIN']}; "
        f"соглашения: {results['DRAW_AGREEMENT']}; заявления 50 ходов: {results['DRAW_FIFTY_MOVES']}; "
        f"заявления повторения: {results['DRAW_REPETITION']}",
        f"- Медианная длина завершённой партии: {summary['medianPlies']} полуходов "
        f"(знаменатель: {summary['completed']} завершённых партий).",
        f"- Уникальные последовательности ходов: {summary['uniqueGames']}/{summary['attempts']}; "
        f"уникальные первые 10 полуходов: {summary['uniqueFirstTenPlyOpenings']}/{summary['attempts']}.",
        f"- Полуходы с несколькими допустимыми кандидатами в дебюте: "
        f"{summary['randomizedPlyCount']}; рандомизация ограничена первыми {OPENING_PLIES} полуходами.",
        f"- Выбрана не первая линия MultiPV в {summary['selectedAlternativePlyCount']} полуходах.",
        f"- Собственные взятия королём: {summary['eventCounts'].get('KING_SELF_CAPTURE', 0)}; "
        f"партий с событием: {summary['partiesWithEvent'].get('KING_SELF_CAPTURE', 0)}/{summary['attempts']}; "
        f"частота: {summary['kingSelfCapturesPer1000CompletedPlies']} на 1000 завершённых полуходов.",
        f"- Жертвы при собственном взятии: {json.dumps(summary['kingSelfCaptureVictims'], ensure_ascii=False)}.",
        f"- Превращения в короля: {summary['eventCounts'].get('KING_PROMOTION', 0)}; "
        f"ответы по долгу: {summary['eventCounts'].get('DEBT_RESPONSE', 0)}; "
        f"взятия последнего короля: {summary['eventCounts'].get('LAST_OPPONENT_KING_CAPTURED', 0)}.",
        f"- Эпизоды пешки на предпоследней горизонтали: {summary['pawnEpisodes']}; "
        f"с ≥3 отказами от доступного превращения: "
        f"{summary['pawnEpisodesWithThreeOrMoreDeclinedPromotions']}; "
        f"максимум длительности: {summary['pawnEpisodeMaxDurationPlies']} полуходов.",
        "- Полные состояния и применённые действия: `game-NNN.jsonl.gz`; все события: "
        "`events.jsonl`; эпизоды пешек: `pawn-episodes.jsonl`; построчная таблица: `games.csv`; "
        "параметры запуска: `manifest.json`.",
        "", "## Ограничения", "",
        "Исследовательский прогон использует одну сборку с фиксированным лимитом узлов; "
        "результаты не характеризуют объективную глубину или силу движка. Начальные "
        "ходы, выбранные из близких MultiPV-кандидатов, помечены в журналах. События "
        "можно пересчитать из полных состояний и списков допустимых действий без нового поиска.",
        "",
    ]
    (output / "report.md").write_text("\n".join(lines), encoding="utf-8")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--engine", type=pathlib.Path, default=pathlib.Path("third_party/fairy-stockfish/src/stockfish.exe"))
    parser.add_argument("--output", type=pathlib.Path, required=True)
    parser.add_argument("--games", type=int, default=100)
    args = parser.parse_args()
    if not 1 <= args.games <= 1000:
        parser.error("--games must be between 1 and 1000")
    if not args.engine.is_file():
        parser.error("engine executable does not exist: " + str(args.engine))
    root = pathlib.Path(__file__).resolve().parents[1]
    output = args.output.resolve()
    output.mkdir(parents=True, exist_ok=False)
    engine_bytes = args.engine.read_bytes()
    manifest = {
        "createdUtc": datetime.now(timezone.utc).isoformat(),
        "gamesRequested": args.games, "enginePath": str(args.engine.resolve()),
        "engineSha256": hashlib.sha256(engine_bytes).hexdigest(),
        "engineRevision": engine_revision(root), "engineWorkingTreePatchSha256": engine_patch_hash(root),
        "variant": "lifechess", "pythonVersion": platform.python_version(),
        "build": {"compiler": "MSYS2 g++", "target": "x86_64 Windows", "optimization": "-O3",
                  "flags": "-Wall -Wcast-qual -fno-exceptions -std=c++17 -DNNUE_EMBEDDING_OFF -Wextra -Wshadow -DNDEBUG -O3 -fno-strict-aliasing -DIS_64BIT -msse -DUSE_SSE2 -msse2",
                  "threads": 1, "hashMb": 16, "nnue": False, "evaluation": "classical"},
        "search": {"limit": "fixed nodes", "nodesPerDecision": NODES,
                   "openingMultiPV": OPENING_MULTIPV, "openingRandomizationPlies": OPENING_PLIES,
                   "openingCpWindow": OPENING_CP_WINDOW, "randomSeedBase": BASE_SEED,
                   "maxGamePlies": MAX_PLIES, "timeoutSecondsPerSearch": MOVE_TIMEOUT_SECONDS},
        "drawPolicy": "phase threshold -500 cp at material phase 24 to -50 cp at phase 0; winning side declines incoming offers",
        "diversification": "seeded choice among final-depth MultiPV moves within the stated centipawn window; only first 8 plies",
        "selfPlayDrawState": "independent offer cooldown latch per side; pending draw offers remain shared game state",
    }
    (output / "manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    attempts = []
    engine = console.UciEngine(str(args.engine.resolve()))
    try:
        for game_id in range(1, args.games + 1):
            seed = BASE_SEED + game_id
            log_path = output / f"game-{game_id:03d}.jsonl.gz"
            print(f"[{game_id}/{args.games}] seed={seed} start", flush=True)
            try:
                result = game_loop(game_id, seed, engine, log_path)
            except console.ProtocolError as exc:
                result = recover_partial_attempt(game_id, seed, log_path, exc.code, str(exc))
                engine.close()
                engine = console.UciEngine(str(args.engine.resolve()))
            except Exception as exc:  # Technical failures are recorded, never converted to losses.
                result = recover_partial_attempt(game_id, seed, log_path, "UNEXPECTED_ERROR", repr(exc))
                engine.close()
                engine = console.UciEngine(str(args.engine.resolve()))
            result["logPath"] = str(log_path)
            attempts.append(result)
            with (output / "attempts.jsonl").open("a", encoding="utf-8", newline="\n") as stream:
                stream.write(json.dumps(result, ensure_ascii=False) + "\n")
            print(f"[{game_id}/{args.games}] {result['status']} {result.get('result', result.get('reason'))} plies={result.get('plies', 0)}", flush=True)
    finally:
        engine.close()

    report = summarize(attempts, output)
    (output / "summary.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    write_report(output, manifest, report)
    with (output / "games.csv").open("w", encoding="utf-8", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=["gameId", "seed", "status", "result", "reason", "plies", "randomizedPlies", "logPath"])
        writer.writeheader()
        for item in attempts:
            row = dict(item)
            row["randomizedPlies"] = json.dumps(row.get("randomizedPlies", []))
            writer.writerow({key: row.get(key) for key in writer.fieldnames})
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
