package net.zllr.precisepitch;

import net.zllr.precisepitch.model.DisplayNote;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class HistogramAnnotator implements DisplayNote.Annotator {
    private boolean twoPlayer;
    public Histogram hist1;
    public Histogram hist2;
    private final Paint borderPaint;
    private final Paint histPaint;
    
    
    public HistogramAnnotator() {
        twoPlayer = false;
        hist1 = new Histogram(100);
        hist2 = null;
        
        borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStrokeWidth(0);
        borderPaint.setStyle(Paint.Style.STROKE);
        
        histPaint = new Paint();
        histPaint.setStrokeWidth(0);
        histPaint.setAntiAlias(true);
    }
    
    public void setTwoPlayer(boolean isTwoPlayer) {
        if (isTwoPlayer) {
            if (hist2 == null) {
                hist2 = new Histogram(100);
            }
        } else {
            hist2 = null;
        }
        twoPlayer = isTwoPlayer;
    }
    
    public void draw(DisplayNote note, Canvas canvas,
                     RectF staffBoundingBox, RectF noteBoundingBox) {
        
        float halfWidth = (noteBoundingBox.right - noteBoundingBox.left) / 2;
        float centerX = noteBoundingBox.left + halfWidth;
        
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
                
                histPaint.setColor(Histogram.getBlackBlueColor(hist1.filtered(i)));
                canvas.drawRect(histSlice1, histPaint);
                
                histPaint.setColor(Histogram.getBlackRedColor(hist2.filtered(i)));
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
                
                histPaint.setColor(Histogram.getBlackBlueColor(hist1.filtered(i)));
                canvas.drawRect(histSlice, histPaint);
            }
            System.out.println("hist draw: (note: " + note.note + ")");
            canvas.drawRect(histBox, borderPaint);
        }
    }
}