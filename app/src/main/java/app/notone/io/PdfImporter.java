package app.notone.io;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import app.notone.core.CanvasPdfDocument;
import app.notone.fragments.CanvasFragment;

public class PdfImporter {
    public static final float FACTOR_72PPI_TO_320PPI = 4.4444444f;

    public static class PdfImporterTaskData {
        PdfRenderer renderer;
        CanvasPdfDocument document;

        public PdfImporterTaskData(PdfRenderer renderer, CanvasPdfDocument document) {
            this.renderer = renderer;
            this.document = document;
        }
    }

    public static class ImportPdfTask extends AsyncTask<PdfImporterTaskData, Integer, Void> {
        protected Void doInBackground(PdfImporterTaskData... args) {
            PdfImporterTaskData data = args[0];
            PdfRenderer renderer = data.renderer;
            CanvasPdfDocument document = data.document;

            final float scaling = FACTOR_72PPI_TO_320PPI / 2f;

            Matrix transform = new Matrix();
            transform.setScale(scaling, scaling);

            final int amtPages = renderer.getPageCount();
            Bitmap[] pages = new Bitmap[amtPages];

            for(int i = 0; i < amtPages; i++) {
                PdfRenderer.Page page = renderer.openPage(i);
                pages[i] = Bitmap.createBitmap((int) (page.getWidth() * scaling), (int) (page.getHeight() * scaling), Bitmap.Config.ARGB_4444);
                page.render(pages[i], null, transform, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();

                publishProgress((int) ((i / (float) amtPages) * 100));

                document.setPages(Arrays.copyOfRange(pages, 0, i));
            }

            document.setPages(pages);

            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            System.out.println("PdfImporter " + progress[0] + " % done.");
            CanvasFragment.mCanvasView.invalidate();
        }

        protected void onPostExecute(Void result) {
        }

    }

    public static void fromUri(Context context, Uri uri, CanvasPdfDocument document) {
        try {
            ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            PdfRenderer renderer = new PdfRenderer(fileDescriptor);

            new PdfImporter.ImportPdfTask().execute(new PdfImporterTaskData(renderer, document));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CanvasPdfDocument fromUri(Context context, Uri uri, float scaling) {
        CanvasPdfDocument document = new CanvasPdfDocument();
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
                page.render(pages[i], null, transform, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
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
