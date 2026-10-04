package ru.lifeschess.engine;

import java.util.ArrayList;
import java.util.List;

public final class PromotionLogic {
    private PromotionLogic() { }

    public static List<PieceType> choices(Board afterPawnMove, Side promotingSide) {
        List<PieceType> result = new ArrayList<>();
        result.add(PieceType.QUEEN);
        result.add(PieceType.ROOK);
        result.add(PieceType.BISHOP);
        result.add(PieceType.KNIGHT);
        if (KingThreatDetector.hasKingUnderAttack(afterPawnMove, promotingSide)) result.add(PieceType.KING);
        return result;
    }
}
