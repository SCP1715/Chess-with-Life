package ru.lifeschess.engine;

public final class MoveApplier {
    private MoveApplier() { }

    public static GameState apply(GameState s, Move move, PieceType promotion) {
        Piece moving = s.board.at(move.from);
        if (moving == null) throw new IllegalArgumentException("No piece on source square");
        Piece captured = s.board.at(move.to);
        boolean capture = captured != null;
        boolean pawnMove = moving.type == PieceType.PAWN;
        int ep = -1;
        Board b;
        if (move.kind == Move.Kind.CASTLE_KING || move.kind == Move.Kind.CASTLE_QUEEN
                || move.kind == Move.Kind.CASTLE_VERTICAL) {
            b = CastlingManager.apply(s.board, move);
        } else {
            b = s.board.with(move.from, null).with(move.to, moving.moved());
            if (move.kind == Move.Kind.EN_PASSANT) {
                int victimSquare = move.to - moving.side.pawnDirection() * 8;
                captured = b.at(victimSquare);
                b = b.with(victimSquare, null);
                capture = true;
            }
            if (pawnMove && Math.abs(Board.row(move.to) - Board.row(move.from)) == 2)
                ep = (move.from + move.to) / 2;
            if (pawnMove && Board.row(move.to) == moving.side.promotionRow()) {
                if (promotion == null || promotion == PieceType.PAWN)
                    throw new IllegalArgumentException("Promotion choice required");
                Board beforePromotion = b;
                if (!PromotionLogic.choices(beforePromotion, moving.side).contains(promotion))
                    throw new IllegalArgumentException("Promotion choice is not available");
                b = b.with(move.to, moving.promoted(promotion));
            }
        }

        boolean wK = s.whiteKingSide, wQ = s.whiteQueenSide, wV = s.whiteVertical;
        boolean bK = s.blackKingSide, bQ = s.blackQueenSide, bV = s.blackVertical;
        if (moving.type == PieceType.KING) {
            if (moving.side == Side.WHITE) wK = wQ = wV = false;
            else bK = bQ = bV = false;
        }
        boolean[] rights = moving.type == PieceType.ROOK
                ? clearRookRight(moving.side, move.from, wK, wQ, wV, bK, bQ, bV)
                : new boolean[]{wK, wQ, wV, bK, bQ, bV};
        wK = rights[0]; wQ = rights[1]; wV = rights[2]; bK = rights[3]; bQ = rights[4]; bV = rights[5];
        if (captured != null && captured.type == PieceType.ROOK) {
            rights = clearRookRight(captured.side, move.to, wK, wQ, wV, bK, bQ, bV);
            wK = rights[0]; wQ = rights[1]; wV = rights[2]; bK = rights[3]; bQ = rights[4]; bV = rights[5];
        }
        if (move.kind == Move.Kind.CASTLE_KING) { if (moving.side == Side.WHITE) wK = false; else bK = false; }
        if (move.kind == Move.Kind.CASTLE_QUEEN) { if (moving.side == Side.WHITE) wQ = false; else bQ = false; }
        if (move.kind == Move.Kind.CASTLE_VERTICAL) { if (moving.side == Side.WHITE) wV = false; else bV = false; }

        GameResult result = s.result;
        Side next = moving.side.opposite();
        Side debt = s.debtTargetKings;
        boolean paidDebt = DebtManager.isPaid(s, captured);
        if (paidDebt) debt = null;
        // Taking the final opposing king ends the game before any counter-debt is created.
        if (captured != null && captured.type == PieceType.KING && b.countKings(captured.side) == 0)
            result = moving.side == Side.WHITE ? GameResult.WHITE_WIN : GameResult.BLACK_WIN;
        if (result == GameResult.NONE && promotion == PieceType.KING) debt = moving.side;
        if (result == GameResult.NONE && b.countKings(next) == 0)
            result = moving.side == Side.WHITE ? GameResult.WHITE_WIN : GameResult.BLACK_WIN;
        int half = pawnMove || capture ? 0 : s.halfMovesSinceCaptureOrPawn + 1;
        GameState applied = s.copy(b, next, ep, debt, half, wK, wQ, wV, bK, bQ, bV,
                result, s.drawOfferBy, s.repetitions);
        return DrawDetector.recordPosition(applied);
    }

    private static boolean[] clearRookRight(Side side, int square,
                                             boolean wK, boolean wQ, boolean wV,
                                             boolean bK, boolean bQ, boolean bV) {
        int row = side == Side.WHITE ? 7 : 0;
        if (square == Board.index(row, 7)) { if (side == Side.WHITE) wK = false; else bK = false; }
        if (square == Board.index(row, 0)) { if (side == Side.WHITE) wQ = false; else bQ = false; }
        // The vertical rook starts on the opposing home row, file e.
        if (square == Board.index(side == Side.WHITE ? 0 : 7, 4)) { if (side == Side.WHITE) wV = false; else bV = false; }
        return new boolean[]{wK, wQ, wV, bK, bQ, bV};
    }
}
