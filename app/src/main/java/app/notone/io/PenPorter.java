package app.notone.io;

import android.content.Context;
import android.util.Log;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.fragment.app.FragmentActivity;
import app.notone.views.PresetPenButton;

public class PenPorter {

    private final static String TAG = "NotOnePenPorter";

    public static JSONObject penPresetsToJSON(ArrayList<PresetPenButton> presetPenButtons) throws JSONException {
        Log.d(TAG, "penPresetsToJSON: converting " + presetPenButtons + " to Json");
        JSONObject json = new JSONObject();
        JSONArray pens = new JSONArray();
        for(int i = 0; i < presetPenButtons.size(); i++){
            PresetPenButton penButton = presetPenButtons.get(i);
            JSONObject pen = new JSONObject();
            pen.put("colorindexmapid", penButton.color_index_mapid);
            pen.put("mddowncolorid", penButton.ddownm_pen_colorsid);
            pen.put("mddownweightid", penButton.ddownm_pen_weightsid);
            pen.put("mddownColorIndex", penButton.mddownColorIndex);
            pen.put("mddownWeightIndex", penButton.mddownWeightIndex);
            pen.put("mddownmWeight", penButton.mddownmWeight);
            pens.put(pen);
            Log.d(TAG, "penPresetsToJSON: converted pen to json");
        }
        json.put("pens", pens);
        return json;
    }

    public static ArrayList<PresetPenButton> penPresetsFromJSON(Context context, FragmentActivity fragmentActivity, String pendata) throws JSONException {
        Log.d(TAG, "penPresetsFromJSON " + pendata + " E");
        JSONObject json = new JSONObject(pendata);
        JSONArray jsonArray = json.getJSONArray("pens");
        ArrayList<PresetPenButton> PPBtns = new ArrayList<PresetPenButton>();

        Log.d(TAG, "penPresetsFromJSON: got " + jsonArray.length() + " pens");
        for(int i = 0; i < jsonArray.length(); i++) {
            Log.d(TAG, "penPresetsFromJSON: adding pen number " + i + " to list");
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
        }
        // do stuff
        return PPBtns;
    }
}
