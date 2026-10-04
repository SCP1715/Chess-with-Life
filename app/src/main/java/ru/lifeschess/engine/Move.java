package ru.lifeschess.engine;

import java.io.Serializable;

public final class Move implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Kind { NORMAL, EN_PASSANT, CASTLE_KING, CASTLE_QUEEN, CASTLE_VERTICAL }
    public final int from;
    public final int to;
    public final Kind kind;
    public Move(int from, int to) { this(from, to, Kind.NORMAL); }
    public Move(int from, int to, Kind kind) { this.from = from; this.to = to; this.kind = kind; }
    @Override public boolean equals(Object other) {
        if (!(other instanceof Move)) return false;
        Move m = (Move) other;
        return from == m.from && to == m.to && kind == m.kind;
    }
    @Override public int hashCode() { return (from * 67 + to) * 7 + kind.ordinal(); }
}
