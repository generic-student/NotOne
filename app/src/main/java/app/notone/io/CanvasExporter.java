package app.notone.io;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import app.notone.CanvasView;
import app.notone.CanvasWriter;
import app.notone.CanvasWriterAction;
import app.notone.Stroke;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

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

        // save Uri of canvas
        json.put("uri", view.getCurrentURI().toString());

        return json;
    }

    public static void canvasViewToPDF(){

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
        List<JSONObject> actionsJSON = writer.getActions().stream().map(action -> {
            final int strokeId = writer.getStrokes().indexOf(action.stroke);
            return canvasWriterActionToJSON(action, strokeId);
        }).collect(Collectors.toList());

        JSONArray actions = new JSONArray(actionsJSON);

        List<JSONObject> undoneActionsJSON = writer.getUndoneActions().stream().map(action -> {
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

    public void saveJSONtoFileStorage(){

    }

    public static final int CREATE_FILE_REQUEST_CODE = 1;

    public static void createFile(
        Activity activity, Uri pickerInitialUri, ActivityResultRegistry activityResultRegistry) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/text");
        intent.putExtra(Intent.EXTRA_TITLE, "invoice.pdf");

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document. (pickerInitialUri)
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        activity.startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
    }
    public static void alterDocument(Activity activity, Uri uri, String json) {
        try {
            ParcelFileDescriptor pfd = activity.getContentResolver().
                    openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write(("Overwritten at " + System.currentTimeMillis() +
                    "\n" + json).getBytes());
            // Let the document provider know you're done by closing the stream.
            fileOutputStream.close();
            pfd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
// https://developer.android.com/training/data-storage/shared/documents-files#java

}
