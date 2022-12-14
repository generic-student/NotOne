package app.notone.core.util;

import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;

import app.notone.core.Stroke;
import app.notone.core.Vector2f;

/**
 * Helper class to convert strokes,
 * that have been classified by the InkRecognizer as certain shapes,
 * to their shape.
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public class StrokeToShapeConverter {
    /** Tag for logging*/
    private static final String TAG = StrokeToShapeConverter.class.getSimpleName();

    /**
     * Converts a stroke to a rectangle.
     * This method changes the points that are stored in the Stroke Object.
     * @param stroke Stroke to be converted
     */
    public static void convertStrokeToRectangle(Stroke stroke) {
        RectF bounds = new RectF();
        stroke.computeBounds(bounds, true);
        stroke.reset();

        stroke.moveTo(bounds.left, bounds.top);
        stroke.lineTo(bounds.right, bounds.top);
        stroke.lineTo(bounds.right, bounds.bottom);
        stroke.lineTo(bounds.left, bounds.bottom);
        stroke.lineTo(bounds.left, bounds.top);

    }

    /**
     * Converts a stroke to a triangle.
     * This method changes the points that are stored in the Stroke Object.
     * @param stroke Stroke to be converted
     */
    public static void convertStrokeToTriangle(Stroke stroke) {
        RectF bounds = new RectF();
        stroke.computeBounds(bounds, true);
        stroke.reset();

        stroke.moveTo(bounds.left, bounds.bottom);
        stroke.lineTo(bounds.right - bounds.width() / 2, bounds.top);
        stroke.lineTo(bounds.right, bounds.bottom);
        stroke.lineTo(bounds.left, bounds.bottom);
    }

    /**
     * Converts a stroke to an ellipse.
     * This method changes the points that are stored in the Stroke Object.
     * @param stroke Stroke to be converted
     */
    public static void convertStrokeToEllipse(Stroke stroke) {
        RectF bounds = new RectF();
        stroke.computeBounds(bounds, true);
        stroke.reset();

        Path oval = new Path();
        oval.addOval(bounds, Path.Direction.CW);
        float[] points = oval.approximate(0.1f);
        stroke.moveTo(points[1], points[2]);
        for(int i = 3; i < points.length; i+=3) {
            stroke.lineTo(points[i+1], points[i+2]);
        }
        stroke.lineTo(points[1], points[2]);

    }

    /**
     * Converts a Stroke to a line.
     * This method changes the points that are stored in the Stroke Object.
     * @param stroke Stroke to be converted
     */
    public static void convertStrokeToArrow(Stroke stroke) {
        Log.d(TAG, "support for arrows is pending.");
    }
}
