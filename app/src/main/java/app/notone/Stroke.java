package app.notone;

import android.graphics.Path;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Paths drawn by user
 */
public class Stroke extends Path implements Serializable {
    private int mColor;
    private float mWeight;
    private ArrayList<float[]> pathPoints;

    public Stroke(int mColor, float mWeight) {
        super();
        this.mColor = mColor;
        this.mWeight = mWeight;
        this.pathPoints = new ArrayList<>();
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

    public ArrayList<float[]> getPathPoints() {
        return pathPoints;
    }

    public void setPathPoints(ArrayList<float[]> pathPoints) {
        this.pathPoints = pathPoints;
    }

    public void initPathFromPathPoints() {
        reset();
        super.moveTo(pathPoints.get(0)[0], pathPoints.get(0)[1]);
        for(int i = 1; i < pathPoints.size(); i++) {
            super.lineTo(pathPoints.get(i)[0], pathPoints.get(i)[1]);
        }
    }

    @Override
    public void moveTo(float x, float y) {
        super.moveTo(x, y);
        pathPoints.add(new float[]{x, y});
    }

    @Override
    public void lineTo(float x, float y) {
        super.lineTo(x, y);
        pathPoints.add(new float[]{x, y});
    }
}
