package app.notone.core.pens;

import app.notone.core.CanvasWriter;

public class CanvasPenFactory {
    public CanvasPen createCanvasPen(String type, CanvasWriter writerReference) {
        if(type == null || type.isEmpty()) {
            return null;
        }
        switch(type) {
            case "PEN":
                return new CanvasWriterPen(writerReference);
            case "ERASER":
                return new CanvasEraserPen(writerReference);
            default:
                throw new IllegalArgumentException("Unknown Pen type " + type);
        }
    }
}
