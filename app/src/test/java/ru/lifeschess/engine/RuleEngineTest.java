package ru.lifeschess.engine;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.List;

public class RuleEngineTest {
    private Board board(Object... entries) {
        Board b = new Board();
        for (int i = 0; i < entries.length; i += 3)
            b = b.with(square((String) entries[i]), new Piece((Side) entries[i + 1], (PieceType) entries[i + 2], false));
        return b;
    }
    private int square(String algebraic) { return Board.index(8 - (algebraic.charAt(1) - '0'), algebraic.charAt(0) - 'a'); }
    private GameState state(Board b, Side turn) { return state(b, turn, -1, null, 0); }
    private GameState state(Board b, Side turn, int ep, Side debt, int half) {
        return new GameState(b, turn, ep, debt, half, false, false, false, false, false, false,
                GameResult.NONE, null, new HashMap<String, Integer>());
    }
    private boolean has(GameState s, String from, String to) {
        for (Move m : RuleEngine.legalMoves(s, square(from))) if (m.to == square(to)) return true;
        return false;
    }

    @Test public void initialPositionIncludesKingsCaptureOwnPieces() { assertEquals(25, MoveGenerator.allLegalMoves(GameState.initial()).size()); }
    @Test public void rookCannotJumpOverBlocker() {
        GameState s = state(board("a1", Side.WHITE, PieceType.ROOK, "a3", Side.WHITE, PieceType.PAWN, "h8", Side.BLACK, PieceType.KING), Side.WHITE);
        assertFalse(has(s, "a1", "a4"));
    }
    @Test public void knightMovesByLShape() {
        GameState s = state(board("b1", Side.WHITE, PieceType.KNIGHT, "h8", Side.BLACK, PieceType.KING), Side.WHITE);
        assertTrue(has(s, "b1", "c3")); assertFalse(has(s, "b1", "b3"));
    }
    @Test public void pawnMovesOneAndTwoFromHomeWhenUnmoved() {
        GameState s = state(board("e2", Side.WHITE, PieceType.PAWN, "h8", Side.BLACK, PieceType.KING), Side.WHITE);
        assertTrue(has(s, "e2", "e3")); assertTrue(has(s, "e2", "e4"));
    }
    @Test public void pawnCannotDoubleAfterItHasMoved() {
        Board b = new Board().with(square("e2"), new Piece(Side.WHITE, PieceType.PAWN, true));
        GameState s = state(b, Side.WHITE); assertFalse(has(s, "e2", "e4"));
    }
    @Test public void pawnCapturesDiagonallyOnly() {
        GameState s = state(board("e4", Side.WHITE, PieceType.PAWN, "d5", Side.BLACK, PieceType.ROOK,
                "f5", Side.WHITE, PieceType.BISHOP), Side.WHITE);
        assertTrue(has(s, "e4", "d5")); assertFalse(has(s, "e4", "f5"));
    }
    @Test public void kingMayMoveIntoGeometricAttack() {
        GameState s = state(board("e1", Side.WHITE, PieceType.KING, "e8", Side.BLACK, PieceType.ROOK), Side.WHITE);
        assertTrue(has(s, "e1", "e2"));
    }
    @Test public void aDifferentPieceMayMoveWithoutSavingAttackedKing() {
        GameState s = state(board("a1", Side.WHITE, PieceType.KING, "h8", Side.BLACK, PieceType.ROOK,
                "b1", Side.WHITE, PieceType.KNIGHT), Side.WHITE);
        assertTrue(has(s, "b1", "c3"));
    }
    @Test public void nonKingCannotCaptureOwnPiece() {
        GameState s = state(board("a1", Side.WHITE, PieceType.ROOK, "a2", Side.WHITE, PieceType.BISHOP), Side.WHITE);
        assertFalse(has(s, "a1", "a2"));
    }
    @Test public void movingKingCanCaptureOwnPieceAndStaysOnBoard() {
        GameState s = state(board("e1", Side.WHITE, PieceType.KING, "e2", Side.WHITE, PieceType.BISHOP,
                "h8", Side.BLACK, PieceType.KING), Side.WHITE);
        GameState after = RuleEngine.play(s, new Move(square("e1"), square("e2")), null);
        assertEquals(PieceType.KING, after.board.at(square("e2")).type);
        assertEquals(1, after.board.countKings(Side.WHITE));
    }
    @Test public void kingMayCaptureOwnOtherKing() {
        GameState s = state(board("e1", Side.WHITE, PieceType.KING, "e2", Side.WHITE, PieceType.KING,
                "h8", Side.BLACK, PieceType.KING), Side.WHITE);
        GameState after = RuleEngine.play(s, new Move(square("e1"), square("e2")), null);
        assertEquals(1, after.board.countKings(Side.WHITE));
        assertEquals(GameResult.NONE, after.result);
    }
    @Test public void takingLastOpponentKingWinsImmediately() {
        GameState s = state(board("e1", Side.WHITE, PieceType.ROOK, "e8", Side.BLACK, PieceType.KING,
                "a8", Side.WHITE, PieceType.KING), Side.WHITE);
        GameState after = RuleEngine.play(s, new Move(square("e1"), square("e8")), null);
        assertEquals(GameResult.WHITE_WIN, after.result);
    }
    @Test public void sideStartingTurnWithoutKingLoses() {
        GameState s = state(board("a1", Side.WHITE, PieceType.KING), Side.BLACK);
        assertEquals(GameResult.WHITE_WIN, RuleEngine.startTurn(s).result);
    }
    @Test public void kingPromotionUnavailableWithoutKingUnderAttack() {
        Board b = board("a7", Side.WHITE, PieceType.PAWN, "h1", Side.WHITE, PieceType.KING,
                "h8", Side.BLACK, PieceType.KING);
        assertFalse(PromotionLogic.choices(b.with(square("a7"), null).with(square("a8"), new Piece(Side.WHITE, PieceType.PAWN, true)), Side.WHITE).contains(PieceType.KING));
    }
    @Test public void kingPromotionAvailableWhenOneFriendlyKingIsAttacked() {
        Board b = board("a7", Side.WHITE, PieceType.PAWN, "h1", Side.WHITE, PieceType.KING,
                "h8", Side.BLACK, PieceType.ROOK);
        Board moved = b.with(square("a7"), null).with(square("a8"), new Piece(Side.WHITE, PieceType.PAWN, true));
        assertTrue(PromotionLogic.choices(moved, Side.WHITE).contains(PieceType.KING));
    }
    @Test public void kingPromotionIsUnavailableWhenPawnMoveBlocksOnlyAttack() {
        Board b = board("e7", Side.WHITE, PieceType.PAWN, "h8", Side.WHITE, PieceType.KING,
                "a8", Side.BLACK, PieceType.ROOK, "h1", Side.BLACK, PieceType.KING);
        Board moved = b.with(square("e7"), null).with(square("e8"), new Piece(Side.WHITE, PieceType.PAWN, true));
        assertFalse(PromotionLogic.choices(moved, Side.WHITE).contains(PieceType.KING));
    }
    @Test public void eitherEligiblePawnCanBeSelectedForASeparatePromotion() {
        Board b = board("a7", Side.WHITE, PieceType.PAWN, "b7", Side.WHITE, PieceType.PAWN,
                "h1", Side.WHITE, PieceType.KING, "h8", Side.BLACK, PieceType.ROOK,
                "a8", Side.BLACK, PieceType.KING);
        Board first = b.with(square("a7"), null).with(square("a8"), new Piece(Side.WHITE, PieceType.PAWN, true));
        Board second = b.with(square("b7"), null).with(square("b8"), new Piece(Side.WHITE, PieceType.PAWN, true));
        assertTrue(PromotionLogic.choices(first, Side.WHITE).contains(PieceType.KING));
        assertTrue(PromotionLogic.choices(second, Side.WHITE).contains(PieceType.KING));
    }
    @Test public void ordinaryPromotionOptionsAlwaysAvailable() {
        Board b = board("h1", Side.WHITE, PieceType.KING);
        List<PieceType> options = PromotionLogic.choices(b, Side.WHITE);
        assertTrue(options.contains(PieceType.QUEEN)); assertTrue(options.contains(PieceType.ROOK));
        assertTrue(options.contains(PieceType.BISHOP)); assertTrue(options.contains(PieceType.KNIGHT));
    }
    @Test public void kingPromotionCreatesDebtAgainstPromotingSide() {
        Board b = board("a7", Side.WHITE, PieceType.PAWN, "h1", Side.WHITE, PieceType.KING,
                "h8", Side.BLACK, PieceType.ROOK);
        GameState s = state(b, Side.WHITE);
        GameState after = RuleEngine.play(s, new Move(square("a7"), square("a8")), PieceType.KING);
        assertEquals(Side.WHITE, after.debtTargetKings); assertEquals(Side.BLACK, after.toMove);
    }
    @Test public void debtAllowsOnlyCapturesOfRequiredSideKings() {
        GameState s = state(board("a1", Side.WHITE, PieceType.ROOK, "a8", Side.BLACK, PieceType.KING,
                "h8", Side.BLACK, PieceType.KING), Side.WHITE, -1, Side.BLACK, 0);
        List<Move> moves = RuleEngine.legalMoves(s, square("a1"));
        assertEquals(1, moves.size()); assertEquals(square("a8"), moves.get(0).to);
    }
    @Test public void debtCanBePaidByCapturingAnyOfSeveralKings() {
        GameState s = state(board("a1", Side.WHITE, PieceType.ROOK, "a8", Side.BLACK, PieceType.KING,
                "h8", Side.BLACK, PieceType.KING, "b1", Side.WHITE, PieceType.KING), Side.WHITE, -1, Side.BLACK, 0);
        assertTrue(has(s, "a1", "a8"));
        GameState after = RuleEngine.play(s, new Move(square("a1"), square("a8")), null);
        assertNull(after.debtTargetKings); assertEquals(GameResult.NONE, after.result);
    }
    @Test public void debtDoesNotApplyOwnKingSafetyFilter() {
        GameState s = state(board("a1", Side.WHITE, PieceType.ROOK, "a8", Side.BLACK, PieceType.KING,
                "h8", Side.BLACK, PieceType.ROOK, "b1", Side.WHITE, PieceType.KING), Side.WHITE, -1, Side.BLACK, 0);
        assertTrue(has(s, "a1", "a8"));
    }
    @Test public void counterDebtIsCreatedAfterDebtCaptureAndKingPromotion() {
        Board b = board("a7", Side.WHITE, PieceType.PAWN, "b8", Side.BLACK, PieceType.KING,
                "g8", Side.BLACK, PieceType.KING, "h8", Side.BLACK, PieceType.ROOK,
                "h1", Side.WHITE, PieceType.KING);
        GameState s = state(b, Side.WHITE, -1, Side.BLACK, 0);
        GameState after = RuleEngine.play(s, new Move(square("a7"), square("b8")), PieceType.KING);
        assertEquals(Side.WHITE, after.debtTargetKings); assertEquals(Side.BLACK, after.toMove);
    }
    @Test public void takingLastKingDuringDebtEndsBeforeCounterDebt() {
        Board b = board("a7", Side.WHITE, PieceType.PAWN, "b8", Side.BLACK, PieceType.KING,
                "h1", Side.WHITE, PieceType.KING);
        GameState s = state(b, Side.WHITE, -1, Side.BLACK, 0);
        GameState after = RuleEngine.play(s, new Move(square("a7"), square("b8")), PieceType.QUEEN);
        assertEquals(GameResult.WHITE_WIN, after.result); assertNull(after.debtTargetKings);
    }
    @Test public void enPassantIsAvailableOnlyWhenTargetAndVictimMatch() {
        Board b = board("e5", Side.WHITE, PieceType.PAWN, "d5", Side.BLACK, PieceType.PAWN,
                "h8", Side.BLACK, PieceType.KING);
        GameState s = state(b, Side.WHITE, square("d6"), null, 0);
        assertTrue(has(s, "e5", "d6"));
        GameState after = RuleEngine.play(s, new Move(square("e5"), square("d6"), Move.Kind.EN_PASSANT), null);
        assertNull(after.board.at(square("d5")));
    }
    @Test public void enPassantCannotPayKingDebt() {
        GameState s = state(board("e5", Side.WHITE, PieceType.PAWN, "d5", Side.BLACK, PieceType.PAWN,
                "h8", Side.BLACK, PieceType.KING), Side.WHITE, square("d6"), Side.BLACK, 0);
        assertFalse(has(s, "e5", "d6"));
    }
    @Test public void horizontalKingSideCastleNeedsRightsAndEmptyPathOnly() {
        Board b = board("e1", Side.WHITE, PieceType.KING, "h1", Side.WHITE, PieceType.ROOK,
                "e8", Side.BLACK, PieceType.ROOK, "a8", Side.BLACK, PieceType.KING);
        GameState s = new GameState(b, Side.WHITE, -1, null, 0, true, false, false, false, false, false,
                GameResult.NONE, null, null);
        assertTrue(has(s, "e1", "g1"));
    }
    @Test public void queenSideCastleMovesRookToo() {
        Board b = board("e1", Side.WHITE, PieceType.KING, "a1", Side.WHITE, PieceType.ROOK,
                "h8", Side.BLACK, PieceType.KING);
        GameState s = new GameState(b, Side.WHITE, -1, null, 0, false, true, false, false, false, false,
                GameResult.NONE, null, null);
        Move m = find(s, "e1", "c1"); GameState after = RuleEngine.play(s, m, null);
        assertEquals(PieceType.ROOK, after.board.at(square("d1")).type);
    }
    @Test public void verticalCastleMovesBothPieces() {
        Board b = board("e1", Side.WHITE, PieceType.KING, "e8", Side.WHITE, PieceType.ROOK,
                "a8", Side.BLACK, PieceType.KING);
        GameState s = new GameState(b, Side.WHITE, -1, null, 0, false, false, true, false, false, false,
                GameResult.NONE, null, null);
        GameState after = RuleEngine.play(s, find(s, "e1", "e3"), null);
        assertEquals(PieceType.KING, after.board.at(square("e3")).type);
        assertEquals(PieceType.ROOK, after.board.at(square("e2")).type);
    }
    @Test public void blackCanCastleVerticallyAndAttacksDoNotMatter() {
        Board b = board("e8", Side.BLACK, PieceType.KING, "e1", Side.BLACK, PieceType.ROOK,
                "h3", Side.WHITE, PieceType.BISHOP, "a1", Side.WHITE, PieceType.KING);
        GameState s = new GameState(b, Side.BLACK, -1, null, 0, false, false, false, false, false, true,
                GameResult.NONE, null, null);
        GameState after = RuleEngine.play(s, find(s, "e8", "e6"), null);
        assertEquals(PieceType.KING, after.board.at(square("e6")).type);
        assertEquals(PieceType.ROOK, after.board.at(square("e7")).type);
    }
    @Test public void rookMoveRemovesCorrespondingCastlingRight() {
        Board b = board("e1", Side.WHITE, PieceType.KING, "h1", Side.WHITE, PieceType.ROOK);
        GameState s = new GameState(b, Side.WHITE, -1, null, 0, true, false, false, false, false, false,
                GameResult.NONE, null, null);
        GameState after = RuleEngine.play(s, new Move(square("h1"), square("h2")), null);
        assertFalse(after.whiteKingSide);
    }
    @Test public void promotedUnmovedRookCanUseVerticalCastleRight() {
        Board b = board("e7", Side.WHITE, PieceType.PAWN, "e1", Side.WHITE, PieceType.KING,
                "a8", Side.BLACK, PieceType.KING);
        GameState s = new GameState(b, Side.WHITE, -1, null, 0, false, false, true, false, false, false,
                GameResult.NONE, null, null);
        GameState promoted = RuleEngine.play(s, new Move(square("e7"), square("e8")), PieceType.ROOK);
        GameState whiteTurn = new GameState(promoted.board, Side.WHITE, -1, null, 0,
                promoted.whiteKingSide, promoted.whiteQueenSide, promoted.whiteVertical,
                promoted.blackKingSide, promoted.blackQueenSide, promoted.blackVertical,
                GameResult.NONE, null, promoted.repetitions);
        assertTrue(has(whiteTurn, "e1", "e3"));
        GameState movedRook = RuleEngine.play(whiteTurn, new Move(square("e8"), square("e7")), null);
        assertFalse(has(new GameState(movedRook.board, Side.WHITE, -1, null, 0,
                movedRook.whiteKingSide, movedRook.whiteQueenSide, movedRook.whiteVertical,
                movedRook.blackKingSide, movedRook.blackQueenSide, movedRook.blackVertical,
                GameResult.NONE, null, movedRook.repetitions), "e1", "e3"));
    }
    @Test public void editorRejectsSideToMoveWithoutKing() {
        GameState s = state(board("a1", Side.WHITE, PieceType.KING), Side.BLACK);
        assertNotNull(RuleEngine.validateEditorStart(s));
    }
    @Test public void editorRejectsUnfulfillableDebt() {
        GameState s = state(board("a1", Side.WHITE, PieceType.KING, "h8", Side.BLACK, PieceType.KING), Side.WHITE, -1, Side.BLACK, 0);
        assertNotNull(RuleEngine.validateEditorStart(s));
    }
    @Test public void editorKeepsUnusualArrangementUnchanged() {
        Board b = board("a1", Side.WHITE, PieceType.KING, "a1", Side.BLACK, PieceType.QUEEN);
        assertEquals(PieceType.QUEEN, b.at(square("a1")).type);
        assertEquals(0, b.countKings(Side.BLACK));
    }
    @Test public void noCheckmateOrStalemateRuleIsApplied() {
        GameState s = state(board("a1", Side.WHITE, PieceType.KING, "a2", Side.WHITE, PieceType.PAWN,
                "h8", Side.BLACK, PieceType.KING), Side.WHITE);
        assertFalse(MoveGenerator.allLegalMoves(s).isEmpty());
    }
    @Test public void threefoldRepeatIsClaimableNotAutomatic() {
        GameState s = GameState.initial();
        String key = PositionKey.of(s);
        HashMap<String, Integer> map = new HashMap<>(); map.put(key, 2);
        s = new GameState(s.board, s.toMove, s.enPassantTarget, null, 0,
                s.whiteKingSide, s.whiteQueenSide, s.whiteVertical, s.blackKingSide, s.blackQueenSide, s.blackVertical,
                GameResult.NONE, null, map);
        s = DrawDetector.recordPosition(s);
        assertTrue(DrawDetector.canClaimRepetition(s)); assertEquals(GameResult.NONE, s.result);
        assertEquals(GameResult.DRAW_REPETITION, RuleEngine.claimDraw(s, true).result);
    }
    @Test public void repetitionKeyDistinguishesKingFromKnight() {
        Board knight = board("a1", Side.WHITE, PieceType.KING, "b1", Side.WHITE, PieceType.KNIGHT,
                "h8", Side.BLACK, PieceType.KING);
        Board extraKing = board("a1", Side.WHITE, PieceType.KING, "b1", Side.WHITE, PieceType.KING,
                "h8", Side.BLACK, PieceType.KING);
        assertNotEquals(PositionKey.of(state(knight, Side.WHITE)), PositionKey.of(state(extraKing, Side.WHITE)));
    }
    @Test public void repetitionKeyIncludesCastlingRightsAndEnPassant() {
        Board b = board("e1", Side.WHITE, PieceType.KING, "h1", Side.WHITE, PieceType.ROOK,
                "e5", Side.WHITE, PieceType.PAWN, "d5", Side.BLACK, PieceType.PAWN,
                "h8", Side.BLACK, PieceType.KING);
        GameState noRights = state(b, Side.WHITE, -1, null, 0);
        GameState rights = new GameState(b, Side.WHITE, square("d6"), null, 0, true, false, false,
                false, false, false, GameResult.NONE, null, null);
        assertNotEquals(PositionKey.of(noRights), PositionKey.of(rights));
        GameState rightsNoEp = new GameState(b, Side.WHITE, -1, null, 0, true, false, false,
                false, false, false, GameResult.NONE, null, null);
        assertNotEquals(PositionKey.of(rightsNoEp), PositionKey.of(rights));
    }
    @Test public void fiftyMoveRuleIsClaimableNotAutomatic() {
        GameState s = state(board("a1", Side.WHITE, PieceType.KING, "h8", Side.BLACK, PieceType.KING), Side.WHITE, -1, null, 100);
        assertTrue(DrawDetector.canClaimFiftyMoves(s)); assertEquals(GameResult.NONE, s.result);
        assertEquals(GameResult.DRAW_FIFTY_MOVES, RuleEngine.claimDraw(s, false).result);
    }
    @Test public void drawAgreementRequiresOfferThenResponse() {
        GameState offered = RuleEngine.offerDraw(GameState.initial());
        assertEquals(GameResult.NONE, offered.result);
        assertEquals(GameResult.DRAW_AGREEMENT, RuleEngine.respondDraw(offered, true).result);
        assertNull(RuleEngine.respondDraw(offered, false).drawOfferBy);
    }

    private Move find(GameState s, String from, String to) {
        for (Move m : RuleEngine.legalMoves(s, square(from))) if (m.to == square(to)) return m;
        throw new AssertionError("No move " + from + " -> " + to);
    }
}
