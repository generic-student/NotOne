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
        Log.d(TAG, "penPresetsToJSON: " + presetPenButtons + " Buttons");
        JSONObject json = new JSONObject();
        JSONArray pens = new JSONArray();
        for(int i = 0; i < presetPenButtons.size(); i++){
            JSONObject pen = new JSONObject();
            pen.put("a","aaaaaaa");
            pens.put(pen);
            Log.d(TAG, "penPresetsToJSON: AAA");
//            presetPenButtons.get(i)
//
//            penjson.put(i, )
//
//            json.put();
        }
        json.put("pens", pens);
        return json;
    }

    public static ArrayList<PresetPenButton> penPresetsFromJSON(Context context, FragmentActivity fragmentActivity, int mddowncolor, int mddownweight, String pendata) throws JSONException {
        Log.d(TAG, "penPresetsFromJSON " + "E");
        JSONObject json = new JSONObject(pendata);
        JSONArray jsonArray = json.getJSONArray("pens");
        ArrayList<PresetPenButton> PPBtns = new ArrayList<PresetPenButton>();

        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonpen = jsonArray.getJSONObject(i);
            Log.d(TAG, "penPresetsFromJSON: " + jsonpen.get("a") + " EEE");
            PPBtns.add(new PresetPenButton(context, fragmentActivity, mddowncolor, mddownweight, new int[]{1, 3}, 1,2,1));
        }
        // do stuff
        return  new ArrayList<PresetPenButton>();
    }
}
