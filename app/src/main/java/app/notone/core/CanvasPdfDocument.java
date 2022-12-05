package app.notone.core;

import android.graphics.Bitmap;

import java.io.Serializable;

public class CanvasPdfDocument implements Serializable {
    private static final String LOG_TAG = CanvasPdfDocument.class.getSimpleName();
    private Bitmap[] pages = {};

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

    public CanvasPdfDocument() {

    }
}
