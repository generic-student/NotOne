package app.notone.fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
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

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import app.notone.core.CanvasView;
import app.notone.R;
import app.notone.core.pens.PenType;
import app.notone.core.util.InkRecognizer;
import app.notone.io.CanvasExporter;
import app.notone.io.CanvasImporter;
import app.notone.io.PdfImporter;
import app.notone.io.PenPorter;
import app.notone.views.PresetPenButton;

import static android.content.Context.MODE_PRIVATE;

public class CanvasFragment extends Fragment {

    private static final String LOG_TAG = CanvasFragment.class.getSimpleName();
    private static final String TAG = "NotOneCanvasFragment";
    private static final String SHARED_PREFS_TAG = "NotOneSharedPrefs";
    private static final String CANVAS_STORAGE_PREF_KEY = "lastOpenedCanvasWriter";
    private static final String PEN_PRESETS_PREF_KEY = "penpresets";

    public static CanvasView mCanvasView; // TODO maybe static is bad
    private View mCanvasFragmentView;
    private ArrayList<ImageButton> mImageButtonCanvasToolGroup = new ArrayList<>(); // For showing/ toggling selected buttons

    ActivityResultLauncher<String> mGetPdfDocument = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    mCanvasView.resetViewMatrices();
                    mCanvasView.setScale(1f);
                    PdfImporter.fromUri(getContext(), uri, mCanvasView.getPdfDocument());
//                    mCanvasView.setPdfDocument(PdfImporter.fromUri(getContext(), uri, PdfImporter.FACTOR_72PPI_TO_320PPI / 2.f));
//                    mCanvasView.invalidate();
                }
            });

    /**
     * Store Data in Shared Prefs to enable persistence
     */
    @Override
    public void onStart() {
        // TODO factorise to worker thread
        super.onStart();
        Log.d(TAG, "onStart: RELOADING DATA");
        System.out.println(getResources().getDisplayMetrics());
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS_TAG, MODE_PRIVATE);
        //load the data from the sharedPrefs
        String data = sharedPreferences.getString(CANVAS_STORAGE_PREF_KEY, "");

        new CanvasImporter.InitCanvasFromJsonTask().execute(new CanvasImporter.CanvasImportData(data, mCanvasView, true));

//        /* restore canvas */
//        try {
//            CanvasImporter.initCanvasViewFromJSON(canvasdata, mCanvasView, true);
//        } catch (JSONException e) {
//            Log.e(LOG_TAG, "Could not load the last opened canvas: " + e.getMessage());
//        }

        /* restore old pens */
        ArrayList<PresetPenButton> mPresetPenButtons = new ArrayList<PresetPenButton>();
        String pendata = sharedPreferences.getString(PEN_PRESETS_PREF_KEY, "");
        try {
            mPresetPenButtons = PenPorter.presetPensFromJSON(getContext(), getActivity(),
                    pendata);
            Log.d(TAG, "onStart: got old preset pens " + mPresetPenButtons);
        } catch (JSONException e) {
            Log.e(TAG, "onStart: failed to extract Pens from json", e);
            e.printStackTrace();
        }
        // add pen to container
        LinearLayout llayoutPenContainer = getActivity().findViewById(R.id.canvas_pens_preset_container);
        if(llayoutPenContainer.getChildCount() == 0) {
            Log.d(TAG, "onViewCreated: restoring old pens " + mPresetPenButtons);
            mPresetPenButtons.forEach(presetPenButton -> {
                Log.d(TAG, "restore old pen ");
                setPresetPenButtonListeners(presetPenButton, llayoutPenContainer);
                llayoutPenContainer.addView(presetPenButton, 0);
                mImageButtonCanvasToolGroup.add((ImageButton) presetPenButton);
            });
        } else {
            Log.d(TAG, "onStart: didnt restore, Pens still there " + llayoutPenContainer.getChildCount());
        }
    }

    @Override
    public void onPause() {
        // TODO factorise to worker thread
        Log.d(TAG, "onPause: STORING DATA");
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS_TAG, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        /* export canvas */
        String jsonString = "";
        try {
            jsonString = CanvasExporter.canvasViewToJSON(mCanvasView, true).toString(1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        Log.d(TAG, "onPause: " + jsonString);
        editor.putString(CANVAS_STORAGE_PREF_KEY, jsonString);

        /* export presetpens */
        ArrayList<PresetPenButton> mPresetPenButtons = new ArrayList<PresetPenButton>();
        LinearLayout llayoutPenContainer = getActivity().findViewById(R.id.canvas_pens_preset_container);
        for(int i = llayoutPenContainer.getChildCount()-1; i >= 0; i--) {
            mPresetPenButtons.add((PresetPenButton) llayoutPenContainer.getChildAt(i));
        }
        String presetPenJson = "";
        try {
            presetPenJson = PenPorter.presetPensToJSON(mPresetPenButtons).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        editor.putString(PEN_PRESETS_PREF_KEY, presetPenJson);

        // write changes to file
        editor.apply();
        super.onPause();
        Toast.makeText(getActivity(), "exported persistence data", Toast.LENGTH_SHORT).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        mCanvasFragmentView = inflater.inflate(R.layout.fragment_canvas, parent, false);
        return mCanvasFragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        FragmentActivity fragmentActivity = getActivity();
        mCanvasView = mCanvasFragmentView.findViewById(R.id.canvasView);
//        MainActivity.mCanvasView = mCanvasView;
        /* Config Dropdowns for Pen Settings */
        int[] penColorValues = getResources().getIntArray(R.array.pen_color_values);
        setddMenuContent(R.id.ddownm_pen_colors, R.array.pen_colors, (adapterView, vw, i, l) -> mCanvasView.setStrokeColor(penColorValues[i]));
        setddMenuContent(R.id.ddownm_pen_weights, R.array.pen_weights, (adapterView, vw, i, l) -> mCanvasView.setStrokeWeight(Float.parseFloat((String) adapterView.getItemAtPosition(i))));

        /* Undo, Redo, activate Eraser Actions */
        ImageButton buttonEraser = fragmentActivity.findViewById(R.id.button_eraser);
        ImageButton buttonUndo = fragmentActivity.findViewById(R.id.button_undo);
        ImageButton buttonRedo = fragmentActivity.findViewById(R.id.button_redo);
        buttonEraser.setOnClickListener(v -> {
            if (mCanvasView.getCanvasWriter().getCurrentPenType() == PenType.ERASER) {
                setToolSelection(mImageButtonCanvasToolGroup, buttonEraser, false);
                mCanvasView.getCanvasWriter().setCurrentPenType(PenType.WRITER);
            } else {
                setToolSelection(mImageButtonCanvasToolGroup, buttonEraser, true);
                mCanvasView.getCanvasWriter().setCurrentPenType(PenType.ERASER);
            }
        });
        buttonUndo.setOnClickListener(v -> mCanvasView.undo());
        buttonRedo.setOnClickListener(v -> mCanvasView.redo());
        mImageButtonCanvasToolGroup.add(buttonEraser);

        /* create pen presets  */
        ImageButton buttonAddPresetPen = fragmentActivity.findViewById(R.id.button_add_pen);
        LinearLayout llayoutPenContainer = fragmentActivity.findViewById(R.id.canvas_pens_preset_container);
        buttonAddPresetPen.setOnClickListener(v -> {
            PresetPenButton buttonPresetPen = createPresetPenButton(fragmentActivity, llayoutPenContainer);
            mImageButtonCanvasToolGroup.add((ImageButton) buttonPresetPen);
            llayoutPenContainer.addView(buttonPresetPen, 0);
            Toast.makeText(fragmentActivity, "long press Pen to remove", Toast.LENGTH_SHORT).show();
        });

        /* insert PDF button */
        ImageButton buttonInsert = fragmentActivity.findViewById(R.id.button_insert);
        buttonInsert.setOnClickListener(v -> {
            mGetPdfDocument.launch("application/pdf");
        });

        /* Test button */
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        Button buttonTest = fragmentActivity.findViewById(R.id.button_test);
        buttonTest.setOnClickListener(v -> {
            Log.d(TAG, sharedPreferences.getAll().toString());
            sharedPreferences.edit().clear().commit();
        });
    }


    @NonNull
    private PresetPenButton createPresetPenButton(FragmentActivity fragmentActivity, LinearLayout llayoutPenContainer) {
        PresetPenButton buttonPresetPen = new PresetPenButton(
                getContext(), fragmentActivity,
                R.id.ddownm_pen_colors, R.id.ddownm_pen_weights,
                R.array.pen_color_values);
        setPresetPenButtonListeners(buttonPresetPen, llayoutPenContainer);
        Log.d(TAG, "generatePresetPenButton: generated Preset Pen Button");
        return buttonPresetPen;
    }

    private void setPresetPenButtonListeners(PresetPenButton buttonPresetPen, LinearLayout llayoutPenContainer) {
        buttonPresetPen.setOnClickListener(v1 -> {
            buttonPresetPen.ddMenuColor.setSelection(buttonPresetPen.mddMenuColorIndex, true);
            buttonPresetPen.ddMenWeight.setSelection(buttonPresetPen.mddMenuWeightIndex, true);
            mCanvasView.getCanvasWriter().setCurrentPenType(PenType.WRITER);
            setOrToggleToolSelection(buttonPresetPen, false);
        });
        buttonPresetPen.setOnLongClickListener(view1 -> {
            llayoutPenContainer.removeView(buttonPresetPen);
//            mPresetPenButtons.remove(buttonPresetPen);
            return true;
        });
    }

    private void setddMenuContent(int ddMenuId, int ddMenuContentArrayId, onClickddMenuFunction onClickddMenuFunction) {
        ArrayAdapter<CharSequence> ddMenuAAdapter = new ArrayAdapter<CharSequence>(getActivity(), R.layout.spinner_dropdown_pen_field, getResources().getStringArray(ddMenuContentArrayId)) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                if (position == 0) {
                    // Set the hint text color of the first element gray
                    textView.setTextColor(Color.GRAY);
                } else {
                    textView.setTextColor(Color.WHITE);
                }
                return view;
            }
        };


        Spinner ddMenu = getActivity().findViewById(ddMenuId);
        ddMenuAAdapter.setDropDownViewResource(R.layout.spinner_dropdown_pen_items);
        ddMenu.setAdapter(ddMenuAAdapter);
        ddMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "Dropdownmenu: itemselected: " + adapterView.getItemAtPosition(i));
                onClickddMenuFunction.onClick(adapterView, view, i, l);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        ddMenu.setSelection(1, false);
    }

    /**
     * selects a button and deselects all others
     * if toggle is true it can be turned on and of by clicking again
     *
     * @param activeButton
     * @param toggleable
     * @return state of the clicked button
     */
    private boolean setOrToggleToolSelection(ImageButton activeButton, boolean toggleable) {
        boolean result = true;
        for (ImageButton imageButton : mImageButtonCanvasToolGroup) {
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

    private void setToolSelection(ArrayList<ImageButton> imageButtonGroup, ImageButton activeButton, boolean enabled) {
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
    private interface onClickddMenuFunction {
        void onClick(AdapterView<?> adapterView, View view, int i, long l);
    }
}
