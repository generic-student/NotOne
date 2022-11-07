package app.notone;

public class CanvasWriterAction {

    public Type type;
    public Stroke stroke;

    public enum Type {
        WRITE,
        ERASE,
        UNDO_WRITE,
        UNDO_ERASE
    }

    public CanvasWriterAction(Type type, Stroke stroke) {
        this.type = type;
        this.stroke = stroke;
    }
}
