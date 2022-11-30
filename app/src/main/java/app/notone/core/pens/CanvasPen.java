package app.notone.core.pens;

import android.graphics.Matrix;
import android.view.MotionEvent;

import app.notone.core.CanvasWriter;
import app.notone.core.Stroke;
import app.notone.core.Vector2f;

public abstract class CanvasPen {
    protected CanvasWriter canvasWriterRef;

    //constructor
    public CanvasPen(CanvasWriter writerReference) {
        this.canvasWriterRef = writerReference;
    }

    //setter
    public void setCanvasWriterRef(CanvasWriter canvasWriterRef) {
        this.canvasWriterRef = canvasWriterRef;
    }

    public abstract boolean handleOnTouchEvent(MotionEvent event, Vector2f currentTouchPoint);
}
