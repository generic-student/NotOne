package app.notone.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;

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

    public void loadFromStorage(Context context, Uri uri) {
        try {
            ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            PdfRenderer renderer = new PdfRenderer(fileDescriptor);

            Matrix transform = new Matrix();
            final float scaling = getScaling();
            transform.setScale(scaling, scaling);

            final int amtPages = renderer.getPageCount();
            pages = new Bitmap[amtPages];

            for(int i = 0; i < amtPages; i++) {
                PdfRenderer.Page page = renderer.openPage(i);
                pages[i] = Bitmap.createBitmap(page.getWidth() * 2, page.getHeight() * 2, Bitmap.Config.ARGB_4444);
                page.render(pages[i], null, transform, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
                page.close();
            }

            //bitmap.eraseColor(Color.BLACK);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
