package app.notone.io;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.fragment.app.FragmentActivity;
import app.notone.ui.PresetPenButton;

/**
 * Export preset pen data to json
 * @author Luca Hackel
 * @since 202212XX
 */

public class PenPorter {

    private final static String TAG = "NotOnePenPorter";

    /**
     * convert the presetPen data to a json object
     * @param presetPenButtons
     * @throws JSONException
     */
    public static JSONObject presetPensToJSON(ArrayList<PresetPenButton> presetPenButtons) throws JSONException {
//        Log.d(TAG, "converting " + presetPenButtons + " to Json");

        JSONObject json = new JSONObject();
        JSONArray pens = new JSONArray();
        for(int i = 0; i < presetPenButtons.size(); i++){
            PresetPenButton penButton = presetPenButtons.get(i);
            JSONObject pen = new JSONObject();
            pen.put("colorindexmapid", penButton.mColor2IndexMapId);
            pen.put("mddowncolorid", penButton.mDDMenuColorId);
            pen.put("mddownweightid", penButton.mDDMenuWeightId);
            pen.put("mddownColorIndex", penButton.mDDMenuColorIndex);
            pen.put("mddownWeightIndex", penButton.mDDMenuWeightIndex);
            pen.put("mddownmWeight", penButton.mDDMenuWeightValue);
            pens.put(pen);
//            Log.d(TAG, "penPresetsToJSON: converted pen to json");
        }
        json.put("pens", pens);
        return json;
    }

    /**
     * generate a arraylist of preset pens from a json object as string
     * @param context
     * @param fragmentActivity
     * @param pendata
     * @return
     * @throws JSONException
     */
    public static ArrayList<PresetPenButton> presetPensFromJSON(Context context, FragmentActivity fragmentActivity, String pendata) throws JSONException {
//        Log.d(TAG, "converting json " + pendata + " to preset pens");

        JSONObject json = new JSONObject(pendata);
        JSONArray jsonArray = json.getJSONArray("pens");
        ArrayList<PresetPenButton> PPBtns = new ArrayList<PresetPenButton>();

//        Log.d(TAG, "penPresetsFromJSON: got " + jsonArray.length() + " pens");
        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonpen = jsonArray.getJSONObject(i);
            int colorindexmapid = jsonpen.getInt("colorindexmapid");
            int mddowncolorid = jsonpen.getInt("mddowncolorid");
            int mddownweightid = jsonpen.getInt("mddownweightid");
            int mddownColorIndex = jsonpen.getInt("mddownColorIndex");
            int mddownWeightIndex  = jsonpen.getInt("mddownWeightIndex");
            int mddownmWeight  = jsonpen.getInt("mddownmWeight");
            PPBtns.add(new PresetPenButton(context, fragmentActivity,
                    mddowncolorid, mddownweightid,
                    colorindexmapid, mddownColorIndex, mddownWeightIndex, mddownmWeight));
//            Log.d(TAG, "adding pen number " + i + " to list");
        }
        return PPBtns;
    }
}
