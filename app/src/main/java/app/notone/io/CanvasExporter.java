package app.notone.io;

import android.graphics.Bitmap;
import android.util.Base64;

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

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import app.notone.core.CanvasPdfDocument;
import app.notone.core.CanvasView;
import app.notone.core.CanvasWriter;
import app.notone.core.CanvasWriterAction;
import app.notone.core.Stroke;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

/**
 * Converts a CanvasView Object and all its associated Members to a JSONObject for serialization
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public class CanvasExporter {
    /** Tag for logging */
    private static final String LOG_TAG = CanvasExporter.class.getSimpleName();

    /**
     * Converts a {@link CanvasView} to a JSONObject
     * @param view CanvasView Object to be converted
     * @param exportUndoTree True if the undo-redo tree should be included in the export
     * @return JSONObject
     * @throws JSONException When the CanvasView cannot be converted to a JSONObject
     */
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
        // save Uri of canvas
        json.put("uri", view.getCurrentURI().toString());

        return json;
    }

    
    /** 
     * Converts a {@link CanvasWriter} to a JSONObject
     * @param writer CanvasWriter Object to be converted
     * @param exportUndoTree True if the undo-redo tree should be incuded in the export
     * @return JSONObject
     * @throws JSONException When the CanvasWriter cannot be converted to a JSONObject
     */
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

    
    /** 
     * Converts a {@link Stroke} to a JSONObject
     * @param stroke Stroke Object to be converted
     * @return JSONObject
     */
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

    
    /** 
     * Converts a {@link CanvasWriterAction} to a JSONObject
     * @param action CanvasWriterAction Object to be converted
     * @param strokeId the index of the stroke that is associated with the action
     * @return JSONObject
     */
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

    
    /** 
     * Converts a {@link CanvasPdfDocument} to a JSONObject
     * @param document CanvasPdfDocument Object to be converted
     * @return JSONObject
     */
    public static JSONObject canvasPdfDocumentToJson(CanvasPdfDocument document) {
        JSONObject json = new JSONObject();
        try {
            Bitmap[] pagesList = document.getPages();//document.getPages().length > 15 ? Arrays.copyOfRange(document.getPages(), 0, 14) : document.getPages();

            List<JSONObject> pagesJSON = Arrays.stream(pagesList).
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

    
    /** 
     * Converts a {@link Bitmap} to a JSONObject
     * @param bitmap Bitmap Object to be converted
     * @return JSONObject
     */
    public static JSONObject bitmapToJson(Bitmap bitmap) {
        JSONObject json = new JSONObject();

        //convert bitmap to string
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        encodedImage = Base64.encodeToString(byteArrayBitmapStream.toByteArray(), Base64.DEFAULT);

        try {
            json.put("data", encodedImage);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return json;
    }
}
