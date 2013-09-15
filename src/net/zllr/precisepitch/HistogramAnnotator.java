package net.zllr.precisepitch;

import net.zllr.precisepitch.model.DisplayNote;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class HistogramAnnotator implements DisplayNote.Annotator {
    private final boolean twoPlayer;
    private final Histogram hist1;
    private final Histogram hist2;
    private final Paint borderPaint;
    private final Paint histPaint;
    
    public HistogramAnnotator(Histogram player1, Histogram player2) {
        twoPlayer = (player2 != null);
        hist1 = player1;
        hist2 = player2;
        
        borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStrokeWidth(0);
        borderPaint.setStyle(Paint.Style.STROKE);
        
        histPaint = new Paint();
        histPaint.setStrokeWidth(0);
        histPaint.setAntiAlias(true);
    }
    
    public HistogramAnnotator(Histogram player1) {
        this(player1, null);
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
                
                histPaint.setColor(getJetColor(hist1.normalized(note.note, i - 50)));
                canvas.drawRect(histSlice1, histPaint);
                
                histPaint.setColor(getJetColor(hist2.normalized(note.note, i - 50)));
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
                
                histPaint.setColor(getJetColor(hist1.normalized(note.note, i - 50)));
                canvas.drawRect(histSlice, histPaint);
            }
            System.out.println("hist draw: (note: " + note.note + ")");
            canvas.drawRect(histBox, borderPaint);
        }
    }
}