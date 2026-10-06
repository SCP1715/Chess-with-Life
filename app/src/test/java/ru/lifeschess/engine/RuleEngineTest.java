package ru.lifeschess.engine;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Test public void initialPositionStartsWithWhiteAndHasTwentyFiveMoves() {
        GameState initial = GameState.initial();
        assertEquals(Side.WHITE, initial.toMove);
        assertEquals(25, MoveGenerator.allLegalMoves(initial).size());
    }

    @Test public void distantBareKingsCanBeClaimedByEitherSideButAreNotAutomatic() {
        Board b = board("a1", Side.WHITE, PieceType.KING, "h8", Side.BLACK, PieceType.KING);
        for (Side turn : Side.values()) {
            GameState position = state(b, turn);
            assertEquals(GameResult.NONE, position.result);
            assertTrue(DrawDetector.canClaimBareKings(position));
            assertTrue(RuleEngine.legalActions(position).contains(
                    GameAction.procedure(GameAction.Type.CLAIM_BARE_KINGS)));
            assertTrue(RuleEngine.legalActions(position).stream()
                    .anyMatch(action -> action.type == GameAction.Type.MOVE));
            GameState claimed = RuleEngine.applyAction(position,
                    GameAction.procedure(GameAction.Type.CLAIM_BARE_KINGS));
            assertEquals(GameResult.DRAW_BARE_KINGS, claimed.result);
            assertEquals(turn, claimed.toMove);
            assertSame(position.board, claimed.board);
            assertEquals(position.halfMovesSinceCaptureOrPawn, claimed.halfMovesSinceCaptureOrPawn);
            assertEquals(1, claimed.history.size());
            assertEquals(GameAction.Type.CLAIM_BARE_KINGS, claimed.history.get(0).type);
        }
    }

    @Test public void adjacentBareKingsCannotBeClaimedAndMayBeCaptured() {
        String[][] pairs = {{"a1", "b1"}, {"a1", "a2"}, {"a1", "b2"}};
        for (String[] pair : pairs) {
            GameState position = state(board(pair[0], Side.WHITE, PieceType.KING,
                    pair[1], Side.BLACK, PieceType.KING), Side.WHITE);
            assertFalse(DrawDetector.canClaimBareKings(position));
            assertFalse(RuleEngine.legalActions(position).contains(
                    GameAction.procedure(GameAction.Type.CLAIM_BARE_KINGS)));
            Move capture = findOrNull(position, pair[0], pair[1]);
            assertNotNull("King capture must remain legal for " + pair[0] + " x " + pair[1], capture);
            assertEquals(GameResult.WHITE_WIN, RuleEngine.play(position, capture, null).result);
        }
    }

    @Test public void bareKingsClaimUsesChebyshevDistanceAndOnlyExactTwoKings() {
        assertTrue(DrawDetector.canClaimBareKings(state(board(
                "a1", Side.WHITE, PieceType.KING, "c1", Side.BLACK, PieceType.KING), Side.WHITE)));
        PieceType[] thirdPieces = {PieceType.BISHOP, PieceType.KNIGHT, PieceType.PAWN,
                PieceType.ROOK, PieceType.QUEEN, PieceType.KING};
        for (PieceType extra : thirdPieces) {
            Board b = board("a1", Side.WHITE, PieceType.KING, "h8", Side.BLACK, PieceType.KING,
                    "d4", extra == PieceType.KING ? Side.WHITE : Side.BLACK, extra);
            assertFalse("Third piece " + extra + " must disable the claim",
                    DrawDetector.canClaimBareKings(state(b, Side.WHITE)));
        }
        assertFalse(DrawDetector.canClaimBareKings(state(board(
                "a1", Side.WHITE, PieceType.KING, "c1", Side.WHITE, PieceType.KING), Side.WHITE)));
    }

    @Test public void closingKingDistanceRemainsLegalAndRemovesOnlyBareKingsClaim() {
        GameState position = state(board("f6", Side.WHITE, PieceType.KING,
                "h8", Side.BLACK, PieceType.KING), Side.WHITE);
        assertTrue(DrawDetector.canClaimBareKings(position));
        Move approach = findOrNull(position, "f6", "g7");
        assertNotNull(approach);
        GameState after = RuleEngine.play(position, approach, null);
        assertEquals(GameResult.NONE, after.result);
        assertFalse(DrawDetector.canClaimBareKings(after));
    }

    @Test public void bareKingsClaimIsIndependentFromFiftyMoveClaimAndOfferLatch() {
        GameState position = new GameState(board("a1", Side.WHITE, PieceType.KING,
                "h8", Side.BLACK, PieceType.KING), Side.WHITE, -1, null, 100,
                false, false, false, false, false, false, GameResult.NONE, null,
                new HashMap<String, Integer>(), null, true);
        assertTrue(RuleEngine.legalActions(position).contains(
                GameAction.procedure(GameAction.Type.CLAIM_FIFTY_MOVES)));
        GameState claimed = RuleEngine.applyAction(position,
                GameAction.procedure(GameAction.Type.CLAIM_BARE_KINGS));
        assertEquals(GameResult.DRAW_BARE_KINGS, claimed.result);
        assertEquals(100, claimed.halfMovesSinceCaptureOrPawn);
    }

    @Test public void bareKingsClaimCannotMaskDebtOrInvalidAction() {
        GameState debt = state(board("a1", Side.WHITE, PieceType.KING,
                "h8", Side.BLACK, PieceType.KING), Side.WHITE, -1, Side.BLACK, 0);
        assertFalse(DrawDetector.canClaimBareKings(debt));
        try {
            RuleEngine.applyAction(debt, GameAction.procedure(GameAction.Type.CLAIM_BARE_KINGS));
            fail("A bare-kings claim cannot bypass an active debt");
        } catch (IllegalArgumentException expected) { }
        assertEquals(GameResult.NONE, debt.result);
        assertEquals(0, debt.history.size());
    }

    @Test public void bareKingsPredicateExhaustivelyMatchesChebyshevDistance() {
        int adjacent = 0;
        int distant = 0;
        for (int white = 0; white < 64; white++) {
            for (int black = 0; black < 64; black++) {
                if (white == black) continue;
                Board b = new Board().with(white, new Piece(Side.WHITE, PieceType.KING, false))
                        .with(black, new Piece(Side.BLACK, PieceType.KING, false));
                int distance = Math.max(Math.abs(Board.col(white) - Board.col(black)),
                        Math.abs(Board.row(white) - Board.row(black)));
                for (Side turn : Side.values()) {
                    boolean expected = distance > 1;
                    assertEquals("white=" + white + ", black=" + black + ", turn=" + turn,
                            expected, DrawDetector.canClaimBareKings(state(b, turn)));
                    if (expected) distant++; else adjacent++;
                }
            }
        }
        assertEquals(840, adjacent);
        assertEquals(7224, distant);
    }

    @Test public void takingLastNonKingFigureCanCreateBareKingsClaim() {
        for (Side victimSide : Side.values()) {
            Board b = board("a1", Side.WHITE, PieceType.KING, "h8", Side.BLACK, PieceType.KING,
                    "b2", victimSide, PieceType.BISHOP);
            GameState before = state(b, Side.WHITE);
            Move capture = findOrNull(before, "a1", "b2");
            assertNotNull("King must take the final non-king piece", capture);
            GameState after = RuleEngine.play(before, capture, null);
            assertEquals(GameResult.NONE, after.result);
            assertEquals(PieceType.KING, after.board.at(square("b2")).type);
            assertTrue(DrawDetector.canClaimBareKings(after));
            assertTrue(RuleEngine.legalActions(after).contains(
                    GameAction.procedure(GameAction.Type.CLAIM_BARE_KINGS)));
        }
    }

    @Test public void lastNonKingCaptureLeavingAdjacentKingsDoesNotCreateClaim() {
        GameState before = state(board("f6", Side.WHITE, PieceType.KING,
                "h8", Side.BLACK, PieceType.KING, "g7", Side.BLACK, PieceType.BISHOP), Side.WHITE);
        Move capture = findOrNull(before, "f6", "g7");
        assertNotNull(capture);
        GameState after = RuleEngine.play(before, capture, null);
        assertFalse(DrawDetector.canClaimBareKings(after));
        assertNotNull(findOrNull(after, "h8", "g7"));
        assertEquals(GameResult.BLACK_WIN,
                RuleEngine.play(after, findOrNull(after, "h8", "g7"), null).result);
    }

    @Test public void newDrawActionRoundTripsThroughVersionedProtocol() {
        GameAction claim = GameAction.procedure(GameAction.Type.CLAIM_BARE_KINGS);
        assertEquals(claim, LifeChessProtocol.decodeAction(LifeChessProtocol.encodeAction(claim)));
        GameState position = state(board("a1", Side.WHITE, PieceType.KING,
                "h8", Side.BLACK, PieceType.KING), Side.WHITE);
        GameState claimed = RuleEngine.applyAction(position, claim);
        String snapshot = LifeChessProtocol.encodeSnapshot("bare-kings", 9, claimed);
        LifeChessProtocol.Snapshot decoded = LifeChessProtocol.decodeSnapshot(snapshot);
        assertEquals(GameResult.DRAW_BARE_KINGS, decoded.state.result);
        assertEquals(claim, decoded.state.history.get(0));
    }

    @Test public void engineReplayPreservesRootAndSkipsDrawProcedureActions() {
        GameState initial = GameState.initial();
        assertEquals(LifeChessFen.encode(initial), initial.searchRootFen);
        GameState offered = RuleEngine.applyAction(initial,
                GameAction.procedure(GameAction.Type.OFFER_DRAW));
        assertTrue(offered.searchMoves.isEmpty());
        GameAction decline = GameAction.procedure(GameAction.Type.DECLINE_DRAW);
        GameState incomingOffer = initial.copy(initial.board, initial.toMove,
                initial.enPassantTarget, initial.debtTargetKings,
                initial.halfMovesSinceCaptureOrPawn, initial.whiteKingSide,
                initial.whiteQueenSide, initial.whiteVertical, initial.blackKingSide,
                initial.blackQueenSide, initial.blackVertical, initial.result,
                Side.BLACK, initial.repetitions);
        GameState declined = RuleEngine.applyAction(incomingOffer, decline);
        assertTrue(declined.searchMoves.isEmpty());

        Move e2e4 = find(initial, "e2", "e4");
        GameState moved = RuleEngine.play(initial, e2e4, null);
        assertEquals(initial.searchRootFen, moved.searchRootFen);
        assertEquals(java.util.Collections.singletonList("e2e4"), moved.searchMoves);
        GameState restored = LifeChessProtocol.decodeSnapshot(LifeChessProtocol.encodeSnapshot(
                "history-roundtrip", 4, moved)).state;
        assertEquals(moved.searchRootFen, restored.searchRootFen);
        assertEquals(moved.searchMoves, restored.searchMoves);
    }

    @Test public void bareKingsClaimIsUnavailableAfterAnyTerminalResult() {
        GameState ongoing = state(board("a1", Side.WHITE, PieceType.KING,
                "h8", Side.BLACK, PieceType.KING), Side.WHITE);
        GameState ended = ongoing.copy(ongoing.board, ongoing.toMove, ongoing.enPassantTarget,
                ongoing.debtTargetKings, ongoing.halfMovesSinceCaptureOrPawn,
                false, false, false, false, false, false, GameResult.WHITE_WIN,
                null, ongoing.repetitions);
        assertFalse(DrawDetector.canClaimBareKings(ended));
        assertTrue(RuleEngine.legalActions(ended).isEmpty());
    }
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
        assertEquals("У стороны, которой ходить, должен быть хотя бы один король.",
                RuleEngine.validateEditorStart(s));
        assertEquals(PieceType.KING, s.board.at(square("a1")).type);
    }
    @Test public void editorRejectsUnfulfillableDebt() {
        GameState s = state(board("a1", Side.WHITE, PieceType.KING, "h8", Side.BLACK, PieceType.KING), Side.WHITE, -1, Side.BLACK, 0);
        assertEquals("Нельзя начать: активный долг невозможно исполнить взятием короля.",
                RuleEngine.validateEditorStart(s));
    }
    @Test public void editorAllowsMultipleKingsAndExecutableDebt() {
        GameState s = state(board("a1", Side.WHITE, PieceType.KING, "h1", Side.WHITE, PieceType.KING,
                "h8", Side.BLACK, PieceType.KING, "a8", Side.BLACK, PieceType.ROOK),
                Side.BLACK, -1, Side.WHITE, 0);
        assertNull(RuleEngine.validateEditorStart(s));
        assertTrue(RuleEngine.legalActions(s).contains(GameAction.move(
                new Move(square("a8"), square("a1")), null)));
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
        GameState offered = RuleEngine.applyAction(GameState.initial(),
                GameAction.procedure(GameAction.Type.OFFER_DRAW));
        assertEquals(GameResult.NONE, offered.result);
        assertTrue(RuleEngine.legalActions(offered).contains(
                GameAction.procedure(GameAction.Type.ACCEPT_DRAW)));
        assertTrue(RuleEngine.legalActions(offered).contains(
                GameAction.procedure(GameAction.Type.DECLINE_DRAW)));

        GameState accepted = RuleEngine.applyAction(offered,
                GameAction.procedure(GameAction.Type.ACCEPT_DRAW));
        assertEquals(GameResult.DRAW_AGREEMENT, accepted.result);
        assertTrue(RuleEngine.legalActions(accepted).isEmpty());

        GameState declined = RuleEngine.applyAction(offered,
                GameAction.procedure(GameAction.Type.DECLINE_DRAW));
        assertEquals(GameResult.NONE, declined.result);
        assertNull(declined.drawOfferBy);
        assertEquals(offered.board, declined.board);
        assertEquals(offered.toMove, declined.toMove);
        assertEquals(offered.halfMovesSinceCaptureOrPawn,
                declined.halfMovesSinceCaptureOrPawn);
        assertEquals(offered.repetitions, declined.repetitions);
        assertEquals(2, declined.history.size());
    }

    @Test public void completeActionsKeepPromotionChoiceDistinct() {
        GameState s = state(board("h2", Side.WHITE, PieceType.KING,
                "c1", Side.WHITE, PieceType.BISHOP, "b7", Side.WHITE, PieceType.PAWN,
                "h6", Side.BLACK, PieceType.KING, "h7", Side.BLACK, PieceType.QUEEN,
                "d2", Side.BLACK, PieceType.ROOK), Side.WHITE);
        GameAction queen = null, king = null;
        for (GameAction action : RuleEngine.legalActions(s)) {
            if (action.type == GameAction.Type.MOVE && action.from == square("b7")
                    && action.to == square("b8")) {
                if (action.promotion == PieceType.QUEEN) queen = action;
                if (action.promotion == PieceType.KING) king = action;
            }
        }
        assertNotNull(queen);
        assertNotNull(king);
        assertNotEquals(queen, king);
        assertNull(RuleEngine.applyAction(s, queen).debtTargetKings);
        GameState kingPromotion = RuleEngine.applyAction(s, king);
        assertEquals(Side.WHITE, kingPromotion.debtTargetKings);
        assertEquals(PieceType.KING, kingPromotion.board.at(square("b8")).type);
    }

    @Test public void completeActionsRepresentDrawProcedureSeparately() {
        GameState s = GameState.initial();
        String key = PositionKey.of(s);
        HashMap<String, Integer> map = new HashMap<>();
        map.put(key, 2);
        s = new GameState(s.board, s.toMove, s.enPassantTarget, s.debtTargetKings,
                s.halfMovesSinceCaptureOrPawn, s.whiteKingSide, s.whiteQueenSide, s.whiteVertical,
                s.blackKingSide, s.blackQueenSide, s.blackVertical, s.result, null, map);
        s = DrawDetector.recordPosition(s);

        assertTrue(RuleEngine.legalActions(s).contains(
                GameAction.procedure(GameAction.Type.CLAIM_REPETITION)));
        GameState draw = RuleEngine.applyAction(s,
                GameAction.procedure(GameAction.Type.CLAIM_REPETITION));
        assertEquals(GameResult.DRAW_REPETITION, draw.result);
        assertTrue(RuleEngine.legalActions(draw).isEmpty());
        assertEquals(1, draw.history.size());
        assertEquals(GameAction.Type.CLAIM_REPETITION, draw.history.get(0).type);
    }

    @Test public void claimRemainsAvailableDuringOpponentDrawOffer() {
        GameState initial = GameState.initial();
        Map<String, Integer> repetitions = new HashMap<>();
        repetitions.put(PositionKey.of(initial), 2);
        GameState claimable = new GameState(initial.board, initial.toMove, initial.enPassantTarget,
                initial.debtTargetKings, 100, initial.whiteKingSide, initial.whiteQueenSide,
                initial.whiteVertical, initial.blackKingSide, initial.blackQueenSide,
                initial.blackVertical, GameResult.NONE, Side.BLACK, repetitions);
        claimable = DrawDetector.recordPosition(claimable);

        List<GameAction> actions = RuleEngine.legalActions(claimable);
        assertTrue(actions.contains(GameAction.procedure(GameAction.Type.CLAIM_REPETITION)));
        assertTrue(actions.contains(GameAction.procedure(GameAction.Type.CLAIM_FIFTY_MOVES)));
        assertTrue(actions.contains(GameAction.procedure(GameAction.Type.ACCEPT_DRAW)));
        assertTrue(actions.contains(GameAction.procedure(GameAction.Type.DECLINE_DRAW)));
        assertFalse(actions.contains(GameAction.procedure(GameAction.Type.OFFER_DRAW)));

        GameState claimed = RuleEngine.applyAction(claimable,
                GameAction.procedure(GameAction.Type.CLAIM_REPETITION));
        assertEquals(GameResult.DRAW_REPETITION, claimed.result);
        assertNull(claimed.drawOfferBy);

        GameState offererTurn = new GameState(initial.board, initial.toMove, -1, null, 0,
                initial.whiteKingSide, initial.whiteQueenSide, initial.whiteVertical,
                initial.blackKingSide, initial.blackQueenSide, initial.blackVertical,
                GameResult.NONE, Side.WHITE, initial.repetitions);
        List<GameAction> offererActions = RuleEngine.legalActions(offererTurn);
        assertTrue(offererActions.stream().anyMatch(a -> a.type == GameAction.Type.MOVE));
        assertTrue(offererActions.contains(GameAction.procedure(GameAction.Type.ACCEPT_DRAW)));
        assertTrue(offererActions.contains(GameAction.procedure(GameAction.Type.DECLINE_DRAW)));

        GameState receiverMove = RuleEngine.play(claimable.copy(claimable.board, Side.WHITE, -1,
                null, 0, true, true, true, true, true, true, GameResult.NONE, Side.BLACK,
                claimable.repetitions), find(initial, "e2", "e4"), null);
        assertNull(receiverMove.drawOfferBy);
    }

    @Test public void gameStateRetainsImmutableActionHistory() {
        GameState start = GameState.initial();
        Move move = find(start, "e2", "e4");
        GameState afterMove = RuleEngine.play(start, move, null);
        assertEquals(1, afterMove.history.size());
        assertEquals(GameAction.move(move, null), afterMove.history.get(0));

        GameAction offer = GameAction.procedure(GameAction.Type.OFFER_DRAW);
        GameState offered = RuleEngine.applyAction(afterMove, offer);
        assertEquals(2, offered.history.size());
        assertEquals(1, afterMove.history.size());
        try {
            offered.history.clear();
            fail("History was externally mutable");
        } catch (UnsupportedOperationException expected) {
            assertEquals(2, offered.history.size());
        }
        try {
            offered.repetitions.clear();
            fail("Repetition context was externally mutable");
        } catch (UnsupportedOperationException expected) {
            assertFalse(offered.repetitions.isEmpty());
        }
    }

    @Test public void promotionChoiceOnOrdinaryMoveIsRejectedAtomically() {
        GameState s = state(board("e1", Side.WHITE, PieceType.KING,
                "h8", Side.BLACK, PieceType.KING, "a2", Side.WHITE, PieceType.ROOK), Side.WHITE);
        try {
            RuleEngine.play(s, find(s, "a2", "a3"), PieceType.QUEEN);
            fail("Non-promotion action accepted a promotion type");
        } catch (IllegalArgumentException expected) {
            assertEquals(PieceType.ROOK, s.board.at(square("a2")).type);
            assertEquals(Side.WHITE, s.toMove);
        }
    }

    @Test public void versionedProtocolRoundTripsFullSnapshotAndRequest() {
        Board b = board("e1", Side.WHITE, PieceType.KING, "h8", Side.BLACK, PieceType.KING,
                "e5", Side.WHITE, PieceType.PAWN, "d5", Side.BLACK, PieceType.PAWN);
        b = b.with(square("e1"), new Piece(Side.WHITE, PieceType.KING, true));
        b = b.with(square("e5"), new Piece(Side.WHITE, PieceType.PAWN, true));
        b = b.with(square("d5"), new Piece(Side.BLACK, PieceType.PAWN, true));
        Map<String, Integer> repetitions = new HashMap<>();
        repetitions.put("board|turn|debt", 2);
        GameAction move = GameAction.move(new Move(square("e5"), square("d6"), Move.Kind.EN_PASSANT), null);
        GameAction offer = GameAction.procedure(GameAction.Type.OFFER_DRAW);
        GameState original = new GameState(b, Side.WHITE, square("d6"), Side.BLACK, 77,
                true, false, true, false, true, false, GameResult.NONE, Side.BLACK,
                repetitions, java.util.Arrays.asList(move, offer), true);

        String encoded = LifeChessProtocol.encodeSnapshot("req-17", 41, original);
        LifeChessProtocol.Snapshot decoded = LifeChessProtocol.decodeSnapshot(encoded);
        assertEquals("req-17", decoded.requestId);
        assertEquals(41L, decoded.stateRevision);
        GameState copy = decoded.state;
        assertEquals(Side.WHITE, copy.toMove);
        assertEquals(square("d6"), copy.enPassantTarget);
        assertEquals(Side.BLACK, copy.debtTargetKings);
        assertEquals(77, copy.halfMovesSinceCaptureOrPawn);
        assertTrue(copy.whiteKingSide);
        assertTrue(copy.whiteVertical);
        assertTrue(copy.blackQueenSide);
        assertEquals(GameResult.NONE, copy.result);
        assertEquals(Side.BLACK, copy.drawOfferBy);
        assertTrue(copy.drawOfferSentInCurrentNonWinningStretch);
        assertEquals(2, (int) copy.repetitions.get("board|turn|debt"));
        assertTrue(copy.board.at(square("e1")).hasMoved);
        assertTrue(copy.board.at(square("e5")).hasMoved);
        assertEquals(original.history, copy.history);
        assertEquals(encoded, LifeChessProtocol.encodeSnapshot("req-17", 41, copy));

        LifeChessProtocol.Request request = new LifeChessProtocol.Request("apply-18", 42,
                LifeChessProtocol.Operation.APPLY_ACTION, original, move);
        LifeChessProtocol.Request decodedRequest = LifeChessProtocol.decodeRequest(
                LifeChessProtocol.encodeRequest(request));
        assertEquals(request.requestId, decodedRequest.requestId);
        assertEquals(request.stateRevision, decodedRequest.stateRevision);
        assertEquals(request.operation, decodedRequest.operation);
        assertEquals(move, decodedRequest.action);
        assertEquals(Side.BLACK, decodedRequest.state.debtTargetKings);

        List<GameAction> actions = RuleEngine.legalActions(original);
        LifeChessProtocol.Response response = new LifeChessProtocol.Response("req-17", 42,
                LifeChessProtocol.ResponseKind.LEGAL_ACTIONS, original, actions,
                actions.get(0), null, null);
        LifeChessProtocol.Response decodedResponse = LifeChessProtocol.decodeResponse(
                LifeChessProtocol.encodeResponse(response));
        assertEquals(response.requestId, decodedResponse.requestId);
        assertEquals(response.stateRevision, decodedResponse.stateRevision);
        assertEquals(response.kind, decodedResponse.kind);
        assertEquals(actions, decodedResponse.legalActions);
        assertEquals(response.selectedAction, decodedResponse.selectedAction);
        assertEquals(encoded, LifeChessProtocol.encodeSnapshot(decodedResponse.requestId,
                41, decodedResponse.state));
    }

    @Test public void protocolRejectsUnsupportedVersionAndMalformedAction() {
        String snapshot = LifeChessProtocol.encodeSnapshot("v-check", 0, GameState.initial());
        try {
            LifeChessProtocol.decodeSnapshot(snapshot.replace("\"version\":2", "\"version\":99"));
            fail("Unsupported protocol version was accepted");
        } catch (IllegalArgumentException expected) { }

        try {
            LifeChessProtocol.decodeAction("{\"type\":\"MOVE\",\"from\":\"z9\","
                    + "\"to\":\"a1\",\"moveKind\":\"NORMAL\",\"promotion\":null}");
            fail("Malformed square was accepted");
        } catch (IllegalArgumentException expected) { }
    }

    private Move find(GameState s, String from, String to) {
        for (Move m : RuleEngine.legalMoves(s, square(from))) if (m.to == square(to)) return m;
        throw new AssertionError("No move " + from + " -> " + to);
    }

    private Move findOrNull(GameState s, String from, String to) {
        for (Move m : RuleEngine.legalMoves(s, square(from))) if (m.to == square(to)) return m;
        return null;
    }
}
