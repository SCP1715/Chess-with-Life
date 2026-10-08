package ru.lifeschess;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import ru.lifeschess.engine.Board;
import ru.lifeschess.engine.GameState;
import ru.lifeschess.engine.Piece;
import ru.lifeschess.engine.PieceType;
import ru.lifeschess.engine.Side;

final class MaterialSummary extends LinearLayout {
    MaterialSummary(Context context, GameState state, boolean boardFlipped) {
        super(context);
        setOrientation(VERTICAL);
        setPadding(dp(context, 4), dp(context, 2), dp(context, 4), dp(context, 2));

        int advantage = material(state.board, Side.WHITE) - material(state.board, Side.BLACK);
        addRow(context, state, boardFlipped ? Side.WHITE : Side.BLACK, advantage);
        addRow(context, state, boardFlipped ? Side.BLACK : Side.WHITE, advantage);
    }

    private void addRow(Context context, GameState state, Side side, int advantage) {
        TextView row = new TextView(context);
        row.setTextSize(16);
        row.setTypeface(Typeface.create("serif", Typeface.NORMAL));
        String advantageText = advantage == 0 || Integer.signum(advantage)
                != (side == Side.WHITE ? 1 : -1) ? ""
                : getResources().getString(R.string.material_advantage, Math.abs(advantage));
        row.setText(getResources().getString(R.string.material_summary_row,
                getResources().getString(side == Side.WHITE ? R.string.side_white_title
                        : R.string.side_black_title), capturedSymbols(state.capturedPieces(side)),
                advantageText));
        addView(row, new LayoutParams(-1, -2));
    }

    static int material(Board board, Side side) {
        int total = 0;
        for (int square = 0; square < 64; square++) {
            Piece piece = board.at(square);
            if (piece != null && piece.side == side) total += value(piece.type);
        }
        return total;
    }

    private static String capturedSymbols(List<Piece> pieces) {
        if (pieces.isEmpty()) return "";
        List<Piece> sorted = new ArrayList<>(pieces);
        Collections.sort(sorted, new Comparator<Piece>() {
            @Override public int compare(Piece first, Piece second) {
                return value(second.type) - value(first.type);
            }
        });
        StringBuilder symbols = new StringBuilder();
        for (Piece piece : sorted) symbols.append(piece.symbol()).append(' ');
        return symbols.toString();
    }

    private static int value(PieceType type) {
        switch (type) {
            case QUEEN: return 9;
            case ROOK: return 5;
            case BISHOP:
            case KNIGHT: return 3;
            case PAWN: return 1;
            case KING:
            default: return 0;
        }
    }

    private static int dp(Context context, int value) {
        return (int) (context.getResources().getDisplayMetrics().density * value + .5f);
    }
}
