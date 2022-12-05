package app.notone.io;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.io.FileNotFoundException;
import java.io.IOException;

import app.notone.core.CanvasPdfDocument;

public class PdfImporter {
    public static final float FACTOR_72PPI_TO_320PPI = 4.4444444f;

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
                Log.d("PdfImporter", String.format("Loaded page %d with dimensions %dx%d", i+1, page.getWidth(), page.getHeight()));
                pages[i] = Bitmap.createBitmap((int) (page.getWidth() * scaling), (int) (page.getHeight() * scaling), Bitmap.Config.ARGB_4444);
                page.render(pages[i], null, transform, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
                page.close();
            }

            Log.d("PdfImporter", String.format("Loaded %d pages from %s", pages.length, uri.toString()));

            document.setPages(pages);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return document;
    }
}
