package app.notone.core.pens;

import android.view.MotionEvent;

import app.notone.core.CanvasWriter;
import app.notone.core.CanvasWriterAction;
import app.notone.core.Stroke;
import app.notone.core.Vector2f;

public class CanvasWriterPen extends CanvasPen{
    public CanvasWriterPen(CanvasWriter writerReference) {
        super(writerReference);
    }

    @Override
    public boolean handleOnTouchEvent(MotionEvent event, Vector2f currentTouchPoint) {
        Stroke currentStroke = canvasWriterRef.getCurrentStroke();
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
                canvasWriterRef.getActions().add(new CanvasWriterAction(CanvasWriterAction.Type.WRITE, currentStroke));
                //reset the current stroke
                canvasWriterRef.setCurrentStroke(new Stroke(canvasWriterRef.getStrokeColor(), canvasWriterRef.getStrokeWeight()));

                return true;
        }

        return false;
    }

    private void clearUndoneStrokes() {
        canvasWriterRef.getUndoneActions().clear();
    }
}
