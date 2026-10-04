package ru.lifeschess.engine;

import java.util.List;

public final class RuleEngine {
    private RuleEngine() { }
    public static List<Move> legalMoves(GameState state, int from) { return MoveGenerator.legalMoves(state, from); }
    public static GameState play(GameState state, Move move, PieceType promotion) {
        if (!MoveGenerator.legalMoves(state, move.from).contains(move))
            throw new IllegalArgumentException("Move is not legal in this position");
        return MoveApplier.apply(state, move, promotion);
    }
    public static GameState startTurn(GameState state) {
        if (state.result != GameResult.NONE) return state;
        if (state.board.countKings(state.toMove) == 0) {
            GameResult result = state.toMove == Side.WHITE ? GameResult.BLACK_WIN : GameResult.WHITE_WIN;
            return state.copy(state.board, state.toMove, state.enPassantTarget, state.debtTargetKings,
                    state.halfMovesSinceCaptureOrPawn, state.whiteKingSide, state.whiteQueenSide, state.whiteVertical,
                    state.blackKingSide, state.blackQueenSide, state.blackVertical, result, state.drawOfferBy, state.repetitions);
        }
        return state;
    }
    public static String validateEditorStart(GameState state) {
        if (state.board.countKings(state.toMove) == 0) return "У стороны, которой ходить, должен быть хотя бы один король.";
        if (!DebtManager.canStart(state))
            return "Нельзя начать: активный долг невозможно исполнить взятием короля.";
        return null;
    }
    public static GameState offerDraw(GameState state) {
        return state.copy(state.board, state.toMove, state.enPassantTarget, state.debtTargetKings,
                state.halfMovesSinceCaptureOrPawn, state.whiteKingSide, state.whiteQueenSide, state.whiteVertical,
                state.blackKingSide, state.blackQueenSide, state.blackVertical, state.result, state.toMove, state.repetitions);
    }
    public static GameState respondDraw(GameState state, boolean accept) {
        if (state.drawOfferBy == null || state.result != GameResult.NONE) return state;
        GameResult result = accept ? GameResult.DRAW_AGREEMENT : state.result;
        return state.copy(state.board, state.toMove, state.enPassantTarget, state.debtTargetKings,
                state.halfMovesSinceCaptureOrPawn, state.whiteKingSide, state.whiteQueenSide, state.whiteVertical,
                state.blackKingSide, state.blackQueenSide, state.blackVertical, result, null, state.repetitions);
    }
    public static GameState claimDraw(GameState state, boolean repetition) {
        boolean valid = repetition ? DrawDetector.canClaimRepetition(state) : DrawDetector.canClaimFiftyMoves(state);
        if (!valid || state.result != GameResult.NONE) return state;
        GameResult result = repetition ? GameResult.DRAW_REPETITION : GameResult.DRAW_FIFTY_MOVES;
        return state.copy(state.board, state.toMove, state.enPassantTarget, state.debtTargetKings,
                state.halfMovesSinceCaptureOrPawn, state.whiteKingSide, state.whiteQueenSide, state.whiteVertical,
                state.blackKingSide, state.blackQueenSide, state.blackVertical, result, null, state.repetitions);
    }
}
