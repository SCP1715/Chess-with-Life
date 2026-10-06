package ru.lifeschess.engine;

/** Converts an app position to the lossless LifeChess FEN understood by Fairy-Stockfish. */
public final class LifeChessFen {
    private LifeChessFen() { }

    public static String encode(GameState state) {
        return encode(state, state == null ? null : state.toMove);
    }

    /** Encodes the current board for an engine-only decision without changing game state. */
    public static String encode(GameState state, Side searchSideToMove) {
        if (state == null) throw new IllegalArgumentException("Game state is required");
        if (searchSideToMove == null) throw new IllegalArgumentException("Search side is required");
        StringBuilder fen = new StringBuilder(128);
        long movedPawns = 0L;
        for (int row = 0; row < 8; row++) {
            if (row != 0) fen.append('/');
            int empty = 0;
            for (int col = 0; col < 8; col++) {
                int square = Board.index(row, col);
                Piece piece = state.board.at(square);
                if (piece == null) {
                    empty++;
                    continue;
                }
                if (empty != 0) {
                    fen.append(empty);
                    empty = 0;
                }
                char symbol = symbol(piece.type);
                fen.append(piece.side == Side.WHITE ? Character.toUpperCase(symbol) : symbol);
                if (piece.type == PieceType.PAWN && piece.hasMoved) {
                    int fenSquare = col + (7 - row) * 8;
                    movedPawns |= 1L << fenSquare;
                }
            }
            if (empty != 0) fen.append(empty);
        }

        fen.append(searchSideToMove == Side.WHITE ? " w " : " b ");
        StringBuilder rights = new StringBuilder(6);
        if (state.whiteKingSide) rights.append('K');
        if (state.whiteQueenSide) rights.append('Q');
        if (state.whiteVertical) rights.append('V');
        if (state.blackKingSide) rights.append('k');
        if (state.blackQueenSide) rights.append('q');
        if (state.blackVertical) rights.append('v');
        fen.append(rights.length() == 0 ? "-" : rights.toString()).append(' ');
        fen.append(state.enPassantTarget < 0 ? "-" : squareName(state.enPassantTarget));
        fen.append(' ').append(state.halfMovesSinceCaptureOrPawn);
        fen.append(' ').append(1 + state.history.size() / 2);
        fen.append(" lc1:").append(state.debtTargetKings == null ? '-' :
                state.debtTargetKings == Side.WHITE ? 'w' : 'b');
        fen.append(" lm1:").append(String.format(java.util.Locale.ROOT, "%016x", movedPawns));
        return fen.toString();
    }

    public static String encodeMove(GameAction action) {
        if (action == null || action.type != GameAction.Type.MOVE)
            throw new IllegalArgumentException("A board move is required");
        String move = squareName(action.from) + squareName(action.to);
        if (action.promotion != null) {
            switch (action.promotion) {
                case KING: move += "k"; break;
                case QUEEN: move += "q"; break;
                case ROOK: move += "r"; break;
                case BISHOP: move += "b"; break;
                case KNIGHT: move += "n"; break;
                default: throw new IllegalArgumentException("Invalid promotion type");
            }
        }
        return move;
    }

    private static char symbol(PieceType type) {
        switch (type) {
            case KING: return 'k';
            case QUEEN: return 'q';
            case ROOK: return 'r';
            case BISHOP: return 'b';
            case KNIGHT: return 'n';
            case PAWN: return 'p';
            default: throw new IllegalArgumentException("Unsupported piece type: " + type);
        }
    }

    private static String squareName(int square) {
        if (!Board.isValid(square)) throw new IllegalArgumentException("Invalid en passant square");
        return "" + (char) ('a' + Board.col(square)) + (8 - Board.row(square));
    }
}
