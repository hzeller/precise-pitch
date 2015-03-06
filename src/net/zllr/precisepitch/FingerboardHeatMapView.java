package net.zllr.precisepitch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
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
    
    private Bitmap fullmap;
    
    private static final float STRING_LENGTH = 4.69f;
    
    private static final float fb_verts[] = {
        0.80f, 4.20f,
        0.20f, 4.20f,
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
        
        0.37f, 0.00f,
        0.37f, 0.16f,
        0.63f, 0.00f,
        
        0.37f, 0.16f,
        0.63f, 0.00f,
        0.63f, 0.16f,
        
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
        0xff532000, 0xff532000, 0xff532000,
        0xff532000, 0xff532000, 0xff532000,
        0xffD96B00, 0xffD96B00, 0xffD96B00,
        0xffD96B00, 0xffD96B00, 0xffD96B00,
        0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0,
    };
    
    
    // Scale factor (1.0 = normal)
    //private double scale;
    // Vertical position (0 = top)
    //private int pos;
    
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
    
    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        //int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        //int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        
        System.out.println("FingerboardHeatMapView onMeasure widthSize: " + widthSize);
        setMeasuredDimension(widthSize, (int)(4.2 * widthSize));
    }
    
    
    @SuppressLint({ "NewApi", "InlinedApi" })
    void init() {
        // Need to disable hardware acceleration so that call
        // to drawVertices will work properly
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        
        //scale = 1.0;
        //pos = 0;
        fullmap = null;
        
        System.out.println("FingerboardHeatMapView init + getWidth(): " + getWidth() + " getHeight(): " + getHeight());
        
        
        p = new Paint();
        p.setFlags(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Style.FILL_AND_STROKE);
        p.setColor(Color.WHITE);
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        System.out.println("FingerboardHeatMapView onDraw");

        if (fullmap == null) {
            createHeatMap();
        }
        
        canvas.drawBitmap(fullmap, 0, 0, null);
    }
    
    private void createHeatMap() {
        System.out.println("getWidth(): " + getWidth() + " getHeight(): " + getHeight());
        fullmap = Bitmap.createBitmap(getWidth(), (int)(4.2 * (float)getWidth()), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(fullmap);
        
        c.scale((float)c.getWidth(), (float)c.getWidth());
        c.drawColor(Color.WHITE);
        
        drawCello(c);
        drawFrets(c);
        drawHistogram(c);
    }
    
    private void drawHistogram(Canvas c) {
        // need to acquire the Histogram object somehow...
    }
    
    private void drawFrets(Canvas c) {
        p.setColor(0x7F0000FF);
        p.setStrokeWidth(0.005f);
        for (int i = 0; i < 56; i++) {
            float yFretPos =
                    STRING_LENGTH -
                    (STRING_LENGTH / (float)Math.pow(1.05946309436, (double)i))
                    + 0.2f;
            c.drawLine(0.08f, yFretPos, 0.92f, yFretPos, p);
        }
    }
    
    private void drawCello(Canvas c) {
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
        c.drawLine(0.38f, -0.01f, 0.3f, 4.21f, p);
        p.setStrokeWidth(0.010f);
        c.drawLine(0.46f, -0.01f, 0.433f, 4.21f, p);
        p.setStrokeWidth(0.008f);
        c.drawLine(0.54f, -0.01f, 0.566f, 4.21f, p);
        p.setStrokeWidth(0.006f);
        c.drawLine(0.62f, -0.01f, 0.7f, 4.21f, p);
    }
}
