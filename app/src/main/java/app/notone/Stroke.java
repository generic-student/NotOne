package app.notone;

import android.graphics.Path;

/**
 * Paths drawn by user
 */
public class Stroke {
    private Path mPath;
    private int mColor;
    private float mWeight;

    public Stroke(int mColor, float mWeight) {
        this.mPath = new Path();
        this.mColor = mColor;
        this.mWeight = mWeight;
    }

    public Path getPath() {
        return mPath;
    }

    public void setPath(Path mPath) {
        this.mPath = mPath;
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
}
