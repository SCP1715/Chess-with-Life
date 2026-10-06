import unittest
from pathlib import Path

from tools.endgame.reference import (Action, Model, Position, Wdl, legal_actions,
                                     parse_square, scope_result)
from tools.endgame.table_io import (load_tables, optimal_actions, preserving_actions,
                                    prove_draw_under_optional_claims, query)


ROOT = Path(__file__).resolve().parents[1]
TABLE_DIR = ROOT / "playtest-results" / "endgame-analysis-20261006" / "tables"


def position(turn, *pieces):
    return Position(tuple((parse_square(square), color, kind)
                          for square, color, kind in pieces), turn)


class EndgameReferenceTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.tables = load_tables(TABLE_DIR)

    def test_t1_adjacent_kings_is_immediate_win(self):
        p = position("w", ("e4", "w", "K"), ("e5", "b", "K"))
        self.assertEqual(query(p, self.tables), (Wdl.WIN, 1))
        self.assertIn(Action(parse_square("e4"), parse_square("e5")),
                      optimal_actions(p, self.tables))

    def test_t2_exhaustively_tests_nonadjacent_king_only_hypothesis(self):
        checked = wins = draws = losses = 0
        for wk in range(64):
            for bk in range(64):
                if wk == bk:
                    continue
                if max(abs(wk // 8 - bk // 8), abs(wk % 8 - bk % 8)) <= 1:
                    continue
                for turn in ("w", "b"):
                    p = Position(((wk, "w", "K"), (bk, "b", "K")), turn)
                    wdl = query(p, self.tables)[0]
                    wins += wdl == Wdl.WIN
                    draws += wdl == Wdl.DRAW
                    losses += wdl == Wdl.LOSS
                    checked += 1
        # The exact table proves the expected hypothesis for the whole class.
        self.assertEqual((checked, wins, draws, losses), (7224, 0, 7224, 0))

    def test_t3_bishop_trap_is_loss_in_two_plies(self):
        p = position("b", ("e6", "w", "K"), ("e7", "w", "B"), ("e8", "b", "K"))
        self.assertEqual(query(p, self.tables), (Wdl.LOSS, 2))
        optimal = optimal_actions(p, self.tables)
        self.assertEqual(len(optimal), 5)
        self.assertEqual({a.source for a in optimal}, {parse_square("e8")})

    def test_t4_knight_trap_is_proven_loss(self):
        p = position("b", ("c7", "w", "K"), ("b5", "w", "N"), ("a8", "b", "K"))
        self.assertEqual(query(p, self.tables)[0], Wdl.LOSS)
        self.assertTrue(query(p, self.tables)[1] > 0)

    def test_t5_four_piece_witness_transfers_to_t3(self):
        p = position("w", ("e5", "w", "K"), ("e7", "w", "B"),
                     ("e8", "b", "K"), ("e6", "b", "B"))
        witness = Action(parse_square("e5"), parse_square("e6"))
        self.assertIn(witness, legal_actions(p))
        child = Position(((parse_square("e6"), "w", "K"),
                          (parse_square("e7"), "w", "B"),
                          (parse_square("e8"), "b", "K")), "b")
        self.assertEqual(query(child, self.tables), (Wdl.LOSS, 2))
        self.assertEqual(query(p, self.tables), (Wdl.WIN, 3))

    def test_t9_real_c1_is_draw_only_in_unbounded_model(self):
        p = position("w", ("b7", "w", "K"), ("f3", "w", "B"),
                     ("h2", "b", "K"), ("a1", "b", "B"))
        self.assertEqual(query(p, self.tables), (Wdl.DRAW, 0))
        self.assertEqual(scope_result(p, Model.FIFTY).wdl, Wdl.UNKNOWN)
        self.assertEqual(scope_result(p, Model.HISTORY).wdl, Wdl.UNKNOWN)
        for model in (Model.FIFTY, Model.HISTORY):
            proof = prove_draw_under_optional_claims(p, self.tables, model)
            self.assertTrue(proof.exact)
            self.assertEqual(proof.wdl, Wdl.DRAW)
        self.assertIsNone(prove_draw_under_optional_claims(
            p, self.tables, Model.HISTORY, pending_draw_offer=True))

    def test_krk_and_kqk_are_covered_and_use_their_own_move_geometry(self):
        rook = position("w", ("a1", "w", "K"), ("d4", "w", "R"),
                        ("h8", "b", "K"))
        queen = position("w", ("a1", "w", "K"), ("d4", "w", "Q"),
                         ("h8", "b", "K"))
        self.assertEqual(query(rook, self.tables), (Wdl.WIN, 23))
        self.assertEqual(query(queen, self.tables), (Wdl.WIN, 1))
        self.assertTrue(optimal_actions(queen, self.tables))

    def test_same_side_two_bishops_are_not_misclassified_as_kbkb(self):
        unsupported = position("w", ("a1", "w", "K"), ("c1", "w", "B"),
                               ("f4", "w", "B"), ("h8", "b", "K"))
        self.assertIsNone(query(unsupported, self.tables))

    def test_t6_king_self_capture_transitions_to_kk_and_keeps_draw_action(self):
        p = position("w", ("e1", "w", "K"), ("e2", "w", "B"), ("h8", "b", "K"))
        capture = Action(parse_square("e1"), parse_square("e2"))
        self.assertIn(capture, preserving_actions(p, self.tables))
        self.assertIn(capture, optimal_actions(p, self.tables))

    def test_t7_clock_99_and_100_do_not_reuse_unbounded_result(self):
        base = ((parse_square("a1"), "w", "K"), (parse_square("h8"), "b", "K"))
        below = Position(base, "w", halfmove_clock=99)
        claimable = Position(base, "w", halfmove_clock=100)
        for p in (below, claimable):
            result = scope_result(p, Model.FIFTY)
            self.assertEqual(result.wdl, Wdl.UNKNOWN)
            self.assertFalse(result.exact)
        self.assertEqual(query(below, self.tables), query(claimable, self.tables))

    def test_t8_history_variants_remain_unknown_even_with_repeat_records(self):
        pieces = ((parse_square("a1"), "w", "K"), (parse_square("h8"), "b", "K"))
        fresh = Position(pieces, "w", repetitions=())
        repeated = Position(pieces, "w", repetitions=(("same-position", 3),))
        for p in (fresh, repeated):
            result = scope_result(p, Model.HISTORY)
            self.assertEqual(result.wdl, Wdl.UNKNOWN)
            self.assertFalse(result.exact)

    def test_unbounded_tables_are_invariant_under_board_symmetry(self):
        samples = (
            position("b", ("e6", "w", "K"), ("e7", "w", "B"),
                     ("e8", "b", "K")),
            position("w", ("c7", "w", "K"), ("b5", "w", "N"),
                     ("a8", "b", "K")),
            position("b", ("a1", "w", "K"), ("d4", "b", "B"),
                     ("h8", "b", "K")),
            position("w", ("b7", "w", "K"), ("f3", "w", "B"),
                     ("h2", "b", "K"), ("a1", "b", "B")),
        )

        def transform(square, symmetry):
            row, col = divmod(square, 8)
            if symmetry >= 4:
                col = 7 - col
            for _ in range(symmetry % 4):
                row, col = col, 7 - row
            return row * 8 + col

        for sample in samples:
            expected = query(sample, self.tables)
            for symmetry in range(8):
                transformed = Position(
                    tuple((transform(square, symmetry), color, kind)
                          for square, color, kind in sample.pieces),
                    sample.turn)
                with self.subTest(position=sample, symmetry=symmetry):
                    self.assertEqual(query(transformed, self.tables), expected)

    def test_draw_models_are_not_substituted_for_unbounded(self):
        p = position("w", ("a1", "w", "K"), ("h8", "b", "K"))
        for model in (Model.FIFTY, Model.HISTORY):
            result = scope_result(p, model)
            self.assertEqual(result.wdl, Wdl.UNKNOWN)
            self.assertFalse(result.exact)

    def test_unsupported_rights_are_out_of_scope(self):
        p = Position(((0, "w", "K"), (63, "b", "K")), "w",
                     castling_rights="100000")
        result = scope_result(p, Model.UNBOUNDED)
        self.assertEqual(result.wdl, Wdl.OUT_OF_SCOPE)
        self.assertFalse(result.exact)


if __name__ == "__main__":
    unittest.main()
