package app.notone.core;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import app.notone.core.pens.CanvasEraserPen;
import app.notone.core.pens.CanvasPen;
import app.notone.core.pens.CanvasPenFactory;
import app.notone.core.pens.CanvasWriterPen;
import app.notone.core.util.MathHelper;
import app.notone.core.util.SAT;

public class CanvasWriter implements Serializable {
    private static final transient int ACTION_DOWN_WITH_PRIMARY_STYLUS_BUTTON = 213;
    private static final transient String LOG_TAG = CanvasWriter.class.getSimpleName();

    private transient Paint mPaint;
    private float mStrokeWeight;
    private int mStrokeColor;

    private ArrayList<CanvasWriterAction> mUndoneActions;
    private ArrayList<CanvasWriterAction> mActions;

    private ArrayList<Stroke> mStrokes; // contains all Paths already drawn by user Path, Color, Weight
    private Stroke mCurrentStroke; //the path that the user is currently drawing

    private Vector2f previousTouchPoint = new Vector2f(0, 0);

    private HashMap<DrawState, CanvasPen> pens;


    public enum DrawState {
        WRITE, ERASE, SELECT
    }
    private CanvasWriter.DrawState mDrawState = CanvasWriter.DrawState.WRITE;

    private WriteMode mWriteMode = WriteMode.PEN;

    public CanvasWriter(float mStrokeWeight, int mStrokeColor) {
        this.mStrokeWeight = mStrokeWeight;
        this.mStrokeColor = mStrokeColor;

        this.mPaint = new Paint();
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setStrokeJoin(Paint.Join.ROUND);
        this.mPaint.setStrokeCap(Paint.Cap.ROUND);

        mStrokes = new ArrayList<>();
        mActions = new ArrayList<>();
        mUndoneActions = new ArrayList<>();
        mCurrentStroke = new Stroke(getStrokeColor(), getStrokeWeight());

        pens = new HashMap<>();
        CanvasPenFactory penFactory = new CanvasPenFactory();
        pens.put(DrawState.WRITE, penFactory.createCanvasPen("WRITER", this));
        pens.put(DrawState.ERASE, penFactory.createCanvasPen("ERASER", this));
        //pens.put(DrawState.SELECT, penFactory.createCanvasPen("SELECTOR", this));
    }

    public Paint getPaint() {
        return mPaint;
    }

    public void setPaint(Paint mPaint) {
        this.mPaint = mPaint;
    }

    public float getStrokeWeight() {
        return mStrokeWeight;
    }

    public void setStrokeWeight(float mStrokeWeight) {
        this.mStrokeWeight = mStrokeWeight;

        if(mCurrentStroke.isEmpty()) {
            mCurrentStroke.setWeight(mStrokeWeight);
        }
    }

    public int getStrokeColor() {
        return mStrokeColor;
    }

    public void setStrokeColor(int mStrokeColor) {
        this.mStrokeColor = mStrokeColor;

        if(mCurrentStroke.isEmpty()) {
            mCurrentStroke.setColor(mStrokeColor);
        }
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

    public ArrayList<CanvasWriterAction> getUndoneActions() {
        return mUndoneActions;
    }

    public ArrayList<CanvasWriterAction> getActions() {
        return mActions;
    }

    public void setUndoneActions(ArrayList<CanvasWriterAction> mUndoneActions) {
        this.mUndoneActions = mUndoneActions;
    }

    public void setActions(ArrayList<CanvasWriterAction> mActions) {
        this.mActions = mActions;
    }

    public WriteMode getWriteMode() {
        return mWriteMode;
    }

    public void setWriteMode(WriteMode mWriteMode) {
        this.mWriteMode = mWriteMode;
    }

    public Stroke getCurrentStroke() {
        return mCurrentStroke;
    }

    public void setCurrentStroke(Stroke mCurrentStroke) {
        this.mCurrentStroke = mCurrentStroke;
    }

    public Vector2f getPreviousTouchPoint() {
        return previousTouchPoint;
    }

    public void setPreviousTouchPoint(Vector2f previousTouchPoint) {
        this.previousTouchPoint = previousTouchPoint;
    }

    public void reset() {
        mUndoneActions.clear();
        mActions.clear();
        mStrokes.clear();
    }

    public boolean handleOnTouchEvent(MotionEvent event, Matrix viewMatrix, Matrix inverseViewMatrix) {
        //compute the draw state
        if(getWriteMode() == WriteMode.PEN) {
            if(event.getButtonState() == MotionEvent.BUTTON_STYLUS_PRIMARY) {
                setDrawState(DrawState.ERASE);
            } else {
                setDrawState(DrawState.WRITE);
            }
        }
        else if(getWriteMode() == WriteMode.ERASER || event.getAction() != ACTION_DOWN_WITH_PRIMARY_STYLUS_BUTTON) {
            setDrawState(DrawState.ERASE);
        }

        //transform the cursor position using the inverse of the view matrix
        Vector2f currentTouchPoint = new Vector2f(event.getX(), event.getY()).transform(inverseViewMatrix);

        //set the previous touch point to the current touch point when the pen touches the canvas
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            previousTouchPoint = currentTouchPoint;
        }

        boolean result = false;
        CanvasPen pen = pens.get(getDrawState());
        if(pen != null) {
            result = pen.handleOnTouchEvent(event, currentTouchPoint);
        }


        //update the previousTouchPoint
        previousTouchPoint = currentTouchPoint;

        return result;
    }

    public void renderStrokes(Canvas canvas) {
        for(Stroke stroke : mStrokes) {
            mPaint.setColor(stroke.getColor());
            mPaint.setStrokeWidth(stroke.getWeight());
            canvas.drawPath(stroke, mPaint); // draw all paths on canvas
        }
        mPaint.setColor(mCurrentStroke.getColor());
        mPaint.setStrokeWidth(mCurrentStroke.getWeight());
        canvas.drawPath(mCurrentStroke, mPaint);
    }

    public boolean undo() {
        //undo the last action
        if(mActions.isEmpty()) {
            return false;
        }

        CanvasWriterAction currentAction = mActions.remove(mActions.size() - 1);
        switch(currentAction.type) {
            case WRITE:
                currentAction.type = CanvasWriterAction.Type.UNDO_WRITE;
                mUndoneActions.add(currentAction);
                mStrokes.remove(currentAction.stroke);
                return true;
            case ERASE:
                currentAction.type = CanvasWriterAction.Type.UNDO_ERASE;
                mUndoneActions.add(currentAction);
                mStrokes.add(currentAction.stroke);
                return true;
            default:
                return false;
        }
    }

    public boolean redo() {
        //undo the last action
        if(mUndoneActions.isEmpty()) {
            return false;
        }

        CanvasWriterAction currentAction = mUndoneActions.remove(mUndoneActions.size() - 1);
        switch(currentAction.type) {
            case UNDO_WRITE:
                currentAction.type = CanvasWriterAction.Type.WRITE;
                mActions.add(currentAction);
                mStrokes.add(currentAction.stroke);
                return true;
            case UNDO_ERASE:
                currentAction.type = CanvasWriterAction.Type.ERASE;
                mActions.add(currentAction);
                mStrokes.remove(currentAction.stroke);
                return true;
            default:
                return false;
        }
    }

}
