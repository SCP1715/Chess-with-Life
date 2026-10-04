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
}
