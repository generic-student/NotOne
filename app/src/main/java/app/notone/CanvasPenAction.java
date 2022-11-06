package app.notone;

public class CanvasPenAction {

    public Type type;
    public Stroke stroke;

    public enum Type {
        WRITE,
        ERASE,
        UNDO_WRITE,
        UNDO_ERASE
    }

    public CanvasPenAction(Type type, Stroke stroke) {
        this.type = type;
        this.stroke = stroke;
    }
}
