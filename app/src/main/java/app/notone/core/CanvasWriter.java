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
import app.notone.core.pens.PenType;

public class CanvasWriter implements Serializable {
    //constants
    private static final transient int ACTION_DOWN_WITH_PRIMARY_STYLUS_BUTTON = 213;

    //current settings
    private final transient Paint mPaint;
    private float mStrokeWeight;
    private int mStrokeColor;

    //handles the undo-redo-tree
    private final UndoRedoManager undoRedoManager;

    //all the strokes that have been drawn
    private ArrayList<Stroke> mStrokes; // contains all Paths already drawn by user Path, Color, Weight

    //all the existing pens
    private final HashMap<DrawState, CanvasPen> pens;

    public enum DrawState {
        WRITE, ERASE, SELECT
    }
    private CanvasWriter.DrawState mDrawState = CanvasWriter.DrawState.WRITE;

    private PenType mCurrentPenType = PenType.PEN;

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
        pens.put(DrawState.WRITE, penFactory.createCanvasPen(PenType.PEN, this));
        pens.put(DrawState.ERASE, penFactory.createCanvasPen(PenType.ERASER, this));
        pens.put(DrawState.SELECT, penFactory.createCanvasPen(PenType.SELECTOR, this));
    }

    public Paint getPaint() {
        return mPaint;
    }

    public float getStrokeWeight() {
        return mStrokeWeight;
    }

    public void setStrokeWeight(float mStrokeWeight) {
        this.mStrokeWeight = mStrokeWeight;
    }

    public int getStrokeColor() {
        return mStrokeColor;
    }

    public void setStrokeColor(int mStrokeColor) {
        this.mStrokeColor = mStrokeColor;
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

    public void reset() {
        undoRedoManager.reset();
        mStrokes.clear();

        for(CanvasPen pen : pens.values()) {
            pen.reset();
        }
    }

    public boolean handleOnTouchEvent(MotionEvent event, Matrix viewMatrix, Matrix inverseViewMatrix) {
        //compute the draw state
        if(getCurrentPenType() == PenType.PEN) {
            if(event.getButtonState() == MotionEvent.BUTTON_STYLUS_PRIMARY) {
                setDrawState(DrawState.ERASE);
            } else {
                setDrawState(DrawState.WRITE);
            }
        }
        else if(getCurrentPenType() == PenType.SELECTOR) {
            setDrawState(DrawState.SELECT);
        }
        else if(getCurrentPenType() == PenType.ERASER || event.getAction() != ACTION_DOWN_WITH_PRIMARY_STYLUS_BUTTON) {
            setDrawState(DrawState.ERASE);
        }

        //transform the cursor position using the inverse of the view matrix
        Vector2f currentTouchPoint = new Vector2f(event.getX(), event.getY()).transform(inverseViewMatrix);

        final CanvasPen pen = pens.get(getDrawState());
        return pen != null && pen.handleOnTouchEvent(event, currentTouchPoint);
    }

    public void renderStrokes(Canvas canvas) {
        for(Stroke stroke : mStrokes) {
            mPaint.setColor(stroke.getColor());
            mPaint.setStrokeWidth(stroke.getWeight());
            canvas.drawPath(stroke, mPaint); // draw all paths on canvas
        }
        pens.get(mDrawState).render(canvas);
    }

}
