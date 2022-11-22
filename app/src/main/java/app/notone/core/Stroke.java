package app.notone.core;

import android.graphics.Path;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Paths drawn by user.
 * This includes its color, weight and points
 */
public class Stroke extends Path implements Serializable {
    private int mColor;
    private float mWeight;
    private ArrayList<Float> pathPoints;

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

    public ArrayList<Float> getPathPoints() {
        return pathPoints;
    }

    public void setPathPoints(ArrayList<Float> pathPoints) {
        this.pathPoints = pathPoints;
    }

    /**
     * Initializes the Path variable after a Stroke Instance has been restored through a byte stream
     */
    public void initPathFromPathPoints() {
        reset();
        super.moveTo(pathPoints.get(0), pathPoints.get(1));
        for(int i = 2; i < pathPoints.size(); i+=2) {
            super.lineTo(pathPoints.get(i), pathPoints.get(i+1));
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
