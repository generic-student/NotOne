package app.notone.core.pens;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

import app.notone.core.CanvasWriter;
import app.notone.core.CanvasWriterAction;
import app.notone.core.Stroke;
import app.notone.core.Vector2f;

public class CanvasWriterPen extends CanvasPen {
    Stroke currentStroke;

    public CanvasWriterPen(CanvasWriter writerReference) {
        super(writerReference);

        currentStroke = new Stroke(writerReference.getStrokeColor(), writerReference.getStrokeWeight());
    }

    @Override
    public boolean handleOnTouchEvent(MotionEvent event, Vector2f currentTouchPoint) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                currentStroke.moveTo(currentTouchPoint.x, currentTouchPoint.y);
                break;

            case MotionEvent.ACTION_MOVE:
                currentStroke.lineTo(currentTouchPoint.x, currentTouchPoint.y);
                return true;

            case MotionEvent.ACTION_UP:
                if(currentStroke.isEmpty()) {
                    return false;
                }
                //delete the strokes that come after the currentStrokeIndex
                clearUndoneStrokes();
                //add the current stroke to the list of strokes
                canvasWriterRef.getStrokes().add(currentStroke);
                //add the action the the list
                canvasWriterRef.getUndoRedoManager().addAction(new CanvasWriterAction(CanvasWriterAction.Type.WRITE, currentStroke));
                //reset the current stroke
                currentStroke = new Stroke(canvasWriterRef.getStrokeColor(), canvasWriterRef.getStrokeWeight());

                return true;
        }

        return false;
    }

    @Override
    public void render(Canvas canvas) {
        Paint paint = canvasWriterRef.getPaint();

        paint.setColor(currentStroke.getColor());
        paint.setStrokeWidth(currentStroke.getWeight());
        canvas.drawPath(currentStroke, paint);
    }

    private void clearUndoneStrokes() {
        canvasWriterRef.getUndoRedoManager().getUndoneActions().clear();
    }

    public Stroke getCurrentStroke() {
        return currentStroke;
    }
}
