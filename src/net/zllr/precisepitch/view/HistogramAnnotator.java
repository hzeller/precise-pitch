package net.zllr.precisepitch.view;

import net.zllr.precisepitch.Histogram;
import net.zllr.precisepitch.model.DisplayNote;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class HistogramAnnotator implements DisplayNote.Annotator {
    private static final Paint borderPaint;
    private static final Paint referenceLine;
    private static final Paint histPaint;
    static {
        // These are always the same and cannot be serialized. Make them static.
        borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStrokeWidth(0);
        borderPaint.setStyle(Paint.Style.STROKE);

        referenceLine = new Paint();
        referenceLine.setColor(Color.BLACK);
        referenceLine.setStrokeWidth(2);

        histPaint = new Paint();
        histPaint.setStrokeWidth(1);
        histPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        histPaint.setAntiAlias(true);
    }

    private boolean twoPlayer;
    private final float drawWidth;
    private final Histogram hist1;
    private final Histogram hist2;

    // Maybe we even want something where we can add an arbitrary number of
    // histograms together with a base color to display. Then we divide the
    // available horizontal space in number of histograms and display them
    // side-by-side.
    // Though: two at a time is probably ok for two players. Doing it generically
    // might be easier to code though.
    public HistogramAnnotator(float drawWidth, Histogram a, Histogram b) {
        hist1 = a;
        hist2 = b;
        twoPlayer = (a != null) && (b != null);
        this.drawWidth = drawWidth;
    }

    public HistogramAnnotator(float drawWidth, Histogram a) {
        this(drawWidth, a, null);
    }

    // Something to set the two player colors.
    public void setHistogramBaseColors(int color_a, int color_b) {
        throw new NoSuchMethodError("not implemented yet :)");
    }

    private static int getBlackBlueColor(double value) {
        int blue = (int)(value * 255.0);
        int redgreen = (int)(value * 0xBB);
        return Color.rgb(redgreen, redgreen, blue);
    }

    private static int getBlackRedColor(double value) {
        int red = (int)(value * 255.0);
        int greenblue = (int)(value * 0xBB);
        return Color.rgb(red, greenblue, greenblue);
    }

    private static int getJetColor(double value) {
        //value from 0.0 to 1.0
        int fourValue = (int)(4.0 * 256.0 * value);
        int red   = Math.min(fourValue - 384, -fourValue + 1152);
        int green = Math.min(fourValue - 128, -fourValue + 896);
        int blue  = Math.min(fourValue + 128, -fourValue + 640);
        red   = Math.min(255, Math.max(0, red));
        green = Math.min(255, Math.max(0, green));
        blue  = Math.min(255, Math.max(0, blue));

        return Color.rgb(red, green, blue);
    }

    public void draw(DisplayNote note, Canvas canvas,
                     RectF staffBoundingBox, RectF noteBoundingBox) {
        float halfWidth = 0.8f * drawWidth;
        // The right side is the stable one, the left side might contain the accidental.
        float centerX = noteBoundingBox.right - halfWidth;

        float histTop = .5f * halfWidth;
        float histBottom = staffBoundingBox.top - (1.2f * halfWidth);
        
        if (noteBoundingBox.top < histBottom) {
            //squeeze histogram box above note
            histBottom = noteBoundingBox.top - (0.2f * halfWidth);
            
            //if we're squeezing too much, move histogram box to bottom
            float boxTopHeight = histBottom - histTop;
            float bottomBoxTop = staffBoundingBox.bottom + (0.2f * halfWidth);
            float bottomBoxBottom = canvas.getHeight() - (0.2f * halfWidth);
            if (boxTopHeight < (bottomBoxBottom - bottomBoxTop)) {
                histTop = bottomBoxTop;
                histBottom = bottomBoxBottom;
            }
        }

        canvas.drawLine(centerX - halfWidth - 20, histTop + (histBottom - histTop) / 2,
                        centerX + halfWidth + 20, histTop + (histBottom - histTop) / 2,
                        referenceLine);
        if (twoPlayer) {
            RectF histBox1 = new RectF(centerX - halfWidth, histTop,
                                 centerX, histBottom);
            RectF histBox2 = new RectF(centerX, histTop,
                                 centerX + halfWidth, histBottom);
            //fill in histograms
            RectF histSlice1 = new RectF(histBox1);
            RectF histSlice2 = new RectF(histBox2);
            for (int i = 0; i < 100; i++) {
                histSlice1.bottom = histBox1.bottom - (i*histBox1.height()/100.0f);
                histSlice1.top = histBox1.bottom - ((i+1)*histBox1.height()/100.0f);
                histSlice2.bottom = histSlice1.bottom;
                histSlice2.top = histSlice1.top;
                
                histPaint.setColor(getBlackBlueColor(hist1.filtered(i)));
                canvas.drawRect(histSlice1, histPaint);
                
                histPaint.setColor(getBlackRedColor(hist2.filtered(i)));
                canvas.drawRect(histSlice2, histPaint);
            }
            canvas.drawRect(histBox1, borderPaint);
            canvas.drawRect(histBox2, borderPaint);
        } else {
            RectF histBox = new RectF(centerX - halfWidth, histTop,
                                 centerX + halfWidth, histBottom);
            //fill in histogram
            RectF histSlice = new RectF(histBox);
            for (int i = 0; i < 100; i++) {
                histSlice.bottom = histBox.bottom - (i*histBox.height()/100.0f);
                histSlice.top = histBox.bottom - ((i+1)*histBox.height()/100.0f);
                
                histPaint.setColor(getBlackBlueColor(hist1.filtered(i)));
                canvas.drawRect(histSlice, histPaint);
            }
            System.out.println("hist draw: (note: " + note.note + ")");
            canvas.drawRect(histBox, borderPaint);
        }
    }
}