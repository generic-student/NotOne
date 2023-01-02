package app.notone.io;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import app.notone.core.CanvasView;
import app.notone.core.util.PageSize;

public class PdfExporter {

    private final static String TAG = "PdfExporter";

    private static List<RectF> getStrokeBoundsSortedY(CanvasView view) {
        //get a list of the bounding boxes for all strokes
        final List<RectF> bounds = view.getCanvasWriter().getStrokes().stream().map(stroke -> {
            RectF b = new RectF();
            stroke.computeBounds(b, false);
            return b;
        }).collect(Collectors.toList());

        //sort the bounding boxes by their y coordinate
        final List<RectF> boundsSortedY = bounds.stream().sorted((b1, b2) -> Float.compare(b1.top, b2.top)).collect(Collectors.toList());

        return boundsSortedY;
    }

    @NonNull
    public static ArrayList<Rect> computePdfPageBoundsFromCanvasViewStrict(@NonNull CanvasView view, float dpi, @NonNull PageSize pageSize) {
        final int heightPixels = pageSize.getHeightPixels(dpi);
        final int widthPixels = pageSize.getWidthPixels(dpi);

        final int pdfPages = view.getPdfDocument().getPages().length;

        //sort the bounding boxes by their y coordinate
        final List<RectF> boundsSortedY = getStrokeBoundsSortedY(view).
                stream().
                filter(b -> b.left < widthPixels && b.right > 0).
                collect(Collectors.toList());
        int maxBottom = (int) boundsSortedY.stream().max((b1, b2) -> Float.compare(b1.bottom, b2.bottom)).orElse(new RectF(0,0,0,0)).bottom;

        ArrayList<Rect> pages = new ArrayList<>();
        for(int i = 0; i < pdfPages; i++) {
            pages.add(new Rect(0, i * heightPixels, widthPixels, (i + 1) * heightPixels));
        }

        int pageIndex = pdfPages;
        Rect page = new Rect(0, pageIndex * heightPixels, widthPixels, (pageIndex + 1) * heightPixels);
        while(page.top < maxBottom) {
            for (int i = 0; i < boundsSortedY.size(); i++) {
                RectF bounds = boundsSortedY.get(i);
                Rect intBounds = new Rect();
                bounds.roundOut(intBounds);

                if (Rect.intersects(intBounds, page)) {
                    pages.add(page);
                    break;
                }
            }
            pageIndex++;
            page = new Rect(0, pageIndex * heightPixels, widthPixels, (pageIndex + 1) * heightPixels);
        }
        return pages;
    }

    @NonNull
    public static ArrayList<Rect> computePdfPageBoundsFromCanvasView(@NonNull CanvasView view, float dpi) {
        //compute the max height of the page (Din A4)
        final int pageHeightPixels = PageSize.A4.getHeightPixels(dpi);

        //sort the bounding boxes by their y coordinate
        final List<RectF> boundsSortedY = getStrokeBoundsSortedY(view);

        //the list of bounds defining the size-constraints for the individual pdf pages
        ArrayList<Rect> pages = new ArrayList<>();

        //compute the largest bounds (within spec) that contain the most strokes
        int startIndex = 0;
        while (startIndex < boundsSortedY.size()) {
            int endIndex = startIndex;


            Rect currentPage = new Rect();
            currentPage.top = pages.isEmpty() ? (int) boundsSortedY.get(startIndex).top : pages.get(pages.size() - 1).bottom;
            final int maxY = currentPage.top + pageHeightPixels;

            for (int i = startIndex; i < boundsSortedY.size(); i++) {
                if (boundsSortedY.get(i).bottom > maxY) {
                    break;
                }
                endIndex = i;
            }
            currentPage.bottom = (int) boundsSortedY.get(endIndex).bottom;

            //compute the width of the page
            currentPage.left = Integer.MAX_VALUE;
            currentPage.right = Integer.MIN_VALUE;

            for (int i = startIndex; i <= endIndex; i++) {
                if (boundsSortedY.get(i).left < currentPage.left) {
                    currentPage.left = (int) boundsSortedY.get(i).left;
                }
                if (boundsSortedY.get(i).right > currentPage.right) {
                    currentPage.right = (int) boundsSortedY.get(i).right;
                }
            }


            startIndex = endIndex + 1;
            pages.add(currentPage);
        }

        return pages;
    }

    public static void renderCanvasViewContentsToPdfDocument(@NonNull CanvasView view, @NonNull ArrayList<Rect> pageBounds, @NonNull PdfDocument document) {
        for (Rect pageRect : pageBounds) {
            PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(pageRect.width(), pageRect.height(), 1).create();
            PdfDocument.Page page = document.startPage(info);

            Canvas canvas = page.getCanvas();
            Matrix transform = new Matrix();
            transform.setTranslate(-pageRect.left, -pageRect.top);
            //transform.postScale(.5f, .5f);
            canvas.setMatrix(transform);

            view.getPdfRenderer().render(view.getPdfDocument(), canvas);
            view.getCanvasWriter().renderStrokes(canvas);


            document.finishPage(page);
        }
    }

    public static void exportPdfDocumentToFolder(@NonNull PdfDocument document, String folder, String filename) {
        File file = new File(folder, filename);
        try {
            document.writeTo(new FileOutputStream(file, false));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void export(CanvasView canvasView, float dpi, String folder, String filename) {
        //compute the size for the pdf pages
        ArrayList<Rect> pages = computePdfPageBoundsFromCanvasView(canvasView, dpi);

        //render the content to the pdf
        PdfDocument pdfDocument = new PdfDocument();
        renderCanvasViewContentsToPdfDocument(canvasView, pages, pdfDocument);

        //export the pdf
        exportPdfDocumentToFolder(pdfDocument, folder, filename);

        //close the document
        pdfDocument.close();
    }

    public static void export(CanvasView canvasView, float dpi, String folder, String filename, boolean enforceA4) {
        //compute the size for the pdf pages
        ArrayList<Rect> pages = enforceA4 ? computePdfPageBoundsFromCanvasViewStrict(canvasView, dpi, PageSize.A4) : computePdfPageBoundsFromCanvasView(canvasView, dpi);

        //render the content to the pdf
        PdfDocument pdfDocument = new PdfDocument();
        renderCanvasViewContentsToPdfDocument(canvasView, pages, pdfDocument);

        //export the pdf
        exportPdfDocumentToFolder(pdfDocument, folder, filename);

        //close the document
        pdfDocument.close();
    }

    public static PdfDocument exportPdfDocument(CanvasView canvasView, float dpi, boolean enforceA4) {
        //compute the size for the pdf pages
        ArrayList<Rect> pages = enforceA4 ? computePdfPageBoundsFromCanvasViewStrict(canvasView, dpi, PageSize.A4) : computePdfPageBoundsFromCanvasView(canvasView, dpi);

        //render the content to the pdf
        PdfDocument pdfDocument = new PdfDocument();
        renderCanvasViewContentsToPdfDocument(canvasView, pages, pdfDocument);

        return pdfDocument;
    }
}
