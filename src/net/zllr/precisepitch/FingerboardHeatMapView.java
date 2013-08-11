package net.zllr.precisepitch;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * View containing a cello fingerboard with a heat map depicting a distribution
 * of where notes were played.
 */
public class FingerboardHeatMapView extends View {
    
    private Paint p;
    
    public FingerboardHeatMapView(Context context) {
        super(context);
        init();
    }
    
    public FingerboardHeatMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public FingerboardHeatMapView(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    void init() {
        p = new Paint();
        p.setColor(Color.RED);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        //canvas.drawColor(Color.WHITE);
        
        //canvas.drawPoint(1, 2, testPaint);
        //canvas.drawRect(2, 2, getWidth() - 2, getHeight() - 2, testPaint);
        //canvas.drawLine(2, 2, 2, getHeight() - 2, testPaint);
        //canvas.drawLine(2, 2, getWidth() - 2, 2, testPaint);
        //canvas.drawLine(getWidth() - 2, getHeight() - 2, 2, getHeight() - 2, testPaint);
        //canvas.drawLine(getWidth() - 2, getHeight() - 2, getWidth() - 2, 2, testPaint);
        
        drawFingerBoard(canvas);
        
    }
    
    private void drawFingerBoard(Canvas c) {
        p.setColor(Color.BLACK);
        
    }
}
