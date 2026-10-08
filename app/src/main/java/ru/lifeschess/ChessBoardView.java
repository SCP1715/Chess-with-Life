package ru.lifeschess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import java.util.Set;
import ru.lifeschess.engine.Board;
import ru.lifeschess.engine.Move;
import ru.lifeschess.engine.Piece;

final class ChessBoardView extends View {
    interface Listener { void onSquare(int square); }
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Board board = new Board();
    private int selected = -1;
    private Set<Integer> targets = new java.util.HashSet<>();
    private Set<Integer> captureTargets = new java.util.HashSet<>();
    private Listener listener;
    private boolean flipped;

    ChessBoardView(Context context) { super(context); setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
    void setListener(Listener l) { listener = l; }
    void setFlipped(boolean value) { flipped = value; invalidate(); }
    void show(Board b, int selectedSquare, Iterable<Move> moves) {
        board = b; selected = selectedSquare;
        MoveHighlights highlights = MoveHighlights.from(b, moves);
        targets = highlights.targets;
        captureTargets = highlights.captures;
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
        for (int viewRow = 0; viewRow < 8; viewRow++) for (int viewCol = 0; viewCol < 8; viewCol++) {
            int row = GamePresentation.boardCoordinate(viewRow, flipped);
            int col = GamePresentation.boardCoordinate(viewCol, flipped);
            int sq = Board.index(row, col);
            paint.setColor(((row + col) & 1) == 0 ? 0xffeee2c8 : 0xff769656);
            canvas.drawRect(viewCol * cell, viewRow * cell, (viewCol + 1) * cell,
                    (viewRow + 1) * cell, paint);
            if (sq == selected) {
                paint.setColor(0x887fc8ff);
                canvas.drawRect(viewCol * cell, viewRow * cell, (viewCol + 1) * cell,
                        (viewRow + 1) * cell, paint);
            } else if (targets.contains(sq)) {
                if (captureTargets.contains(sq)) {
                    paint.setColor(0x5530a050);
                    canvas.drawRect(viewCol * cell, viewRow * cell, (viewCol + 1) * cell,
                            (viewRow + 1) * cell, paint);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(cell * .075f);
                    paint.setColor(0xff392b1f);
                    canvas.drawCircle((viewCol + .5f) * cell, (viewRow + .5f) * cell,
                            cell * .39f, paint);
                    paint.setStrokeWidth(cell * .045f);
                    paint.setColor(0xffffa000);
                    canvas.drawCircle((viewCol + .5f) * cell, (viewRow + .5f) * cell,
                            cell * .39f, paint);
                    paint.setStyle(Paint.Style.FILL);
                } else {
                    paint.setColor(0xdd168b3b);
                    canvas.drawCircle((viewCol + .5f) * cell, (viewRow + .5f) * cell,
                            cell * .13f, paint);
                }
            }
            Piece p = board.at(sq);
            if (p != null) {
                paint.setTypeface(Typeface.create("serif", Typeface.NORMAL));
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(cell * .78f);
                boolean white = p.side == ru.lifeschess.engine.Side.WHITE;
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeJoin(Paint.Join.ROUND);
                paint.setStrokeWidth(cell * .035f);
                paint.setColor(white ? 0xff30291f : 0xfff5e8cf);
                canvas.drawText(String.valueOf(p.symbol()), (viewCol + .5f) * cell,
                        viewRow * cell + cell * .81f, paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(white ? 0xffffffff : 0xff171717);
                paint.setShadowLayer(1.5f, 0, 1, white ? 0xff222222 : 0xffdddddd);
                canvas.drawText(String.valueOf(p.symbol()), (viewCol + .5f) * cell,
                        viewRow * cell + cell * .81f, paint);
                paint.clearShadowLayer();
            }
        }
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float cell = Math.min(getWidth(), getHeight()) / 8f;
            int viewCol = (int) (event.getX() / cell), viewRow = (int) (event.getY() / cell);
            if (viewRow >= 0 && viewRow < 8 && viewCol >= 0 && viewCol < 8 && listener != null) {
                int row = GamePresentation.boardCoordinate(viewRow, flipped);
                int col = GamePresentation.boardCoordinate(viewCol, flipped);
                listener.onSquare(Board.index(row, col));
            }
            return true;
        }
        return true;
    }
}
