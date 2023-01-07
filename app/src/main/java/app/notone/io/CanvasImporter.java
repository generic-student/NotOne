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

import app.notone.core.CanvasPdfDocument;
import app.notone.core.CanvasView;
import app.notone.core.CanvasWriter;
import app.notone.core.CanvasWriterAction;
import app.notone.core.Stroke;
import app.notone.ui.CanvasFragmentFlags;
import app.notone.ui.fragments.CanvasFragment;

/**
 * Constructs a CanvasView and all its associated Members from a JSONObject
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public class CanvasImporter {

    /** Tag for logging */
    private static final String TAG = "CanvasImporter";

    /**
     * Dataclass that will be sent to the AsyncTask
     * {@link InitCanvasFromJsonTask}
     * so that it has all the required data
     * @author Kai Titgens
     * @author kai.titgens@stud.th-owl.de
     * @version 0.1
     * @since 0.1
     */
    public static class CanvasImportData {
        /** Reference to the canvasView being initialized */
        CanvasView canvasView;
        /** String containing the data in a JSON format */
        String jsonString;
        /** True if the undo-redo tree should be included in the import */
        boolean loadUndoTree;
        /** 
         * Settings that determine what the current state of the 
         * {@link CanvasFragment} is and what should be imported
         */
        CanvasFragmentFlags canvasFragmentFlags;

        public CanvasImportData(String jsonString, CanvasView canvasView, boolean loadUndoTree, CanvasFragmentFlags canvasFragmentFlags) {
            this.canvasView = canvasView;
            this.jsonString = jsonString;
            this.loadUndoTree = loadUndoTree;
            this.canvasFragmentFlags = canvasFragmentFlags;
        }
    }

    /**
     * AsyncTask for initializing a {@link CanvasView} from a JSON-String.
     * This Task will run in the background but will invalidate the Canvas
     * once it is finished.
     */
    public static class InitCanvasFromJsonTask extends AsyncTask<CanvasImportData, Integer, Void> {
        /**
         * The function that will run in the background
         */
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

                    //determine if the pdf import should be loaded from the save file or from a uri.
                    //it will be loaded from a uri if the importPdf action had been called earlier.
                    if(canvasImportData.canvasFragmentFlags == null || !canvasImportData.canvasFragmentFlags.isLoadingPdf()) {
                        CanvasPdfDocument document = canvasPdfDocumentFromJson(json.getJSONObject("pdf"));
                        canvasImportData.canvasView.setPdfDocument(document);
                        Log.d("PDF", "(CanvasImporter) loaded from file");
                        canvasImportData.canvasView.invalidate();
                    }

                    String uri = json.getString("uri");
                    canvasImportData.canvasView.setUri(Uri.parse(uri));

                    Log.d(TAG, "Canvas is fully loaded!");
                    canvasImportData.canvasView.setLoaded(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        /**
         * This function will be called on each progress increment
         * @param progress
         */
        protected void onProgressUpdate(Integer... progress) {

        }

        /**
         * This function will be called once the async task is finished.
         * @param result
         */
        protected void onPostExecute(Void result) {
            CanvasFragment.sFlags.setOpenFile(false);
            CanvasFragment.sFlags.setNewFile(false);
            CanvasFragment.sCanvasView.invalidate();
        }

    }


    
    /** 
     * Initializes a {@link CanvasView} from a JSON-String.
     * This Task will not run in the background and it will
     * block the main thread.
     * @param jsonString String containing the data for the CanvasView in JSON format
     * @param view The CanvasView to be initialized
     * @param loadUndoTree True if the undo-redo tree should be included in the import
     * @throws JSONException
     * @throws IllegalArgumentException
     */
    @Deprecated
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

    
    /** 
     * Initializes a {@link CanvasWriter} from a JSONObject
     * @param json JSONObject containing the required data
     * @param loadUndoTree True if the undo-redo tree should be included in the import
     * @return CanvasWriter
     * @throws JSONException
     */
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

    
    /** 
     * Initializes a {@link Stroke} from a JSONObject
     * @param json JSONObject containing the required data
     * @return Stroke
     * @throws JSONException
     */
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

    
    /** 
     * Initializes a {@link CanvasWriterAction} from a JSONObject.
     * Since the action contains an index pointing to an associated stroke,
     * a list of strokes has to be included that can be indexed.
     * @param json JSONObject containing the required data
     * @param strokes list of strokes that the Action indexes
     * @return CanvasWriterAction
     * @throws JSONException
     */
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

    
    /** 
     * Initializes a {@link CanvasPdfDocument} from a JSONObject
     * @param json JSONObject containing the required data
     * @return CanvasPdfDocument
     * @throws JSONException
     */
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

    
    /** 
     * Initializes a {@link Bitmap} from a JSONObject
     * @param json JSONObject containing the require data
     * @return Bitmap
     * @throws JSONException
     */
    public static Bitmap bitmapFromJSON(JSONObject json) throws JSONException {
        String encodedImage = json.getString("data");
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        return decodedByte;
    }
}
