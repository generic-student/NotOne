package app.notone.core;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class PdfCanvasRenderer {
    private Paint pdfPaint;
    private Paint borderPaint;
    private float scaling;
    private int padding;

    public PdfCanvasRenderer() {
        borderPaint = new Paint();
        borderPaint.setStrokeWidth(3);
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);

        pdfPaint = new Paint();
        pdfPaint.setAntiAlias(true);
        pdfPaint.setFilterBitmap(true);
        pdfPaint.setDither(true);

        scaling = .5f;
        padding = 20;
    }

    public Paint getPdfPaint() {
        return pdfPaint;
    }

    public void setPdfPaint(Paint pdfPaint) {
        this.pdfPaint = pdfPaint;
    }

    public Paint getBorderPaint() {
        return borderPaint;
    }

    public void setBorderPaint(Paint borderPaint) {
        this.borderPaint = borderPaint;
    }

    public float getScaling() {
        return scaling;
    }

    public void setScaling(float scaling) {
        this.scaling = scaling;
    }

    public int getPadding() {
        return padding;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public void render(CanvasPdfDocument doc, Canvas canvas, Matrix viewMatrix) {
        final RectF viewSpace = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());

        final float scaling = getScaling();
        final int padding = getPadding();

        Matrix pdfMat = new Matrix();
        for(int i = 0; i < doc.getPages().length; i++) {
            Rect source = new Rect(0, 0, doc.getPage(i).getWidth(), doc.getPage(i).getHeight());
            RectF dest = new RectF(0, 0, doc.getPage(i).getWidth() * scaling, doc.getPage(i).getHeight() * scaling);
            pdfMat.mapRect(dest);
            pdfMat.postTranslate(0, doc.getPage(i).getHeight() * scaling + padding);

            RectF transformedDest = new RectF();
            viewMatrix.mapRect(transformedDest, dest);
            if(transformedDest.intersect(viewSpace) == false) {
                continue;
            }


            canvas.drawBitmap(doc.getPage(i), source, dest, pdfPaint);
            canvas.drawRect(dest, borderPaint);
        }
    }
}
