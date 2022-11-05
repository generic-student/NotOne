package app.notone;

import android.graphics.Matrix;

public class Point2D {
    public float x;
    public float y;

    public Point2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float distance(Point2D other) {
        return (float) Math.hypot(x - other.x, y - other.y);
    }

    public float dotProduct(Point2D other) {
        return x * other.x + y * other.y;
    }

    public float crossProduct(Point2D other) {
        return x * other.y - y * other.x;
    }

    public Point2D subtract(Point2D other) {
        return new Point2D(x - other.x, y - other.y);
    }

    public Point2D add(Point2D other) {
        return new Point2D(x + other.y, y + other.y);
    }

    public Point2D transform(Matrix mat) {
        float[] pts = new float[]{x, y};
        mat.mapPoints(pts);
        return new Point2D(pts[0], pts[1]);
    }
}
