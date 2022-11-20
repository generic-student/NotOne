package app.notone;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.io.Serializable;
import java.util.ArrayList;

public class CanvasWriter implements Serializable {
    private static final int ACTION_DOWN_WITH_PRIMARY_STYLUS_BUTTON = 213;
    private static final String LOG_TAG = CanvasWriter.class.getSimpleName();

    private transient Paint mPaint;
    private float mStrokeWeight;
    private int mStrokeColor;

    private ArrayList<CanvasWriterAction> mUndoneActions;
    private ArrayList<CanvasWriterAction> mActions;

    private ArrayList<Stroke> mStrokes; // contains all Paths already drawn by user Path, Color, Weight
    private Stroke mCurrentStroke; //the path that the user is currently drawing

    private Vector2f previousTouchPoint = new Vector2f(0, 0);

    /*  draw state is whats currently happening, write mode determines what can happen */
    public enum DrawState {
        WRITE, ERASE
    }
    private CanvasWriter.DrawState mDrawState = CanvasWriter.DrawState.WRITE;

    private WriteMode mWritemode = WriteMode.PEN;

    private double mLinearRegressionBound = 0.7;

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
    }

    public void initDefaultPaint() {
        this.mPaint = new Paint();
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setStrokeJoin(Paint.Join.ROUND);
        this.mPaint.setStrokeCap(Paint.Cap.ROUND);
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

    public WriteMode getWritemode() {
        return mWritemode;
    }

    public void setWritemode(WriteMode mWritemode) {
        this.mWritemode = mWritemode;
    }

    public void setLinearRegressorBound(double bound) {
        this.mLinearRegressionBound = bound;
    }

    public boolean handleOnTouchEvent(MotionEvent event, Matrix viewMatrix, Matrix inverseViewMatrix) {
        //compute the draw state

        // Set Draw State based on Write Mode
        if(getWritemode() == WriteMode.PEN || getWritemode() == WriteMode.PATTERN) {
            if(event.getButtonState() == MotionEvent.BUTTON_STYLUS_PRIMARY) {
                setDrawState(DrawState.ERASE);
            } else {
                setDrawState(DrawState.WRITE);
            }
        } else if(getWritemode() == WriteMode.ERASER || event.getAction() != ACTION_DOWN_WITH_PRIMARY_STYLUS_BUTTON) {
            setDrawState(DrawState.ERASE);
        }

        //transform the cursor position using the inverse of the view matrix
        Vector2f currentTouchPoint = new Vector2f(event.getX(), event.getY()).transform(inverseViewMatrix);

        //set the previous touch point to the current touch point when the pen touches the canvas
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            previousTouchPoint = currentTouchPoint;
        }

        // handle touch event based on draw state
        boolean result = false;
        switch(getDrawState()) {
            case WRITE:
                result = handleOnTouchEventWrite(event, currentTouchPoint);
                break;
            case ERASE:
                result = handleOnTouchEventErase(event, currentTouchPoint, inverseViewMatrix);
                break;
        }

        //update the previousTouchPoint
        previousTouchPoint = currentTouchPoint;

        return result;
    }

    private boolean handleOnTouchEventWrite(MotionEvent event, Vector2f pos) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                moveTo(mCurrentStroke, pos);
                return false;

            case MotionEvent.ACTION_MOVE:
                lineTo(mCurrentStroke, pos);
                return true;

            case MotionEvent.ACTION_UP:
                if(mCurrentStroke.isEmpty()) {
                    return false;
                }
                break;

            default:
                return false;
        }

        //delete the strokes that come after the currentStrokeIndex
        clearUndoneStrokes();
        // check if the current stroke was a line
        setWritemode(WriteMode.PATTERN);
        if(getWritemode() == WriteMode.PATTERN) {
            if (PatternRecognizer.isLine(mLinearRegressionBound, mCurrentStroke.getPathPoints())) {
                mCurrentStroke.setPathPoints(PatternRecognizer.getStraight(mCurrentStroke));
                mCurrentStroke.initPathFromPathPoints();
            } else if (PatternRecognizer.isSquare(mLinearRegressionBound, mCurrentStroke.getPathPoints())) {
                mCurrentStroke.setPathPoints(PatternRecognizer.getSquare(mCurrentStroke));
                mCurrentStroke.initPathFromPathPoints();
            }
        }
        //add the current stroke to the list of strokes
        mStrokes.add(mCurrentStroke);
        //add the action the the list
        mActions.add(new CanvasWriterAction(CanvasWriterAction.Type.WRITE, mCurrentStroke));
        //reset the current stroke
        mCurrentStroke = new Stroke(getStrokeColor(), getStrokeWeight());

        return true;
    }

    private boolean handleOnTouchEventErase(MotionEvent event, Vector2f pos, Matrix inverseViewMatrix) {
        final float circleRadius = getStrokeWeight();
        //erase the strokes that the eraser touches using the Eraser Class
        final int strokesErased = erase(pos, circleRadius, inverseViewMatrix);
        //if at least one stroke was erased, invalidate the canvas
        return strokesErased > 0;
    }

    public void moveTo(Stroke currentStroke, Vector2f point) {
        currentStroke.moveTo(point.x, point.y);
    }

    public void lineTo(Stroke currentStroke, Vector2f point) {
        currentStroke.lineTo(point.x, point.y);
    }

    public int erase(Vector2f eraserPosition, float eraserRadius, Matrix inverseViewMatrix) {
        Vector2f touchCenter = previousTouchPoint.add(eraserPosition.subtract(previousTouchPoint).divide(2));

        //build a rectangle between the previous and current touch point
        RectF rect = new RectF(
                touchCenter.x,
                touchCenter.y,
                touchCenter.x + eraserRadius,
                touchCenter.y + eraserRadius);
        rect.offset(- rect.width() / 2, - rect.height() / 2);
        //inverseViewMatrix.mapRect(rect);
        Matrix m = new Matrix();
        m.setRotate(previousTouchPoint.angle(eraserPosition), rect.centerX(), rect.centerY());

        RectF bounds = new RectF();
        float[] rectPts = MathHelper.rectToFloatArray(rect);
        m.mapRect(rect);
        m.mapPoints(rectPts);

        int strokesErased = 0;

        for(int i = 0; i < mStrokes.size(); i++) {
            mStrokes.get(i).computeBounds(bounds, true);
            float boundsPts[] = MathHelper.rectToFloatArray(bounds);

            if(SAT.rectangleRectangleIntersection(rectPts, boundsPts)) {
                if(SAT.LinesRectangleIntersection(mStrokes.get(i).getPathPoints(), rectPts)) {
                    Stroke erasedStroke = mStrokes.remove(i);
                    mActions.add(new CanvasWriterAction(CanvasWriterAction.Type.ERASE, erasedStroke));
                    strokesErased++;
                }
            }
        }

        return strokesErased;
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

    private void clearUndoneStrokes() {
        mUndoneActions.clear();
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
