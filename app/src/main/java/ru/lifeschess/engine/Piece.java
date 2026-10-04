package ru.lifeschess.engine;

import java.io.Serializable;

public final class Piece implements Serializable {
    private static final long serialVersionUID = 1L;
    public final Side side;
    public final PieceType type;
    public final boolean hasMoved;

    public Piece(Side side, PieceType type, boolean hasMoved) {
        if (side == null || type == null) throw new IllegalArgumentException("Piece side/type required");
        this.side = side;
        this.type = type;
        this.hasMoved = hasMoved;
    }

    public Piece moved() { return hasMoved ? this : new Piece(side, type, true); }
    public Piece promoted(PieceType promotion) { return new Piece(side, promotion, false); }

    public char symbol() {
        switch (type) {
            case KING: return side == Side.WHITE ? '\u2654' : '\u265A';
            case QUEEN: return side == Side.WHITE ? '\u2655' : '\u265B';
            case ROOK: return side == Side.WHITE ? '\u2656' : '\u265C';
            case BISHOP: return side == Side.WHITE ? '\u2657' : '\u265D';
            case KNIGHT: return side == Side.WHITE ? '\u2658' : '\u265E';
            default: return side == Side.WHITE ? '\u2659' : '\u265F';
        }
    }
}
