package ru.lifeschess;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;
import ru.lifeschess.engine.Board;
import ru.lifeschess.engine.Move;

/** Game surface: status and board. Move legality remains owned by the engine. */
final class GameScreen extends LinearLayout {
    final TextView status;
    final ChessBoardView board;
    GameScreen(Context context, Board position, int selected, Iterable<Move> legalMoves,
               ChessBoardView.Listener listener, String statusText) {
        super(context); setOrientation(VERTICAL);
        status = new TextView(context); status.setText(statusText); status.setTextSize(16);
        status.setTextColor(0xff242424); status.setGravity(Gravity.CENTER);
        addView(status, new LayoutParams(-1, -2));
        board = new ChessBoardView(context); board.setListener(listener);
        board.show(position, selected, legalMoves);
        addView(board, new LayoutParams(-1, 0, 1));
    }
}
