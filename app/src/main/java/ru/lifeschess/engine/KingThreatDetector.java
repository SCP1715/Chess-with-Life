package ru.lifeschess.engine;

/** Geometric attacks only; these are used for the special king-promotion rule. */
public final class KingThreatDetector {
    private KingThreatDetector() { }

    public static boolean isSquareAttacked(Board board, int square, Side bySide) {
        int tr = Board.row(square), tc = Board.col(square);
        for (int from = 0; from < 64; from++) {
            Piece p = board.at(from);
            if (p != null && p.side == bySide && attacks(board, from, tr, tc, p)) return true;
        }
        return false;
    }

    public static boolean hasKingUnderAttack(Board board, Side side) {
        for (int i = 0; i < 64; i++) {
            Piece p = board.at(i);
            if (p != null && p.side == side && p.type == PieceType.KING
                    && isSquareAttacked(board, i, side.opposite())) return true;
        }
        return false;
    }

    static boolean attacks(Board b, int from, int tr, int tc, Piece p) {
        int r = Board.row(from), c = Board.col(from), dr = tr - r, dc = tc - c;
        switch (p.type) {
            case PAWN: return dr == (p.side == Side.WHITE ? -1 : 1) && Math.abs(dc) == 1;
            case KNIGHT: return Math.abs(dr) * Math.abs(dc) == 2;
            case KING: return Math.max(Math.abs(dr), Math.abs(dc)) == 1;
            case BISHOP: return Math.abs(dr) == Math.abs(dc) && clear(b, r, c, tr, tc);
            case ROOK: return (dr == 0 || dc == 0) && clear(b, r, c, tr, tc);
            case QUEEN: return (dr == 0 || dc == 0 || Math.abs(dr) == Math.abs(dc)) && clear(b, r, c, tr, tc);
            default: return false;
        }
    }

    private static boolean clear(Board b, int r, int c, int tr, int tc) {
        int sr = Integer.compare(tr, r), sc = Integer.compare(tc, c);
        r += sr; c += sc;
        while (r != tr || c != tc) {
            if (b.at(r, c) != null) return false;
            r += sr; c += sc;
        }
        return true;
    }
}
