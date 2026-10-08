package ru.lifeschess;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Locale;
import java.util.List;
import ru.lifeschess.engine.Board;
import ru.lifeschess.engine.Move;
import ru.lifeschess.engine.Side;

/** Game surface: status and board. Move legality remains owned by the engine. */
final class GameScreen extends LinearLayout {
    final TextView status;
    final ChessBoardView board;
    final EvaluationBar evaluationBar;
    private final TextView upperEvaluation, lowerEvaluation;
    private final boolean boardFlipped;
    GameScreen(Context context, Board position, int selected, Iterable<Move> legalMoves,
               ChessBoardView.Listener listener, String statusText, boolean boardFlipped,
               boolean showEvaluation) {
        super(context); setOrientation(VERTICAL);
        status = new TextView(context); status.setText(statusText); status.setTextSize(16);
        status.setTextColor(0xff242424); status.setGravity(Gravity.CENTER);
        addView(status, new LayoutParams(-1, -2));
        this.boardFlipped = boardFlipped;
        upperEvaluation = showEvaluation ? evaluationLabel(context) : null;
        lowerEvaluation = showEvaluation ? evaluationLabel(context) : null;
        if (upperEvaluation != null) addView(upperEvaluation, new LayoutParams(-1, -2));
        LinearLayout playArea = new LinearLayout(context);
        playArea.setOrientation(HORIZONTAL);
        playArea.setGravity(Gravity.CENTER_VERTICAL);
        board = new ChessBoardView(context); board.setListener(listener);
        board.setFlipped(boardFlipped);
        board.show(position, selected, legalMoves);
        playArea.addView(board, new LayoutParams(0, -1, 1));
        evaluationBar = showEvaluation ? new EvaluationBar(context) : null;
        if (evaluationBar != null) {
            evaluationBar.setFlipped(boardFlipped);
            int width = (int) (context.getResources().getDisplayMetrics().density * 24 + .5f);
            LayoutParams barParams = new LayoutParams(width, -1);
            barParams.leftMargin = (int) (context.getResources().getDisplayMetrics().density * 6 + .5f);
            playArea.addView(evaluationBar, barParams);
            playArea.addOnLayoutChangeListener((view, left, top, right, bottom,
                                                oldLeft, oldTop, oldRight, oldBottom) -> {
                int boardHeight = board.getHeight();
                if (boardHeight > 0 && evaluationBar.getLayoutParams().height != boardHeight) {
                    LayoutParams current = (LayoutParams) evaluationBar.getLayoutParams();
                    current.height = boardHeight;
                    evaluationBar.setLayoutParams(current);
                }
            });
        }
        addView(playArea, new LayoutParams(-1, 0, 1));
        if (lowerEvaluation != null) addView(lowerEvaluation, new LayoutParams(-1, -2));
    }

    void setEvaluation(int whiteScore, boolean mate) {
        if (evaluationBar == null) return;
        evaluationBar.setEvaluation(whiteScore, mate);
        int upperSide = boardFlipped ? R.string.side_white_title : R.string.side_black_title;
        int lowerSide = boardFlipped ? R.string.side_black_title : R.string.side_white_title;
        int upperScore = boardFlipped ? whiteScore : -whiteScore;
        int lowerScore = -upperScore;
        upperEvaluation.setText(evaluationLabelText(upperSide, upperScore, mate));
        lowerEvaluation.setText(evaluationLabelText(lowerSide, lowerScore, mate));
    }

    void setEvaluationCalculating() {
        if (evaluationBar == null) return;
        evaluationBar.setCalculating();
        upperEvaluation.setText(evaluationLabelText(upperSide(),
                getContext().getString(R.string.evaluation_short_calculating)));
        lowerEvaluation.setText(evaluationLabelText(lowerSide(),
                getContext().getString(R.string.evaluation_short_calculating)));
    }

    void setEvaluationUnavailable() {
        if (evaluationBar == null) return;
        evaluationBar.setUnavailable();
        upperEvaluation.setText(evaluationLabelText(upperSide(),
                getContext().getString(R.string.evaluation_short_unavailable)));
        lowerEvaluation.setText(evaluationLabelText(lowerSide(),
                getContext().getString(R.string.evaluation_short_unavailable)));
    }

    private TextView evaluationLabel(Context context) {
        TextView label = new TextView(context);
        label.setTextSize(12);
        label.setTextColor(0xff242424);
        label.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        return label;
    }

    private String evaluationLabelText(int sideResource, int score, boolean mate) {
        String value = mate ? getContext().getString(R.string.evaluation_short_mate)
                : String.format(Locale.ROOT, "%+.2f", score / 100.0);
        return evaluationLabelText(sideResource, value);
    }

    private String evaluationLabelText(int sideResource, String value) {
        return getContext().getString(R.string.evaluation_side_score,
                getContext().getString(sideResource), value);
    }

    private int upperSide() {
        return boardFlipped ? R.string.side_white_title : R.string.side_black_title;
    }

    private int lowerSide() {
        return boardFlipped ? R.string.side_black_title : R.string.side_white_title;
    }
}
