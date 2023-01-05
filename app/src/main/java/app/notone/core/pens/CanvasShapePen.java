package app.notone.core.pens;

import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;

import com.google.mlkit.vision.digitalink.Ink;

import app.notone.core.CanvasWriter;
import app.notone.core.Stroke;
import app.notone.core.Vector2f;
import app.notone.core.util.InkRecognizer;
import app.notone.core.util.StrokeToShapeConverter;
import app.notone.ui.fragments.CanvasFragment;

/**
 * Pen for writing strokes to the canvas and recognizing basic shapes.
 * Will convert drawn shapes when recognized.
 * Currently recognizable shapes include: Rectangle, Ellipse, Triangle
 *
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public class CanvasShapePen extends CanvasWriterPen{
    private static final String TAG = CanvasShapePen.class.getSimpleName();

    /**
     * Constructs an Ink object from Strokes that can be used
     * by the ML Toolkit for recognition.
     */
    Ink.Builder inkBuilder;
    /**
     * Constructs a Stroke Object that defines a collection of points
     */
    Ink.Stroke.Builder strokeBuilder;

    public CanvasShapePen(CanvasWriter writerReference) {
        super(writerReference);

        inkBuilder = new Ink.Builder();

        if(!InkRecognizer.getInstance().isInitialized()) {
            InkRecognizer.init("zxx-Zsym-x-shapes");
        }
    }

    @Override
    public boolean handleOnTouchEvent(MotionEvent event, Vector2f currentTouchPoint) {
        boolean invalidate = super.handleOnTouchEvent(event, currentTouchPoint);
        long t = System.currentTimeMillis();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                strokeBuilder = Ink.Stroke.builder();
                strokeBuilder.addPoint(Ink.Point.create(currentTouchPoint.x, currentTouchPoint.y, t));
                break;

            case MotionEvent.ACTION_MOVE:
                strokeBuilder.addPoint(Ink.Point.create(currentTouchPoint.x, currentTouchPoint.y, t));
                return invalidate;

            case MotionEvent.ACTION_UP:
                strokeBuilder.addPoint(Ink.Point.create(currentTouchPoint.x, currentTouchPoint.y, t));
                inkBuilder.addStroke(strokeBuilder.build());
                strokeBuilder = null;

                recognizeShape();

                return invalidate;
        }

        return invalidate;
    }

    @Override
    public void render(Canvas canvas) {
        super.render(canvas);
    }

    @Override
    public void reset() {
        super.reset();
    }

    /**
     * Tries to recognize the shape that is in the Ink Object constructed from
     * the InkBuilder. When it recognizes a shape it converts that Stroke into
     * the recognized shape by altering the points defining the shape.
     */
    private void recognizeShape() {
        Ink ink = inkBuilder.build();
        //get the last written stroke since this is the one being analyzed
        Stroke strokeReference = mCanvasWriterRef.getStrokes().get(mCanvasWriterRef.getStrokes().size() - 1);

        if(InkRecognizer.getInstance().isInitialized()) {
            InkRecognizer.getInstance().getRecognizer().recognize(ink)
                    .addOnSuccessListener(
                            result -> {
                                final String shape = result.getCandidates().get(0).getText();
                                convertStrokeToShape(shape, strokeReference);
                            }
                    )
                    .addOnFailureListener(
                            e -> Log.e(TAG, "Error during recognition: " + e)
                    );
        }

        inkBuilder = new Ink.Builder();
    }

    /**
     * Converts a Stroke to a given shape using the {@link StrokeToShapeConverter}
     * @param shape Recognized shape e.g. "RECTANGLE"
     * @param strokeReference Reference to the stroke containing the shape
     */
    private void convertStrokeToShape(String shape, Stroke strokeReference) {
        //check if the referenced stroke still exists
        if(!mCanvasWriterRef.getStrokes().contains(strokeReference)) {
            return;
        }

        switch(shape) {
            case "RECTANGLE":
                StrokeToShapeConverter.convertStrokeToRectangle(strokeReference);
                break;
            case "TRIANGLE":
                StrokeToShapeConverter.convertStrokeToTriangle(strokeReference);
                break;
            case "ELLIPSE":
                StrokeToShapeConverter.convertStrokeToEllipse(strokeReference);
                break;
            case "ARROW":
                StrokeToShapeConverter.convertStrokeToArrow(strokeReference);
                break;
            default:
                break;
        }
        CanvasFragment.sCanvasView.invalidate();
    }
}
