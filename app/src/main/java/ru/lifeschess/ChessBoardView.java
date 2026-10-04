package ru.lifeschess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import java.util.HashSet;
import java.util.Set;
import ru.lifeschess.engine.Board;
import ru.lifeschess.engine.Move;
import ru.lifeschess.engine.Piece;

final class ChessBoardView extends View {
    interface Listener { void onSquare(int square); }
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Board board = new Board();
    private int selected = -1;
    private Set<Integer> targets = new HashSet<>();
    private Listener listener;

    ChessBoardView(Context context) { super(context); setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
    void setListener(Listener l) { listener = l; }
    void show(Board b, int selectedSquare, Iterable<Move> moves) {
        board = b; selected = selectedSquare; targets = new HashSet<>();
        if (moves != null) for (Move m : moves) targets.add(m.to);
        invalidate();
    }

    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        int width = MeasureSpec.getSize(widthSpec);
        int height = MeasureSpec.getSize(heightSpec);
        int size = Math.min(width, height == 0 ? width : height);
        setMeasuredDimension(size, size);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cell = Math.min(getWidth(), getHeight()) / 8f;
        for (int row = 0; row < 8; row++) for (int col = 0; col < 8; col++) {
            int sq = Board.index(row, col);
            paint.setColor(((row + col) & 1) == 0 ? 0xffeee2c8 : 0xff769656);
            canvas.drawRect(col * cell, row * cell, (col + 1) * cell, (row + 1) * cell, paint);
            if (sq == selected) {
                paint.setColor(0x887fc8ff);
                canvas.drawRect(col * cell, row * cell, (col + 1) * cell, (row + 1) * cell, paint);
            } else if (targets.contains(sq)) {
                paint.setColor(0x9950cf70);
                canvas.drawCircle((col + .5f) * cell, (row + .5f) * cell, cell * .13f, paint);
            }
            Piece p = board.at(sq);
            if (p != null) {
                paint.setTypeface(Typeface.create("serif", Typeface.NORMAL));
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(cell * .78f);
                paint.setColor(p.side == ru.lifeschess.engine.Side.WHITE ? 0xfffafafa : 0xff171717);
                paint.setShadowLayer(1.5f, 0, 1, p.side == ru.lifeschess.engine.Side.WHITE ? 0xff222222 : 0xffdddddd);
                canvas.drawText(String.valueOf(p.symbol()), (col + .5f) * cell,
                        row * cell + cell * .81f, paint);
                paint.clearShadowLayer();
            }
        }
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float cell = Math.min(getWidth(), getHeight()) / 8f;
            int col = (int) (event.getX() / cell), row = (int) (event.getY() / cell);
            if (row >= 0 && row < 8 && col >= 0 && col < 8 && listener != null) listener.onSquare(Board.index(row, col));
            return true;
        }
        return true;
    }
}
