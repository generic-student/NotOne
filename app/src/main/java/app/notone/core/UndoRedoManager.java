package app.notone.core;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class handles undoing and redoing strokes that have been added to
 * the {@link CanvasWriter}.
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public class UndoRedoManager implements Serializable {
    /** Max amount of actions that can be undone and redone*/
    private transient final int MAX_ACTIONS = 20;
    /**
     * List of actions that have been undone.
     * These are needed to be able to redo an action.
     * */
    private ArrayList<CanvasWriterAction> mUndoneActions;
    /**
     * List of actions that have been done.
     */
    private ArrayList<CanvasWriterAction> mActions;
    /**
     * Reference to a list of strokes.
     * A reference is need because undoing an action can cause
     * a stroke to be removed or added from/to the list of strokes.
     */
    private transient ArrayList<Stroke> mStrokesReference;

    public UndoRedoManager(ArrayList<Stroke> strokesRef) {
        mActions = new ArrayList<>();
        mUndoneActions = new ArrayList<>();
        mStrokesReference = strokesRef;
    }

    /**
     * Reset all actions and undone actions
     */
    public void reset() {
        mUndoneActions.clear();
        mActions.clear();
    }

    /**
     * Adds an action to the list of actions.
     * If MAX_ACTIONS is exceeded removes the first element from the list.
     * @param action CanvasWriterAction to add
     */
    public void addAction(CanvasWriterAction action) {
        mActions.add(action);
        if(mActions.size() > MAX_ACTIONS) {
            mActions.remove(0);
        }
    }

    /**
     * Adds an action to the list of undone actions.
     * If MAX_ACTIONS is exceeded removes the first element from the list
     * @param action CanvasWriterAction to add
     */
    public void addUndoneAction(CanvasWriterAction action) {
        mUndoneActions.add(action);
        if(mUndoneActions.size() > MAX_ACTIONS) {
            mUndoneActions.remove(0);
        }
    }

    /**
     * Returns the list of undone actions
     * @return Actions
     */
    public ArrayList<CanvasWriterAction> getUndoneActions() {
        return mUndoneActions;
    }

    /**
     * Set the list of undone actions
     * @param mUndoneActions Actions
     */
    public void setUndoneActions(ArrayList<CanvasWriterAction> mUndoneActions) {
        this.mUndoneActions = mUndoneActions;
    }

    /**
     * Returns the list of actions
     * @return Actions
     */
    public ArrayList<CanvasWriterAction> getActions() {
        return mActions;
    }

    /**
     * Set the list of actions
     * @param mActions Actions
     */
    public void setActions(ArrayList<CanvasWriterAction> mActions) {
        this.mActions = mActions;
    }

    /**
     * Undo the last action in the list of actions and add it to the list of
     * undone actions.
     * @return True if the action could be undone
     */
    public boolean undo() {
        //undo the last action
        if (mActions.isEmpty()) {
            return false;
        }

        CanvasWriterAction currentAction = mActions.remove(mActions.size() - 1);
        switch (currentAction.type) {
            case WRITE:
                currentAction.type = CanvasWriterAction.Type.UNDO_WRITE;
                mUndoneActions.add(currentAction);
                mStrokesReference.remove(currentAction.stroke);
                return true;
            case ERASE:
                currentAction.type = CanvasWriterAction.Type.UNDO_ERASE;
                mUndoneActions.add(currentAction);
                mStrokesReference.add(currentAction.stroke);
                return true;
            default:
                return false;
        }
    }

    /**
     * Redo the last action in the list of undone actions and add it to the
     * list of actions
     * @return True if the action could be redone
     */
    public boolean redo() {
        //undo the last action
        if (mUndoneActions.isEmpty()) {
            return false;
        }

        CanvasWriterAction currentAction = mUndoneActions.remove(mUndoneActions.size() - 1);
        switch (currentAction.type) {
            case UNDO_WRITE:
                currentAction.type = CanvasWriterAction.Type.WRITE;
                mActions.add(currentAction);
                mStrokesReference.add(currentAction.stroke);
                return true;
            case UNDO_ERASE:
                currentAction.type = CanvasWriterAction.Type.ERASE;
                mActions.add(currentAction);
                mStrokesReference.remove(currentAction.stroke);
                return true;
            default:
                return false;
        }
    }
}
