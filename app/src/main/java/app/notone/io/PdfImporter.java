package app.notone.io;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Arrays;

import app.notone.core.CanvasPdfDocument;
import app.notone.core.util.PageSize;
import app.notone.ui.fragments.CanvasFragment;

public class PdfImporter {
    public static final float FACTOR_72PPI_TO_320PPI = 4.4444444f;

    public static class PdfImporterTaskData {
        PdfRenderer renderer;
        CanvasPdfDocument document;
        float dpi;

        public PdfImporterTaskData(PdfRenderer renderer, CanvasPdfDocument document, float dpi) {
            this.renderer = renderer;
            this.document = document;
            this.dpi = dpi;
        }
    }

    public static class ImportPdfTask extends AsyncTask<PdfImporterTaskData, Integer, Void> {
        protected Void doInBackground(@NonNull PdfImporterTaskData... args) {
            PdfImporterTaskData data = args[0];
            PdfRenderer renderer = data.renderer;
            CanvasPdfDocument document = data.document;

            Log.d("PDF", "Loaded into: " + document.toString());

            final float scaling = FACTOR_72PPI_TO_320PPI / 2f;

            Matrix transform = new Matrix();
            transform.setScale(scaling, scaling);

            final int amtPages = renderer.getPageCount();
            Bitmap[] pages = new Bitmap[amtPages];

            for(int i = 0; i < amtPages; i++) {
                PdfRenderer.Page page = renderer.openPage(i);
                final float widthScaling = PageSize.A4.getWidthPixels(data.dpi) / (float)page.getWidth();
                final float heightScaling = PageSize.A4.getHeightPixels(data.dpi) / (float)page.getHeight();
                transform.setScale(widthScaling, heightScaling);

                pages[i] = Bitmap.createBitmap((int) (page.getWidth() * widthScaling), (int) (page.getHeight() * heightScaling), Bitmap.Config.ARGB_4444);
                page.render(pages[i], null, transform, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();

                System.out.println(String.format("Loaded Page with size %dx%d", page.getWidth(), page.getHeight()));

                publishProgress((int) (((i+1) / (float) amtPages) * 100));

                document.setPages(Arrays.copyOfRange(pages, 0, i));
            }

            document.setPages(pages);

            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            System.out.println("PdfImporter " + progress[0] + " % done.");
            CanvasFragment.sCanvasView.invalidate();
        }

        protected void onPostExecute(Void result) {
            CanvasFragment.sCanvasView.invalidate();
            CanvasFragment.sSettings.setLoadPdf(false);
        }

    }

    public static void fromUri(Context context, Uri uri, CanvasPdfDocument document) {
        try {
            ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            PdfRenderer renderer = new PdfRenderer(fileDescriptor);

            final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            final float dpi = (float) metrics.densityDpi / metrics.density;

            new PdfImporter.ImportPdfTask().execute(new PdfImporterTaskData(renderer, document, dpi));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
