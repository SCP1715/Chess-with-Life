import copy
import tempfile
import unittest
from pathlib import Path

from tools import run_lifechess_selfplay as selfplay


def make_state(pieces, turn="WHITE", debt=None):
    board = ["."] * 64
    for square, piece in pieces.items():
        board[selfplay.board_index(square)] = piece
    return {
        "board": board, "toMove": turn, "enPassant": None, "debtTargetKings": debt,
        "halfMovesSinceCaptureOrPawn": 0, "castlingRights": "000000", "result": "NONE",
        "drawOfferBy": None, "drawOfferSentInCurrentNonWinningStretch": False,
        "repetitions": [], "history": [],
    }


def move(source, target, promotion=None):
    return {"type": "MOVE", "from": source, "to": target, "moveKind": "NORMAL", "promotion": promotion}


def next_state(state, action, pieces_after=None, turn=None, debt=None):
    result = copy.deepcopy(state)
    if pieces_after is not None:
        result["board"] = ["."] * 64
        for square, piece in pieces_after.items():
            result["board"][selfplay.board_index(square)] = piece
    result["toMove"] = turn or ("BLACK" if state["toMove"] == "WHITE" else "WHITE")
    if debt is not None:
        result["debtTargetKings"] = debt
    result["history"].append(copy.deepcopy(action))
    return result


class SelfPlayMetricsTest(unittest.TestCase):
    def test_opening_parser_preserves_mate_scores_without_cp_conversion(self):
        lines = "\n".join([
            "info depth 8 multipv 1 score mate 2 nodes 90 pv e2e4 e7e5",
            "info depth 8 multipv 2 score cp 35 nodes 90 pv d2d4 d7d5",
        ])
        candidates = selfplay.parse_pvs(lines)
        self.assertEqual([("mate", 2), ("cp", 35)],
                         [(item["scoreType"], item["score"]) for item in candidates])
        chosen, details = selfplay.choose_opening_move(lines, "e2e4", __import__("random").Random(3))
        self.assertEqual("e2e4", chosen)
        self.assertEqual(["e2e4"], details["candidateMoves"])

    def test_opening_choice_uses_only_top_depth_and_centipawn_window(self):
        lines = "\n".join([
            "info depth 7 multipv 1 score cp 100 nodes 80 pv e2e4 e7e5",
            "info depth 7 multipv 2 score cp 55 nodes 80 pv d2d4 d7d5",
            "info depth 7 multipv 3 score cp 49 nodes 80 pv c2c4 e7e5",
            "info depth 6 multipv 1 score cp 0 nodes 50 pv g1f3 d7d5",
        ])
        candidates = selfplay.parse_pvs(lines)
        self.assertEqual(3, len(candidates))
        _chosen, details = selfplay.choose_opening_move(lines, "e2e4", __import__("random").Random(4))
        self.assertEqual(["e2e4", "d2d4"], details["candidateMoves"])

    def test_draw_offer_cooldown_isolated_per_player(self):
        latches = {"WHITE": True, "BLACK": False}
        selfplay.update_offer_latch(latches, "BLACK", True, "DECLINE_DRAW")
        self.assertTrue(latches["WHITE"])
        self.assertFalse(latches["BLACK"])
        selfplay.update_offer_latch(latches, "BLACK", False, "OFFER_DRAW")
        self.assertTrue(latches["WHITE"])
        self.assertTrue(latches["BLACK"])

    def test_position_history_ignores_procedures_but_preserves_every_move(self):
        state = selfplay.initial_state()
        state["history"] = [
            {"type": "OFFER_DRAW"},
            move("e2", "e4"),
            {"type": "DECLINE_DRAW"},
            move("e7", "e5"),
        ]
        self.assertEqual("position startpos moves e2e4 e7e5", selfplay.move_history_position(state))

    def test_compressed_jsonl_round_trip(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "game.jsonl.gz"
            selfplay.write_json_line(path, {"state": {"debt": "WHITE", "moves": ["e2e4"]}})
            self.assertEqual([{"state": {"debt": "WHITE", "moves": ["e2e4"]}}],
                             selfplay.read_json_lines(path))

    def test_king_self_capture_records_safe_alternative(self):
        before = make_state({"e1": "wk0", "e2": "wr0", "e8": "bk0"})
        action = move("e1", "e2")
        after = next_state(before, action, {"e2": "wk1", "e8": "bk0"})
        legal = [action, move("e1", "d1")]
        events = selfplay.transition_events(before, action, after, legal)
        event = next(item for item in events if item["type"] == "KING_SELF_CAPTURE")
        self.assertEqual("ROOK", event["capturedType"])
        self.assertTrue(event["hasSafeNonSelfCapture"])

    def test_king_promotion_creates_debt_and_response_counts_targets(self):
        before = make_state({"h2": "wk0", "c1": "wb0", "b7": "wp0", "h6": "bk0",
                             "h7": "bq0", "d2": "br0"})
        promote = move("b7", "b8", "KING")
        after = next_state(before, promote, {"h2": "wk0", "b8": "wk0", "c1": "wb0",
                                            "h6": "bk0", "h7": "bq0", "d2": "br0"},
                           turn="BLACK", debt="WHITE")
        king_ids, counters = selfplay.king_id_map(before)
        ids_before = dict(king_ids)
        details = selfplay.advance_king_ids(before, promote, after, king_ids, counters)
        events = selfplay.transition_events(before, promote, after, [promote], details, ids_before)
        promotion_event = next(item for item in events if item["type"] == "KING_PROMOTION")
        self.assertTrue(promotion_event["debtCreated"])
        self.assertIsNotNone(promotion_event["newKingId"])

        response = move("d2", "h2")
        after_response = next_state(after, response, {"b8": "wk0", "c1": "wb0", "h6": "bk0",
                                                      "h7": "bq0", "h2": "br1"}, turn="WHITE")
        after_response["debtTargetKings"] = None
        response_ids_before = dict(king_ids)
        response_details = selfplay.advance_king_ids(after, response, after_response, king_ids, counters)
        debt_events = selfplay.transition_events(after, response, after_response, [response],
                                                 response_details, response_ids_before)
        debt_event = next(item for item in debt_events if item["type"] == "DEBT_RESPONSE")
        self.assertEqual("WHITE", debt_event["targetSide"])
        self.assertEqual(2, len(debt_event["targetKingIds"]))
        self.assertTrue(debt_event["capturedTargetKing"])

    def test_blocked_advanced_pawn_is_counted_as_unavailable(self):
        start = make_state({"e1": "wk0", "e8": "bk0", "b6": "wp0", "b8": "wr0"})
        advance = move("b6", "b7")
        after_advance = next_state(start, advance, {"e1": "wk0", "e8": "bk0", "b7": "wp1", "b8": "wr0"})
        black_move = move("e8", "e7")
        after_black = next_state(after_advance, black_move, {"e1": "wk0", "e7": "bk1", "b7": "wp1", "b8": "wr0"})
        white_move = move("e1", "e2")
        after_white = next_state(after_black, white_move, {"e2": "wk1", "e7": "bk1", "b7": "wp1", "b8": "wr0"})
        records = [
            {"record": "START", "initialState": start},
            {"record": "ACTION", "ply": 1, "before": start, "after": after_advance,
             "action": advance, "legalActions": [advance]},
            {"record": "ACTION", "ply": 1, "before": after_advance, "after": after_black,
             "action": black_move, "legalActions": [black_move]},
            {"record": "ACTION", "ply": 2, "before": after_black, "after": after_white,
             "action": white_move, "legalActions": [white_move]},
            {"record": "END", "status": "INTERRUPTED"},
        ]
        episodes = selfplay.analyze_pawn_episodes(records, 1)
        self.assertEqual(1, len(episodes))
        self.assertEqual("BLOCKED", episodes[0]["unavailable"][0]["reason"])
        self.assertEqual(0, episodes[0]["availablePromotionDecisions"])

    def test_deferred_available_promotion_counts_refusal(self):
        start = make_state({"e1": "wk0", "e8": "bk0", "b6": "wp0"})
        white_pawn = move("b6", "b7")
        s1 = next_state(start, white_pawn, {"e1": "wk0", "e8": "bk0", "b7": "wp1"})
        black1 = move("e8", "e7")
        s2 = next_state(s1, black1, {"e1": "wk0", "e7": "bk1", "b7": "wp1"})
        white_wait = move("e1", "e2")
        s3 = next_state(s2, white_wait, {"e2": "wk1", "e7": "bk1", "b7": "wp1"})
        black2 = move("e7", "e6")
        s4 = next_state(s3, black2, {"e2": "wk1", "e6": "bk1", "b7": "wp1"})
        promote = move("b7", "b8", "QUEEN")
        s5 = next_state(s4, promote, {"e2": "wk1", "e6": "bk1", "b8": "wq0"})
        promotions = [move("b7", "b8", kind) for kind in ("QUEEN", "ROOK", "BISHOP", "KNIGHT", "KING")]
        records = [
            {"record": "START", "initialState": start},
            {"record": "ACTION", "ply": 1, "before": start, "after": s1,
             "action": white_pawn, "legalActions": [white_pawn]},
            {"record": "ACTION", "ply": 1, "before": s1, "after": s2,
             "action": black1, "legalActions": [black1]},
            {"record": "ACTION", "ply": 2, "before": s2, "after": s3,
             "action": white_wait, "legalActions": [white_wait] + promotions},
            {"record": "ACTION", "ply": 2, "before": s3, "after": s4,
             "action": black2, "legalActions": [black2]},
            {"record": "ACTION", "ply": 3, "before": s4, "after": s5,
             "action": promote, "legalActions": promotions},
            {"record": "END", "status": "COMPLETED"},
        ]
        episodes = selfplay.analyze_pawn_episodes(records, 2)
        self.assertEqual(1, len(episodes))
        self.assertEqual(1, episodes[0]["declinedAvailablePromotions"])
        self.assertEqual(2, episodes[0]["availableKingPromotionDecisions"])
        self.assertEqual("PROMOTED_QUEEN", episodes[0]["finish"])

    def test_draw_procedures_do_not_count_as_pawn_decisions_or_refusals(self):
        start = make_state({"e1": "wk0", "e8": "bk0", "b7": "wp1"})
        promotions = [move("b7", "b8", kind)
                      for kind in ("QUEEN", "ROOK", "BISHOP", "KNIGHT")]
        offered = copy.deepcopy(start)
        offered["drawOfferBy"] = "WHITE"
        offered["history"].append({"type": "OFFER_DRAW"})
        opponent_offer = copy.deepcopy(start)
        opponent_offer["drawOfferBy"] = "BLACK"
        declined = copy.deepcopy(start)
        declined["history"].append({"type": "DECLINE_DRAW"})
        actual_move = move("e1", "e2")
        after_move = next_state(start, actual_move,
                                {"e2": "wk1", "e8": "bk0", "b7": "wp1"})
        records = [
            {"record": "START", "initialState": start},
            {"record": "ACTION", "ply": 1, "before": start, "after": offered,
             "action": {"type": "OFFER_DRAW"}, "legalActions": promotions},
            {"record": "ACTION", "ply": 1, "before": opponent_offer, "after": declined,
             "action": {"type": "DECLINE_DRAW"}, "legalActions": promotions},
            {"record": "ACTION", "ply": 1, "before": start, "after": after_move,
             "action": actual_move, "legalActions": [actual_move] + promotions},
            {"record": "END", "status": "INTERRUPTED"},
        ]
        episodes = selfplay.analyze_pawn_episodes(records, 3)
        self.assertEqual(1, len(episodes))
        self.assertEqual(1, episodes[0]["ownDecisions"])
        self.assertEqual(1, episodes[0]["availablePromotionDecisions"])
        self.assertEqual(1, episodes[0]["declinedAvailablePromotions"])


if __name__ == "__main__":
    unittest.main()
