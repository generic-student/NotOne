package app.notone.io;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import app.notone.CanvasView;
import app.notone.CanvasWriter;
import app.notone.Stroke;

public class CanvasExporter {
    private static final String LOG_TAG = CanvasExporter.class.getSimpleName();

    public static JSONObject canvasViewToJSON(@NonNull CanvasView view) throws JSONException{
        JSONObject json = new JSONObject();

        //get the current scale
        json.put("scale", view.getScale());

        float[] values = new float[9];
        //get the view matrix
        view.getViewTransform().getValues(values);
        final JSONArray viewTransform = new JSONArray(values);
        json.put("viewTransform", viewTransform);
        //get the inverse view matrix
        view.getInverseViewTransform().getValues(values);
        final JSONArray inverseViewTransform = new JSONArray(values);
        json.put("inverseViewTransform", inverseViewTransform);
        //get the write instance
        json.put("writer", canvasWriterToJSON(view.getCanvasWriter()));

        return json;
    }

    public static JSONObject canvasWriterToJSON(@NonNull CanvasWriter writer) throws JSONException{
        //convert the strokes to json
        JSONObject json = new JSONObject();

        json.put("color", writer.getStrokeColor());
        json.put("weight", writer.getStrokeWeight());
        List<JSONObject> strokes =  writer.getStrokes().stream().
                map(s -> StrokeToJSON(s)).filter(Objects::nonNull).
                collect(Collectors.toList());

        JSONArray strokesJSON = new JSONArray(strokes);
        json.put("strokes", strokesJSON);


        return json;
    }

    public static JSONObject StrokeToJSON(@NonNull Stroke stroke) {
        JSONObject json = new JSONObject();

        try {
            json.put("color", stroke.getColor());
            json.put("weight", stroke.getWeight());
            JSONArray arr = new JSONArray(stroke.getPathPoints());
            json.put("path", arr);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return json;
    }
}
