package app.notone.core;

import java.io.Serializable;
import java.util.ArrayList;

public class UndoRedoManager implements Serializable {
    private transient final int MAX_ACTIONS = 20;
    private ArrayList<CanvasWriterAction> mUndoneActions;
    private ArrayList<CanvasWriterAction> mActions;
    private transient ArrayList<Stroke> mStrokesReference;

    public UndoRedoManager(ArrayList<Stroke> strokesRef) {
        mActions = new ArrayList<>();
        mUndoneActions = new ArrayList<>();
        mStrokesReference = strokesRef;
    }

    public void reset() {
        mUndoneActions.clear();
        mActions.clear();
    }

    public void addAction(CanvasWriterAction action) {
        mActions.add(action);
        if(mActions.size() > MAX_ACTIONS) {
            mActions.remove(0);
        }
    }

    public void addUndoneAction(CanvasWriterAction action) {
        mUndoneActions.add(action);
        if(mUndoneActions.size() > MAX_ACTIONS) {
            mUndoneActions.remove(0);
        }
    }

    public ArrayList<CanvasWriterAction> getUndoneActions() {
        return mUndoneActions;
    }

    public void setUndoneActions(ArrayList<CanvasWriterAction> mUndoneActions) {
        this.mUndoneActions = mUndoneActions;
    }

    public ArrayList<CanvasWriterAction> getActions() {
        return mActions;
    }

    public void setActions(ArrayList<CanvasWriterAction> mActions) {
        this.mActions = mActions;
    }

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
