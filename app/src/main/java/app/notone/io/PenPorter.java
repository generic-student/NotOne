package app.notone.io;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.fragment.app.FragmentActivity;
import app.notone.views.PresetPenButton;

/**
 * Export Preset Pen Data to Json
 * @author Hackel
 */
public class PenPorter {

    private final static String TAG = "NotOnePenPorter";

    public static JSONObject presetPensToJSON(ArrayList<PresetPenButton> presetPenButtons) throws JSONException {
        Log.d(TAG, "converting " + presetPenButtons + " to Json");

        JSONObject json = new JSONObject();
        JSONArray pens = new JSONArray();
        for(int i = 0; i < presetPenButtons.size(); i++){
            PresetPenButton penButton = presetPenButtons.get(i);
            JSONObject pen = new JSONObject();
            pen.put("colorindexmapid", penButton.mcolorindexMapId);
            pen.put("mddowncolorid", penButton.mddMenuColorId);
            pen.put("mddownweightid", penButton.mddMenuWeightId);
            pen.put("mddownColorIndex", penButton.mddMenuColorIndex);
            pen.put("mddownWeightIndex", penButton.mddMenuWeightIndex);
            pen.put("mddownmWeight", penButton.mddMenuWeightValue);
            pens.put(pen);
//            Log.d(TAG, "penPresetsToJSON: converted pen to json");
        }
        json.put("pens", pens);
        return json;
    }

    public static ArrayList<PresetPenButton> presetPensFromJSON(Context context, FragmentActivity fragmentActivity, String pendata) throws JSONException {
        Log.d(TAG, "converting json " + pendata + " to preset pens");

        JSONObject json = new JSONObject(pendata);
        JSONArray jsonArray = json.getJSONArray("pens");
        ArrayList<PresetPenButton> PPBtns = new ArrayList<PresetPenButton>();

        Log.d(TAG, "penPresetsFromJSON: got " + jsonArray.length() + " pens");
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
