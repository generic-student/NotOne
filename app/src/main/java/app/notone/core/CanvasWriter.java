package app.notone.core;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.MotionEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import app.notone.core.pens.CanvasPen;
import app.notone.core.pens.CanvasPenFactory;
import app.notone.core.pens.CanvasWriterPen;
import app.notone.core.pens.PenType;

/**
 * Handles the interaction with the stylus and the canvas.
 * Manages adding and removing strokes and undoing and redoing
 * these actions using the {@link UndoRedoManager}
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public class CanvasWriter implements Serializable {
    /**
     * Describes the state that the writer is in.
     * If the writer is in WRITE state, that means that 
     * a stroke is currently being written.
     * If it is in ERASE state, that means the eraser is
     * active and strokes are being deleted etc.
     */
    private enum DrawState {
        WRITE, ERASE, SELECT, SHAPE
    }

    //constants
    /** The event code when the primary button on a stylus is being pressed */
    private static final transient int ACTION_DOWN_WITH_PRIMARY_STYLUS_BUTTON = 213;

    //current settings
    /** The paint with wich the strokes are rendered */
    private final transient Paint mPaint;
    /** 
     * The currently set stroke weight. 
     * it will be used for all strokes following the current
     * unless changed. 
     * */
    private float mStrokeWeight;
    /** 
     * The currently set stroke color. 
     * it will be used for all strokes following the current
     * unless changed. 
     * */
    private int mStrokeColor;

    /** Handles undoing and redoing actions like erasing a stroke */
    private final UndoRedoManager undoRedoManager;

    /** A list of all strokes that have been drawn */
    private ArrayList<Stroke> mStrokes; // contains all Paths already drawn by user Path, Color, Weight

    /** A map of all pens associated with the corresponding DrawState */
    private final transient HashMap<DrawState, CanvasPen> pens;

    /** The current DrawState */
    private CanvasWriter.DrawState mDrawState = CanvasWriter.DrawState.WRITE;

    //TODO: add the penType to the Pen
    /** The type of pen currently being used */
    private PenType mCurrentPenType = PenType.WRITER;

    public CanvasWriter(float mStrokeWeight, int mStrokeColor) {
        this.mStrokeWeight = mStrokeWeight;
        this.mStrokeColor = mStrokeColor;

        this.mPaint = new Paint();
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setStrokeJoin(Paint.Join.ROUND);
        this.mPaint.setStrokeCap(Paint.Cap.ROUND);

        mStrokes = new ArrayList<>();
        undoRedoManager = new UndoRedoManager(mStrokes);

        pens = new HashMap<>();
        CanvasPenFactory penFactory = new CanvasPenFactory();
        pens.put(DrawState.WRITE, penFactory.createCanvasPen(PenType.WRITER, this));
        pens.put(DrawState.ERASE, penFactory.createCanvasPen(PenType.ERASER, this));
        pens.put(DrawState.SELECT, penFactory.createCanvasPen(PenType.SELECTOR, this));
        pens.put(DrawState.SHAPE, penFactory.createCanvasPen(PenType.SHAPE_DETECTOR, this));
    }

    public Paint getPaint() {
        return mPaint;
    }

    public float getStrokeWeight() {
        return mStrokeWeight;
    }

    /**
     * Sets the stroke weight and updates the stroke weight of the
     * currently being drawn stroke
     * @param mStrokeWeight
     */
    public void setStrokeWeight(float mStrokeWeight) {
        this.mStrokeWeight = mStrokeWeight;

        ((CanvasWriterPen)pens.get(DrawState.WRITE)).setStrokeWeight(mStrokeWeight);
    }

    public int getStrokeColor() {
        return mStrokeColor;
    }

    /**
     * Sets the stroke weight and updates the stroke color of the
     * currently being drawn stroke
     * @param mStrokeColor
     */
    public void setStrokeColor(int mStrokeColor) {
        this.mStrokeColor = mStrokeColor;

        ((CanvasWriterPen)pens.get(DrawState.WRITE)).setStrokeColor(mStrokeColor);
        ((CanvasWriterPen)pens.get(DrawState.SHAPE)).setStrokeColor(mStrokeColor);
    }

    public DrawState getDrawState() {
        return mDrawState;
    }

    public void setDrawState(DrawState mDrawState) {
        this.mDrawState = mDrawState;
    }

    public ArrayList<Stroke> getStrokes() {
        return mStrokes;
    }

    public void setStrokes(ArrayList<Stroke> mStrokes) {
        this.mStrokes = mStrokes;
    }

    public UndoRedoManager getUndoRedoManager() {
        return undoRedoManager;
    }

    public PenType getCurrentPenType() {
        return mCurrentPenType;
    }

    public void setCurrentPenType(PenType currentPenType) {
        this.mCurrentPenType = currentPenType;
    }

    /**
     * Clears all strokes and resets the undo-redo tree
     * as well as resets all pens
     */
    public void reset() {
        undoRedoManager.reset();
        mStrokes.clear();

        for(CanvasPen pen : pens.values()) {
            pen.reset();
        }
    }

    /**
     * Called by the {@link CanavsView} when a touch event is recognized.
     * Determines the correct pen by the penType and if the stylus button
     * is being pressed.
     * When the button is being pressed it is always in 'erase' mode.
     * @param event Information about the touch event
     * @param viewMatrix Matrix describing the scaling and translation of the canvas
     * @param inverseViewMatrix Inverse of the view matrix
     * @return True if the canvas has to be invalidated
     */
    public boolean handleOnTouchEvent(MotionEvent event, Matrix viewMatrix, Matrix inverseViewMatrix) {
        //compute the draw state
        if(getCurrentPenType() == PenType.WRITER) {
            if(event.getButtonState() == MotionEvent.BUTTON_STYLUS_PRIMARY) {
                setDrawState(DrawState.ERASE);
            } else {
                setDrawState(DrawState.WRITE);
            }
        }
        else if(getCurrentPenType() == PenType.SELECTOR) {
            if(event.getButtonState() == MotionEvent.BUTTON_STYLUS_PRIMARY) {
                setDrawState(DrawState.ERASE);
            } else {
                setDrawState(DrawState.SELECT);
            }
        }
        else if(getCurrentPenType() == PenType.SHAPE_DETECTOR) {
            if(event.getButtonState() == MotionEvent.BUTTON_STYLUS_PRIMARY) {
                setDrawState(DrawState.ERASE);
            } else {
                setDrawState(DrawState.SHAPE);
            }
        }
        else if(getCurrentPenType() == PenType.ERASER || event.getAction() != ACTION_DOWN_WITH_PRIMARY_STYLUS_BUTTON) {
            setDrawState(DrawState.ERASE);
        }

        //transform the cursor position using the inverse of the view matrix
        Vector2f currentTouchPoint = new Vector2f(event.getX(), event.getY()).transform(inverseViewMatrix);

        final CanvasPen pen = pens.get(getDrawState());
        return pen != null && pen.handleOnTouchEvent(event, currentTouchPoint);
    }

    /**
     * Renders the strokes to the Canavs
     * @param canvas Canvas to renderto
     */
    public void renderStrokes(Canvas canvas) {
        for(Stroke stroke : mStrokes) {
            mPaint.setColor(stroke.getColor());
            mPaint.setStrokeWidth(stroke.getWeight());
            canvas.drawPath(stroke, mPaint); // draw all paths on canvas
        }
        pens.get(mDrawState).render(canvas);
    }

}
