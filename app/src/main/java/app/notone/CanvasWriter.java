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

    private Paint mPaint;
    private float mStrokeWeight;
    private int mStrokeColor;

    private final ArrayList<CanvasWriterAction> mUndoneActions;
    private final ArrayList<CanvasWriterAction> mActions;

    private ArrayList<Stroke> mStrokes; // contains all Paths already drawn by user Path, Color, Weight
    private Stroke mCurrentStroke; //the path that the user is currently drawing

    public enum DrawState {
        WRITE, ERASE
    }
    private CanvasWriter.DrawState mDrawState = CanvasWriter.DrawState.WRITE;

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

        if(mCurrentStroke.getPath().isEmpty()) {
            mCurrentStroke.setWeight(mStrokeWeight);
        }
    }

    public int getStrokeColor() {
        return mStrokeColor;
    }

    public void setStrokeColor(int mStrokeColor) {
        this.mStrokeColor = mStrokeColor;

        if(mCurrentStroke.getPath().isEmpty()) {
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

    public boolean handleOnTouchEvent(MotionEvent event, Matrix viewMatrix, Matrix inverseViewMatrix) {
        //compute the draw state
        if(event.getButtonState() == MotionEvent.BUTTON_STYLUS_PRIMARY) {
            setDrawState(DrawState.ERASE);
        } else {
            setDrawState(DrawState.WRITE);
        }

        switch(getDrawState()) {
            case WRITE:
                return handleOnTouchEventWrite(event, viewMatrix, inverseViewMatrix);
            case ERASE:
                return handleOnTouchEventErase(event, viewMatrix, inverseViewMatrix);
        }

        return false;
    }

    private boolean handleOnTouchEventWrite(MotionEvent event, Matrix viewMatrix, Matrix inverseViewMatrix) {
        Point2D pos = new Point2D(event.getX(), event.getY()).transform(inverseViewMatrix);

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                moveTo(mCurrentStroke, pos);
                break;

            case MotionEvent.ACTION_MOVE:
                lineTo(mCurrentStroke, pos);
                return true;

            case MotionEvent.ACTION_UP:
                if(mCurrentStroke.getPath().isEmpty()) {
                    return false;
                }
                //delete the strokes that come after the currentStrokeIndex
                clearUndoneStrokes();
                //add the current stroke to the list of strokes
                mStrokes.add(mCurrentStroke);
                //add the action the the list
                mActions.add(new CanvasWriterAction(CanvasWriterAction.Type.WRITE, mCurrentStroke));
                //reset the current stroke
                mCurrentStroke = new Stroke(getStrokeColor(), getStrokeWeight());
                break;
        }

        return false;
    }

    private boolean handleOnTouchEventErase(MotionEvent event, Matrix viewMatrix, Matrix inverseViewMatrix) {
        if(event.getAction() != ACTION_DOWN_WITH_PRIMARY_STYLUS_BUTTON) {
            return false;
        }

        //transform the cursor position using the inverse of the view matrix
        Point2D pos = new Point2D(event.getX(), event.getY()).transform(inverseViewMatrix);

        final float circleRadius = getStrokeWeight();
        //erase the strokes that the eraser touches using the Eraser Class
        final int strokesErased = erase(pos, circleRadius);
        //if at least one stroke was erased, invalidate the canvas
        return strokesErased > 0;
    }

    public void moveTo(Stroke currentStroke, Point2D point) {
        currentStroke.getPath().moveTo(point.x, point.y);
    }

    public void lineTo(Stroke currentStroke, Point2D point) {
        currentStroke.getPath().lineTo(point.x, point.y);
    }

    public int erase(Point2D eraserPosition, float eraserRadius) {
        int strokesErased = 0;
        RectF bounds = new RectF();

        //check if the current cursor position intersects the bounds of one of the strokes and remove it
        for(int i = 0; i < mStrokes.size(); i++) {
            mStrokes.get(i).getPath().computeBounds(bounds, true);

            //check if the outer bounds that encompass the entire path intersects with the cursor
            if(bounds.isEmpty() || bounds.contains(eraserPosition.x, eraserPosition.y)) {
                if(MathHelper.pathIntersectsCircle(mStrokes.get(i).getPath(), eraserPosition, eraserRadius)) {
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
            canvas.drawPath(stroke.getPath(), mPaint); // draw all paths on canvas
        }
        mPaint.setColor(mCurrentStroke.getColor());
        mPaint.setStrokeWidth(mCurrentStroke.getWeight());
        canvas.drawPath(mCurrentStroke.getPath(), mPaint);
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
