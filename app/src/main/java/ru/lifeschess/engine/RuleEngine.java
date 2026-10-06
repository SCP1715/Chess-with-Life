package ru.lifeschess.engine;

import java.util.List;
import java.util.ArrayList;

public final class RuleEngine {
    private RuleEngine() { }
    public static List<Move> legalMoves(GameState state, int from) { return MoveGenerator.legalMoves(state, from); }

    /** Returns complete protocol actions; promotions are separate actions. */
    public static List<GameAction> legalActions(GameState state) {
        List<GameAction> actions = new ArrayList<>();
        if (state.result != GameResult.NONE) return actions;

        for (Move move : MoveGenerator.allLegalMoves(state)) {
            Piece piece = state.board.at(move.from);
            if (piece != null && piece.type == PieceType.PAWN
                    && Board.row(move.to) == piece.side.promotionRow()) {
                Board afterPawnMove = state.board.with(move.from, null)
                        .with(move.to, piece.moved());
                for (PieceType promotion : PromotionLogic.choices(afterPawnMove, piece.side))
                    actions.add(GameAction.move(move, promotion));
            } else {
                actions.add(GameAction.move(move, null));
            }
        }

        if (DrawDetector.canClaimRepetition(state))
            actions.add(GameAction.procedure(GameAction.Type.CLAIM_REPETITION));
        if (DrawDetector.canClaimFiftyMoves(state))
            actions.add(GameAction.procedure(GameAction.Type.CLAIM_FIFTY_MOVES));
        if (DrawDetector.canClaimBareKings(state))
            actions.add(GameAction.procedure(GameAction.Type.CLAIM_BARE_KINGS));

        if (state.drawOfferBy != null) {
            actions.add(GameAction.procedure(GameAction.Type.ACCEPT_DRAW));
            actions.add(GameAction.procedure(GameAction.Type.DECLINE_DRAW));
        } else if (state.drawOfferBy == null) {
            actions.add(GameAction.procedure(GameAction.Type.OFFER_DRAW));
        }
        return actions;
    }

    /** Applies only an exact complete action currently offered by legalActions(). */
    public static GameState applyAction(GameState state, GameAction action) {
        if (action == null || !legalActions(state).contains(action))
            throw new IllegalArgumentException("Action is not legal in this position");
        switch (action.type) {
            case MOVE:
                return MoveApplier.apply(state, action.asMove(), action.promotion);
            case CLAIM_REPETITION:
                return claimDraw(state, true).withHistoryAction(action);
            case CLAIM_FIFTY_MOVES:
                return claimDraw(state, false).withHistoryAction(action);
            case CLAIM_BARE_KINGS:
                return claimBareKings(state).withHistoryAction(action);
            case OFFER_DRAW:
                return offerDraw(state).withHistoryAction(action);
            case ACCEPT_DRAW:
                return respondDraw(state, true).withHistoryAction(action);
            case DECLINE_DRAW:
                return respondDraw(state, false).withHistoryAction(action);
            default:
                throw new IllegalArgumentException("Unsupported action type");
        }
    }

    public static GameState play(GameState state, Move move, PieceType promotion) {
        return applyAction(state, GameAction.move(move, promotion));
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

    public static GameState claimBareKings(GameState state) {
        if (!DrawDetector.canClaimBareKings(state)) return state;
        return state.copy(state.board, state.toMove, state.enPassantTarget, state.debtTargetKings,
                state.halfMovesSinceCaptureOrPawn, state.whiteKingSide, state.whiteQueenSide,
                state.whiteVertical, state.blackKingSide, state.blackQueenSide,
                state.blackVertical, GameResult.DRAW_BARE_KINGS, null, state.repetitions);
    }
}
