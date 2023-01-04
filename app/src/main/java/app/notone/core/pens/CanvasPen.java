package app.notone.core.pens;

import android.graphics.Canvas;
import android.view.MotionEvent;

import app.notone.core.CanvasWriter;
import app.notone.core.Vector2f;

/**
 * Abstract class describing all pens that can interact with the canvas
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public abstract class CanvasPen {
    /** Reference to the writer which contains all drawn strokes */
    protected CanvasWriter mCanvasWriterRef;

    public CanvasPen(CanvasWriter writerReference) {
        this.mCanvasWriterRef = writerReference;
    }

    public void setCanvasWriterRef(CanvasWriter mCanvasWriterRef) {
        this.mCanvasWriterRef = mCanvasWriterRef;
    }

    /**
     * Handles what happens when the pen touches the canvas
     * @param event The touch event
     * @param currentTouchPoint The current point on the canvas that was touched
     * @return True if the canvas needs to be redrawn (invalidated)
     */
    public abstract boolean handleOnTouchEvent(MotionEvent event, Vector2f currentTouchPoint);

    /**
     * Renders additional content that is only visible when the pen is selected
     * @param canvas The canvas to be rendered to
     */
    public abstract void render(Canvas canvas);

    /**
     * Resets the pen to its default state
     */
    public abstract void reset();
}
