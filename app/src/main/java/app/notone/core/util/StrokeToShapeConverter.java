package app.notone.core.util;

import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;

import app.notone.core.Stroke;

public class StrokeToShapeConverter {
    private static final String TAG = StrokeToShapeConverter.class.getSimpleName();

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

    public static void convertStrokeToTriangle(Stroke stroke) {
        Log.d(TAG, "support for triangles is pending.");
    }

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

    public static void convertStrokeToArrow(Stroke stroke) {
        Log.d(TAG, "support for arrows is pending.");
    }
}
