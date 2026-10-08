package ru.lifeschess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

final class EvaluationBar extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float whiteShare = .5f;
    private boolean flipped;

    EvaluationBar(Context context) {
        super(context);
        setCalculating();
    }

    void setEvaluation(int whiteScore, boolean mate) {
        whiteShare = GamePresentation.whiteShare(whiteScore, mate);
        if (mate) {
            String side = getContext().getString(whiteScore >= 0
                    ? R.string.side_white_title : R.string.side_black_title);
            setContentDescription(getContext().getString(R.string.evaluation_bar_mate, side));
        } else {
            String value = String.format(java.util.Locale.ROOT, "%+.2f", whiteScore / 100.0);
            setContentDescription(getContext().getString(R.string.evaluation_bar_description,
                    value));
        }
        invalidate();
    }

    void setFlipped(boolean value) {
        flipped = value;
        invalidate();
    }

    void setCalculating() {
        setContentDescription(getContext().getString(R.string.evaluation_bar_calculating));
        invalidate();
    }

    void setUnavailable() {
        setContentDescription(getContext().getString(R.string.evaluation_bar_unavailable));
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        if (width <= 0 || height <= 0) return;
        paint.setStyle(Paint.Style.FILL);
        float split = flipped ? height * whiteShare : height * (1f - whiteShare);
        paint.setColor(flipped ? 0xfff7f3e9 : 0xff171717);
        canvas.drawRect(0, 0, width, split, paint);
        paint.setColor(flipped ? 0xff171717 : 0xfff7f3e9);
        canvas.drawRect(0, split, width, height, paint);
        paint.setColor(0xffd6a742);
        canvas.drawRect(0, height * .5f - dp(1), width, height * .5f + dp(1), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f, width * .06f));
        paint.setColor(0xff555555);
        canvas.drawRect(0, 0, width, height, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private float dp(int value) {
        return getResources().getDisplayMetrics().density * value;
    }
}
