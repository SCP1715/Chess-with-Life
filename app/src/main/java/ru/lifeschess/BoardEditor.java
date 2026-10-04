package ru.lifeschess;

import java.io.Serializable;
import ru.lifeschess.engine.Board;
import ru.lifeschess.engine.Piece;
import ru.lifeschess.engine.PieceType;
import ru.lifeschess.engine.Side;

final class BoardEditor implements Serializable {
    private static final long serialVersionUID = 1L;
    Board board = new Board();
    Side turn = Side.WHITE;
    Side debt;
    Side paletteSide = Side.WHITE;
    PieceType paletteType = PieceType.KING;
    boolean paletteErase;
    boolean moved;
    int enPassant = -1;
    boolean wK, wQ, wV, bK, bQ, bV;

    void place(int square) {
        if (paletteErase) board = board.with(square, null);
        else board = board.with(square, new Piece(paletteSide, paletteType, moved));
    }
}
