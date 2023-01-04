package app.notone.core.pens;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.util.ArrayList;

import app.notone.core.CanvasWriter;
import app.notone.core.CanvasWriterAction;
import app.notone.core.Stroke;
import app.notone.core.Vector2f;
import app.notone.core.util.MathHelper;
import app.notone.core.util.SAT;

/**
 * Pen used for erasing strokes from the canvas
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public class CanvasEraserPen extends CanvasPen{
    /** Defines the collision bounds of the eraser */
    public RectF mEraserBounds;

    public CanvasEraserPen(CanvasWriter writerReference) {
        super(writerReference);
        mEraserBounds = new RectF();
    }

    @Override
    public boolean handleOnTouchEvent(MotionEvent event, Vector2f currentTouchPoint) {
        final float circleRadius = mCanvasWriterRef.getStrokeWeight();
        //erase the strokes that the eraser touches using the Eraser Class
        final int strokesErased = erase(currentTouchPoint, circleRadius);
        //if at least one stroke was erased, invalidate the canvas
        return strokesErased > 0;
    }

    @Override
    public void render(Canvas canvas) {

    }

    @Override
    public void reset() {
        mEraserBounds = new RectF();
    }

    /**
     * Computes the new bounds with the current touch point in the center and a given side-length
     * @param eraserPosition The current touch position
     * @param eraserRadius The side-length of the bounds
     */
    private void computeEraserBounds(Vector2f eraserPosition, float eraserRadius) {

        //build a rectangle between the previous and current touch point
        mEraserBounds = new RectF(
                eraserPosition.x,
                eraserPosition.y,
                eraserPosition.x + eraserRadius,
                eraserPosition.y + eraserRadius);
        mEraserBounds.offset(- mEraserBounds.width() / 2, - mEraserBounds.height() / 2);

    }

    /**
     * Deletes all strokes that intersect with the eraserBounds
     * @param eraserPosition The current touch position
     * @param eraserRadius The side-length of the bounds
     * @return The amount of strokes that have been deleted
     */
    public int erase(Vector2f eraserPosition, float eraserRadius) {
        computeEraserBounds(eraserPosition, eraserRadius);

        RectF bounds = new RectF();
        float[] rectPts = MathHelper.rectToFloatArray(mEraserBounds);

        int strokesErased = 0;
        ArrayList<Stroke> strokes = mCanvasWriterRef.getStrokes();

        //check all strokes if the intersect with the bounds
        for(int i = 0; i < strokes.size(); i++) {
            strokes.get(i).computeBounds(bounds, true);
            float[] boundsPts = MathHelper.rectToFloatArray(bounds);

            //check if the bounding box intersects the bounds, if not then the stroke itself will not either
            if(SAT.rectangleRectangleIntersection(rectPts, boundsPts)) {

                ArrayList<Float> points = strokes.get(i).getPathPoints();
                //check if the path defined by the points in the stroke intersect with the bounds
                //if there is only one point (x, y) in the path check if that point lies inside the bounds
                final boolean intersects = (strokes.get(i).getPathPoints().size() == 2) ?
                        SAT.rectangularPointRectangleIntersection(points.get(0), points.get(1), strokes.get(i).getWeight(), rectPts) :
                        SAT.linesRectangleIntersection(points, rectPts);

                //remove the stroke if an intersection has been found
                if(intersects) {
                    Stroke erasedStroke = strokes.remove(i);
                    mCanvasWriterRef.getUndoRedoManager().addAction(new CanvasWriterAction(CanvasWriterAction.Type.ERASE, erasedStroke));
                    strokesErased++;
                }
            }
        }

        return strokesErased;
    }
}
