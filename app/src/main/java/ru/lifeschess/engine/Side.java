package ru.lifeschess.engine;

import java.io.Serializable;

public enum Side implements Serializable {
    WHITE, BLACK;

    public Side opposite() { return this == WHITE ? BLACK : WHITE; }
    public int pawnDirection() { return this == WHITE ? -1 : 1; }
    public int homeRow() { return this == WHITE ? 7 : 0; }
    public int pawnHomeRow() { return this == WHITE ? 6 : 1; }
    public int promotionRow() { return this == WHITE ? 0 : 7; }
}
