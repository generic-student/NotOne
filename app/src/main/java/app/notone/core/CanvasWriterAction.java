package app.notone.core;

import java.io.Serializable;

/**
 * Describes a type of action that happened in the {@link CanvasWriter} and
 * the affected Stroke. E.g. a Stroke has been deleted.
 *
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public class CanvasWriterAction implements Serializable {

    /**
     * The type of action that happened
     */
    public Type type;
    /**
     * The stroke affected by the action
     */
    public Stroke stroke;

    /**
     * Types of actions
     */
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
