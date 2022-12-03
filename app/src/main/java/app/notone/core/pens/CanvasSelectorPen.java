package app.notone.core.pens;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.util.ArrayList;

import app.notone.R;
import app.notone.core.CanvasWriter;
import app.notone.core.Stroke;
import app.notone.core.Vector2f;

public class CanvasSelectorPen extends CanvasPen {
    private boolean mIsSelecting;
    private RectF mSelectionRect;
    private Paint mSelectionBorderPaint;
    private Paint mSelectedStrokesPaint;

    private ArrayList<Stroke> selectedStrokes;

    public CanvasSelectorPen(CanvasWriter writerReference) {
        super(writerReference);

        mIsSelecting = false;
        mSelectionRect = new RectF(0, 0, 0, 0);

        mSelectionBorderPaint = new Paint();
        mSelectionBorderPaint.setStyle(Paint.Style.STROKE);
        mSelectionBorderPaint.setPathEffect(new DashPathEffect(new float[]{10f, 20f}, 0));
        mSelectionBorderPaint.setColor(Color.BLACK);
        mSelectionBorderPaint.setStrokeWidth(5);

        mSelectedStrokesPaint = new Paint();
        mSelectedStrokesPaint.setStyle(Paint.Style.STROKE);
        mSelectedStrokesPaint.setColor(Color.LTGRAY);
        mSelectedStrokesPaint.setStrokeWidth(5);

        selectedStrokes = new ArrayList<>();
    }

    @Override
    public boolean handleOnTouchEvent(MotionEvent event, Vector2f currentTouchPoint) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsSelecting = true;
                mSelectionRect.left = currentTouchPoint.x;
                mSelectionRect.top = currentTouchPoint.y;
                mSelectionRect.right = currentTouchPoint.x;
                mSelectionRect.bottom = currentTouchPoint.y;
                break;

            case MotionEvent.ACTION_MOVE:
                mSelectionRect.right = currentTouchPoint.x;
                mSelectionRect.bottom = currentTouchPoint.y;
                break;

            case MotionEvent.ACTION_UP:
                mIsSelecting = false;

                //
                correctSelectionBounds();
                //select the strokes in the selectionRect
                selectStrokes();

                return true;
        }

        return mIsSelecting;
    }

    @Override
    public void render(Canvas canvas) {
        for(Stroke stroke : selectedStrokes) {
            canvas.drawPath(stroke, mSelectedStrokesPaint);
        }

        if(mIsSelecting)
            canvas.drawRect(mSelectionRect, mSelectionBorderPaint);

    }

    @Override
    public void reset() {
        selectedStrokes.clear();
        mIsSelecting = false;
        mSelectionRect = new RectF(0, 0, 0, 0);
    }

    private void correctSelectionBounds() {
        if(mSelectionRect.left > mSelectionRect.right) {
            final float left = mSelectionRect.left;
            mSelectionRect.left = mSelectionRect.right;
            mSelectionRect.right = left;
        }

        if(mSelectionRect.top > mSelectionRect.bottom) {
            final float top = mSelectionRect.top;
            mSelectionRect.top = mSelectionRect.bottom;
            mSelectionRect.bottom = top;
        }
    }

    private void selectStrokes() {
        selectedStrokes.clear();

        RectF bounds = new RectF();
        for(Stroke stroke : canvasWriterRef.getStrokes()) {
            stroke.computeBounds(bounds, false);

            if(mSelectionRect.contains(bounds)) {
                selectedStrokes.add(stroke);
            }
        }
    }
}
