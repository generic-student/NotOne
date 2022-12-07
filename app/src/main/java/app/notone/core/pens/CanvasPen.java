package app.notone.core.pens;

import android.graphics.Canvas;
import android.view.MotionEvent;

import app.notone.core.CanvasWriter;
import app.notone.core.Vector2f;

/**
 * Abstract class describing all pens that can interact with the canvas
 */
public abstract class CanvasPen {
    //reference to the writer which contains all drawn strokes
    protected CanvasWriter mCanvasWriterRef;

    public CanvasPen(CanvasWriter writerReference) {
        this.mCanvasWriterRef = writerReference;
    }

    public void setCanvasWriterRef(CanvasWriter mCanvasWriterRef) {
        this.mCanvasWriterRef = mCanvasWriterRef;
    }

    /**
     * Handles what happens when the pen touches the canvas
     * @param event the touch event
     * @param currentTouchPoint the current point on the canvas that was touched
     * @return true if the canvas needs to be redrawn (invalidated)
     */
    public abstract boolean handleOnTouchEvent(MotionEvent event, Vector2f currentTouchPoint);

    /**
     * Renders additional content that is only visible when the pen is selected
     * @param canvas
     */
    public abstract void render(Canvas canvas);

    /**
     * Resets the pen to its default state
     */
    public abstract void reset();
}
