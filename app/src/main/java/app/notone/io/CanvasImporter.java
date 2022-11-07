package app.notone.io;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import app.notone.CanvasView;
import app.notone.CanvasWriter;
import app.notone.Stroke;

public class CanvasImporter {
    public static void initCanvasViewFromJSON(String jsonString, CanvasView view) throws JSONException {
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

        CanvasWriter writer = canvasWriterFromJSON(json.getJSONObject("writer"));

        view.setScale(scale);
        view.getViewTransform().setValues(viewTransformData);
        view.getInverseViewTransform().setValues(inverseViewTransformData);
        view.setCanvasWriter(writer);

    }

    public static CanvasWriter canvasWriterFromJSON(JSONObject json) throws JSONException {
        final int color = json.getInt("color");
        final float weight = (float) json.getDouble("weight");
        CanvasWriter writer = new CanvasWriter(weight, color);

        JSONArray strokesJSON = json.getJSONArray("strokes");
        for (int i = 0; i < strokesJSON.length(); i++) {
            JSONObject strokeJSON = strokesJSON.getJSONObject(i);
            Stroke stroke = StrokeFromJSON(strokeJSON);
            writer.getStrokes().add(stroke);
        }

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
}
