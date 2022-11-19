package app.notone;

import android.graphics.Matrix;

/**
 * Simple class to store a 2-dimensional vector as floating point values
 */
public class Vector2f {
    public float x;
    public float y;

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * computes the euclidean distance between two vectors
     * @param other
     * @return
     */
    public float distance(Vector2f other) {
        return (float) Math.hypot(x - other.x, y - other.y);
    }

    /**
     * computes the dot product of 2 vectors
     * @param other
     * @return
     */
    public float dotProduct(Vector2f other) {
        return x * other.x + y * other.y;
    }

    /**
     * computes the cross product of 2 points
     * @param other
     * @return
     */
    public float crossProduct(Vector2f other) {
        return x * other.y - y * other.x;
    }


    /**
     * element wise subtraction of vectors
     * @param other
     * @return
     */
    public Vector2f subtract(Vector2f other) {
        return new Vector2f(x - other.x, y - other.y);
    }

    /**
     * element wise addition of vectors
     * @param other
     * @return
     */
    public Vector2f add(Vector2f other) {
        return new Vector2f(x + other.y, y + other.y);
    }

    public Vector2f divide(float value) {
        return new Vector2f(x/value, y/value);
    }

    public Vector2f multiply(float value) {
        return new Vector2f(x * value, y * value);
    }

    public float length() {
        return (float)Math.hypot(x, y);
    }

    public Vector2f unitVector() {
        return this.divide(this.length());
    }

    public float angle(Vector2f other) {
        float angle = (float) (Math.atan2(other.y - y, other.x - x));

        if(angle < 0){
            angle += 360;
        }

        return angle;
    }

    /**
     * multiplies the vector by a given 2x2 matrix
     * @param mat
     * @return
     */
    public Vector2f transform(Matrix mat) {
        float[] pts = new float[]{x, y};
        mat.mapPoints(pts);
        return new Vector2f(pts[0], pts[1]);
    }
}
