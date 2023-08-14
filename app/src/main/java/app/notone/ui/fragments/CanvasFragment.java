package app.notone.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import app.notone.MainActivity;
import app.notone.core.CanvasPdfDocument;
import app.notone.core.PeriodicSaveHandler;
import app.notone.core.CanvasView;
import app.notone.R;
import app.notone.core.pens.PenType;
import app.notone.core.util.RecentCanvas;
import app.notone.core.util.SettingsHolder;
import app.notone.io.FileManager;
import app.notone.io.PenPorter;
import app.notone.ui.ActivityResultLauncherProvider;
import app.notone.ui.CanvasFragmentFlags;
import app.notone.ui.PresetPenButton;

import static android.content.Context.MODE_PRIVATE;

/**
 * The most important fragment of the app.
 * It adds the functionality to the toolbar of the canvas,
 * holds the {@link CanvasView} and enables data persistence.
 * @author Luca Hackel
 * @since 202212XX
 */

public class CanvasFragment extends Fragment {

    public static final String TAG = "NotOneCanvasFragment";
    public static final String SHARED_PREFS_TAG = "NotOneSharedPrefs";
    private static final String PEN_PRESETS_PREF_KEY = "penpresets";

    public static CanvasView sCanvasView;
    private View mCanvasFragmentView;

    public static CanvasFragmentFlags sFlags = new CanvasFragmentFlags();

    ActivityResultLauncher<String> mGetPdfDocument = ActivityResultLauncherProvider.getImportPdfActivityResultLauncher(this);

// region Android Lifecycle

    /**
     * load Data for canvas and pens from uri and Shared Prefs to enable persistence
     */
    @Override
    public void onStart() {
        super.onStart();

        //if a previous canvas was open get its uri
        if(MainActivity.sRecentCanvases.size() > 0) {
            RecentCanvas recentCanvas = MainActivity.sRecentCanvases.get(0);
            sCanvasView.setUri(recentCanvas.mUri);

            if (recentCanvas.mName == null || recentCanvas.mName.equals("")) {
                Log.e(TAG, "Cannot set the canvas title. Title is empty");
                return;
            }
            TextView tvTitle = ((TextView) getActivity().findViewById(R.id.tv_fragment_title));
            tvTitle.setText(recentCanvas.mName);
        }

        //try to load the canvas from file
        try {
            if(!sFlags.isOpenFile() && !sCanvasView.isLoaded()) {
                FileManager.load(getContext(), sCanvasView, sFlags);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Could not load previously opened canvas. Was it moved or deleted?", Toast.LENGTH_LONG).show();

            //TODO: create a new canvas in the temp folder
            //IMPORTANT
            sCanvasView.setUri(null);
        }

        ArrayList<PresetPenButton> pens = loadPresetPensFromSharedPreferences();
        addPresetPensToLayout(pens);
    }

    /**
     * Store Data of canvas and pens in Shared Prefs to enable persistence
     */
    @Override
    public void onPause() {
        //stop the periodic save handler if it is running as to not overwrite the canvasView while its empty
        if(PeriodicSaveHandler.getInstance().isRunning()) {
            PeriodicSaveHandler.getInstance().stop();
        }

        //get the shared preferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS_TAG, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //TODO: add this to the FileManager.java @author Kai
        if(!sharedPreferences.contains("firebase-userid")) {
            Log.d(TAG, "Setting userid for the FirebaseStorage.");
            editor.putString("firebase-userid", UUID.randomUUID().toString()).apply();
        }

        //save the canvas if it is not already saved
        if(!sCanvasView.isSaved()) {
            /* export canvas */
            try {
                Toast.makeText(getContext(), "Saving current canvas", Toast.LENGTH_LONG).show();
                FileManager.save(getContext(), sCanvasView);
            } catch (IOException e) {
                Log.e(TAG, "onPause: Failed to save Canvas; missing permissions likely", e);
            } catch (JSONException e) {
                Log.e(TAG, "onPause: Failed to save Canvas as the JSON is corrupted ", e);
            }
        }

        /* export presetpens */
        putPresetPensIntoSharedPreferences(getActivity(), editor);

        editor.apply();
        super.onPause();
    }

    /**
     * init autosave functionality
     * update settings
     */
    @Override
    public void onResume() {
        Log.d("PDF", "(Fragment) active: " + sCanvasView.getPdfDocument().toString());

        if(!PeriodicSaveHandler.isInitialized()) {
            PeriodicSaveHandler.init(getContext());
        }

        if(SettingsHolder.shouldAutoSaveCanvas() && !PeriodicSaveHandler.getInstance().isRunning()) {
            Toast.makeText(getContext(), "Started Periodic Saving", Toast.LENGTH_LONG).show();
            PeriodicSaveHandler.getInstance().start();
        }

        super.onResume();
    }

    /**
     * inflate and store the view statically for other methods
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        mCanvasFragmentView = inflater.inflate(R.layout.fragment_canvas, parent, false);
        return mCanvasFragmentView;
    }

    /**
     * Setup the autosave
     * Config the DDMenus for the Pen Settings
     * Setup Canvas Actions for undo, redo, pdf import and eraser
     * Setup the button to add {@link PresetPenButton}s
     *
     * @param view the view this fragment resides in
     * @param savedInstanceState data from when the instance was saved
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        FragmentActivity fragmentActivity = getActivity();
        sCanvasView = mCanvasFragmentView.findViewById(R.id.canvasView);
        if(!PeriodicSaveHandler.isInitialized()) {
            PeriodicSaveHandler.init(getContext());
        }
        PeriodicSaveHandler.getInstance().start();


        if(MainActivity.sRecentCanvases.size() > 0) {
            sCanvasView.setUri(MainActivity.sRecentCanvases.get(0).mUri);
        }


        /* Config Dropdowns for Pen Settings */
        int[] penColorValues = getResources().getIntArray(R.array.pen_color_values);
        setDDMenuContent(R.id.ddownm_pen_colors, R.array.pen_colors,
                (adapterView, vw, i, l) -> sCanvasView.setStrokeColor(penColorValues[i]));
        setDDMenuContent(R.id.ddownm_pen_weights, R.array.pen_weights,
                (adapterView, vw, i, l) -> sCanvasView.setStrokeWeight(
                        Float.parseFloat((String) adapterView.getItemAtPosition(i))));



        /* Setup Undo, Redo, activate Eraser Action Buttons */
        MaterialButton buttonEraser = fragmentActivity.findViewById(R.id.button_eraser);
        MaterialButton buttonMarker = fragmentActivity.findViewById(R.id.button_marker);
        MaterialButton buttonUndo = fragmentActivity.findViewById(R.id.button_undo);
        MaterialButton buttonRedo = fragmentActivity.findViewById(R.id.button_redo);
        buttonEraser.setOnClickListener(v -> {
            if (sCanvasView.getCanvasWriter().getCurrentPenType() == PenType.ERASER) {
                sCanvasView.getCanvasWriter().setCurrentPenType(PenType.WRITER);
            } else {
                sFlags.setMarkerEnabled(false);
                sCanvasView.getCanvasWriter().setCurrentPenType(PenType.ERASER);
            }
        });
        buttonMarker.setOnClickListener(v -> {
            if(!sFlags.isMarkerEnabled()) {
                sCanvasView.getCanvasWriter().setCurrentPenType(PenType.WRITER);
                int color = sCanvasView.getStrokeColor();
                int transparent = Color.argb(90, Color.red(color), Color.green(color), Color.blue(color));
                sCanvasView.setStrokeColor(transparent);
            } else {
                sCanvasView.getCanvasWriter().setCurrentPenType(PenType.WRITER);
                int color = sCanvasView.getStrokeColor();
                int transparent = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
                sCanvasView.setStrokeColor(transparent);
            }
            sFlags.setMarkerEnabled(!sFlags.isMarkerEnabled());
        });
        buttonUndo.setOnClickListener(v -> sCanvasView.undo());
        buttonRedo.setOnClickListener(v -> sCanvasView.redo());



        /* Setup insert PDF button */
        MaterialButton buttonInsert = fragmentActivity.findViewById(R.id.button_insert);
        buttonInsert.setOnClickListener(v -> {
            //CanvasFragment.sFlags.setLoadPdf(true);
            mGetPdfDocument.launch("application/pdf");
            Toast.makeText(fragmentActivity, "long press to remove pdf", Toast.LENGTH_SHORT).show();
        });
        buttonInsert.setOnLongClickListener(v -> {
            CanvasFragment.sCanvasView.setPdfDocument(new CanvasPdfDocument());
            CanvasFragment.sCanvasView.invalidate();
            return false;
        });



        /* Setup return to origin button */
        TextView tvTitle = ((TextView) fragmentActivity.findViewById(R.id.tv_fragment_title));
//        ImageButton buttonOrigin = fragmentActivity.findViewById(R.id.button_return_to_origin);
        tvTitle.setOnClickListener(v -> {
            sCanvasView.resetViewMatrices();
            sCanvasView.setScale(1);
            sCanvasView.invalidate();
        });



        /* Setup Shape Detection Button */
        MaterialButton buttonDetectShapes = fragmentActivity.findViewById(R.id.button_shape);
        buttonDetectShapes.setOnClickListener(v -> {
            int color = sCanvasView.getStrokeColor();
            int transparent = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
            sCanvasView.setStrokeColor(transparent);
            sFlags.setMarkerEnabled(false);

            if(sCanvasView.getCanvasWriter().getCurrentPenType() != PenType.SHAPE_DETECTOR) {
                sCanvasView.getCanvasWriter().setCurrentPenType(PenType.SHAPE_DETECTOR);
            } else {
                sCanvasView.getCanvasWriter().setCurrentPenType(PenType.WRITER);
            }
        });



        /* Setup Add Preset Pen Button */
        MaterialButton buttonAddPresetPen = fragmentActivity.findViewById(R.id.button_add_pen);
        MaterialButtonToggleGroup mbPresetPenStateToggleGroup = fragmentActivity.findViewById(R.id.preset_pen_state_toogle_group);
        buttonAddPresetPen.setOnClickListener(v -> {
            PresetPenButton buttonPresetPen = createPresetPenButtonFromUI(getContext(), fragmentActivity, mbPresetPenStateToggleGroup);
            mbPresetPenStateToggleGroup.addView(buttonPresetPen, 0);
            Toast.makeText(fragmentActivity, "long press to remove pen", Toast.LENGTH_SHORT).show();
        });
    }

// endregion

// region Preset Pens

    /**
     * loads the preset pens from a json string saved in the shared preferences
     * @return
     */
    @NonNull
    private ArrayList<PresetPenButton> loadPresetPensFromSharedPreferences() {
        Log.d(TAG, "onStart: reloading canvas data saved in shared prefs");
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS_TAG, MODE_PRIVATE);
        /* restore old pens */
        ArrayList<PresetPenButton> presetPenButtons = new ArrayList<>();
        String pendata = sharedPreferences.getString(PEN_PRESETS_PREF_KEY, "");
        try {
            presetPenButtons = PenPorter.presetPensFromJSON(getContext(), getActivity(),
                    pendata);
        } catch (JSONException e) {
            Log.e(TAG, "onStart: failed to extract Pens from json", e);
            e.printStackTrace();
        }
        return presetPenButtons;
    }

    /**
     * puts the preset pens as a json string in the shared preferences
     * afterwards editor.apply() has to be called
     *
     * @param editor the shared preferences to put the pens into
     */
    private static void putPresetPensIntoSharedPreferences(Activity activity, SharedPreferences.Editor editor) {
        ArrayList<PresetPenButton> mPresetPenButtons = new ArrayList<PresetPenButton>();
        LinearLayout llayoutPenContainer = activity.findViewById(R.id.preset_pen_state_toogle_group);
        if(llayoutPenContainer == null)
            return;
        for(int i = llayoutPenContainer.getChildCount()-1; i >= 0; i--) {
            try {
                mPresetPenButtons.add((PresetPenButton) llayoutPenContainer.getChildAt(i));
            }
            catch(ClassCastException e){
                Log.e(TAG, "putPresetPensIntoSharedPreferences: cant be cast");
            }
        }
        String presetPenJson = "";
        try {
            presetPenJson = PenPorter.presetPensToJSON(mPresetPenButtons).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        editor.putString(PEN_PRESETS_PREF_KEY, presetPenJson);
    }

    /**
     * add a list of preset pens to the layout
     * @param pens list of the preset pens
     */
    private void addPresetPensToLayout(@NonNull ArrayList<PresetPenButton> pens) {
        // add pen to container
        LinearLayout llayoutPenContainer = getActivity().findViewById(R.id.preset_pen_state_toogle_group);
        if(llayoutPenContainer == null)
            return;
        if(llayoutPenContainer.getChildCount() == 0) {
            Log.d(TAG, "onStart: restoring old pens " + pens);
            pens.forEach(presetPenButton -> {
                setPresetPenButtonListeners(presetPenButton, llayoutPenContainer);
                llayoutPenContainer.addView(presetPenButton, 0);
            });
        } else {
            Log.d(TAG, "onStart: could not restore all pens. Pens available: " + llayoutPenContainer.getChildCount());
        }
    }

    /**
     * Create a preset pen from the current canvas settings (color and weight)
     * @param fragmentActivity the activity to add the pen into
     * @param llayoutPenContainer the layout to add the pen into
     * @return a PresetPen
     */
    @NonNull
    private static PresetPenButton createPresetPenButtonFromUI(Context context, FragmentActivity fragmentActivity, LinearLayout llayoutPenContainer) {
        PresetPenButton buttonPresetPen = new PresetPenButton(
                context, fragmentActivity,
                R.id.ddownm_pen_colors, R.id.ddownm_pen_weights,
                R.array.pen_color_values);
        setPresetPenButtonListeners(buttonPresetPen, llayoutPenContainer);
        MaterialButtonToggleGroup toggleGroup = fragmentActivity.findViewById(R.id.preset_pen_state_toogle_group);
        toggleGroup.check(buttonPresetPen.getId());
        Log.d(TAG, "generatePresetPenButton: generated Preset Pen Button");
        return buttonPresetPen;
    }

    /**
     * Sets the onclick methods for the preset pens
     * @param buttonPresetPen the individual preset pen
     * @param llayoutPenContainer the container they are in
     */
    private static void setPresetPenButtonListeners(PresetPenButton buttonPresetPen, LinearLayout llayoutPenContainer) {
        buttonPresetPen.setOnClickListener(v1 -> {
            buttonPresetPen.mDDMenuColor.setSelection(buttonPresetPen.mDDMenuColorIndex, true);
            buttonPresetPen.mDDMenWeight.setSelection(buttonPresetPen.mDDMenuWeightIndex, true);
            sCanvasView.getCanvasWriter().setCurrentPenType(PenType.WRITER);
            sFlags.setMarkerEnabled(false);
            int color = sCanvasView.getStrokeColor();
            int transparent = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
            sCanvasView.setStrokeColor(transparent);
        });
        buttonPresetPen.setOnLongClickListener(view1 -> {
            llayoutPenContainer.removeView(buttonPresetPen);
//            mPresetPenButtons.remove(buttonPresetPen);
            return true;
        });
    }

// endregion

// region Tool Selection



    /**
     * set up the drop down menu with the data
     * @param ddMenuId id of the dd menu
     * @param ddMenuContentArrayId id of its contents
     * @param onClickddMenuFunction callback function
     */
    private void setDDMenuContent(int ddMenuId, int ddMenuContentArrayId, onClickDDMenuFunction onClickddMenuFunction) {
        ArrayAdapter<CharSequence> ddMenuAAdapter = new ArrayAdapter<CharSequence>(getActivity(), R.layout.spinner_dropdown_pen_field, getResources().getStringArray(ddMenuContentArrayId)) {
            @Override
            public boolean isEnabled(int position) {
                // Disable the first item from Spinner
                return position != 0;
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
//                Log.d(TAG, "Dropdownmenu: itemselected: " + adapterView.getItemAtPosition(i));
                onClickddMenuFunction.onClick(adapterView, view, i, l);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        ddMenu.setSelection(1, false);
    }

    /**
     * necessary to pass a onclick method to the DropDownMethod setOnItemSelectedListener
     */
    @FunctionalInterface
    private interface onClickDDMenuFunction {
        void onClick(AdapterView<?> adapterView, View view, int i, long l);
    }
}

//endregion