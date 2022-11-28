package app.notone.io;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import app.notone.R;
import app.notone.core.CanvasView;
import app.notone.core.Stroke;

public class PdfExporter {
    private static final float defaultPageHeightInches = 11.69f;

    public static PdfDocument export(CanvasView canvasView, float dpi, String folder, String filename) {
        PdfDocument pdfDocument = new PdfDocument();

        final int pageHeightPixels = (int) (defaultPageHeightInches * dpi);

        List<RectF> bounds = canvasView.getCanvasWriter().getStrokes().stream().map(s -> {
            RectF b = new RectF();
            s.computeBounds(b, false);
            return b;
        }).collect(Collectors.toList());

        List<RectF> boundsSortedY = bounds.stream().sorted((b1, b2) -> Float.compare(b1.top, b2.top)).collect(Collectors.toList());

        ArrayList<Rect> pages = new ArrayList<>();

        //get the bounds that fit on the current page
        int startIndex = 0;
        while(startIndex < boundsSortedY.size()) {
            int endIndex = startIndex;


            Rect currentPage = new Rect();
            currentPage.top = (int) boundsSortedY.get(startIndex).top;
            final int maxY = currentPage.top + pageHeightPixels;

            for(int i = startIndex; i < boundsSortedY.size(); i++) {
                if(boundsSortedY.get(i).bottom < maxY) {
                    endIndex = i;
                    continue;
                }
                break;
            }
            currentPage.bottom = (int) boundsSortedY.get(endIndex).bottom;

            //compute the width of the page
            currentPage.left = Integer.MAX_VALUE;
            currentPage.right = Integer.MIN_VALUE;

            for(int i = startIndex; i <= endIndex; i++) {
                if(boundsSortedY.get(i).left < currentPage.left) {
                    currentPage.left = (int) boundsSortedY.get(i).left;
                }
                if(boundsSortedY.get(i).right > currentPage.right) {
                    currentPage.right = (int) boundsSortedY.get(i).right;
                }
            }


            startIndex = endIndex + 1;
            pages.add(currentPage);
        }


        for(Rect pageRect : pages) {
            PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(pageRect.width(), pageRect.height(), 1).create();
            PdfDocument.Page page = pdfDocument.startPage(info);

            Canvas canvas = page.getCanvas();
            Matrix offset = new Matrix();
            offset.setTranslate(-pageRect.left, -pageRect.top);
            canvas.setMatrix(offset);

            canvasView.getCanvasWriter().renderStrokes(canvas);

            pdfDocument.finishPage(page);
        }

        File file = new File(folder, filename);
        try {
            pdfDocument.writeTo(new FileOutputStream(file, false));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        pdfDocument.close();
        return pdfDocument;
    }
}
