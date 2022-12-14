package app.notone.io;

import android.net.Uri;
import android.util.Log;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import app.notone.core.CanvasPdfDocument;
import app.notone.core.CanvasView;
import app.notone.core.CanvasWriter;
import app.notone.core.CanvasWriterAction;
import app.notone.core.Stroke;

public class CanvasImporter {

    private static final String TAG = "CanvasImporter";

    public static class CanvasImportData {
        CanvasView canvasView;
        String jsonString;
        boolean loadUndoTree;

        public CanvasImportData(String jsonString, CanvasView canvasView, boolean loadUndoTree) {
            this.canvasView = canvasView;
            this.jsonString = jsonString;
            this.loadUndoTree = loadUndoTree;
        }
    }

    public static class InitCanvasFromJsonTask extends AsyncTask<CanvasImportData, Integer, Void> {
        protected Void doInBackground(CanvasImportData... data) {
            for(CanvasImportData canvasImportData : data) {
                JSONObject json = null;
                try {
                    json = new JSONObject(canvasImportData.jsonString);
                    final float scale = (float) json.getDouble("scale");
                    canvasImportData.canvasView.setScale(scale);
                    canvasImportData.canvasView.invalidate();

                    JSONArray viewTransformJSON = json.getJSONArray("viewTransform");
                    JSONArray inverseViewTransformJSON = json.getJSONArray("inverseViewTransform");
                    float[] viewTransformData = new float[9];
                    for(int i = 0; i < 9; i++) {
                        viewTransformData[i] = (float) viewTransformJSON.getDouble(i);
                    }
                    canvasImportData.canvasView.getViewTransform().setValues(viewTransformData);
                    canvasImportData.canvasView.invalidate();

                    float[] inverseViewTransformData = new float[9];
                    for(int i = 0; i < 9; i++) {
                        inverseViewTransformData[i] = (float) inverseViewTransformJSON.getDouble(i);
                    }
                    canvasImportData.canvasView.getInverseViewTransform().setValues(inverseViewTransformData);
                    canvasImportData.canvasView.invalidate();

                    CanvasWriter writer = canvasWriterFromJSON(json.getJSONObject("writer"), canvasImportData.loadUndoTree);
                    canvasImportData.canvasView.setCanvasWriter(writer);
                    canvasImportData.canvasView.invalidate();

                    CanvasPdfDocument document = canvasPdfDocumentFromJson(json.getJSONObject("pdf"));
                    canvasImportData.canvasView.setPdfDocument(document);
                    canvasImportData.canvasView.invalidate();

                    String uri = json.getString("uri");
                    canvasImportData.canvasView.setUri(Uri.parse(uri));

//                    canvasImportData.canvasView.setScale(scale);
//                    canvasImportData.canvasView.getViewTransform().setValues(viewTransformData);
//                    canvasImportData.canvasView.getInverseViewTransform().setValues(inverseViewTransformData);
//                    canvasImportData.canvasView.setCanvasWriter(writer);
//                    canvasImportData.canvasView.setPdfDocument(document);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Void result) {

        }

    }


    public static void initCanvasViewFromJSON(String jsonString, CanvasView view, boolean loadUndoTree) throws JSONException, IllegalArgumentException {
        if(jsonString.equals("")){
            Log.e(TAG, "initCanvasViewFromJSON: no Data in CanvasFile");
            throw new IllegalArgumentException("no Data in CanvasFile");
        }
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


        CanvasPdfDocument document = canvasPdfDocumentFromJson(json.getJSONObject("pdf"));

        view.setScale(scale);
        view.getViewTransform().setValues(viewTransformData);
        view.getInverseViewTransform().setValues(inverseViewTransformData);
        view.setCanvasWriter(writer);
        view.setPdfDocument(document);

        // import uri
        String uri = json.getString("uri");
        view.setUri(Uri.parse(uri));
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

        writer.getUndoRedoManager().setActions(actions);
        writer.getUndoRedoManager().setUndoneActions(undoneActions);

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

    public static CanvasPdfDocument canvasPdfDocumentFromJson(JSONObject json) throws JSONException {
        JSONArray pagesJSON = json.getJSONArray("pages");
        Bitmap[] pages = new Bitmap[pagesJSON.length()];
        for (int i = 0; i < pagesJSON.length(); i++) {
            JSONObject bitmapJSON = pagesJSON.getJSONObject(i);
            pages[i] = bitmapFromJSON(bitmapJSON);
        }

        CanvasPdfDocument document = new CanvasPdfDocument();
        document.setPages(pages);

        return document;
    }

    public static Bitmap bitmapFromJSON(JSONObject json) throws JSONException {
        String encodedImage = json.getString("data");
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        return decodedByte;
    }
}
