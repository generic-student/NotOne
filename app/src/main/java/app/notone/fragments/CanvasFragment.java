package app.notone.fragments;

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
import app.notone.CanvasView;
import app.notone.R;

public class CanvasFragment extends Fragment {
    // The onCreateView method is called when Fragment should create its View object hierarchy, via XML layout inflation.
    String TAG = "NotOneCanvasFragment";
    View mCanvasFragmentView;

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
        CanvasView canvasView = mCanvasFragmentView.findViewById(R.id.canvasView);
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
        buttonEraser.setOnClickListener(v -> Log.d(TAG, "onClick: ERASE"));
        buttonUndo.setOnClickListener(v -> Log.d(TAG, "onClick: UNDO"));
        buttonRedo.setOnClickListener(v -> Log.d(TAG, "onClick: REDO"));


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
        Button buttonTest = fragmentActivity.findViewById(R.id.button_test);
        buttonTest.setOnClickListener(v -> Log.d(TAG, sharedPreferences.getAll().toString()));
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
}
