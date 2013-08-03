package net.zllr.precisepitch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

// Displays a little sequence of circles, color-filled when 'active'.
// Something like [o o o o o O o o o o o].
//
// The (green) center circle represents frequency being 'in-tune', while the
// (red) off-center circles indicate if and how far off the pitch is.
// It attempts to mimick LEDs in a regular physical tuner.
public class CenterOffsetView extends View {
    private static final int kWidth = 600;  // TODO: make property
    private static final int kHeight = 50;

    private final Paint emptyCirclePaint;
    private final Paint filledRedCirclePaint;
    private final Paint filledGreenCirclePaint;
    private final Paint centAnnotationPaint;

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
        centAnnotationPaint = new Paint();
        centAnnotationPaint.setColor(Color.rgb(255, 255, 255));
        centAnnotationPaint.setTextSize((int)(kHeight * 0.8));
        centAnnotationPaint.setTextAlign(Paint.Align.CENTER);

        setRange(45);
        setQuantization(10);
        setValue(0);
    }

    // Range being displayed. A range of 45 covers -45 .. +45.
    // Default: 45
    public void setRange(double range) {
        if (range == this.range) return;
        this.range = range;
        invalidate();
    }

    // The value to display. Values between +/- range are displayed (0 being
    // the center-'LED'). Out of range leaves the display dark.
    public void setValue(double value) {
        if (value == this.value) return;
        this.value = value;
        invalidate();
    }

    // Quantization of the range. Default: 10
    public void setQuantization(int q) {
        if (q == this.quantization) return;
        if (q < 1) throw new IllegalArgumentException(
                                     "Quantization needs to be >= 1: " + q);
        this.quantization = q;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(kWidth, 2 * kHeight);  // we show text below.
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
            canvas.drawCircle(kWidth/2 + i * widthSteps, kHeight/2,
                              radius, paint);
        }
        if (Math.abs(value) <= range) {
            canvas.drawText(String.format("%+.0fc", value),
                            kWidth/2, kHeight + kHeight/2 + 2,
                            centAnnotationPaint);
        }
    }
}
