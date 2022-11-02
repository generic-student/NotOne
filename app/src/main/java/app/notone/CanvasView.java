package app.notone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;

public class CanvasView extends View {
    private static final String LOG_TAG = CanvasView.class.getSimpleName();

    private ArrayList<Path> mPaths;
    private int currentPath = 0;

    private Paint mPaint;
    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mState = NONE;

    // Remember some things for zooming
    Matrix mViewTransform;
    Matrix mInverseViewTransform;

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        setStrokeWeight(10);
        setStrokeColor(Color.RED);

        mViewTransform = new Matrix();
        mInverseViewTransform = new Matrix();
        mPaths = new ArrayList<Path>();
        mPaths.add(new Path());

        mScaleDetector = new ScaleGestureDetector(context, new CanvasScaleListener());
        mGestureDetector = new GestureDetector(context, new CanvasGestureListener());
    }

    public void setStrokeWeight(float weight) {
        mPaint.setStrokeWidth(weight);
    }

    public float getStrokeWeight() {
        return mPaint.getStrokeWidth();
    }

    public void setStrokeColor(int color) {
        mPaint.setColor(color);
    }

    public int getStrokeColor() {
        return mPaint.getColor();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setMatrix(mViewTransform);
        for(Path path : mPaths) {
            canvas.drawPath(path, mPaint);
        }
        super.onDraw(canvas);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mScaleDetector.onTouchEvent(event);

        if(event.getPointerCount() > 1) {
            return true;
        }

        if(mState == ZOOM && event.getAction() == MotionEvent.ACTION_UP && event.getPointerCount() == 1) {
            mState = NONE;
            return true;
        }

        if(mState != NONE || event.getToolType(0) != MotionEvent.TOOL_TYPE_STYLUS) {
            return true;
        }

        float pts[] = new float[]{event.getX(), event.getY()};
        mViewTransform.invert(mInverseViewTransform);

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:

                mInverseViewTransform.mapPoints(pts);
                mPaths.get(currentPath).moveTo(pts[0], pts[1]);
                break;

            case MotionEvent.ACTION_MOVE:
                mInverseViewTransform.mapPoints(pts);
                mPaths.get(currentPath).lineTo(pts[0], pts[1]);
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                mPaths.add(new Path());
                currentPath++;
                break;
        }

        return true;
    }

    private class CanvasScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mState = ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            float mScaleFactor = detector.getScaleFactor();
            mViewTransform.postTranslate(-detector.getFocusX(), -detector.getFocusY());
            mViewTransform.postScale(mScaleFactor, mScaleFactor);
            mViewTransform.postTranslate(detector.getFocusX(), detector.getFocusY());
            invalidate();

            return true;
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private class CanvasGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e)
        {
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e)
        {
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
        {
            if(e2.getPointerCount() <= 1) {
                return true;
            }

            mViewTransform.postTranslate(-distanceX, -distanceY);
            invalidate();

            return true;
        }

        @Override
        public void onLongPress(MotionEvent e)
        {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
        {
            return true;
        }
    }
}
