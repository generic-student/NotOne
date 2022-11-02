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

public class CanvasView extends View {
    private static final String LOG_TAG = CanvasView.class.getSimpleName();

    private Paint mPaint;
    private Path mPath;
    private Path mDrawPath;
    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // Remember some things for zooming
    Matrix matrix;
    Matrix inverse;

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(10);
        mPath = new Path();
        mDrawPath = new Path();
        matrix = new Matrix();
        inverse = new Matrix();

        mScaleDetector = new ScaleGestureDetector(context, new CanvasScaleListener());
        mGestureDetector = new GestureDetector(context, new CanvasGestureListener());
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(mDrawPath, mPaint);
        super.onDraw(canvas);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mScaleDetector.onTouchEvent(event);

        if(event.getPointerCount() > 1) {
            return true;
        }

        if(mode == ZOOM && event.getAction() == MotionEvent.ACTION_UP && event.getPointerCount() == 1) {
            mode = NONE;
            return true;
        }

        if(mode != NONE || event.getToolType(0) != MotionEvent.TOOL_TYPE_STYLUS) {
            return true;
        }

        float pts[] = new float[]{event.getX(), event.getY()};
        matrix.invert(inverse);

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:

                inverse.mapPoints(pts);
                mPath.moveTo(pts[0], pts[1]);
                mPath.transform(matrix, mDrawPath);
                break;

            case MotionEvent.ACTION_MOVE:
                inverse.mapPoints(pts);
                mPath.lineTo(pts[0], pts[1]);
                mPath.transform(matrix, mDrawPath);
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                break;
        }

        return true;
    }

    private class CanvasScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mode = ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            float mScaleFactor = detector.getScaleFactor();
            matrix.postTranslate(-detector.getFocusX(), -detector.getFocusY());
            matrix.postScale(mScaleFactor, mScaleFactor);
            matrix.postTranslate(detector.getFocusX(), detector.getFocusY());
            mPath.transform(matrix, mDrawPath);
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
