package app.notone.core.pens;

import app.notone.core.CanvasWriter;

public class CanvasPenFactory {
    public CanvasPen createCanvasPen(PenType type, CanvasWriter writerReference) {
        if(type == null) {
            throw new IllegalArgumentException("Pen type cannot be null");
        }
        switch(type) {
            case PEN:
                return new CanvasWriterPen(writerReference);
            case ERASER:
                return new CanvasEraserPen(writerReference);
            case SELECTOR:
                return new CanvasSelectorPen(writerReference);
            default:
                throw new IllegalArgumentException("Unknown Pen type: " + type);
        }
    }
}
