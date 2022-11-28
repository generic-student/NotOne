package app.notone.core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class CanvasView extends View {
    private static final String LOG_TAG = CanvasView.class.getSimpleName();
    private final float MAX_SCALE = 10.f;
    private final float MIN_SCALE = 0.1f;

    private CanvasWriter mCanvasWriter;

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;

    // Remember some things for zooming
    private Matrix mViewTransform;
    private Matrix mInverseViewTransform;
    private float mScale    = 1.f;

    private CanvasPdfDocument mPdfDocument;
    private PdfCanvasRenderer mPdfRenderer;

    /**
     * Constructor
     * initializes vars for Paths and Transforms
     * defines detectors for scaling and gestures
     * @param context android
     * @param attrs android
     */
    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCanvasWriter = new CanvasWriter(10.f, Color.RED);
        mViewTransform = new Matrix();
        mInverseViewTransform = new Matrix();
        mScaleDetector = new ScaleGestureDetector(context, new CanvasScaleListener());
        mScaleDetector.setStylusScaleEnabled(false);
        mGestureDetector = new GestureDetector(context, new CanvasGestureListener());

        mPdfDocument = new CanvasPdfDocument(2.f);
        mPdfRenderer = new PdfCanvasRenderer();
    }

    /**
     * set the width of the strokes
     * @param weight
     */
    public void setStrokeWeight(float weight) {
        mCanvasWriter.setStrokeWeight(weight);
    }

    public float getStrokeWeight() {
        return mCanvasWriter.getStrokeWeight();
    }

    public void setStrokeColor(int color) {
        mCanvasWriter.setStrokeColor(color);
    }

    public int getStrokeColor() {
        return mCanvasWriter.getStrokeColor();
    }

    public float getScale() {
        return mScale;
    }

    public void setScale(float mScale) {
        this.mScale = mScale;
    }

    public Matrix getViewTransform() {
        return mViewTransform;
    }

    public void setViewTransform(Matrix mViewTransform) {
        this.mViewTransform = mViewTransform;
    }

    public Matrix getInverseViewTransform() {
        return mInverseViewTransform;
    }

    public void setInverseViewTransform(Matrix mInverseViewTransform) {
        this.mInverseViewTransform = mInverseViewTransform;
    }

    public CanvasWriter getCanvasWriter() {
        return mCanvasWriter;
    }

    public void setCanvasWriter(CanvasWriter mCanvasWriter) {
        this.mCanvasWriter = mCanvasWriter;
    }

    public CanvasPdfDocument getPdfDocument() {
        return mPdfDocument;
    }

    public void setPdfDocument(CanvasPdfDocument mPdfDocument) {
        this.mPdfDocument = mPdfDocument;
    }

    public PdfCanvasRenderer getPdfRenderer() {
        return mPdfRenderer;
    }

    public void setPdfRenderer(PdfCanvasRenderer mPdfRenderer) {
        this.mPdfRenderer = mPdfRenderer;
    }

    public void resetViewMatrices() {
        mViewTransform = new Matrix();
        mViewTransform.invert(mInverseViewTransform);
    }

    public void reset() {
        resetViewMatrices();
        mCanvasWriter.reset();
        mPdfDocument = new CanvasPdfDocument(2.f);
        invalidate();
    }

    /**
     * called when canvas is updated or invalidated
     * @param canvas current Canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setMatrix(mViewTransform); // transform here after having drawn paths instead of transforming paths directly

        mPdfRenderer.render(mPdfDocument, canvas, mViewTransform);

        mCanvasWriter.renderStrokes(canvas);
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

        mViewTransform.invert(mInverseViewTransform);
        boolean invalidated = mCanvasWriter.handleOnTouchEvent(event, mViewTransform, mInverseViewTransform);

        if(invalidated) {
            invalidate();
        }

        return true;
    }

    public boolean undo() {
        boolean invalidated = mCanvasWriter.undo();
        if(invalidated) {
            invalidate();
        }
        return invalidated;
    }

    public boolean redo() {
        boolean invalidated = mCanvasWriter.redo();
        if(invalidated) {
            invalidate();
        }
        return invalidated;
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
            if(e2.getPointerCount() <= 1 && true) { // TODO check with shared preferneces
                return true;
            }

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