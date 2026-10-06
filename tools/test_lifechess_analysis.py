import unittest

from tools import analyze_lifechess_experiment as analysis
from tools import run_lifechess_experiment as experiment
from tools import run_lifechess_selfplay as selfplay


class ExperimentAnalysisTest(unittest.TestCase):
    def test_error_count_defaults_to_zero_when_counter_has_no_errors(self):
        self.assertEqual(0, analysis.error_count({"attempts": 1}))
        self.assertEqual(2, analysis.error_count({"errors": 2}))

    def test_prefix_counts_game_moves_and_keeps_procedures_in_exact_prefix(self):
        actions = [
            {"action": {"type": "MOVE", "from": "e2", "to": "e4"}, "after": {"ply": 1}},
            {"action": {"type": "OFFER_DRAW"}, "after": {"ply": 1}},
            {"action": {"type": "MOVE", "from": "e7", "to": "e5"}, "after": {"ply": 2}},
        ]
        prefix, state = analysis.first_move_plies(actions, target=2)
        self.assertEqual(actions, prefix)
        self.assertEqual({"ply": 2}, state)

    def test_unavailable_prefix_is_not_mistaken_for_a_pair(self):
        actions = [{"action": {"type": "MOVE", "from": "e2", "to": "e4"}, "after": {}}]
        self.assertEqual((None, None), analysis.first_move_plies(actions, target=2))

    def test_last_king_capture_predicate_uses_occupied_target_square(self):
        state = selfplay.initial_state()
        legal = [
            {"type": "MOVE", "from": "e7", "to": "e2", "moveKind": "NORMAL"},
            {"type": "MOVE", "from": "d7", "to": "d5", "moveKind": "NORMAL"},
        ]
        state["board"][selfplay.board_index("e2")] = "wk0"
        captures = analysis.immediate_last_king_captures(state, legal, "WHITE")
        self.assertEqual([legal[0]], captures)

    def test_attempt_summary_drops_duplicate_snapshot_but_keeps_resume_references(self):
        source = {"gameId": 5, "profile": "C", "finalState": {"board": ["."] * 64},
                  "finalKingIds": {4: "WK1"}, "finalOfferLatches": {"WHITE": True},
                  "parentProfileGameId": "A:5", "logPath": "games/game-A-000005.jsonl.gz"}
        result = experiment.attempt_summary(source)
        self.assertEqual("A:5", result["parentProfileGameId"])
        self.assertIn("logPath", result)
        self.assertNotIn("finalState", result)
        self.assertNotIn("finalKingIds", result)


if __name__ == "__main__":
    unittest.main()
