package app.notone.core;

import android.graphics.Path;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Paths drawn by user.
 * This includes its color, weight and points.
 * This class extends the class {@link Path} which does not include a method
 * for accessing the coordinates of the points without approximating them.
 *
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public class Stroke extends Path implements Serializable {
    /**
     * Color of the stroke
     */
    private int mColor;
    /**
     * Weight of the stroke
     */
    private float mWeight;
    /**
     * List of points that the stroke is made of (saved as [x,y,x,y,...])
     */
    private ArrayList<Float> pathPoints;

    public Stroke(int mColor, float mWeight) {
        super();
        this.mColor = mColor;
        this.mWeight = mWeight;
        this.pathPoints = new ArrayList<>();
    }

    @Override
    public void reset() {
        super.reset();
        this.pathPoints.clear();
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int mColor) {
        this.mColor = mColor;
    }

    public float getWeight() {
        return mWeight;
    }

    public void setWeight(float mWeight) {
        this.mWeight = mWeight;
    }

    public ArrayList<Float> getPathPoints() {
        return pathPoints;
    }

    public void setPathPoints(ArrayList<Float> pathPoints) {
        this.pathPoints = pathPoints;
    }

    /**
     * Constructs the Path from the PathPoints.
     * This is required if the Stroke has been constructed from
     * a list of points since the internal data from the super class
     * {@link Path} needs to be set separately.
     */
    public void initPathFromPathPoints() {
        super.reset();
        super.moveTo(pathPoints.get(0), pathPoints.get(1));
        for (int i = 2; i < pathPoints.size(); i += 2) {
            super.lineTo(pathPoints.get(i), pathPoints.get(i + 1));
        }
    }

    @Override
    public void moveTo(float x, float y) {
        super.moveTo(x, y);
        pathPoints.add(x);
        pathPoints.add(y);
    }

    @Override
    public void lineTo(float x, float y) {
        super.lineTo(x, y);
        pathPoints.add(x);
        pathPoints.add(y);
    }
}
