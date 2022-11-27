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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import app.notone.CanvasView;
import app.notone.R;
import app.notone.WriteMode;
import app.notone.io.CanvasExporter;
import app.notone.io.CanvasImporter;
import app.notone.io.PenPorter;
import app.notone.views.PresetPenButton;

import static android.content.Context.MODE_PRIVATE;

public class CanvasFragment extends Fragment {

    private static final String LOG_TAG = CanvasFragment.class.getSimpleName();
    private final String TAG = "NotOneCanvasFragment";
    private static final String SHARED_PREFS_TAG = "NotOneSharedPrefs";
    private final String PREF_KEY_CANVAS_STORAGE = "lastOpenedCanvasWriter";
    private final String PREF_KEY_PEN_PRESETS = "penpresets";

    private ArrayList<ImageButton> mImageButtonPenToolGroup = new ArrayList<>(); // For showing/ toggling selected buttons
    private ArrayList<PresetPenButton> mPresetPenButtons = new ArrayList<PresetPenButton>();
    private View mCanvasFragmentView;
    private CanvasView mCanvasView;

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: RELOADING DATA");
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS_TAG, MODE_PRIVATE);
        //load the data from the sharedPrefs
        String data = sharedPreferences.getString(PREF_KEY_CANVAS_STORAGE, "");

        try {
            CanvasImporter.initCanvasViewFromJSON(data, mCanvasView, true);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Could not load the last opened canvas: " + e.getMessage());
        }

        String pendata = sharedPreferences.getString(PREF_KEY_PEN_PRESETS, "");
//        R.id.ddownm_pen_colors, R.id.ddownm_pen_weights,
        getActivity().findViewById(R.id.ddownm_pen_colors);
        getActivity().findViewById(R.id.ddownm_pen_weights);
        try {
            mPresetPenButtons = PenPorter.penPresetsFromJSON(getContext(), getActivity(),
                    R.id.ddownm_pen_colors,
                    R.id.ddownm_pen_weights,
                    pendata);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: STORING DATA");
        String jsonString = "";
        try {
            jsonString = CanvasExporter.canvasViewToJSON(mCanvasView, true).toString(1);
//            Log.d(LOG_TAG, jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String presetPenJson = "";
        try {
            presetPenJson = PenPorter.penPresetsToJSON(mPresetPenButtons).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Storing data into SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS_TAG, MODE_PRIVATE);
        // Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Write the byte stream to the preferences
        editor.putString(PREF_KEY_CANVAS_STORAGE, jsonString);
        editor.putString(PREF_KEY_PEN_PRESETS, presetPenJson);
        // write changes to file
        editor.commit();
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mCanvasFragmentView = inflater.inflate(R.layout.fragment_canvas, parent, false);
        return mCanvasFragmentView;
    }

    // This event is triggered soon after onCreateView()
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        FragmentActivity fragmentActivity = getActivity();

        /* Config Dropdowns for Pen Settings */
        mCanvasView = mCanvasFragmentView.findViewById(R.id.canvasView);
        int[] penColorValues = getResources().getIntArray(R.array.pen_color_values);
        setddownContent(R.id.ddownm_pen_colors, R.array.pen_colors, (adapterView, vw, i, l) -> mCanvasView.setStrokeColor(penColorValues[i]));
        setddownContent(R.id.ddownm_pen_weights, R.array.pen_weights, (adapterView, vw, i, l) -> mCanvasView.setStrokeWeight(Float.parseFloat((String) adapterView.getItemAtPosition(i))));

        /* Undo Redo activate Eraser Actions */
        ImageButton buttonEraser = fragmentActivity.findViewById(R.id.button_eraser);
        ImageButton buttonUndo = fragmentActivity.findViewById(R.id.button_undo);
        ImageButton buttonRedo = fragmentActivity.findViewById(R.id.button_redo);
        buttonEraser.setOnClickListener(v -> {
            if (mCanvasView.getCanvasWriter().getWritemode() == WriteMode.ERASER) {
                setSelection(mImageButtonPenToolGroup, buttonEraser, false);
                mCanvasView.getCanvasWriter().setWritemode(WriteMode.PEN);
            } else {
                setSelection(mImageButtonPenToolGroup, buttonEraser, true);
                mCanvasView.getCanvasWriter().setWritemode(WriteMode.ERASER);
            }
        });
        buttonUndo.setOnClickListener(v -> mCanvasView.undo());
        buttonRedo.setOnClickListener(v -> mCanvasView.redo());

        mImageButtonPenToolGroup.add(buttonEraser);

        /* create pen presets */
        ImageButton buttonAddPresetPen = fragmentActivity.findViewById(R.id.button_add_pen);
        LinearLayout llayoutPenContainer = fragmentActivity.findViewById(R.id.canvas_pens_container);
        mPresetPenButtons.forEach(presetPenButton -> { // readd old ones
            presetPenButton.setOnClickListener(v1 -> {
                presetPenButton.mDdownmColor.setSelection(presetPenButton.mddownColorIndex, true);
                presetPenButton.mDdownmWeight.setSelection(presetPenButton.mddownWeightIndex, true);
                mCanvasView.getCanvasWriter().setWritemode(WriteMode.PEN);
                setOrToggleSelection(presetPenButton, false);
            });
            presetPenButton.setOnLongClickListener(view1 -> {
                llayoutPenContainer.removeView(presetPenButton);
                mPresetPenButtons.remove(presetPenButton);
                return true;
            });
            llayoutPenContainer.removeView(presetPenButton);
            llayoutPenContainer.addView(presetPenButton, 0); // cause of the test button put 1
            mImageButtonPenToolGroup.add((ImageButton) presetPenButton);
        });

        buttonAddPresetPen.setOnClickListener(v -> {
            PresetPenButton buttonPresetPen = generatePresetPenButton(fragmentActivity, penColorValues, llayoutPenContainer);

            mPresetPenButtons.add(buttonPresetPen);
            mImageButtonPenToolGroup.add((ImageButton) buttonPresetPen);
            llayoutPenContainer.addView(buttonPresetPen, 0); // cause of the test button put 1
            Toast.makeText(fragmentActivity, "long press Pen to remove", Toast.LENGTH_SHORT).show();
        });


        /* insert PDF button */
        ImageButton buttonInsert = fragmentActivity.findViewById(R.id.button_insert);
        buttonInsert.setOnClickListener(v -> {
            Log.d(TAG, "onViewCreated: Insert Pdf");
        });


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
//         Test
        Button buttonTest = fragmentActivity.findViewById(R.id.button_test);
        buttonTest.setOnClickListener(v -> {
            Log.d(TAG, sharedPreferences.getAll().toString());
            sharedPreferences.edit().clear().commit();
        });
    }

    @NonNull
    private PresetPenButton generatePresetPenButton(FragmentActivity fragmentActivity, int[] penColorValues, LinearLayout llayoutPenContainer) {
        PresetPenButton buttonPresetPen = new PresetPenButton(
                getContext(), fragmentActivity,
                R.id.ddownm_pen_colors, R.id.ddownm_pen_weights,
                penColorValues);
        buttonPresetPen.setOnClickListener(v1 -> {
            buttonPresetPen.mDdownmColor.setSelection(buttonPresetPen.mddownColorIndex, true);
            buttonPresetPen.mDdownmWeight.setSelection(buttonPresetPen.mddownWeightIndex, true);
            mCanvasView.getCanvasWriter().setWritemode(WriteMode.PEN);
            setOrToggleSelection(buttonPresetPen, false);
        });
        buttonPresetPen.setOnLongClickListener(view1 -> {
            llayoutPenContainer.removeView(buttonPresetPen);
            mPresetPenButtons.remove(buttonPresetPen);
            return true;
        });
        Log.d(TAG, "generatePresetPenButton: generated Preset Pen Button");
        return buttonPresetPen;
    }

    private void setddownContent(int spinnerId, int spinnerContentArrayId, onClickDropDownItem onClickDropDownItem) {
        // ArrayAdapter<CharSequence> dropdownColors = ArrayAdapter.createFromResource(getActivity(), spinnerContentArrayId, R.layout.spinner_dropdown_pen_field);
        ArrayAdapter<CharSequence> ddownmContentAA = new ArrayAdapter<CharSequence>(getActivity(), R.layout.spinner_dropdown_pen_field, getResources().getStringArray(spinnerContentArrayId)) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color of the first element gray
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.WHITE);
                }
                return view;
            }
        };


        Spinner ddownm = getActivity().findViewById(spinnerId);
        ddownmContentAA.setDropDownViewResource(R.layout.spinner_dropdown_pen_items);
        ddownm.setAdapter(ddownmContentAA);
        ddownm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "Dropdownmenu: " + adapterView.getItemAtPosition(i));
                onClickDropDownItem.onClick(adapterView, view, i, l);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        ddownm.setSelection(1, false);
    }

    /**
     * selects a button and deselects all others
     * if toggle is true it can be turned on and of by clicking again
     *
     * @param activeButton
     * @param toggleable
     * @return state of the clicked button
     */
    private boolean setOrToggleSelection(ImageButton activeButton, boolean toggleable) {
        boolean result = true;
        for (ImageButton imageButton : mImageButtonPenToolGroup) {
            if (imageButton != activeButton) { // if non active button
                // deselect all else
                imageButton.setSelected(false);
                imageButton.setBackgroundColor(Color.TRANSPARENT);
                continue;
            }
            if (!toggleable) { // if not toggleable
                // select
                imageButton.setSelected(true);
                imageButton.setBackgroundColor(Color.argb(60, 255, 255, 255));
                continue;
            }
            if (imageButton.isSelected()) { // if toggleable and selected
                // deselect
                result = false;
                imageButton.setSelected(false);
                imageButton.setBackgroundColor(Color.TRANSPARENT);
                continue;
            }
            // if toggleable and not selected
            // select
            imageButton.setSelected(true);
            imageButton.setBackgroundColor(Color.argb(60, 255, 255, 255));
        }
        return result;
    }

    private void setSelection(ArrayList<ImageButton> imageButtonGroup, ImageButton activeButton, boolean enabled) {
        if (enabled) { // if selected
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
