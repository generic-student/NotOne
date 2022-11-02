package app.notone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;

public class CanvasView extends View {
    private static final String LOG_TAG = CanvasView.class.getSimpleName();

    private ArrayList<Stroke> mStrokes; // contains all Paths drawn by user Path, Color, Weight
    private int currentPathIndex = 0;

    private Paint mPaint;
    private float mStrokeWeight = 10.f;
    private int mStrokeColor = Color.RED;
    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;

    // Remember some things for zooming
    private Matrix mViewTransform;
    private Matrix mInverseViewTransform;
    private final float MAX_SCALE = 5f;
    private final float MIN_SCALE = 0.01f;
    private float mScale    = 1;
    /**
     * Constructor
     * initializes vars for Paths and Transforms
     * defines detectors for scaling and gestures
     * @param context android
     * @param attrs android
     */
    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mViewTransform = new Matrix();
        mInverseViewTransform = new Matrix();
        mStrokes = new ArrayList<Stroke>();
        mStrokes.add(new Stroke(getStrokeColor(), getStrokeWeight()));

        mScaleDetector = new ScaleGestureDetector(context, new CanvasScaleListener());
        mGestureDetector = new GestureDetector(context, new CanvasGestureListener());
    }

    /**
     * set the width of the strokes
     * @param weight
     */
    public void setStrokeWeight(float weight) {
        mStrokeWeight = weight;
        // dont allow changing stroke width while drawing
        if(mStrokes.get(currentPathIndex).getPath().isEmpty()) {
            mStrokes.get(currentPathIndex).setWeight(weight);
        }
    }

    public float getStrokeWeight() {
        return mStrokeWeight;
    }

    public void setStrokeColor(int color) {
        mStrokeColor = color;
        // dont allow changing stroke color
        if(mStrokes.get(currentPathIndex).getPath().isEmpty()) {
            mStrokes.get(currentPathIndex).setColor(color);
        }
    }

    public int getStrokeColor() {
        return mStrokeColor;
    }

    /**
     * called when canvas is updated or invalidated
     * @param canvas current Canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setMatrix(mViewTransform); // transform here after having drawn paths instead of transforming paths directly
        for(Stroke stroke : mStrokes) {
            mPaint.setColor(stroke.getColor());
            mPaint.setStrokeWidth(stroke.getWeight());
            canvas.drawPath(stroke.getPath(), mPaint); // draw all paths on canvas
        }
        super.onDraw(canvas);
    }

    /**
     * if user interacts via touch
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mScaleDetector.onTouchEvent(event);

        // input with two fingers (transformations) not handled here
        if(event.getPointerCount() > 1) {
            return true;
        }

        // if input with stylus dont handle here
        if(event.getToolType(0) != MotionEvent.TOOL_TYPE_STYLUS) {
            return true;
        }

        float pts[] = new float[]{event.getX(), event.getY()};
        mViewTransform.invert(mInverseViewTransform); // mInverseViewTransform = mViewTransform.inverted

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:

                mInverseViewTransform.mapPoints(pts); // revert current transformation to work non transformed point // pts = transformed
                mStrokes.get(currentPathIndex).getPath().moveTo(pts[0], pts[1]);; // set origin of path from touch point
                // no transform
                break;

            case MotionEvent.ACTION_MOVE:
                mInverseViewTransform.mapPoints(pts);
                mStrokes.get(currentPathIndex).getPath().lineTo(pts[0], pts[1]);
                invalidate(); // call draw
                break;

            case MotionEvent.ACTION_UP:
                mStrokes.add(new Stroke(getStrokeColor(), getStrokeWeight())); // prep empty next
                currentPathIndex++;
                break;
        }

        return true;
    }

    /**
     * Scaling
     */
    private class CanvasScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            float mScaleFactor = detector.getScaleFactor();
            // clamp scale
            if(mScale * mScaleFactor > MAX_SCALE) {
                mScaleFactor = MAX_SCALE / mScale;
                mScale = MAX_SCALE;
            }
            else if(mScale * mScaleFactor < MIN_SCALE) {
                mScaleFactor = MIN_SCALE / mScale;
                mScale = MIN_SCALE;
            }
            else {
                mScale *= mScaleFactor;
            }
//            10 / mScale = mScaleFactor;
            mViewTransform.postTranslate(-detector.getFocusX(), -detector.getFocusY()); // post applies transform to mViewTransform // translate origin of mViewTransform to focuspoint
            mViewTransform.postScale(mScaleFactor, mScaleFactor); // scale around origin (focus)
            mViewTransform.postTranslate(detector.getFocusX(), detector.getFocusY()); // translate origin back away from focuspoint
            invalidate();

            return true;
        }
    }

    /**
     * android magic
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * all gestures except scaling cause android
     */
    private class CanvasGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }

        /**
         *
         * @param e1 start point of the translation
         * @param e2 current point of translation
         * @param distanceX
         * @param distanceY
         * @return
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            if(e2.getPointerCount() <= 1 && true) { // TODO check with shared preferneces
//                return true;
//            }

            if(e2.getToolType(0) == MotionEvent.TOOL_TYPE_STYLUS) {
                return true;
            }

            mViewTransform.postTranslate(-distanceX, -distanceY); // slide with finger with negative transform
            invalidate();

            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) { }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return true;
        }
    }
}
