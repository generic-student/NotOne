package app.notone.core.pens;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

import app.notone.core.CanvasWriter;
import app.notone.core.CanvasWriterAction;
import app.notone.core.Stroke;
import app.notone.core.Vector2f;

/**
 * Handles adding strokes to the canvas (writing strokes)
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public class CanvasWriterPen extends CanvasPen {
    /** The current stroke that is being written */
    private Stroke mCurrentStroke;

    public CanvasWriterPen(CanvasWriter writerReference) {
        super(writerReference);

        mCurrentStroke = new Stroke(writerReference.getStrokeColor(), writerReference.getStrokeWeight());
    }

    @Override
    public boolean handleOnTouchEvent(MotionEvent event, Vector2f currentTouchPoint) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mCurrentStroke.moveTo(currentTouchPoint.x, currentTouchPoint.y);
                break;

            case MotionEvent.ACTION_MOVE:
                mCurrentStroke.lineTo(currentTouchPoint.x, currentTouchPoint.y);
                return true;

            case MotionEvent.ACTION_UP:
                if(mCurrentStroke.isEmpty()) {
                    return false;
                }
                //clear the redo tree since a new branch has been created
                clearUndoneStrokes();
                //add the current stroke to the list of strokes
                mCanvasWriterRef.getStrokes().add(mCurrentStroke);
                //add the action the the list
                mCanvasWriterRef.getUndoRedoManager().addAction(new CanvasWriterAction(CanvasWriterAction.Type.WRITE, mCurrentStroke));
                //reset the current stroke
                mCurrentStroke = new Stroke(mCanvasWriterRef.getStrokeColor(), mCanvasWriterRef.getStrokeWeight());

                return true;
        }

        return false;
    }

    @Override
    public void render(Canvas canvas) {
        Paint paint = mCanvasWriterRef.getPaint();

        paint.setColor(mCurrentStroke.getColor());
        paint.setStrokeWidth(mCurrentStroke.getWeight());
        canvas.drawPath(mCurrentStroke, paint);
    }

    @Override
    public void reset() {
        mCurrentStroke.reset();
    }

    /**
     * Clears the list of undone actions (the redo tree)
     */
    private void clearUndoneStrokes() {
        mCanvasWriterRef.getUndoRedoManager().getUndoneActions().clear();
    }

    /**
     * Sets the color of the current stroke that is being drawn
     * @param c color
     */
    public void setStrokeColor(int c) {
        mCurrentStroke.setColor(c);
    }

    /**
     * Sets the strokeWeight of the current stroke being drawn
     * @param weight
     */
    public void setStrokeWeight(float weight) {
        mCurrentStroke.setWeight(weight);
    }
}
