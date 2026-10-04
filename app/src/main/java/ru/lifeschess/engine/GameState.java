package ru.lifeschess.engine;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public final class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    public final Board board;
    public final Side toMove;
    public final int enPassantTarget;
    // The obliged side must capture a king belonging to debtTargetKings.
    public final Side debtTargetKings;
    public final int halfMovesSinceCaptureOrPawn;
    public final boolean whiteKingSide, whiteQueenSide, whiteVertical;
    public final boolean blackKingSide, blackQueenSide, blackVertical;
    public final GameResult result;
    public final Side drawOfferBy;
    public final Map<String, Integer> repetitions;

    public GameState(Board board, Side toMove, int enPassantTarget, Side debtTargetKings,
                     int halfMoves, boolean whiteKingSide, boolean whiteQueenSide,
                     boolean whiteVertical, boolean blackKingSide, boolean blackQueenSide,
                     boolean blackVertical, GameResult result, Side drawOfferBy,
                     Map<String, Integer> repetitions) {
        this.board = board;
        this.toMove = toMove;
        this.enPassantTarget = enPassantTarget;
        this.debtTargetKings = debtTargetKings;
        this.halfMovesSinceCaptureOrPawn = halfMoves;
        this.whiteKingSide = whiteKingSide;
        this.whiteQueenSide = whiteQueenSide;
        this.whiteVertical = whiteVertical;
        this.blackKingSide = blackKingSide;
        this.blackQueenSide = blackQueenSide;
        this.blackVertical = blackVertical;
        this.result = result;
        this.drawOfferBy = drawOfferBy;
        this.repetitions = new HashMap<>(repetitions == null ? new HashMap<String, Integer>() : repetitions);
    }

    public static GameState empty() {
        return new GameState(new Board(), Side.WHITE, -1, null, 0,
                false, false, false, false, false, false,
                GameResult.NONE, null, new HashMap<String, Integer>());
    }

    public static GameState initial() {
        Board board = new Board();
        PieceType[] back = {PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK};
        for (int col = 0; col < 8; col++) {
            board = board.with(0, col, new Piece(Side.BLACK, back[col], false));
            board = board.with(1, col, new Piece(Side.BLACK, PieceType.PAWN, false));
            board = board.with(6, col, new Piece(Side.WHITE, PieceType.PAWN, false));
            board = board.with(7, col, new Piece(Side.WHITE, back[col], false));
        }
        GameState state = new GameState(board, Side.WHITE, -1, null, 0,
                true, true, true, true, true, true,
                GameResult.NONE, null, new HashMap<String, Integer>());
        return DrawDetector.recordPosition(state);
    }

    public boolean hasCastleRight(Side side, Move.Kind kind) {
        if (side == Side.WHITE) {
            if (kind == Move.Kind.CASTLE_KING) return whiteKingSide;
            if (kind == Move.Kind.CASTLE_QUEEN) return whiteQueenSide;
            if (kind == Move.Kind.CASTLE_VERTICAL) return whiteVertical;
        } else {
            if (kind == Move.Kind.CASTLE_KING) return blackKingSide;
            if (kind == Move.Kind.CASTLE_QUEEN) return blackQueenSide;
            if (kind == Move.Kind.CASTLE_VERTICAL) return blackVertical;
        }
        return false;
    }

    public GameState copy(Board b, Side turn, int ep, Side debt, int half,
                          boolean wK, boolean wQ, boolean wV,
                          boolean bK, boolean bQ, boolean bV,
                          GameResult outcome, Side offer, Map<String, Integer> reps) {
        return new GameState(b, turn, ep, debt, half, wK, wQ, wV,
                bK, bQ, bV, outcome, offer, reps);
    }
}
