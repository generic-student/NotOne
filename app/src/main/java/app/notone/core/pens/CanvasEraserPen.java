package app.notone.core.pens;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.util.ArrayList;

import app.notone.core.CanvasWriter;
import app.notone.core.CanvasWriterAction;
import app.notone.core.Stroke;
import app.notone.core.Vector2f;
import app.notone.core.util.MathHelper;
import app.notone.core.util.SAT;

public class CanvasEraserPen extends CanvasPen{
    public CanvasEraserPen(CanvasWriter writerReference) {
        super(writerReference);
    }

    @Override
    public boolean handleOnTouchEvent(MotionEvent event, Vector2f currentTouchPoint) {
        final float circleRadius = canvasWriterRef.getStrokeWeight();
        //erase the strokes that the eraser touches using the Eraser Class
        final int strokesErased = erase(currentTouchPoint, circleRadius);
        //if at least one stroke was erased, invalidate the canvas
        return strokesErased > 0;
    }

    public int erase(Vector2f eraserPosition, float eraserRadius) {
        final Vector2f previousTouchPoint = canvasWriterRef.getPreviousTouchPoint();

        final Vector2f touchCenter = previousTouchPoint.add(eraserPosition.subtract(previousTouchPoint).divide(2));

        //build a rectangle between the previous and current touch point
        RectF rect = new RectF(
                touchCenter.x,
                touchCenter.y,
                touchCenter.x + eraserRadius,
                touchCenter.y + eraserRadius);
        rect.offset(- rect.width() / 2, - rect.height() / 2);
        //inverseViewMatrix.mapRect(rect);
        Matrix m = new Matrix();
        m.setRotate(previousTouchPoint.angle(eraserPosition), rect.centerX(), rect.centerY());

        RectF bounds = new RectF();
        float[] rectPts = MathHelper.rectToFloatArray(rect);
        m.mapRect(rect);
        m.mapPoints(rectPts);

        int strokesErased = 0;
        ArrayList<Stroke> strokes = canvasWriterRef.getStrokes();

        for(int i = 0; i < strokes.size(); i++) {
            strokes.get(i).computeBounds(bounds, true);
            float boundsPts[] = MathHelper.rectToFloatArray(bounds);

            if(SAT.rectangleRectangleIntersection(rectPts, boundsPts)) {

                ArrayList<Float> points = strokes.get(i).getPathPoints();
                final boolean intersects = (strokes.get(i).getPathPoints().size() == 2) ?
                        SAT.rectangularPointRectangleIntersection(points.get(0), points.get(1), strokes.get(i).getWeight(), rectPts) :
                        SAT.linesRectangleIntersection(points, rectPts);


                if(intersects) {
                    Stroke erasedStroke = strokes.remove(i);
                    canvasWriterRef.getActions().add(new CanvasWriterAction(CanvasWriterAction.Type.ERASE, erasedStroke));
                    strokesErased++;
                }
            }
        }

        return strokesErased;
    }
}
