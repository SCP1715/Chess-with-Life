package ru.lifeschess.engine;

public final class PositionKey {
    private PositionKey() { }
    public static String of(GameState s) {
        StringBuilder b = new StringBuilder(100);
        for (int i = 0; i < 64; i++) {
            Piece p = s.board.at(i);
            boolean pawnFirstMove = p != null && p.type == PieceType.PAWN
                    && Board.row(i) == p.side.pawnHomeRow() && !p.hasMoved;
            if (p == null) b.append('.');
            else b.append(p.side == Side.WHITE ? 'w' : 'b').append(p.type.ordinal())
                    .append(pawnFirstMove ? 'f' : 'm');
        }
        b.append('|').append(s.toMove).append('|').append(s.debtTargetKings);
        b.append('|').append(s.whiteKingSide).append(s.whiteQueenSide).append(s.whiteVertical)
                .append(s.blackKingSide).append(s.blackQueenSide).append(s.blackVertical);
        int ep = s.enPassantTarget;
        if (ep >= 0) {
            boolean capturable = false;
            int row = Board.row(ep), col = Board.col(ep);
            int sourceRow = row - s.toMove.pawnDirection();
            for (int dc : new int[]{-1, 1}) {
                Piece p = s.board.at(sourceRow, col + dc);
                Piece victim = s.board.at(ep - s.toMove.pawnDirection() * 8);
                if (p != null && p.side == s.toMove && p.type == PieceType.PAWN
                        && victim != null && victim.side != s.toMove && victim.type == PieceType.PAWN)
                    capturable = true;
            }
            if (!capturable) ep = -1;
        }
        b.append('|').append(ep);
        return b.toString();
    }
}
