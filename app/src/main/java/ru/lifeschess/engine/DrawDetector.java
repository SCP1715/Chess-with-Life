package ru.lifeschess.engine;

import java.util.HashMap;
import java.util.Map;

public final class DrawDetector {
    private DrawDetector() { }
    public static GameState recordPosition(GameState s) {
        Map<String, Integer> counts = new HashMap<>(s.repetitions);
        String key = PositionKey.of(s);
        counts.put(key, counts.containsKey(key) ? counts.get(key) + 1 : 1);
        return s.copy(s.board, s.toMove, s.enPassantTarget, s.debtTargetKings,
                s.halfMovesSinceCaptureOrPawn, s.whiteKingSide, s.whiteQueenSide, s.whiteVertical,
                s.blackKingSide, s.blackQueenSide, s.blackVertical, s.result, s.drawOfferBy, counts);
    }
    public static int repetitions(GameState s) { Integer n = s.repetitions.get(PositionKey.of(s)); return n == null ? 0 : n; }
    public static boolean canClaimRepetition(GameState s) { return repetitions(s) >= 3; }
    public static boolean canClaimFiftyMoves(GameState s) { return s.halfMovesSinceCaptureOrPawn >= 100; }

    /** Exact, claim-only rule; this is not an automatic material draw. */
    public static boolean canClaimBareKings(GameState s) {
        if (s == null || s.result != GameResult.NONE || s.debtTargetKings != null
                || s.board.countKings(Side.WHITE) != 1 || s.board.countKings(Side.BLACK) != 1)
            return false;

        int whiteKing = -1;
        int blackKing = -1;
        int pieces = 0;
        for (int square = 0; square < 64; square++) {
            Piece piece = s.board.at(square);
            if (piece == null) continue;
            pieces++;
            if (piece.type != PieceType.KING) return false;
            if (piece.side == Side.WHITE) whiteKing = square;
            else blackKing = square;
        }
        if (pieces != 2 || whiteKing < 0 || blackKing < 0) return false;
        int fileDistance = Math.abs(Board.col(whiteKing) - Board.col(blackKing));
        int rankDistance = Math.abs(Board.row(whiteKing) - Board.row(blackKing));
        return Math.max(fileDistance, rankDistance) > 1;
    }
}
