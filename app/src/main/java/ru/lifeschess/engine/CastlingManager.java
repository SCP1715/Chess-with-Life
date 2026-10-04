package ru.lifeschess.engine;

import java.util.ArrayList;
import java.util.List;

public final class CastlingManager {
    private CastlingManager() { }

    public static List<Move> moves(GameState s, int from) {
        List<Move> out = new ArrayList<>();
        Piece king = s.board.at(from);
        if (king == null || king.type != PieceType.KING || king.hasMoved) return out;
        int row = king.side == Side.WHITE ? 7 : 0;
        if (from != Board.index(row, 4)) return out;
        addIfAvailable(s, from, row, 7, 6, 5, Move.Kind.CASTLE_KING, out);
        addIfAvailable(s, from, row, 0, 2, 3, Move.Kind.CASTLE_QUEEN, out);
        int verticalRookRow = king.side == Side.WHITE ? 0 : 7;
        int verticalToRow = king.side == Side.WHITE ? 5 : 2;
        int rookToRow = king.side == Side.WHITE ? 6 : 1;
        int rookSquare = Board.index(verticalRookRow, 4);
        Piece rook = s.board.at(rookSquare);
        if (s.hasCastleRight(king.side, Move.Kind.CASTLE_VERTICAL) && rook != null
                && rook.side == king.side && rook.type == PieceType.ROOK && !rook.hasMoved
                && pathEmpty(s.board, from, rookSquare)) {
            out.add(new Move(from, Board.index(verticalToRow, 4), Move.Kind.CASTLE_VERTICAL));
        }
        return out;
    }

    private static void addIfAvailable(GameState s, int from, int row, int rookCol,
                                       int kingToCol, int rookToCol, Move.Kind kind, List<Move> out) {
        if (!s.hasCastleRight(s.board.at(from).side, kind)) return;
        Piece rook = s.board.at(row, rookCol);
        if (rook == null || rook.side != s.board.at(from).side || rook.type != PieceType.ROOK || rook.hasMoved) return;
        int step = Integer.compare(rookCol, 4);
        for (int c = 4 + step; c != rookCol; c += step) if (s.board.at(row, c) != null) return;
        out.add(new Move(from, Board.index(row, kingToCol), kind));
    }

    private static boolean pathEmpty(Board b, int from, int rook) {
        int fromRow = Board.row(from), fromCol = Board.col(from);
        int rookRow = Board.row(rook), rookCol = Board.col(rook);
        int rowStep = Integer.compare(rookRow, fromRow), colStep = Integer.compare(rookCol, fromCol);
        int row = fromRow + rowStep, col = fromCol + colStep;
        while (row != rookRow || col != rookCol) {
            if (b.at(row, col) != null) return false;
            row += rowStep; col += colStep;
        }
        return true;
    }

    public static Board apply(Board b, Move m) {
        Piece king = b.at(m.from);
        Board next = b.with(m.from, null).with(m.to, king.moved());
        if (m.kind == Move.Kind.CASTLE_KING) {
            int row = Board.row(m.from); Piece rook = next.at(row, 7);
            return next.with(row, 7, null).with(row, 5, rook.moved());
        }
        if (m.kind == Move.Kind.CASTLE_QUEEN) {
            int row = Board.row(m.from); Piece rook = next.at(row, 0);
            return next.with(row, 0, null).with(row, 3, rook.moved());
        }
        if (m.kind == Move.Kind.CASTLE_VERTICAL) {
            int row = king.side == Side.WHITE ? 0 : 7;
            int to = king.side == Side.WHITE ? 6 : 1;
            Piece rook = next.at(row, 4);
            return next.with(row, 4, null).with(to, 4, rook.moved());
        }
        return next;
    }
}
