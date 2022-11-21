package app.notone.fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import app.notone.CanvasView;
import app.notone.R;
import app.notone.WriteMode;
import app.notone.io.CanvasExporter;
import app.notone.io.CanvasImporter;

import static android.content.Context.MODE_PRIVATE;

public class CanvasFragment extends Fragment {

    private static final String SHARED_PREFS_TAG = "NotOneSharedPrefs";
    private static final String LOG_TAG = CanvasFragment.class.getSimpleName();

    String TAG = "NotOneCanvasFragment";
    ArrayList<ImageButton> mImageButtonPenToolGroup = new ArrayList<>();
    View mCanvasFragmentView;
    private CanvasView canvasView;


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
            jsonString = CanvasExporter.canvasViewToJSON(canvasView, true).toString(1);
//            Log.d(LOG_TAG, jsonString);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        mCanvasFragmentView = inflater.inflate(R.layout.fragment_canvas, parent, false);
        return mCanvasFragmentView;
    }

    // This event is triggered soon after onCreateView()
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        /* Config Dropdowns for Pen Settings */
        canvasView = mCanvasFragmentView.findViewById(R.id.canvasView);
        int[] colors = getResources().getIntArray(R.array.pen_color_values);
        setDropdownContent(R.id.spinner_pen_colors, R.array.pen_colors, (adapterView, vw, i, l) -> canvasView.setStrokeColor(colors[i]));
        setDropdownContent(R.id.spinner_pen_weights, R.array.pen_weights, (adapterView, vw, i, l) -> canvasView.setStrokeWeight(Float.parseFloat((String) adapterView.getItemAtPosition(i))));

        /* Undo Redo activate Eraser Actions */
        FragmentActivity fragmentActivity = getActivity();
        ImageButton buttonEraser = fragmentActivity.findViewById(R.id.button_eraser);
        ImageButton buttonUndo = fragmentActivity.findViewById(R.id.button_undo);
        ImageButton buttonRedo = fragmentActivity.findViewById(R.id.button_redo);
        mImageButtonPenToolGroup.add(buttonEraser);
        buttonEraser.setOnClickListener(v -> {
            if (canvasView.getCanvasWriter().getWritemode() == WriteMode.ERASER) {
                setActiveState(mImageButtonPenToolGroup, buttonEraser, false);
                canvasView.getCanvasWriter().setWritemode(WriteMode.PEN);
            } else {
                setActiveState(mImageButtonPenToolGroup, buttonEraser, true);
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
        LinearLayout linearLayout = fragmentActivity.findViewById(R.id.canvas_pens_container);

        AtomicReference<Integer> presetPenNumber = new AtomicReference<>(0);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        mImageButtonPenToolGroup.add(buttonAddPen);

        buttonAddPen.setOnClickListener(v -> {
            // save pen preset
            Set<String> penPreset = new HashSet<>();
            String presetPenWeight = spinnerPenWeight.getSelectedItem().toString();
            int presetPenWeightIndex = spinnerPenWeight.getSelectedItemPosition();
            String presetPenColor = spinnerPenColors.getSelectedItem().toString();
            int presetPenColorIndex = spinnerPenColors.getSelectedItemPosition();
            penPreset.add(presetPenWeight);
            penPreset.add(presetPenColor);
            spEditor.putStringSet("penpreset_" + presetPenNumber.toString(), penPreset).apply();
            presetPenNumber.getAndSet(presetPenNumber.get() + 1);

            // add to layout
            ConstraintLayout penPresetLayout = (ConstraintLayout) inflater.inflate(R.layout.button_preset_pen, null, false);
            ImageButton buttonPresetPen = penPresetLayout.findViewById(R.id.imgbtn_pen_preset);
            penPresetLayout.removeAllViews();
            buttonPresetPen.setColorFilter(colors[presetPenColorIndex]);
            float scale = (float) ((2 - 2 * Math.exp(-Double.parseDouble(presetPenWeight) / 8)));
            scale = scale < 0.5 ? 1 : scale;
            buttonPresetPen.setScaleX(scale);
            buttonPresetPen.setScaleY(scale);
            Log.d(TAG, "onViewCreated: Scaled to: " + scale);
            mImageButtonPenToolGroup.add(buttonPresetPen);
            buttonPresetPen.setOnClickListener(v1 -> {
                // update spinners
                spinnerPenColors.setSelection(presetPenColorIndex, true);
                spinnerPenWeight.setSelection(presetPenWeightIndex, true);

                // activate pen
                canvasView.setStrokeColor(colors[presetPenColorIndex]);
                canvasView.setStrokeWeight(Float.parseFloat(presetPenWeight));
                canvasView.getCanvasWriter().setWritemode(WriteMode.PEN);

                // show active in ui
                toggleActive(mImageButtonPenToolGroup, buttonPresetPen, false);
            });
            buttonPresetPen.setOnLongClickListener(v1 -> {
                linearLayout.removeView(buttonPresetPen);
                return true;
            });

            linearLayout.addView(buttonPresetPen, 1); // cause of the test button put 1

            Log.d(TAG, "onViewCreated: saved Pen Preset: " + (presetPenNumber.get() - 1) + penPreset.toString());
            Toast.makeText(fragmentActivity, "long press Pen to remove", Toast.LENGTH_SHORT).show();
        });

        /* insert button */
        ImageButton buttonInsert = fragmentActivity.findViewById(R.id.button_insert);
        buttonInsert.setOnClickListener(v -> {
            Log.d(TAG, "onViewCreated: Insert Pdf");
        });

        // Test
//        Button buttonTest = fragmentActivity.findViewById(R.id.button_test);
//        buttonTest.setOnClickListener(v -> Log.d(TAG, sharedPreferences.getAll().toString()));
    }

    private void setDropdownContent(int spinnerId, int spinnerContentId, onClickDropDownItem clickDropDownItem) {
        ArrayAdapter<CharSequence> dropdownColors = ArrayAdapter.createFromResource(getActivity(), spinnerContentId, R.layout.spinner_dropdown_pen_field);
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

    private boolean toggleActive(ArrayList<ImageButton> imageButtonGroup, ImageButton activeButton, boolean toogleable) {
        for (ImageButton imageButton : imageButtonGroup) {
            if (imageButton != activeButton) { // if non active button
                // deselect all else
                imageButton.setSelected(false);
                imageButton.setBackgroundColor(Color.TRANSPARENT);
                continue;
            }
            if (!toogleable) { // if not toggleable
                // select
                imageButton.setSelected(true);
                imageButton.setBackgroundColor(Color.argb(60, 255, 255, 255));
                continue;
            }
            if (imageButton.isSelected()) { // if toggleable and selected
                // deselect
                imageButton.setSelected(false);
                imageButton.setBackgroundColor(Color.TRANSPARENT);
                continue;
            }
            // if toggleable and not selected
            // select
            imageButton.setSelected(true);
            imageButton.setBackgroundColor(Color.argb(60, 255, 255, 255));
        }
        return false;
    }

    private void setActiveState(ArrayList<ImageButton> imageButtonGroup, ImageButton activeButton, boolean enabled) {
        if (enabled) { // if non active button
            for (ImageButton imageButton : imageButtonGroup) {
                if (imageButton != activeButton) { // if non active button
                    // deselect all else
                    imageButton.setSelected(false);
                    imageButton.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    // select
                    Log.d(TAG, "setActiveState: select Button");
                    imageButton.setSelected(true);
                    imageButton.setBackgroundColor(Color.argb(60, 255, 255, 255));
                }
            }
        } else {
            activeButton.setSelected(false);
            activeButton.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @FunctionalInterface
    private interface onClickDropDownItem {
        void onClick(AdapterView<?> adapterView, View view, int i, long l);
    }
}
