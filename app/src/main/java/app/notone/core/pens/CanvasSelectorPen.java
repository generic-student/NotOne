package app.notone.core.pens;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.util.ArrayList;

import app.notone.core.CanvasWriter;
import app.notone.core.Stroke;
import app.notone.core.Vector2f;

/**
 * Handles selecting strokes
 */
public class CanvasSelectorPen extends CanvasPen {
    private boolean mIsSelecting;
    private RectF mSelectionBounds;
    //how the selectionBounds should be rendered
    private final Paint mSelectionBorderPaint;
    //how the selected strokes should be rendered
    private final Paint mSelectedStrokesPaint;

    private final ArrayList<Stroke> mSelectedStrokes;

    public CanvasSelectorPen(CanvasWriter writerReference) {
        super(writerReference);

        mIsSelecting = false;
        mSelectionBounds = new RectF(0, 0, 0, 0);

        mSelectionBorderPaint = new Paint();
        mSelectionBorderPaint.setStyle(Paint.Style.STROKE);
        mSelectionBorderPaint.setPathEffect(new DashPathEffect(new float[]{10f, 20f}, 0));
        mSelectionBorderPaint.setColor(Color.BLACK);
        mSelectionBorderPaint.setStrokeWidth(5);

        mSelectedStrokesPaint = new Paint();
        mSelectedStrokesPaint.setStyle(Paint.Style.STROKE);
        mSelectedStrokesPaint.setColor(Color.LTGRAY);
        mSelectedStrokesPaint.setStrokeWidth(5);

        mSelectedStrokes = new ArrayList<>();
    }

    @Override
    public boolean handleOnTouchEvent(MotionEvent event, Vector2f currentTouchPoint) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsSelecting = true;
                mSelectionBounds.left = currentTouchPoint.x;
                mSelectionBounds.top = currentTouchPoint.y;
                mSelectionBounds.right = currentTouchPoint.x;
                mSelectionBounds.bottom = currentTouchPoint.y;
                break;

            case MotionEvent.ACTION_MOVE:
                mSelectionBounds.right = currentTouchPoint.x;
                mSelectionBounds.bottom = currentTouchPoint.y;
                break;

            case MotionEvent.ACTION_UP:
                mIsSelecting = false;

                //correct the bounds if the user has been selecting e.g. from bottom-right to top-left
                correctSelectionBounds();
                //select the strokes in the selectionRect
                selectStrokes();

                return true;
        }

        return mIsSelecting;
    }

    @Override
    public void render(Canvas canvas) {
        for(Stroke stroke : mSelectedStrokes) {
            canvas.drawPath(stroke, mSelectedStrokesPaint);
        }

        if(mIsSelecting)
            canvas.drawRect(mSelectionBounds, mSelectionBorderPaint);

    }

    @Override
    public void reset() {
        mSelectedStrokes.clear();
        mIsSelecting = false;
        mSelectionBounds = new RectF(0, 0, 0, 0);
    }

    /**
     * ensures that the bottom-right corner of the selectionBounds is actually on the bottom-right
     */
    private void correctSelectionBounds() {
        if(mSelectionBounds.left > mSelectionBounds.right) {
            final float left = mSelectionBounds.left;
            mSelectionBounds.left = mSelectionBounds.right;
            mSelectionBounds.right = left;
        }

        if(mSelectionBounds.top > mSelectionBounds.bottom) {
            final float top = mSelectionBounds.top;
            mSelectionBounds.top = mSelectionBounds.bottom;
            mSelectionBounds.bottom = top;
        }
    }

    /**
     * Select all strokes whose bounding boxes intersect with the selectionBounds
     */
    private void selectStrokes() {
        mSelectedStrokes.clear();

        RectF bounds = new RectF();
        for(Stroke stroke : mCanvasWriterRef.getStrokes()) {
            stroke.computeBounds(bounds, false);

            if(mSelectionBounds.contains(bounds)) {
                mSelectedStrokes.add(stroke);
            }
        }
    }
}
