package app.notone.io;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.FileNotFoundException;
import java.io.IOException;

import app.notone.core.CanvasPdfDocument;

public class PdfImporter {
    public static CanvasPdfDocument fromUri(Context context, Uri uri, float scaling) {
        CanvasPdfDocument document = new CanvasPdfDocument(scaling);
        try {
            ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            PdfRenderer renderer = new PdfRenderer(fileDescriptor);

            Matrix transform = new Matrix();
            transform.setScale(scaling, scaling);

            final int amtPages = renderer.getPageCount();
            Bitmap[] pages = new Bitmap[amtPages];

            for(int i = 0; i < amtPages; i++) {
                PdfRenderer.Page page = renderer.openPage(i);
                pages[i] = Bitmap.createBitmap(page.getWidth() * 2, page.getHeight() * 2, Bitmap.Config.ARGB_4444);
                page.render(pages[i], null, transform, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
                page.close();
            }

            document.setPages(pages);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return document;
    }
}
