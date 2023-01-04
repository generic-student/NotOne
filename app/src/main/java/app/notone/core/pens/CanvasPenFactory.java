package app.notone.core.pens;

import app.notone.core.CanvasWriter;

/**
 * Constructs different CanvasPens from a PenType and a CanvasWriter
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public class CanvasPenFactory {
    /**
     * Returns the CanvasPen that corresponds to the given PenType
     * @param type The type of the pen
     * @param writerReference Reference to a CanvasWriter
     * @return Instance of a CanvasPen
     */
    public CanvasPen createCanvasPen(PenType type, CanvasWriter writerReference) {
        if(type == null) {
            throw new IllegalArgumentException("Pen type cannot be null");
        }
        switch(type) {
            case WRITER:
                return new CanvasWriterPen(writerReference);
            case ERASER:
                return new CanvasEraserPen(writerReference);
            case SELECTOR:
                return new CanvasSelectorPen(writerReference);
            case SHAPE_DETECTOR:
                return new CanvasShapePen(writerReference);
            default:
                throw new IllegalArgumentException("Unknown Pen type: " + type);
        }
    }
}
