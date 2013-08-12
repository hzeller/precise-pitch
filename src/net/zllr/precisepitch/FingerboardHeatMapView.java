package net.zllr.precisepitch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Canvas.VertexMode;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

/**
 * View containing a cello fingerboard with a heat map depicting a distribution
 * of where notes were played.
 */
public class FingerboardHeatMapView extends View {
    
    private Paint p;
    
    private static final float fb_verts[] = {
        0.80f, 4.00f,
        0.20f, 4.00f,
        0.70f, 0.20f,
        0.30f, 0.20f,
    };
    
    private static final int fb_colors[] = {
        Color.BLACK,
        Color.DKGRAY,
        Color.BLACK,
        Color.DKGRAY,
        0, 0, 0, 0,
    };
    
    private static final float fb_nut_verts[] = {
        0.72f, 0.20f,
        0.28f, 0.20f,
        0.72f, 0.16f,
        0.28f, 0.16f,
    };
    
    private static final float scroll_verts[] = {
        0.28f, 0.16f,
        0.28f, 0.00f,
        0.37f, 0.00f,
        
        0.37f, 0.00f,
        0.28f, 0.16f,
        0.37f, 0.16f,
        
        0.63f, 0.16f,
        0.63f, 0.00f,
        0.72f, 0.00f,
        
        0.72f, 0.00f,
        0.63f, 0.16f,
        0.72f, 0.16f,
    };
    
    private static final int scroll_colors[] = {
        0xffD96B00, 0xffD96B00, 0xffD96B00,
        0xffD96B00, 0xffD96B00, 0xffD96B00,
        0xffD96B00, 0xffD96B00, 0xffD96B00,
        0xffD96B00, 0xffD96B00, 0xffD96B00,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    };
    
    
    // Scale factor (1.0 = normal)
    private double scale;
    // Vertical position (0 = top)
    private int pos;
    
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
    
    @SuppressLint({ "NewApi", "InlinedApi" })
    void init() {
        // Need to disable hardware acceleration so that call
        // to drawVertices will work properly
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        
        
        scale = 1.0;
        pos = 0;
        
        p = new Paint();
        p.setFlags(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Style.FILL_AND_STROKE);
        p.setColor(Color.WHITE);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        canvas.scale((float)canvas.getWidth(), (float)canvas.getHeight());
        canvas.drawColor(Color.WHITE);
        
        drawFingerBoard(canvas);
        
    }
    
    private void drawFingerBoard(Canvas c) {
        p.setColor(Color.BLUE);
        
        c.drawVertices(VertexMode.TRIANGLE_STRIP,
                fb_verts.length,
                fb_verts, 0,
                null, 0,
                fb_colors, 0,
                null, 0, 0,
                p);
        
        c.drawVertices(VertexMode.TRIANGLE_STRIP,
                fb_nut_verts.length,
                fb_nut_verts, 0,
                null, 0,
                fb_colors, 0,
                null, 0, 0,
                p);
        
        c.drawVertices(VertexMode.TRIANGLES,
                scroll_verts.length,
                scroll_verts, 0,
                null, 0,
                scroll_colors, 0,
                null, 0, 0,
                p);
        
        // Strings
        p.setColor(Color.GRAY);
        p.setStrokeWidth(0.015f);
        c.drawLine(0.38f, -0.01f, 0.3f, 4.01f, p);
        p.setStrokeWidth(0.010f);
        c.drawLine(0.46f, -0.01f, 0.433f, 4.01f, p);
        p.setStrokeWidth(0.008f);
        c.drawLine(0.54f, -0.01f, 0.566f, 4.01f, p);
        p.setStrokeWidth(0.006f);
        c.drawLine(0.62f, -0.01f, 0.7f, 4.01f, p);
        
    }
}
