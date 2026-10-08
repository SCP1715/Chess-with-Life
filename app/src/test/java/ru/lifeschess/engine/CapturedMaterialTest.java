package ru.lifeschess.engine;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import org.junit.Test;

public class CapturedMaterialTest {
    @Test public void recordsCapturedPieceForCapturingSideAndPreservesItOnCopy() {
        Board board = new Board()
                .with(square("a1"), new Piece(Side.WHITE, PieceType.KING, false))
                .with(square("h8"), new Piece(Side.BLACK, PieceType.KING, false))
                .with(square("d4"), new Piece(Side.WHITE, PieceType.ROOK, false))
                .with(square("d5"), new Piece(Side.BLACK, PieceType.KNIGHT, false));
        GameState state = position(board, Side.WHITE, -1);

        GameState after = RuleEngine.play(state,
                new Move(square("d4"), square("d5")), null);

        assertEquals(1, after.capturedPieces(Side.WHITE).size());
        assertEquals(Side.BLACK, after.capturedPieces(Side.WHITE).get(0).side);
        assertEquals(PieceType.KNIGHT, after.capturedPieces(Side.WHITE).get(0).type);
        assertEquals(0, after.capturedPieces(Side.BLACK).size());
        assertEquals(1, after.withDrawOfferLatch(true).capturedPieces(Side.WHITE).size());
    }

    @Test public void recordsTheRemovedPawnForEnPassant() {
        Board board = new Board()
                .with(square("a1"), new Piece(Side.WHITE, PieceType.KING, false))
                .with(square("h8"), new Piece(Side.BLACK, PieceType.KING, false))
                .with(square("e5"), new Piece(Side.WHITE, PieceType.PAWN, true))
                .with(square("d5"), new Piece(Side.BLACK, PieceType.PAWN, true));
        GameState state = position(board, Side.WHITE, square("d6"));

        GameState after = RuleEngine.play(state,
                new Move(square("e5"), square("d6"), Move.Kind.EN_PASSANT), null);

        assertEquals(1, after.capturedPieces(Side.WHITE).size());
        assertEquals(PieceType.PAWN, after.capturedPieces(Side.WHITE).get(0).type);
        assertEquals(Side.BLACK, after.capturedPieces(Side.WHITE).get(0).side);
    }

    @Test public void recordsFriendlyPieceTakenByKingAsCaptureByThatKing() {
        Board board = new Board()
                .with(square("a1"), new Piece(Side.WHITE, PieceType.KING, false))
                .with(square("b2"), new Piece(Side.WHITE, PieceType.BISHOP, false))
                .with(square("h8"), new Piece(Side.BLACK, PieceType.KING, false));
        GameState state = position(board, Side.WHITE, -1);

        GameState after = RuleEngine.play(state,
                new Move(square("a1"), square("b2")), null);

        assertEquals(1, after.capturedPieces(Side.WHITE).size());
        assertEquals(Side.WHITE, after.capturedPieces(Side.WHITE).get(0).side);
        assertEquals(PieceType.BISHOP, after.capturedPieces(Side.WHITE).get(0).type);
    }

    private GameState position(Board board, Side turn, int enPassantTarget) {
        return DrawDetector.recordPosition(new GameState(board, turn, enPassantTarget, null, 0,
                false, false, false, false, false, false,
                GameResult.NONE, null, new HashMap<String, Integer>()));
    }

    private static int square(String name) {
        return Board.index(8 - (name.charAt(1) - '0'), name.charAt(0) - 'a');
    }
}
