package ru.lifeschess.engine;

import java.io.Serializable;

/** A complete action in the LifeChess protocol, including promotion choice. */
public final class GameAction implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        MOVE,
        CLAIM_REPETITION,
        CLAIM_FIFTY_MOVES,
        OFFER_DRAW,
        ACCEPT_DRAW,
        DECLINE_DRAW,
        // Append new values to preserve the ordinals of legacy serialized actions.
        CLAIM_BARE_KINGS
    }

    public final Type type;
    public final int from;
    public final int to;
    public final Move.Kind moveKind;
    public final PieceType promotion;

    private GameAction(Type type, int from, int to, Move.Kind moveKind, PieceType promotion) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.moveKind = moveKind;
        this.promotion = promotion;
    }

    public static GameAction move(Move move, PieceType promotion) {
        if (move == null) throw new IllegalArgumentException("Move required");
        if (promotion == PieceType.PAWN) throw new IllegalArgumentException("Cannot promote to pawn");
        return new GameAction(Type.MOVE, move.from, move.to, move.kind, promotion);
    }

    public static GameAction procedure(Type type) {
        if (type == null || type == Type.MOVE)
            throw new IllegalArgumentException("A draw-procedure action is required");
        return new GameAction(type, -1, -1, null, null);
    }

    public Move asMove() {
        if (type != Type.MOVE) throw new IllegalStateException("Action is not a move");
        return new Move(from, to, moveKind);
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof GameAction)) return false;
        GameAction a = (GameAction) other;
        return type == a.type && from == a.from && to == a.to
                && moveKind == a.moveKind && promotion == a.promotion;
    }

    @Override public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + from;
        result = 31 * result + to;
        result = 31 * result + (moveKind == null ? 0 : moveKind.hashCode());
        result = 31 * result + (promotion == null ? 0 : promotion.hashCode());
        return result;
    }
}
