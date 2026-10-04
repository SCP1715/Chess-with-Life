package ru.lifeschess.engine;

import java.util.ArrayList;
import java.util.List;

public final class MoveGenerator {
    private MoveGenerator() { }

    public static List<Move> legalMoves(GameState state, int from) {
        List<Move> moves = new ArrayList<>();
        Piece p = state.board.at(from);
        if (p == null || p.side != state.toMove || state.result != GameResult.NONE) return moves;
        for (int to = 0; to < 64; to++) {
            if (to == from) continue;
            if (isGeometricallyPossible(state, from, to, p)) {
                boolean ep = p.type == PieceType.PAWN && to == state.enPassantTarget
                        && state.board.at(to) == null;
                moves.add(new Move(from, to, ep ? Move.Kind.EN_PASSANT : Move.Kind.NORMAL));
            }
        }
        if (p.type == PieceType.KING) moves.addAll(CastlingManager.moves(state, from));
        if (state.debtTargetKings != null) {
            for (int i = moves.size() - 1; i >= 0; i--)
                if (!DebtManager.isRequiredCapture(state, moves.get(i))) moves.remove(i);
        }
        return moves;
    }

    public static List<Move> allLegalMoves(GameState s) {
        List<Move> all = new ArrayList<>();
        for (int i = 0; i < 64; i++) all.addAll(legalMoves(s, i));
        return all;
    }

    private static boolean isGeometricallyPossible(GameState s, int from, int to, Piece p) {
        int r = Board.row(from), c = Board.col(from), tr = Board.row(to), tc = Board.col(to);
        int dr = tr - r, dc = tc - c;
        Piece target = s.board.at(to);
        if (target != null && target.side == p.side && p.type != PieceType.KING) return false;
        switch (p.type) {
            case PAWN:
                int dir = p.side.pawnDirection();
                if (dc == 0 && dr == dir && target == null) return true;
                if (dc == 0 && dr == 2 * dir && r == p.side.pawnHomeRow() && !p.hasMoved
                        && target == null && s.board.at(r + dir, c) == null) return true;
                if (Math.abs(dc) == 1 && dr == dir) {
                    if (target != null) return true;
                    return to == s.enPassantTarget && isEnPassantVictim(s, p, to);
                }
                return false;
            case KNIGHT: return Math.abs(dr) * Math.abs(dc) == 2;
            case KING: return Math.max(Math.abs(dr), Math.abs(dc)) == 1;
            case BISHOP: return Math.abs(dr) == Math.abs(dc) && clear(s.board, r, c, tr, tc);
            case ROOK: return (dr == 0 || dc == 0) && clear(s.board, r, c, tr, tc);
            case QUEEN: return (dr == 0 || dc == 0 || Math.abs(dr) == Math.abs(dc)) && clear(s.board, r, c, tr, tc);
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

    private static boolean isEnPassantVictim(GameState s, Piece pawn, int to) {
        Piece victim = s.board.at(to - pawn.side.pawnDirection() * 8);
        return victim != null && victim.type == PieceType.PAWN && victim.side != pawn.side;
    }

}
