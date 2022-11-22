package app.notone.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;

import org.json.JSONException;

import app.notone.CanvasView;
import app.notone.CanvasWriter;
import app.notone.R;
import app.notone.WriteMode;
import app.notone.io.CanvasExporter;
import app.notone.io.CanvasImporter;

public class CanvasFragment extends Fragment {
    private static final String SHARED_PREFS_TAG = "NotOneSharedPrefs";
    private static final String LOG_TAG = CanvasFragment.class.getSimpleName();

    // The onCreateView method is called when Fragment should create its View object hierarchy, via XML layout inflation.
    String TAG = "NotOneCanvasFragment";
    View mCanvasFragmentView;
    public CanvasView canvasView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        mCanvasFragmentView = inflater.inflate(R.layout.fragment_canvas, parent, false);
        return mCanvasFragmentView;
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Config Dropdowns for Pen Settings
        canvasView = mCanvasFragmentView.findViewById(R.id.canvasView);
        HashMap<String, Integer> penColors = new HashMap<>(); // get from res instead
        penColors.put("RED", Color.RED);
        penColors.put("GREEN", Color.GREEN);
        penColors.put("BLUE", Color.BLUE);
        penColors.put("YELLOW", Color.YELLOW);
        penColors.put("CYAN", Color.CYAN);
        setDropdownContent(R.id.spinner_pen_colors, R.array.pen_colors, (adapterView, vw, i, l) ->
                canvasView.setStrokeColor(penColors.get(adapterView.getItemAtPosition(i))));
        setDropdownContent(R.id.spinner_pen_weights, R.array.pen_weights, (adapterView, vw, i, l) ->
                canvasView.setStrokeWeight(Float.parseFloat((String) adapterView.getItemAtPosition(i))));

        // Undo Redo activate Eraser Actions
        FragmentActivity fragmentActivity = getActivity();
        ImageButton buttonEraser = fragmentActivity.findViewById(R.id.button_eraser);
        ImageButton buttonUndo = fragmentActivity.findViewById(R.id.button_undo);
        ImageButton buttonRedo = fragmentActivity.findViewById(R.id.button_redo);
        buttonEraser.setOnClickListener(v -> {
            if(canvasView.getCanvasWriter().getWritemode() == WriteMode.ERASER) {
                buttonEraser.setBackgroundColor(Color.TRANSPARENT);
                canvasView.getCanvasWriter().setWritemode(WriteMode.PEN);
            }
            else {
                buttonEraser.setBackgroundColor(Color.argb(120, 255, 255, 255));
                canvasView.getCanvasWriter().setWritemode(WriteMode.ERASER);
            }
        });
        buttonUndo.setOnClickListener(v -> canvasView.undo());
        buttonRedo.setOnClickListener(v -> canvasView.redo());


        /* create pen presets */
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(fragmentActivity);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();

        ImageButton buttonAddPen = fragmentActivity.findViewById(R.id.button_add_pen);
        Spinner spinnerPenColors = fragmentActivity.findViewById(R.id.spinner_pen_colors);
        Spinner spinnerPenWeight = fragmentActivity.findViewById(R.id.spinner_pen_weights);
        LinearLayout linearLayout = fragmentActivity.findViewById(R.id.canvas_tools_container);

        AtomicReference<Integer> presetPenNumber = new AtomicReference<>(0);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
//        ConstraintLayout constraintLayout = (ConstraintLayout) inflater.inflate(R.layout.button_preset_pen, null, false);

        buttonAddPen.setOnClickListener(v -> {
            // save pen preset
            Set<String> penPreset = new HashSet<>();
            String presetPenWeight = spinnerPenWeight.getSelectedItem().toString();
            String presetPenColor = spinnerPenColors.getSelectedItem().toString();
            penPreset.add(presetPenWeight);
            penPreset.add(presetPenColor);
            spEditor.putStringSet("penpreset_" + presetPenNumber.toString(), penPreset).apply();
            presetPenNumber.getAndSet(presetPenNumber.get() + 1);

            // add to layout
            ConstraintLayout penPresetLayout = (ConstraintLayout) inflater.inflate(R.layout.button_preset_pen, null, false);
            ImageButton imageButton = penPresetLayout.findViewById(R.id.imgbtn_pen_preset);
            imageButton.setColorFilter(penColors.get(presetPenColor));
            imageButton.setOnClickListener(v1 -> {
//                TODO update spinners
//                spinnerPenColors.setSelection(penColors.get,true);
                canvasView.setStrokeColor(penColors.get(presetPenColor));
                canvasView.setStrokeWeight(Float.parseFloat(presetPenWeight));
            });

            linearLayout.addView(penPresetLayout, 1); // cause of the test button

            Log.d(TAG, "onViewCreated: saved Pen Preset: " + (presetPenNumber.get() - 1) +  penPreset.toString());
        });


        /* remove pen preset */
        ImageButton buttonSubPen = fragmentActivity.findViewById(R.id.button_del_pen);
        buttonSubPen.setOnClickListener(v -> {
            Log.d(TAG, "onViewCreated: delete active pen preset");
        });

        // Test
        //Button buttonTest = fragmentActivity.findViewById(R.id.button_test);
        //buttonTest.setOnClickListener(v -> Log.d(TAG, sharedPreferences.getAll().toString()));
    }

    @FunctionalInterface
    private interface onClickDropDownItem {
        void onClick(AdapterView<?> adapterView, View view, int i, long l);
    }

    private void setDropdownContent(int spinnerId, int spinnerContentId, onClickDropDownItem clickDropDownItem) {
        ArrayAdapter<CharSequence> dropdownColors = ArrayAdapter.createFromResource(
                getActivity(), spinnerContentId, R.layout.spinner_dropdown_pen_field);
        Spinner dropdownPenColor = getActivity().findViewById(spinnerId);

        dropdownColors.setDropDownViewResource(R.layout.spinner_dropdown_pen_items);
        dropdownPenColor.setAdapter(dropdownColors); // set to spinner
        dropdownPenColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: " + adapterView.getItemAtPosition(i));
                clickDropDownItem.onClick(adapterView, view, i, l);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS_TAG, MODE_PRIVATE);

        //load the data from the sharedPrefs
        String data = sharedPreferences.getString("lastOpenedCanvasWriter", "");

        try {
            CanvasImporter.initCanvasViewFromJSON(data, canvasView, true);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Could not load the last opened canvas: " + e.getMessage());
        }
    }

    @Override
    public void onPause() {

        String jsonString = "";
        try {
            jsonString = CanvasExporter.canvasViewToJSON(canvasView, true).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Storing data into SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS_TAG, MODE_PRIVATE);

        // Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Write the byte stream to the preferences
        editor.putString("lastOpenedCanvasWriter", jsonString);

        // write changes to file
        editor.commit();

        super.onPause();
    }


//
//    @Override
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//
//        savedInstanceState.putSerializable("CanvasWriter", canvasView.getCanvasWriter());
//
//        super.onSaveInstanceState(savedInstanceState);
//    }
//
//
//    @Override
//    public void onRestoreInstanceState(Bundle savedInstanceState) {
//
//        super.onRestoreInstanceState(savedInstanceState);
//
//        CanvasWriter pen = (CanvasWriter) savedInstanceState.getSerializable("CanvasWriter");
//        canvasView.setCanvasWriter(pen);
//
//
//    }
}
