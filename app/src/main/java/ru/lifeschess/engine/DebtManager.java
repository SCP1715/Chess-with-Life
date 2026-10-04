package ru.lifeschess.engine;

/** Rules for the one-turn obligation created by a king promotion. */
public final class DebtManager {
    private DebtManager() { }

    public static boolean isRequiredCapture(GameState state, Move move) {
        if (state.debtTargetKings == null || move.kind == Move.Kind.EN_PASSANT) return false;
        Piece captured = state.board.at(move.to);
        return captured != null && captured.type == PieceType.KING
                && captured.side == state.debtTargetKings;
    }

    public static boolean isPaid(GameState state, Piece captured) {
        return state.debtTargetKings != null && captured != null
                && captured.type == PieceType.KING && captured.side == state.debtTargetKings;
    }

    public static boolean canStart(GameState state) {
        return state.debtTargetKings == null || !MoveGenerator.allLegalMoves(state).isEmpty();
    }
}
