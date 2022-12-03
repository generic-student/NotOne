package app.notone.io;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import app.notone.core.CanvasView;

public class PdfExporter {

    public static class PageSize {
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

        //sort the bounding boxes by their y coordinate
        final List<RectF> boundsSortedY = getStrokeBoundsSortedY(view);

        ArrayList<Rect> pages = new ArrayList<>();


        int startIndex = 0;
        int pageIndex = 0;
        while (startIndex < boundsSortedY.size()) {
            final Rect page = new Rect(0, pageIndex * heightPixels, widthPixels, (pageIndex + 1) * heightPixels);

            for (int i = 0; i < boundsSortedY.size(); i++) {
                RectF bounds = boundsSortedY.get(i);
                Rect intBounds = new Rect();
                bounds.roundOut(intBounds);

                if (intBounds.intersect(page)) {
                    pages.add(page);
                    startIndex = i;
                    break;
                }
            }
            pageIndex++;

            if(page.top > boundsSortedY.get(boundsSortedY.size() - 1).top) {
                break;
            }
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
            Matrix offset = new Matrix();
            offset.setTranslate(-pageRect.left, -pageRect.top);
            canvas.setMatrix(offset);

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
