package app.notone.core.pens;

import android.graphics.Canvas;
import android.view.MotionEvent;

import app.notone.core.CanvasWriter;
import app.notone.core.Vector2f;

public class CanvasSelectorPen extends CanvasPen {
    public CanvasSelectorPen(CanvasWriter writerReference) {
        super(writerReference);
    }

    @Override
    public boolean handleOnTouchEvent(MotionEvent event, Vector2f currentTouchPoint) {
        return false;
    }

    @Override
    public void render(Canvas canvas) {

    }
}
