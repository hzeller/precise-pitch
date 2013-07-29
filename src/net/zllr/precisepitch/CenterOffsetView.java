package net.zllr.precisepitch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CenterOffsetView extends View {
    private static final int kWidth = 600;  // TODO: make property
    private static final int kHeight = 50;

    private final Paint emptyCirclePaint;
    private final Paint filledRedCirclePaint;
    private final Paint filledGreenCirclePaint;

    private double range = 50;
    private double value;
    private int quantization = 5;

    public CenterOffsetView(Context context) {
        this(context, null);
    }

    public CenterOffsetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO: not doing anything with the XML properties yet.
        emptyCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emptyCirclePaint.setColor(Color.rgb(63, 63, 63));
        emptyCirclePaint.setStrokeWidth(0);
        emptyCirclePaint.setStyle(Paint.Style.STROKE);
        filledRedCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        filledRedCirclePaint.setColor(Color.rgb(200, 0, 0));
        filledRedCirclePaint.setStyle(Paint.Style.FILL);
        filledGreenCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        filledGreenCirclePaint.setColor(Color.rgb(40, 255, 40));
        filledGreenCirclePaint.setStyle(Paint.Style.FILL);
        setRange(45);
        setQuantization(10);
        setValue(0);
    }

    public void setRange(double range) {
        this.range = range;
        invalidate();
    }

    public void setValue(double value) {
        this.value = value;
        invalidate();
    }

    public void setQuantization(int q) {
        if (q < 1) throw new IllegalArgumentException("Quantization needs to be >= 1: " + q);
        this.quantization = q;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(kWidth, 2 * kHeight);  // on top we show text.
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int steps = (int) (range / quantization);
        final int radius = kHeight/2 - 5;
        final int widthSteps = (kWidth/2 - 2 * radius) / steps;
        final int highlightStep = (int) Math.round(value / quantization);
        for (int i = -steps; i <= steps; ++i) {
            Paint paint;
            if (i == 0 && Math.abs(highlightStep) <= 3)
                paint = filledGreenCirclePaint;   // Show always on zero when highlight is in range.
            else if (i != 0 && highlightStep == i)
                paint = filledRedCirclePaint;
            else
                paint = emptyCirclePaint;
            canvas.drawCircle(kWidth/2 + i * widthSteps, kHeight/2, radius, paint);
        }
        if (Math.abs(value) <= range) {
            Paint p = new Paint();
            p.setColor(Color.rgb(255, 255, 255));
            final int textHeight = (int) (kHeight * 0.8);
            p.setTextSize(textHeight);
            p.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(String.format("%.0f", value), kWidth/2, kHeight + kHeight/2 + 2, p);
        }
    }
}
