package com.jigneshspanchal.myvq;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by v548433 on 07/23/2017.
 */

//Create Canvas View
public class CanvasView extends View {

    public int width;
    public int height;
    public Bitmap mBitmap;
    public Bitmap origBWBitmap;
    public Canvas mCanvas;
    private Path mPath;
    Context context;
    public Paint mPaint;
    private float mX, mY;
    private static final float TOLERANCE = 5;

    public CanvasView (Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;

        // we set a new Path
        //mPath = new Path();

        // and we set a new Paint with the desired attributes
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        /*mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(4f);*/
    }



    //override onSizeChanged
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // your Canvas will draw onto the defined Bitmap
        //mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        //mCanvas = new Canvas(mBitmap);

        if(origBWBitmap != null) {
            drawBitmapImage(origBWBitmap);
            if (mBitmap != null) {
                float x, y;
                float xoffset, yoffset;
                xoffset = (mCanvas.getWidth() - mBitmap.getWidth()) / 2;
                yoffset = (mCanvas.getHeight() - mBitmap.getHeight()) / 2;
                for (y = 0; y < mBitmap.getHeight(); y++) {
                    for (x = 0; x < mBitmap.getWidth(); x++) {
                        mPaint.setColor(mBitmap.getPixel((int) x, (int) y));
                        mCanvas.drawPoint(x + xoffset, y + yoffset, mPaint);
                    }
                }
            }
        }

    }


    //override onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;

        if(mBitmap != null) {
            float x, y;
            float xoffset, yoffset;
            xoffset =  (mCanvas.getWidth() - mBitmap.getWidth())/2;
            yoffset =  (mCanvas.getHeight() - mBitmap.getHeight())/2;
            for (y = 0; y < mBitmap.getHeight(); y++) {
                for (x = 0; x < mBitmap.getWidth(); x++) {
                    mPaint.setColor(mBitmap.getPixel((int)x,(int)y));
                    mCanvas.drawPoint(x+xoffset, y+yoffset, mPaint);
                }
            }
        }

    }

    public void clearCanvas() {
        //mPath.reset();
        //mBitmap.recycle();
        invalidate();
    }

    public Bitmap scaledBitmapImage(Bitmap bitmap, int W, int H){
        final int maxSize = Math.min(W,H);
        int outWidth;
        int outHeight;
        int inWidth = bitmap.getWidth();
        int inHeight = bitmap.getHeight();
        if(inWidth > inHeight){
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }
        bitmap = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, false);

        return bitmap;
    }

    public void drawBitmapImage(Bitmap bitmap){
        mBitmap = scaledBitmapImage(bitmap, this.getWidth(), this.getHeight());
    }

    public void drawBitmapImage(InputStream demoImg){
        int x,y;
        int XMAX=512, YMAX=512;
        int data=0;

        mBitmap = Bitmap.createBitmap(XMAX, YMAX, Bitmap.Config.ARGB_8888);

        try {
            for (y = 0; y < YMAX; y++) {
                for (x = 0; x < XMAX; x++) {
                    data = demoImg.read();
                    if (data == -1) break;
                    if (data >= 128) {
                        mBitmap.setPixel(x,y,Color.WHITE);
                    } else {
                        mBitmap.setPixel(x,y,Color.BLACK);
                    }
                }
                if (data == -1) break;
            }
            demoImg.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bitmap convertToBW(Bitmap src, double value) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();

        // create output bitmap
        origBWBitmap = Bitmap.createBitmap(width, height, src.getConfig());

        // color information
        int A, R, G, B;
        int pixel;
        // get contrast value
        double contrast = Math.pow((100 + value) / 100, 2);

        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel);
                R = (int)(((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(R < 0) { R = 0; }
                else if(R > 255) { R = 255; }

                G = Color.red(pixel);
                G = (int)(((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(G < 0) { G = 0; }
                else if(G > 255) { G = 255; }

                B = Color.red(pixel);
                B = (int)(((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(B < 0) { B = 0; }
                else if(B > 255) { B = 255; }

                // set new pixel color to output bitmap
                origBWBitmap.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        return origBWBitmap;
    }

    /*
    // when ACTION_DOWN start touch according to the x,y values
    private void startTouch(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    // when ACTION_MOVE move touch according to the x,y values
    private void moveTouch(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOLERANCE || dy >= TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    // when ACTION_UP stop touch
    private void upTouch() {
        mPath.lineTo(mX, mY);
    }

    //override the onTouchEvent
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                upTouch();
                invalidate();
                break;
        }
        return true;
    }*/

}

