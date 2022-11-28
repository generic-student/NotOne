package app.notone.core;

import android.graphics.Bitmap;

public class CanvasPdfDocument {
    private static final String LOG_TAG = CanvasPdfDocument.class.getSimpleName();
    private Bitmap[] pages = {};
    private float scaling = 2.f;

    public Bitmap getPage(int value) {
        return pages[value];
    }

    public Bitmap[] getPages() {
        return pages;
    }

    public void setPage(int value, Bitmap bitmap) { this.pages[value] = bitmap;}

    public void setPages(Bitmap[] pages) {
        this.pages = pages;
    }

    public float getScaling() {
        return scaling;
    }

    public void setScaling(float scaling) {
        this.scaling = scaling;
    }

    public CanvasPdfDocument(float baseScaling) {
        setScaling(baseScaling);
    }
}
