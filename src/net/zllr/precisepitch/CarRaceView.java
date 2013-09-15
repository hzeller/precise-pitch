package net.zllr.precisepitch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

//This class is based heavily on LunarView.java from the Lunar Lander sample
//in the Android SDK
public class CarRaceView extends SurfaceView
                      implements SurfaceHolder.Callback {
    public class CarRaceThread extends Thread {
        
        private boolean mIsRunning = false;
        private int mWidth = 1;
        private int mHeight = 1;
        
        private float xPos = 0.0f;
        private float yPos = 0.0f;
        
        private final Paint dotPaint;
        
        private SurfaceHolder mSurfaceHolder;
        private Context mContext;
        
        public CarRaceThread(SurfaceHolder surfaceHolder, Context context) {
            mSurfaceHolder = surfaceHolder;
            mContext = context;
            
            dotPaint = new Paint();
            dotPaint.setColor(Color.YELLOW);
            dotPaint.setAntiAlias(true);
        }
        
        @Override
        public void run() {
            while (mIsRunning) {
                Canvas c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {
                        c.drawCircle(xPos, yPos, 10.0f, dotPaint);
                    }
                } finally {
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
        
        public void setRunning(boolean running) {
            mIsRunning = running;
        }
        
        public void setSurfaceSize(int width, int height) {
            synchronized (mSurfaceHolder) {
                mWidth = width;
                mHeight = height;
            }
        }

    }
    //private Context context;
    private CarRaceThread thread;
    
    //private final int score1;
    //private final int score2;
    
    public CarRaceView(Context c, AttributeSet attrs) {
        super(c, attrs);
        
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        
        thread = new CarRaceThread(holder, c);
        setFocusable(true);
    }
    
    public CarRaceThread getThread() {
        return thread;
    }



    
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format,
                               int width, int height) {
        thread.setSurfaceSize(width, height);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {}
        }
    }
    
    
}
