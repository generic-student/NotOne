package app.notone.io;

import android.graphics.Bitmap;
import android.util.Base64;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import app.notone.core.CanvasPdfDocument;
import app.notone.core.CanvasView;
import app.notone.core.CanvasWriter;
import app.notone.core.CanvasWriterAction;
import app.notone.core.Stroke;

public class CanvasExporter {
    private static final String LOG_TAG = CanvasExporter.class.getSimpleName();

    public static JSONObject canvasViewToJSON(@NonNull CanvasView view, boolean exportUndoTree) throws JSONException{
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
        json.put("writer", canvasWriterToJSON(view.getCanvasWriter(), exportUndoTree));
        //get the pdfDocument instance
        json.put("pdf", canvasPdfDocumentToJson(view.getPdfDocument()));

        return json;
    }

    public static JSONObject canvasWriterToJSON(@NonNull CanvasWriter writer, boolean exportUndoTree) throws JSONException{
        //convert the strokes to json
        JSONObject json = new JSONObject();

        json.put("color", writer.getStrokeColor());
        json.put("weight", writer.getStrokeWeight());
        List<JSONObject> strokes =  writer.getStrokes().stream().
                map(s -> strokeToJSON(s)).filter(Objects::nonNull).
                collect(Collectors.toList());

        JSONArray strokesJSON = new JSONArray(strokes);
        json.put("strokes", strokesJSON);

        if(!exportUndoTree) {
            return json;
        }

        //export the undo tree
        List<JSONObject> actionsJSON = writer.getUndoRedoManager().getActions().stream().map(action -> {
            final int strokeId = writer.getStrokes().indexOf(action.stroke);
            return canvasWriterActionToJSON(action, strokeId);
        }).collect(Collectors.toList());

        JSONArray actions = new JSONArray(actionsJSON);

        List<JSONObject> undoneActionsJSON = writer.getUndoRedoManager().getUndoneActions().stream().map(action -> {
            final int strokeId = writer.getStrokes().indexOf(action.stroke);
            return canvasWriterActionToJSON(action, strokeId);
        }).collect(Collectors.toList());

        JSONArray undoneActions = new JSONArray(undoneActionsJSON);

        json.put("actions", actions);
        json.put("undoneActions", undoneActions);

        return json;
    }

    public static JSONObject strokeToJSON(@NonNull Stroke stroke) {
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

    public static JSONObject canvasWriterActionToJSON(CanvasWriterAction action, int strokeId) {
        JSONObject json = new JSONObject();
        try {
            json.put("actionType", action.type);
            json.put("strokeId", strokeId);
            if(strokeId == -1) {
                json.put("stroke", strokeToJSON(action.stroke));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return json;
    }

    public static JSONObject canvasPdfDocumentToJson(CanvasPdfDocument document) {
        JSONObject json = new JSONObject();
        try {
            json.put("scaling", document.getScaling());
            List<JSONObject> pagesJSON = Arrays.stream(document.getPages()).
                    map(page -> bitmapToJson(page)).
                    filter(Objects::nonNull).
                    collect(Collectors.toList());
            JSONArray pages = new JSONArray(pagesJSON);
            json.put("pages", pages);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return json;
    }

    public static JSONObject bitmapToJson(Bitmap bitmap) {
        JSONObject json = new JSONObject();

        //convert bitmap to string
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

        System.out.println(encodedImage);

        try {
            json.put("data", encodedImage);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return json;
    }
}
