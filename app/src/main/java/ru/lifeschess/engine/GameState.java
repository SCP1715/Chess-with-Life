package ru.lifeschess.engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
    /** Complete action history since the initial/editor position was created. */
    public final List<GameAction> history;
    /** Captured pieces grouped by the side that made the capture. */
    public final List<Piece> capturedByWhite, capturedByBlack;
    /** Engine replay anchor and only the board-changing moves after it. */
    public final String searchRootFen;
    public final List<String> searchMoves;
    /** Prevents another bot draw offer during the same non-winning score stretch. */
    public final boolean drawOfferSentInCurrentNonWinningStretch;

    public GameState(Board board, Side toMove, int enPassantTarget, Side debtTargetKings,
                     int halfMoves, boolean whiteKingSide, boolean whiteQueenSide,
                     boolean whiteVertical, boolean blackKingSide, boolean blackQueenSide,
                     boolean blackVertical, GameResult result, Side drawOfferBy,
                     Map<String, Integer> repetitions) {
        this(board, toMove, enPassantTarget, debtTargetKings, halfMoves,
                whiteKingSide, whiteQueenSide, whiteVertical,
                blackKingSide, blackQueenSide, blackVertical,
                result, drawOfferBy, repetitions, Collections.<GameAction>emptyList());
    }

    public GameState(Board board, Side toMove, int enPassantTarget, Side debtTargetKings,
                     int halfMoves, boolean whiteKingSide, boolean whiteQueenSide,
                     boolean whiteVertical, boolean blackKingSide, boolean blackQueenSide,
                     boolean blackVertical, GameResult result, Side drawOfferBy,
                     Map<String, Integer> repetitions, List<GameAction> history) {
        this(board, toMove, enPassantTarget, debtTargetKings, halfMoves,
                whiteKingSide, whiteQueenSide, whiteVertical,
                blackKingSide, blackQueenSide, blackVertical,
                result, drawOfferBy, repetitions, history, false);
    }

    public GameState(Board board, Side toMove, int enPassantTarget, Side debtTargetKings,
                     int halfMoves, boolean whiteKingSide, boolean whiteQueenSide,
                     boolean whiteVertical, boolean blackKingSide, boolean blackQueenSide,
                     boolean blackVertical, GameResult result, Side drawOfferBy,
                     Map<String, Integer> repetitions, List<GameAction> history,
                     boolean drawOfferSentInCurrentNonWinningStretch) {
        this(board, toMove, enPassantTarget, debtTargetKings, halfMoves,
                whiteKingSide, whiteQueenSide, whiteVertical,
                blackKingSide, blackQueenSide, blackVertical,
                result, drawOfferBy, repetitions, history,
                drawOfferSentInCurrentNonWinningStretch, null, null);
    }

    public GameState(Board board, Side toMove, int enPassantTarget, Side debtTargetKings,
                     int halfMoves, boolean whiteKingSide, boolean whiteQueenSide,
                     boolean whiteVertical, boolean blackKingSide, boolean blackQueenSide,
                     boolean blackVertical, GameResult result, Side drawOfferBy,
                     Map<String, Integer> repetitions, List<GameAction> history,
                     boolean drawOfferSentInCurrentNonWinningStretch,
                     String searchRootFen, List<String> searchMoves) {
        this(board, toMove, enPassantTarget, debtTargetKings, halfMoves,
                whiteKingSide, whiteQueenSide, whiteVertical,
                blackKingSide, blackQueenSide, blackVertical,
                result, drawOfferBy, repetitions, history,
                drawOfferSentInCurrentNonWinningStretch, searchRootFen, searchMoves,
                null, null);
    }

    public GameState(Board board, Side toMove, int enPassantTarget, Side debtTargetKings,
                     int halfMoves, boolean whiteKingSide, boolean whiteQueenSide,
                     boolean whiteVertical, boolean blackKingSide, boolean blackQueenSide,
                     boolean blackVertical, GameResult result, Side drawOfferBy,
                     Map<String, Integer> repetitions, List<GameAction> history,
                     boolean drawOfferSentInCurrentNonWinningStretch,
                     String searchRootFen, List<String> searchMoves,
                     List<Piece> capturedByWhite, List<Piece> capturedByBlack) {
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
        this.repetitions = Collections.unmodifiableMap(new HashMap<>(
                repetitions == null ? new HashMap<String, Integer>() : repetitions));
        this.history = Collections.unmodifiableList(new ArrayList<>(
                history == null ? Collections.<GameAction>emptyList() : history));
        this.capturedByWhite = immutablePieces(capturedByWhite);
        this.capturedByBlack = immutablePieces(capturedByBlack);
        this.searchMoves = Collections.unmodifiableList(new ArrayList<>(
                searchMoves == null ? Collections.<String>emptyList() : searchMoves));
        this.searchRootFen = searchRootFen != null ? searchRootFen
                : this.history.isEmpty() ? LifeChessFen.encode(this) : null;
        this.drawOfferSentInCurrentNonWinningStretch = drawOfferSentInCurrentNonWinningStretch;
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
                bK, bQ, bV, outcome, offer, reps, history,
                drawOfferSentInCurrentNonWinningStretch, searchRootFen, searchMoves,
                capturedPieces(Side.WHITE), capturedPieces(Side.BLACK));
    }

    public GameState withHistoryAction(GameAction action) {
        List<GameAction> nextHistory = new ArrayList<>(history);
        nextHistory.add(action);
        return new GameState(board, toMove, enPassantTarget, debtTargetKings,
                halfMovesSinceCaptureOrPawn, whiteKingSide, whiteQueenSide, whiteVertical,
                blackKingSide, blackQueenSide, blackVertical, result, drawOfferBy,
                repetitions, nextHistory, drawOfferSentInCurrentNonWinningStretch,
                searchRootFen, searchMoves, capturedPieces(Side.WHITE), capturedPieces(Side.BLACK));
    }

    public GameState withDrawOfferLatch(boolean sent) {
        return new GameState(board, toMove, enPassantTarget, debtTargetKings,
                halfMovesSinceCaptureOrPawn, whiteKingSide, whiteQueenSide, whiteVertical,
                blackKingSide, blackQueenSide, blackVertical, result, drawOfferBy,
                repetitions, history, sent, searchRootFen, searchMoves,
                capturedPieces(Side.WHITE), capturedPieces(Side.BLACK));
    }

    public GameState withEngineMove(GameAction action) {
        if (action == null || action.type != GameAction.Type.MOVE) return this;
        List<String> moves = new ArrayList<>(searchMoves);
        moves.add(LifeChessFen.encodeMove(action));
        return new GameState(board, toMove, enPassantTarget, debtTargetKings,
                halfMovesSinceCaptureOrPawn, whiteKingSide, whiteQueenSide, whiteVertical,
                blackKingSide, blackQueenSide, blackVertical, result, drawOfferBy,
                repetitions, history, drawOfferSentInCurrentNonWinningStretch,
                searchRootFen, moves, capturedPieces(Side.WHITE), capturedPieces(Side.BLACK));
    }

    public List<Piece> capturedPieces(Side capturer) {
        List<Piece> pieces = capturer == Side.WHITE ? capturedByWhite : capturedByBlack;
        return pieces == null ? Collections.<Piece>emptyList() : pieces;
    }

    public GameState withCapturedPiece(Side capturer, Piece captured) {
        if (capturer == null || captured == null) return this;
        List<Piece> white = new ArrayList<>(capturedPieces(Side.WHITE));
        List<Piece> black = new ArrayList<>(capturedPieces(Side.BLACK));
        (capturer == Side.WHITE ? white : black).add(captured);
        return new GameState(board, toMove, enPassantTarget, debtTargetKings,
                halfMovesSinceCaptureOrPawn, whiteKingSide, whiteQueenSide, whiteVertical,
                blackKingSide, blackQueenSide, blackVertical, result, drawOfferBy,
                repetitions, history, drawOfferSentInCurrentNonWinningStretch,
                searchRootFen, searchMoves, white, black);
    }

    private static List<Piece> immutablePieces(List<Piece> pieces) {
        return Collections.unmodifiableList(new ArrayList<>(
                pieces == null ? Collections.<Piece>emptyList() : pieces));
    }
}
