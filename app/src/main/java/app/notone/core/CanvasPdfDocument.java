package app.notone.core;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * Describes the Pages of a PdfDocument as a List of Bitmaps
 *
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public class CanvasPdfDocument implements Serializable {
    /**
     * Tag for logging
     */
    private static final String TAG = CanvasPdfDocument.class.getSimpleName();
    /**
     * Array of Bitmaps representing the pages of a pdf document
     */
    private Bitmap[] pages;

    /**
     * Returns a Bitmap from a given index
     *
     * @param value index
     * @return Bitmap
     * @throws IndexOutOfBoundsException When the index is out of bounds
     */
    public Bitmap getPage(int value) {
        return pages[value];
    }

    /**
     * Returns the Array of Bitmaps
     *
     * @return Bitmap[]
     */
    @NonNull
    public Bitmap[] getPages() {
        return pages;
    }

    /**
     * Set a Bitmap at a given index
     *
     * @param value index
     * @throws IndexOutOfBoundsException When the index is out of bounds
     */
    public void setPage(int value, @NonNull Bitmap bitmap) {
        this.pages[value] = bitmap;
    }

    /**
     * Set the Array of Bitmaps
     *
     * @param pages Bitmap[]
     */
    public void setPages(@NonNull Bitmap[] pages) {
        this.pages = pages;
    }

    public CanvasPdfDocument() {
        pages = new Bitmap[]{};
    }
}
