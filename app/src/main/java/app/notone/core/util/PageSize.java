package app.notone.core.util;

import app.notone.io.PdfExporter;

public class PageSize {
    private final float widthInches;
    private final float heightInches;

    public static final PageSize A1 = new PageSize(23.39f, 33.11f);
    public static final PageSize A2 = new PageSize(16.54f, 23.39f);
    public static final PageSize A3 = new PageSize(11.69f, 16.54f);
    public static final PageSize A4 = new PageSize(8.29f, 11.69f);
    public static final PageSize A5 = new PageSize(5.83f, 8.29f);

    public PageSize(float width, float height) {
        this.widthInches = width;
        this.heightInches = height;
    }

    public int getHeightPixels(float dpi) {
        return (int) (heightInches * dpi);
    }

    public int getWidthPixels(float dpi) {
        return (int) (widthInches * dpi);
    }
}
