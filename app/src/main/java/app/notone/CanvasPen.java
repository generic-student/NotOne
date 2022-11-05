package app.notone;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.util.ArrayList;

public class CanvasPen {
    private static final int ACTION_DOWN_WITH_PRIMARY_STYLUS_BUTTON = 213;

    private Paint mPaint;
    private float mStrokeWeight;
    private int mStrokeColor;

    private ArrayList<Stroke> mStrokes; // contains all Paths drawn by user Path, Color, Weight
    private int mCurrentStrokeIndex = 0;

    public enum DrawState {
        WRITE, ERASE
    }
    private CanvasPen.DrawState mDrawState = CanvasPen.DrawState.WRITE;

    public CanvasPen(float mStrokeWeight, int mStrokeColor) {
        this.mStrokeWeight = mStrokeWeight;
        this.mStrokeColor = mStrokeColor;

        this.mPaint = new Paint();
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setStrokeJoin(Paint.Join.ROUND);
        this.mPaint.setStrokeCap(Paint.Cap.ROUND);

        mStrokes = new ArrayList<>();
        mStrokes.add(new Stroke(getStrokeColor(), getStrokeWeight()));
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

        if(mStrokes.get(mCurrentStrokeIndex).getPath().isEmpty()) {
            mStrokes.get(mCurrentStrokeIndex).setWeight(mStrokeWeight);
        }
    }

    public int getStrokeColor() {
        return mStrokeColor;
    }

    public void setStrokeColor(int mStrokeColor) {
        this.mStrokeColor = mStrokeColor;

        if(mStrokes.get(mCurrentStrokeIndex).getPath().isEmpty()) {
            mStrokes.get(mCurrentStrokeIndex).setColor(mStrokeColor);
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

    public int getCurrentStrokeIndex() {
        return mCurrentStrokeIndex;
    }

    public void setCurrentStrokeIndex(int mCurrentStrokeIndex) {
        this.mCurrentStrokeIndex = mCurrentStrokeIndex;
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
                moveTo(mStrokes.get(mCurrentStrokeIndex), pos);
                break;

            case MotionEvent.ACTION_MOVE:
                lineTo(mStrokes.get(mCurrentStrokeIndex), pos);
                return true;

            case MotionEvent.ACTION_UP:
                mStrokes.add(new Stroke(getStrokeColor(), getStrokeWeight())); // prep empty next
                mCurrentStrokeIndex++;
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
        if(strokesErased > 0) {
            mCurrentStrokeIndex -= strokesErased;
            return true;
        }

        return false;
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
        for(int i = mStrokes.size() - 1; i >= 0; i--) {
            mStrokes.get(i).getPath().computeBounds(bounds, true);

            //check if the outer bounds that encompass the entire path intersects with the cursor
            if(bounds.isEmpty() || bounds.contains(eraserPosition.x, eraserPosition.y)) {
                if(MathHelper.pathIntersectsCircle(mStrokes.get(i).getPath(), eraserPosition, eraserRadius)) {
                    mStrokes.remove(i);
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
    }

}
