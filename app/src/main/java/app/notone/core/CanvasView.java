package app.notone.core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import app.notone.core.util.SettingsHolder;

/**
 * This class handles everything about the canvas.
 * It holds the information about the canvas and manages the rendering to the
 * screen. It also delegates the scaling and panning to the respective
 * GestureDetectors.
 * The use of the GestureDetector and ScaleGestureDetector were inspired by
 * the android developer documentation about 'Scale and Drag Gestures':
 * https://developer.android.com/develop/ui/views/touch-and-input/gestures/scale
 *
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public class CanvasView extends View {
    /**
     * Tag for logging
     */
    private static final String LOG_TAG = CanvasView.class.getSimpleName();
    /**
     * Maximum value that the scaling can have (zoom level)
     */
    private final float MAX_SCALE = 5.f;
    /**
     * Minimum value that the scaling can have (zoom level)
     */
    private final float MIN_SCALE = 0.05f;
    /**
     * The location where the canvas is saved
     */
    private Uri mCurrentURI = null;
    /**
     * True if the canvas has been saved and not been changed since
     */
    private boolean mSaved = false;
    /**
     * True if the canvas is fully loaded
     */
    private boolean mLoaded = false;

    /**
     * Handles the interaction with the pen and the canvas.
     * The CanvasWriter manages the different kinds of pens
     * that exist e.g. writing and erasing.
     * It also manages the Strokes that are being created and/or
     * deleted by the pens.
     */
    private CanvasWriter mCanvasWriter;

    /**
     * Handles detecting scaling gestures
     * (pinch-zooming with two fingers)
     */
    private ScaleGestureDetector mScaleDetector;
    /**
     * Handles detecting general gestures like
     * pressing, dragging and long-press
     */
    private GestureDetector mGestureDetector;

    /**
     * Matrix describing the zoom and translation of the canvas
     */
    private Matrix mViewTransform;
    /**
     * Inverse Matrix of the mViewTransform
     */
    private Matrix mInverseViewTransform;
    /**
     * Current scaling factor
     */
    private float mScale = 1.f;

    /**
     * Holds a printout of a pdf document as a collection of Bitmaps
     */
    private CanvasPdfDocument mPdfDocument;
    /**
     * Renders the CanvasPdfDocument and the preview border of
     * the pdf export to the canvas
     */
    private PdfCanvasRenderer mPdfRenderer;

    /**
     * Constructor
     * initializes vars for Paths and Transforms
     * defines detectors for scaling and gestures
     *
     * @param context android
     * @param attrs   android
     */
    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCanvasWriter = new CanvasWriter(10.f, Color.RED);
        mViewTransform = new Matrix();
        mInverseViewTransform = new Matrix();
        mScaleDetector = new ScaleGestureDetector(context,
                new CanvasScaleListener());
        mScaleDetector.setStylusScaleEnabled(false);
        mGestureDetector = new GestureDetector(context,
                new CanvasGestureListener());

        mSaved = false;
        mLoaded = false;

        mPdfDocument = new CanvasPdfDocument();
        mPdfRenderer = new PdfCanvasRenderer();
        mPdfRenderer.setPadding(0);
        mPdfRenderer.setScaling(1.f);
    }

    /**
     * set the width of the strokes
     *
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
        Log.d("PDF",
                "(CanvasView) changed: " + getPdfDocument().toString());
    }

    public PdfCanvasRenderer getPdfRenderer() {
        return mPdfRenderer;
    }

    public void setPdfRenderer(PdfCanvasRenderer mPdfRenderer) {
        this.mPdfRenderer = mPdfRenderer;
    }

    public boolean isSaved() {
        return mSaved;
    }

    public void setSaved(boolean saved) {
        mSaved = saved;
    }

    public boolean isLoaded() {
        return mLoaded;
    }

    public void setLoaded(boolean loaded) {
        mLoaded = loaded;
    }

    /**
     * Resets the view matrices to the unit-matrix
     */
    public void resetViewMatrices() {
        mViewTransform = new Matrix();
        mViewTransform.invert(mInverseViewTransform);
    }

    /**
     * Resets alls values and removes all elements from the canvas.
     * This includes all strokes and the pdf printout
     */
    public void reset() {
        resetViewMatrices();
        setScale(1.f);
        mCanvasWriter.reset();
        mPdfDocument = new CanvasPdfDocument();
        invalidate();
    }

    /**
     * Called when canvas is updated or invalidated
     *
     * @param canvas Current Canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        setSaved(false);
        canvas.setMatrix(mViewTransform); // transform here after having
        // drawn paths instead of transforming paths directly

        mPdfRenderer.render(mPdfDocument, canvas);

        mCanvasWriter.renderStrokes(canvas);

        if (SettingsHolder.shouldShowPdfBounds()) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            mPdfRenderer.renderBorder(this, canvas, metrics);
        }
        super.onDraw(canvas);
    }

    /**
     * Called when the user interacts with the canvas via touch
     *
     * @param event Contains information about the touch
     * @return True if the canvas has to be invalidated
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mScaleDetector.onTouchEvent(event);

        // input with two fingers (transformations) not handled here
        if (event.getPointerCount() > 1) {
            return true;
        }

        // if input with stylus dont handle here
        if (event.getToolType(0) != MotionEvent.TOOL_TYPE_STYLUS) {
            return true;
        }

        mViewTransform.invert(mInverseViewTransform);
        boolean invalidated = mCanvasWriter.handleOnTouchEvent(event,
                mViewTransform, mInverseViewTransform);

        if (invalidated) {
            invalidate();
        }

        return true;
    }

    public boolean undo() {
        boolean invalidated = mCanvasWriter.getUndoRedoManager().undo();
        if (invalidated) {
            invalidate();
        }
        return invalidated;
    }

    public boolean redo() {
        boolean invalidated = mCanvasWriter.getUndoRedoManager().redo();
        if (invalidated) {
            invalidate();
        }
        return invalidated;
    }

    public Uri getCurrentURI() {
        if (mCurrentURI == null) {
            Log.e(LOG_TAG, "getCurrentURI: URI is not yet set, save the " +
                    "document first");
            return Uri.parse("");
        }
        return mCurrentURI;
    }

    public void setUri(Uri uri) {
        this.mCurrentURI = uri;
    }

    /**
     * Handles all interactions with the canavs that were classified as
     * 'scaling'
     */
    private class CanvasScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        /**
         * Called when the scaling interaction starts
         *
         * @param detector The detector that recognized the event
         * @return
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Called when the scaling interaction is in progress.
         * The mScaleFactor and the translation will be adjusted
         * according to how much is being zoomed in/out and if
         * the fingers are moving while zooming.
         *
         * @param detector The detector that recognized the event
         * @return
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            float mScaleFactor = detector.getScaleFactor();
            // clamp scale
            if (mScale * mScaleFactor > MAX_SCALE) {
                mScaleFactor = MAX_SCALE / mScale;
                mScale = MAX_SCALE;
            } else if (mScale * mScaleFactor < MIN_SCALE) {
                mScaleFactor = MIN_SCALE / mScale;
                mScale = MIN_SCALE;
            } else {
                mScale *= mScaleFactor;
            }
//            10 / mScale = mScaleFactor;
            mViewTransform.postTranslate(-detector.getFocusX(),
                    -detector.getFocusY()); // post applies transform to
            // mViewTransform // translate origin of mViewTransform to
            // focuspoint
            mViewTransform.postScale(mScaleFactor, mScaleFactor); // scale
            // around origin (focus)
            mViewTransform.postTranslate(detector.getFocusX(),
                    detector.getFocusY()); // translate origin back away from
            // focuspoint
            invalidate();

            return true;
        }
    }

    /**
     * Function required by the View superclass
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Handles all gestures except scaling
     */
    private class CanvasGestureListener extends GestureDetector.SimpleOnGestureListener {
        /**
         * Called when a gesture is classified as a single tap
         *
         * @param e Information about the touch event
         * @return true if the event is consumed, else false
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        /**
         * Called when a gesture is classified as a double tap
         *
         * @param e Information about the touch event
         * @return true if the event is consumed, else false
         */
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }

        /**
         * Called when the pen/finger is first touching the canvas
         *
         * @param e Information about the touch event
         * @return true if the event is consumed, else false
         */
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        /**
         * Called when the a single tap is lifted
         *
         * @param e Information about the touch event
         * @return true if the event is consumed, else false
         */
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        /**
         * Called when a gesture is classified as scrolling/panning.
         * This function adjusts the translation of the view matrix
         * depending on how far the user has scrolled.
         * <p>
         * If the panning has been set to restricted in the settings
         * the user can only scroll with one finger.
         * Scrolling with a stylus is disabled.
         *
         * @param e1        Information about the touch at the start
         * @param e2        Information about the touch at the current point
         *                  in time
         * @param distanceX current distance scrolled in the x direction
         * @param distanceY current distance scrolled in the y direction
         * @return true if the event is consumed, else false
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            if (SettingsHolder.isPanningRestricted() && e2.getPointerCount() <= 1)
                return false; // if two finger panning is required and not
            // fullfilled: dont pan


            if (e2.getToolType(0) == MotionEvent.TOOL_TYPE_STYLUS)
                return false; // dont pan with pen


            mViewTransform.postTranslate(-distanceX, -distanceY); // slide
            // with finger with negative transform
            invalidate();

            return true;
        }

        /**
         * Called when a gesture is classified as a long press
         *
         * @param e Information about the touch event
         */
        @Override
        public void onLongPress(MotionEvent e) {
        }


        /**
         * Called when a gesture is recognized as a fling
         *
         * @param e1        Information about the touch at the start
         * @param e2        Information about the touch at the current point
         *                  in time
         * @param velocityX current x-velocity of the fling
         * @param velocityY current y-velocity of the fling
         * @return true if the event is consumed, else false
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            return false;
        }
    }
}
