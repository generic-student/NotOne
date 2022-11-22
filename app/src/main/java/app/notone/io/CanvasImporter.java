package app.notone.io;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import app.notone.core.CanvasView;
import app.notone.core.CanvasWriter;
import app.notone.core.CanvasWriterAction;
import app.notone.core.Stroke;

public class CanvasImporter {
    public static void initCanvasViewFromJSON(String jsonString, CanvasView view, boolean loadUndoTree) throws JSONException {
        JSONObject json = new JSONObject(jsonString);

        final float scale = (float) json.getDouble("scale");
        JSONArray viewTransformJSON = json.getJSONArray("viewTransform");
        JSONArray inverseViewTransformJSON = json.getJSONArray("inverseViewTransform");

        float[] viewTransformData = new float[9];
        for(int i = 0; i < 9; i++) {
            viewTransformData[i] = (float) viewTransformJSON.getDouble(i);
        }

        float[] inverseViewTransformData = new float[9];
        for(int i = 0; i < 9; i++) {
            inverseViewTransformData[i] = (float) inverseViewTransformJSON.getDouble(i);
        }

        CanvasWriter writer = canvasWriterFromJSON(json.getJSONObject("writer"), loadUndoTree);

        view.setScale(scale);
        view.getViewTransform().setValues(viewTransformData);
        view.getInverseViewTransform().setValues(inverseViewTransformData);
        view.setCanvasWriter(writer);

    }

    public static CanvasWriter canvasWriterFromJSON(JSONObject json, boolean loadUndoTree) throws JSONException {
        final int color = json.getInt("color");
        final float weight = (float) json.getDouble("weight");
        CanvasWriter writer = new CanvasWriter(weight, color);

        JSONArray strokesJSON = json.getJSONArray("strokes");
        for (int i = 0; i < strokesJSON.length(); i++) {
            JSONObject strokeJSON = strokesJSON.getJSONObject(i);
            Stroke stroke = StrokeFromJSON(strokeJSON);
            writer.getStrokes().add(stroke);
        }

        if(!loadUndoTree) {
            return writer;
        }

        //load the undo tree
        JSONArray actionsJSON = json.getJSONArray("actions");
        JSONArray undoneActionsJSON = json.getJSONArray("undoneActions");

        ArrayList<CanvasWriterAction> actions = new ArrayList<>();
        ArrayList<CanvasWriterAction> undoneActions = new ArrayList<>();

        for (int i = 0; i < actionsJSON.length(); i++) {
            JSONObject actionJSON = actionsJSON.getJSONObject(i);
            CanvasWriterAction action = canvasWriterActionFromJSON(actionJSON, writer.getStrokes());
            actions.add(action);
        }

        for (int i = 0; i < undoneActionsJSON.length(); i++) {
            JSONObject undoneActionJSON = undoneActionsJSON.getJSONObject(i);
            CanvasWriterAction undoneAction = canvasWriterActionFromJSON(undoneActionJSON, writer.getStrokes());
            undoneActions.add(undoneAction);
        }

        writer.setActions(actions);
        writer.setUndoneActions(undoneActions);

        return writer;
    }

    public static Stroke StrokeFromJSON(JSONObject json) throws JSONException {
        final int color = json.getInt("color");
        final float weight = (float) json.getDouble("weight");
        Stroke stroke = new Stroke(color, weight);

        JSONArray pathJSON = json.getJSONArray("path");
        for (int i = 0; i < pathJSON.length(); i++) {
            stroke.getPathPoints().add((float) pathJSON.getDouble(i));
        }
        stroke.initPathFromPathPoints();

        return stroke;
    }

    public static CanvasWriterAction canvasWriterActionFromJSON(JSONObject json, ArrayList<Stroke> strokes) throws JSONException {
        final String typeString = json.getString("actionType");
        final CanvasWriterAction.Type type = CanvasWriterAction.Type.valueOf(typeString);
        final int strokeId = json.getInt("strokeId");

        Stroke stroke;
        if(strokeId == -1) {
            stroke = StrokeFromJSON(json.getJSONObject("stroke"));
        } else {
            stroke = strokes.get(strokeId);
        }

        //map the strokeId with a Stroke in the strokes list
        CanvasWriterAction action = new CanvasWriterAction(type, stroke);

        return action;
    }
}
