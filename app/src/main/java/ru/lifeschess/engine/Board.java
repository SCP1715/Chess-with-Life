package ru.lifeschess.engine;

import java.io.Serializable;
import java.util.Arrays;

public final class Board implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Piece[] cells;

    public Board() { cells = new Piece[64]; }
    private Board(Piece[] cells) { this.cells = cells; }

    public Piece at(int square) { return isValid(square) ? cells[square] : null; }
    public Piece at(int row, int col) {
        return row < 0 || row > 7 || col < 0 || col > 7 ? null : cells[index(row, col)];
    }
    public Board with(int square, Piece piece) {
        if (!isValid(square)) throw new IllegalArgumentException("Square outside board");
        Piece[] copy = Arrays.copyOf(cells, cells.length);
        copy[square] = piece;
        return new Board(copy);
    }
    public Board with(int row, int col, Piece piece) { return with(index(row, col), piece); }
    public Board copy() { return new Board(Arrays.copyOf(cells, cells.length)); }
    public int countKings(Side side) {
        int count = 0;
        for (Piece p : cells) if (p != null && p.side == side && p.type == PieceType.KING) count++;
        return count;
    }
    public int kingSquares(Side side, int[] output) {
        int count = 0;
        for (int i = 0; i < cells.length; i++) {
            Piece p = cells[i];
            if (p != null && p.side == side && p.type == PieceType.KING) {
                if (output != null && count < output.length) output[count] = i;
                count++;
            }
        }
        return count;
    }
    public static int index(int row, int col) { return row * 8 + col; }
    public static int row(int square) { return square / 8; }
    public static int col(int square) { return square % 8; }
    public static boolean isValid(int square) { return square >= 0 && square < 64; }
}
