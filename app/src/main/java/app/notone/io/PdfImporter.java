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

/**
 * This class is used for loading PdfDocuments from the
 * filesystem and importing them as a printout into the
 * Canvas using the intermediate {@link CanvasPdfDocument}
 *
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public class PdfImporter {
    public static final float FACTOR_72PPI_TO_320PPI = 4.4444444f;

    /**
     * Data class for the {@link ImportPdfTask} that is running in
     * the background. It contains all required data for the task
     * to complete.
     *
     * @author Kai Titgens
     * @author kai.titgens@stud.th-owl.de
     * @version 0.1
     * @since 0.1
     */
    public static class PdfImporterTaskData {
        /**
         * PdfRenderer for creating a printout of the pdf document
         */
        PdfRenderer renderer;
        /**
         * Object where the printout is being stored as a list of bitmaps
         */
        CanvasPdfDocument document;
        /**
         * Screen resolution
         */
        float dpi;

        public PdfImporterTaskData(PdfRenderer renderer,
                                   CanvasPdfDocument document, float dpi) {
            this.renderer = renderer;
            this.document = document;
            this.dpi = dpi;
        }
    }

    /**
     * AsyncTask for loading a pdf document from the filesystem and importing
     * it into a CanvasPdfDocument for displaying it in the CanvasView as a
     * printout.
     */
    public static class ImportPdfTask extends AsyncTask<PdfImporterTaskData,
            Integer, Void> {
        /**
         * The function that runs in the background
         *
         * @param args List of PdfImporterTaskData for handling multiple imports
         * @return
         */
        protected Void doInBackground(@NonNull PdfImporterTaskData... args) {
            PdfImporterTaskData data = args[0];
            PdfRenderer renderer = data.renderer;
            CanvasPdfDocument document = data.document;

            Log.d("PDF", "Loaded into: " + document.toString());

            Matrix transform = new Matrix();

            //allocate space for the pages
            final int amtPages = renderer.getPageCount();
            Bitmap[] pages = new Bitmap[amtPages];

            //load each page individually
            for (int i = 0; i < amtPages; i++) {
                PdfRenderer.Page page = renderer.openPage(i);
                //scale the page so that it conforms to the A4 format
                final float widthScaling =
                        PageSize.A4.getWidthPixels(data.dpi) / (float) page.getWidth();
                final float heightScaling =
                        PageSize.A4.getHeightPixels(data.dpi) / (float) page.getHeight();
                transform.setScale(widthScaling, heightScaling);

                //load the pages as bitmaps (printout)
                pages[i] = Bitmap.createBitmap(
                                (int) (page.getWidth() * widthScaling),
                                (int) (page.getHeight() * heightScaling),
                                Bitmap.Config.ARGB_4444);
                page.render(pages[i], null, transform,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();

                System.out.println(String.format("Loaded Page with size " +
                        "%dx%d", page.getWidth(), page.getHeight()));

                //invokes the onProgressUpdate method
                publishProgress((int) (((i + 1) / (float) amtPages) * 100));

                //add the loaded subrange of pages to the document
                //so that the already loaded pages can be displayed already
                document.setPages(Arrays.copyOfRange(pages, 0, i));
            }

            document.setPages(pages);
            //ensure that the current CanvasView is holding this instance of
            // the PdfDocument
            CanvasFragment.sCanvasView.setPdfDocument(document);

            return null;
        }

        /**
         * Function that will be called on each progress increment.
         * That means each time a page is finished loading.
         * It will display the percentage of pages that are done loading
         * and invalidate the canvas respectively, so that the pages
         * appear one after another.
         *
         * @param progress
         */
        protected void onProgressUpdate(Integer... progress) {
            System.out.println("PdfImporter " + progress[0] + " % done.");
            CanvasFragment.sCanvasView.invalidate();
        }

        /**
         * Function that will be called after the task is completed
         *
         * @param result
         */
        protected void onPostExecute(Void result) {
            CanvasFragment.sCanvasView.invalidate();
            CanvasFragment.sFlags.setLoadPdf(false);
            CanvasFragment.sCanvasView.setLoaded(true);
        }

    }

    /**
     * Imports a pdf document from a uri into a CanvasPdfDocument.
     * this function calls the ImportPdfTask which will run in the background.
     *
     * @param context  Application context
     * @param uri      Uri
     * @param document CanvasPdfDocument to load the printout into
     */
    public static void fromUri(Context context, Uri uri,
                               CanvasPdfDocument document) {
        try {
            ParcelFileDescriptor fileDescriptor =
                    context.getContentResolver().openFileDescriptor(uri, "r");
            PdfRenderer renderer = new PdfRenderer(fileDescriptor);

            final DisplayMetrics metrics =
                    context.getResources().getDisplayMetrics();
            final float dpi = (float) metrics.densityDpi / metrics.density;

            new PdfImporter.ImportPdfTask().execute(
                    new PdfImporterTaskData(renderer, document, dpi));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
