import os
import json
import pathlib
import subprocess
import sys
import unittest

from tools import lifechess_console as console


ROOT = pathlib.Path(__file__).resolve().parents[1]
ENGINE = os.environ.get(
    "LIFECHESS_ENGINE",
    str(ROOT / "third_party" / "fairy-stockfish" / "src" / "stockfish.exe"),
)


def square(row, col):
    return row * 8 + col


def board(entries):
    result = ["."] * 64
    for coordinate, cell in entries.items():
        row = 8 - int(coordinate[1])
        col = ord(coordinate[0]) - ord("a")
        result[square(row, col)] = cell
    return result


def state(position, turn="WHITE", debt=None, ep=None, half=0, rights="000000"):
    result = {
        "board": position,
        "toMove": turn,
        "enPassant": ep,
        "debtTargetKings": debt,
        "halfMovesSinceCaptureOrPawn": half,
        "castlingRights": rights,
        "result": "NONE",
        "drawOfferBy": None,
        "drawOfferSentInCurrentNonWinningStretch": False,
        "repetitions": [],
        "history": [],
    }
    result["searchMoves"] = []
    result["searchRootFen"] = console.state_fen(result)
    return result


def request(operation, position, action=None, request_id="test-1"):
    return {
        "protocol": "lifechess", "version": 2, "requestId": request_id,
        "stateRevision": 7, "variant": "lifechess", "operation": operation,
        "state": position, "action": action,
    }


class ConsoleAdapterTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        if not pathlib.Path(ENGINE).is_file():
            raise unittest.SkipTest("Fairy-Stockfish executable is not built: " + ENGINE)
        cls.engine = console.UciEngine(ENGINE)

    @classmethod
    def tearDownClass(cls):
        if hasattr(cls, "engine"):
            cls.engine.close()

    def test_real_engine_returns_25_initial_moves(self):
        pieces = {}
        white_back = "RNBQKBNR"
        black_back = "rnbqkbnr"
        for col, (white, black) in enumerate(zip(white_back, black_back)):
            file_name = chr(ord("a") + col)
            pieces[file_name + "1"] = "w" + white.lower() + "0"
            pieces[file_name + "2"] = "wp0"
            pieces[file_name + "7"] = "bp0"
            pieces[file_name + "8"] = "b" + black + "0"
        initial = state(board(pieces), rights="111111")
        reply = console.handle(request("LEGAL_ACTIONS", initial), self.engine, 5)
        move_actions = [action for action in reply["legalActions"] if action["type"] == "MOVE"]
        self.assertEqual("LEGAL_ACTIONS", reply["kind"])
        self.assertEqual(25, len(move_actions))
        self.assertIn({"type": "MOVE", "from": "e1", "to": "d1", "moveKind": "NORMAL", "promotion": None}, move_actions)

    def test_bare_non_adjacent_kings_are_a_typed_claim_not_automatic(self):
        position = state(board({"a1": "wk0", "h8": "bk0"}))
        actions = console.procedural_actions(position)
        self.assertIn({"type": "CLAIM_BARE_KINGS"}, actions)
        self.assertEqual("NONE", position["result"])
        self.assertEqual([], position["searchMoves"])
        reply = console.handle(request("SEARCH", position), self.engine, 1)
        self.assertEqual("DRAW_ACTION", reply["kind"])
        self.assertEqual({"type": "CLAIM_BARE_KINGS"}, reply["selectedAction"])
        self.assertEqual("DRAW_BARE_KINGS", reply["state"]["result"])
        self.assertEqual([], reply["state"]["searchMoves"])

    def test_bare_kings_claim_requires_exactly_two_non_adjacent_kings(self):
        for piece in ("wb0", "wn0", "wp0", "bk0"):
            position = state(board({"a1": "wk0", "h8": "bk0", "d4": piece}))
            self.assertNotIn({"type": "CLAIM_BARE_KINGS"}, console.procedural_actions(position))
        for square in ("b1", "a2", "b2"):
            position = state(board({"a1": "wk0", square: "bk0"}))
            self.assertNotIn({"type": "CLAIM_BARE_KINGS"}, console.procedural_actions(position))

    def test_engine_enforces_promotion_debt_and_applies_full_line(self):
        current = state(board({
            "h2": "wk0", "c1": "wb0", "b7": "wp0",
            "h6": "bk0", "h7": "bq0", "d2": "br0",
        }))

        promotion_actions = console.handle(request("LEGAL_ACTIONS", current), self.engine, 5)["legalActions"]
        promotion_choices = {
            action["promotion"] for action in promotion_actions
            if action["type"] == "MOVE" and action["from"] == "b7" and action["to"] == "b8"
        }
        self.assertEqual({"KING", "QUEEN", "ROOK", "BISHOP", "KNIGHT"}, promotion_choices)

        promote = {"type": "MOVE", "from": "b7", "to": "b8", "moveKind": "NORMAL", "promotion": "KING"}
        after_promotion = console.handle(request("APPLY_ACTION", current, promote), self.engine, 5)["state"]
        self.assertEqual("WHITE", after_promotion["debtTargetKings"])
        self.assertEqual("BLACK", after_promotion["toMove"])
        self.assertEqual("wk0", after_promotion["board"][square(0, 1)])

        moves = self.engine.legal_moves(console.state_fen(after_promotion))
        self.assertEqual(["d2h2"], moves)
        capture = {"type": "MOVE", "from": "d2", "to": "h2", "moveKind": "NORMAL", "promotion": None}
        after_debt = console.handle(request("APPLY_ACTION", after_promotion, capture), self.engine, 5)["state"]
        self.assertIsNone(after_debt["debtTargetKings"])
        self.assertEqual("wk0", after_debt["board"][square(0, 1)])

        finish = {"type": "MOVE", "from": "c1", "to": "h6", "moveKind": "NORMAL", "promotion": None}
        won = console.handle(request("APPLY_ACTION", after_debt, finish), self.engine, 5)["state"]
        self.assertEqual("WHITE_WIN", won["result"])
        self.assertEqual(3, len(won["history"]))

    def test_search_uses_real_engine_and_returns_legal_action(self):
        position = state(board({
            "h2": "wk0", "c1": "wb0", "b7": "wp0",
            "h6": "bk0", "h7": "bq0", "d2": "br0",
        }))
        reply = console.handle(request("SEARCH", position, request_id="search-4"), self.engine, 5)
        self.assertEqual("SEARCH_RESULT", reply["kind"])
        available = [console.uci_action(move, position) for move in self.engine.legal_moves(console.state_fen(position))]
        self.assertIn(reply["selectedAction"], available)
        self.assertTrue(reply["message"].startswith("info depth "))

    def test_search_offers_draw_only_once_while_non_winning(self):
        position = state(board({"e1": "wk0", "d1": "wq0", "e8": "bk0"}), turn="BLACK")
        first = console.handle(request("SEARCH", position), self.engine, 4)
        self.assertEqual("DRAW_ACTION", first["kind"], first)
        self.assertEqual("OFFER_DRAW", first["selectedAction"]["type"])
        self.assertEqual("BLACK", first["state"]["drawOfferBy"])
        self.assertTrue(first["state"]["drawOfferSentInCurrentNonWinningStretch"])

        repeated = console.handle(request("SEARCH", first["state"], request_id="test-repeat"), self.engine, 4)
        self.assertEqual("SEARCH_RESULT", repeated["kind"])
        self.assertEqual("BLACK", repeated["state"]["toMove"])

    def test_search_does_not_offer_a_draw_in_a_balanced_opening(self):
        position = state(board({
            "a8": "br0", "b8": "bn0", "c8": "bb0", "d8": "bq0",
            "e8": "bk0", "f8": "bb0", "g8": "bn0", "h8": "br0",
            **{f"{file}7": "bp0" for file in "abcdefgh"},
            **{f"{file}2": "wp0" for file in "abcdefgh"},
            "a1": "wr0", "b1": "wn0", "c1": "wb0", "d1": "wq0",
            "e1": "wk0", "f1": "wb0", "g1": "wn0", "h1": "wr0",
        }))
        reply = console.handle(request("SEARCH", position), self.engine, 1)
        self.assertEqual("SEARCH_RESULT", reply["kind"])
        self.assertEqual("NONE", reply["state"]["result"])

        deeper_reply = console.handle(request("SEARCH", position, request_id="balanced-opening-depth4"),
                                      self.engine, 4)
        self.assertEqual("SEARCH_RESULT", deeper_reply["kind"], deeper_reply)
        self.assertEqual("MOVE", deeper_reply["selectedAction"]["type"])

    def test_first_neutral_endgame_iteration_does_not_trigger_draw_offer(self):
        position = state(board({"a1": "wk0", "a2": "wp0", "h8": "bk0", "h7": "bp0"}),
                         turn="BLACK")
        reply = console.handle(request("SEARCH", position, request_id="neutral-endgame"),
                               self.engine, 1)
        self.assertNotEqual("OFFER_DRAW", reply.get("selectedAction", {}).get("type"))
        self.assertNotEqual("DRAW_ACTION", reply["kind"])

    def test_search_claims_available_draw_instead_of_offering(self):
        position = state(board({"a8": "bk0", "a7": "wq0", "b6": "wk0"}),
                         turn="BLACK", half=100)
        reply = console.handle(request("SEARCH", position), self.engine, 1)
        self.assertEqual("DRAW_ACTION", reply["kind"])
        self.assertEqual("CLAIM_FIFTY_MOVES", reply["selectedAction"]["type"])
        self.assertEqual("DRAW_FIFTY_MOVES", reply["state"]["result"])

    def test_c1_zero_evaluation_selects_the_exact_fifty_move_claim(self):
        position = state(board({
            "b7": "wk0", "f3": "wb0", "h2": "bk0", "a1": "bb0",
        }), half=100)
        reply = console.handle(request("SEARCH", position, request_id="c1-claim50"),
                               self.engine, 3)
        self.assertEqual("DRAW_ACTION", reply["kind"], reply)
        self.assertEqual({"type": "CLAIM_FIFTY_MOVES"}, reply["selectedAction"])
        self.assertEqual("DRAW_FIFTY_MOVES", reply["state"]["result"])

    def test_winning_engine_declines_offer_and_claimable_draw(self):
        position = state(board({"e1": "wk0", "e2": "wq0", "e8": "bk0"}),
                         turn="WHITE", half=100)
        position["drawOfferBy"] = "BLACK"
        reply = console.handle(request("SEARCH", position), self.engine, 4)
        self.assertEqual("DRAW_ACTION", reply["kind"])
        self.assertEqual("DECLINE_DRAW", reply["selectedAction"]["type"])
        self.assertIsNone(reply["state"]["drawOfferBy"])
        self.assertEqual("NONE", reply["state"]["result"])

    def test_claim_has_priority_over_pending_offer(self):
        position = state(board({"a8": "bk0", "a7": "wq0", "b6": "wk0"}),
                         turn="BLACK", half=100)
        position["drawOfferBy"] = "WHITE"
        reply = console.handle(request("SEARCH", position), self.engine, 1)
        self.assertEqual("DRAW_ACTION", reply["kind"])
        self.assertEqual("CLAIM_FIFTY_MOVES", reply["selectedAction"]["type"])
        self.assertEqual("DRAW_FIFTY_MOVES", reply["state"]["result"])

    def test_draw_offer_cooldown_does_not_block_legal_draw_claim(self):
        position = state(board({"a8": "bk0", "a7": "wq0", "b6": "wk0"}),
                         turn="BLACK", half=100)
        position["drawOfferSentInCurrentNonWinningStretch"] = True
        reply = console.handle(request("SEARCH", position), self.engine, 1)
        self.assertEqual("DRAW_ACTION", reply["kind"])
        self.assertEqual("CLAIM_FIFTY_MOVES", reply["selectedAction"]["type"])
        self.assertEqual("DRAW_FIFTY_MOVES", reply["state"]["result"])

    def test_draw_offer_cooldown_does_not_block_repetition_claim(self):
        position = state(board({"a8": "bk0", "a7": "wq0", "b6": "wk0"}),
                         turn="BLACK")
        position["repetitions"] = [{"key": console.position_key(position), "count": 3}]
        position["drawOfferSentInCurrentNonWinningStretch"] = True
        reply = console.handle(request("SEARCH", position), self.engine, 1)
        self.assertEqual("DRAW_ACTION", reply["kind"])
        self.assertEqual("CLAIM_REPETITION", reply["selectedAction"]["type"])
        self.assertEqual("DRAW_REPETITION", reply["state"]["result"])

    def test_positive_evaluation_resets_draw_offer_cooldown(self):
        position = state(board({"e1": "wk0", "e2": "wq0", "e8": "bk0"}))
        position["drawOfferSentInCurrentNonWinningStretch"] = True
        reply = console.handle(request("SEARCH", position), self.engine, 2)
        self.assertEqual("SEARCH_RESULT", reply["kind"])
        self.assertFalse(reply["state"]["drawOfferSentInCurrentNonWinningStretch"])

    def test_wrong_variant_fails_explicitly(self):
        bad = request("LEGAL_ACTIONS", state(board({"e1": "wk0", "e8": "bk0"})))
        bad["variant"] = "chess"
        with self.assertRaises(console.ProtocolError) as error:
            console.validate_request(bad)
        self.assertEqual("UNSUPPORTED_VARIANT", error.exception.code)

    def test_protocol_rejects_ambiguous_or_extra_move_fields(self):
        bad = request("APPLY_ACTION", state(board({"e1": "wk0", "e8": "bk0"})),
                      {"type": "MOVE", "from": "e1", "to": "e2", "moveKind": "NORMAL",
                       "promotion": "QUEEN", "legacyDefault": True})
        with self.assertRaises(console.ProtocolError) as error:
            console.validate_request(bad)
        self.assertEqual("INVALID_ACTION", error.exception.code)

        bad = request("LEGAL_ACTIONS", state(board({"e1": "wk0", "e8": "bk0"})))
        bad["stateRevision"] = True
        with self.assertRaises(console.ProtocolError) as error:
            console.validate_request(bad)
        self.assertEqual("INVALID_REVISION", error.exception.code)

        bad = request("LEGAL_ACTIONS", state(board({"e1": "wk0", "e8": "bk0"})))
        bad["version"] = True
        with self.assertRaises(console.ProtocolError) as error:
            console.validate_request(bad)
        self.assertEqual("UNSUPPORTED_PROTOCOL", error.exception.code)

    def test_side_to_move_without_a_king_loses_instead_of_becoming_a_draw(self):
        position = state(board({"e1": "wk0", "a8": "br0"}), turn="BLACK")
        reply = console.handle(request("LEGAL_ACTIONS", position), self.engine, 1)
        self.assertEqual("GAME_OVER", reply["kind"])
        self.assertEqual("WHITE_WIN", reply["state"]["result"])
        self.assertEqual([], reply["legalActions"])

    def test_opponent_move_implicitly_declines_pending_draw_offer(self):
        fixture = pathlib.Path(__file__).with_name("fixtures") / "lifechess-initial.jsonl"
        position = json.loads(fixture.read_text(encoding="utf-8"))["state"]
        position["drawOfferBy"] = "BLACK"
        action = {"type": "MOVE", "from": "e2", "to": "e4", "moveKind": "NORMAL", "promotion": None}
        reply = console.handle(request("APPLY_ACTION", position, action), self.engine, 1)
        self.assertEqual("STATE_UPDATED", reply["kind"])
        self.assertIsNone(reply["state"]["drawOfferBy"])

    def test_repetition_key_keeps_en_passant_for_previously_moved_pawn(self):
        position = state(board({"e1": "wk0", "e8": "bk0", "e5": "wp1", "d5": "bp1"}),
                         turn="WHITE", ep="d6")
        without_ep = dict(position)
        without_ep["enPassant"] = None
        self.assertNotEqual(console.position_key(position), console.position_key(without_ep))

    def test_json_lines_console_round_trip_uses_full_snapshot(self):
        fixture = pathlib.Path(__file__).with_name("fixtures") / "lifechess-initial.jsonl"
        request_line = fixture.read_text(encoding="utf-8")
        parsed_request = json.loads(request_line)
        self.assertEqual(64, len(parsed_request["state"]["board"]))
        completed = subprocess.run(
            [sys.executable, str(ROOT / "tools" / "lifechess_console.py"),
             "--engine", ENGINE, "--depth", "2"],
            input=request_line, text=True, encoding="utf-8", capture_output=True,
            timeout=10, check=True,
        )
        reply = json.loads(completed.stdout)
        self.assertEqual("example-1", reply["requestId"])
        self.assertEqual("LIFECHESS", reply["variant"].upper())
        self.assertEqual("LEGAL_ACTIONS", reply["kind"])
        self.assertEqual(parsed_request["state"], reply["state"])
        self.assertEqual(25, sum(action["type"] == "MOVE" for action in reply["legalActions"]))


if __name__ == "__main__":
    unittest.main()
