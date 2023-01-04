package app.notone.core;

import android.graphics.Matrix;

/**
 * Simple class to store a 2-dimensional vector as floating point values
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public class Vector2f {
    public float x;
    public float y;

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Computes the euclidean distance between two vectors
     * @param other Vector2f
     * @return Euclidean distance between the vectors
     */
    public float distance(Vector2f other) {
        return (float) Math.hypot(x - other.x, y - other.y);
    }

    /**
     * Computes the dot product of 2 vectors
     * @param other Vector2f
     * @return 2d dot product
     */
    public float dotProduct(Vector2f other) {
        return x * other.x + y * other.y;
    }

    /**
     * Computes the cross product of 2 points
     * @param other Vector2f
     * @return 2d cross product
     */
    public float crossProduct(Vector2f other) {
        return x * other.y - y * other.x;
    }


    /**
     * Element-wise subtraction of vectors
     * @param other Vector2f
     * @return Resulting Vector2f
     */
    public Vector2f subtract(Vector2f other) {
        return new Vector2f(x - other.x, y - other.y);
    }

    /**
     * Element wise addition of vectors
     * @param other Vector2f
     * @return Resulting Vector2f
     */
    public Vector2f add(Vector2f other) {
        return new Vector2f(x + other.y, y + other.y);
    }

    /**
     * Divides the vector by a number
     * @param value Number
     * @return Resulting Vector2f
     */
    public Vector2f divide(float value) {
        return new Vector2f(x/value, y/value);
    }

    /**
     * Multiplies the vector by a number
     * @param value Number
     * @return Resulting Vector2f
     */
    public Vector2f multiply(float value) {
        return new Vector2f(x * value, y * value);
    }

    /**
     * Returns the length of the vector (which can be described by the
     * euclidean distance between the vector and the origin.
     * @return Length of the vector
     */
    public float length() {
        return (float)Math.hypot(x, y);
    }

    /**
     * Returns Returns a vector with the same direction but a length of 1
     * @return Resulting Vector2f
     */
    public Vector2f unitVector() {
        return this.divide(this.length());
    }

    /**
     * Calculates the angle between two vectors in radians
     * @param other Vector2f
     * @return angle
     */
    public float angle(Vector2f other) {
        float angle = (float) (Math.atan2(other.y - y, other.x - x));

        return angle < 0 ? (float) (angle + Math.PI * 2) : angle;
    }

    /**
     * Multiplies the vector by a given 2x2 matrix
     * @param mat Vector2f
     * @return Resulting Vector2f
     */
    public Vector2f transform(Matrix mat) {
        float[] pts = new float[]{x, y};
        mat.mapPoints(pts);
        return new Vector2f(pts[0], pts[1]);
    }
}
