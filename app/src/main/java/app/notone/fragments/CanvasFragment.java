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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import app.notone.MainActivity;
import app.notone.core.PeriodicSaveHandler;
import app.notone.core.CanvasView;
import app.notone.R;
import app.notone.core.pens.PenType;
import app.notone.core.util.RecentCanvas;
import app.notone.core.util.RecentCanvases;
import app.notone.core.util.SettingsHolder;
import app.notone.io.FileManager;
import app.notone.io.PdfImporter;
import app.notone.io.PenPorter;
import app.notone.views.PresetPenButton;

import static android.content.Context.MODE_PRIVATE;

public class CanvasFragment extends Fragment {

    private static final String LOG_TAG = CanvasFragment.class.getSimpleName();
    private static final String TAG = "NotOneCanvasFragment";
    public static final String SHARED_PREFS_TAG = "NotOneSharedPrefs";
    private static final String PEN_PRESETS_PREF_KEY = "penpresets";

    public static CanvasView mCanvasView; // TODO maybe static is bad
    private View mCanvasFragmentView;
    private ArrayList<ImageButton> mImageButtonCanvasToolGroup = new ArrayList<>(); // For showing/ toggling selected buttons
    private boolean markerEnabled = false;
    public static boolean isLoadingPdfPages = false;

    ActivityResultLauncher<String> mGetPdfDocument = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if(uri == null){
                        return;
                    }
                    mCanvasView.resetViewMatrices();
                    mCanvasView.setScale(1f);
                    isLoadingPdfPages = true;
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
        super.onStart();

        //if a previous canvas was open get its uri
        if(MainActivity.sRecentCanvases.size() > 0) {
            RecentCanvas recentCanvas = MainActivity.sRecentCanvases.get(0);
            mCanvasView.setUri(recentCanvas.mUri);

            if (recentCanvas.mName == null || recentCanvas.mName.equals("")) {
                Log.e(TAG, "Cannot set the canvas title. Title is empty");
                return;
            }
            TextView tvTitle = ((TextView) getActivity().findViewById(R.id.tv_fragment_title));
            tvTitle.setText(recentCanvas.mName);
        }

        //try to load the canvas from file
        try {
            if(!isLoadingPdfPages) {
                FileManager.load(getContext(), mCanvasView);
            }
            else {
                Log.d(LOG_TAG, "Not loading canvas because pdf is still loading");
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Could not load previously opened canvas. Was it moved or deleted?", Toast.LENGTH_LONG).show();

            //TODO: create a new canvas in the temp folder
            //IMPORTANT
            mCanvasView.setUri(null);
        }

        ArrayList<PresetPenButton> pens = loadPresetPensFromSharedPreferences();
        addPresetPensToLayout(pens);
    }

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

    private void addPresetPensToLayout(@NonNull ArrayList<PresetPenButton> pens) {
        // add pen to container
        LinearLayout llayoutPenContainer = getActivity().findViewById(R.id.canvas_pens_preset_container);
        if(llayoutPenContainer.getChildCount() == 0) {
            Log.d(TAG, "onStart: restoring old pens " + pens);
            pens.forEach(presetPenButton -> {
                setPresetPenButtonListeners(presetPenButton, llayoutPenContainer);
                llayoutPenContainer.addView(presetPenButton, 0);
                mImageButtonCanvasToolGroup.add((ImageButton) presetPenButton);
            });
        } else {
            Log.d(TAG, "onStart: could not restore all pens. Pens available: " + llayoutPenContainer.getChildCount());
        }
    }

    @Override
    public void onPause() {
        //stop the periodic save handler if it is running as to not overwrite the canvasView while its empty
        if(PeriodicSaveHandler.getInstance().isRunning()) {
            PeriodicSaveHandler.getInstance().stop();
        }

        //get the shared preferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS_TAG, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //TODO: add this to the FileManager.java
        if(!sharedPreferences.contains("firebase-userid")) {
            Log.d(TAG, "Setting userid for the FirebaseStorage.");
            editor.putString("firebase-userid", UUID.randomUUID().toString()).apply();
        }


        //save the canvas if it is not already saved
        if(!mCanvasView.isSaved()) {
            /* export canvas */
            try {
                Toast.makeText(getContext(), "Saving current canvas", Toast.LENGTH_LONG).show();
                FileManager.save(getContext(), mCanvasView);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        /* export presetpens */
        savePresetPensToSharedPreferences(editor);

        editor.apply();
        super.onPause();
    }

    @Override
    public void onResume() {
        if(!PeriodicSaveHandler.isInitialized()) {
            PeriodicSaveHandler.init(getContext());
        }

        if(SettingsHolder.isAutoSaveCanvas() && !PeriodicSaveHandler.getInstance().isRunning()) {
            Toast.makeText(getContext(), "Started Periodic Saving", Toast.LENGTH_LONG).show();
            PeriodicSaveHandler.getInstance().start();
        }

        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        mCanvasFragmentView = inflater.inflate(R.layout.fragment_canvas, parent, false);
        return mCanvasFragmentView;
    }

    /**
     * Determines what happens when the view is fully created
     * @param view the view this fragment resides in
     * @param savedInstanceState data from when the instance was saved
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");

        FragmentActivity fragmentActivity = getActivity();
        mCanvasView = mCanvasFragmentView.findViewById(R.id.canvasView);

        if(MainActivity.sRecentCanvases.size() > 0) {
            mCanvasView.setUri(MainActivity.sRecentCanvases.get(0).mUri);
        }

        if(!PeriodicSaveHandler.isInitialized()) {
            PeriodicSaveHandler.init(getContext());
        }
        PeriodicSaveHandler.getInstance().start();


//        MainActivity.mCanvasView = mCanvasView;

        /* Config Dropdowns for Pen Settings */
        int[] penColorValues = getResources().getIntArray(R.array.pen_color_values);
        setddMenuContent(R.id.ddownm_pen_colors, R.array.pen_colors, (adapterView, vw, i, l) -> mCanvasView.setStrokeColor(penColorValues[i]));
        setddMenuContent(R.id.ddownm_pen_weights, R.array.pen_weights, (adapterView, vw, i, l) -> mCanvasView.setStrokeWeight(Float.parseFloat((String) adapterView.getItemAtPosition(i))));

        /* Undo, Redo, activate Eraser Actions */
        ImageButton buttonEraser = fragmentActivity.findViewById(R.id.button_eraser);
        ImageButton buttonMarker = fragmentActivity.findViewById(R.id.button_marker);
        ImageButton buttonUndo = fragmentActivity.findViewById(R.id.button_undo);
        ImageButton buttonRedo = fragmentActivity.findViewById(R.id.button_redo);
        buttonEraser.setOnClickListener(v -> {
            if (mCanvasView.getCanvasWriter().getCurrentPenType() == PenType.ERASER) {
                setToolSelection(mImageButtonCanvasToolGroup, buttonEraser, false);
                mCanvasView.getCanvasWriter().setCurrentPenType(PenType.WRITER);
            } else {
                markerEnabled = false;
                setToolSelection(mImageButtonCanvasToolGroup, buttonEraser, true);
                mCanvasView.getCanvasWriter().setCurrentPenType(PenType.ERASER);
            }
        });
        buttonMarker.setOnClickListener(v -> {
            if(!markerEnabled) {
                setToolSelection(mImageButtonCanvasToolGroup, buttonMarker, true);
                mCanvasView.getCanvasWriter().setCurrentPenType(PenType.WRITER);
                int color = mCanvasView.getStrokeColor();
                int transparent = Color.argb(90, Color.red(color), Color.green(color), Color.blue(color));
                mCanvasView.setStrokeColor(transparent);
            } else {
                setToolSelection(mImageButtonCanvasToolGroup, buttonMarker, false);
                mCanvasView.getCanvasWriter().setCurrentPenType(PenType.WRITER);
                int color = mCanvasView.getStrokeColor();
                int transparent = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
                mCanvasView.setStrokeColor(transparent);
            }
            markerEnabled = !markerEnabled;
        });
        buttonUndo.setOnClickListener(v -> mCanvasView.undo());
        buttonRedo.setOnClickListener(v -> mCanvasView.redo());
        mImageButtonCanvasToolGroup.add(buttonEraser);
        mImageButtonCanvasToolGroup.add(buttonMarker);

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

        /* return to origin button */
        TextView tvTitle = ((TextView) fragmentActivity.findViewById(R.id.tv_fragment_title));
//        ImageButton buttonOrigin = fragmentActivity.findViewById(R.id.button_return_to_origin);
        tvTitle.setOnClickListener(v -> {
            mCanvasView.resetViewMatrices();
            mCanvasView.setScale(1);
            mCanvasView.invalidate();
        });

        ImageButton buttonDetectShapes = fragmentActivity.findViewById(R.id.button_shape);
        buttonDetectShapes.setOnClickListener(v -> {
            int color = mCanvasView.getStrokeColor();
            int transparent = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
            mCanvasView.setStrokeColor(transparent);
            markerEnabled = false;

            if(mCanvasView.getCanvasWriter().getCurrentPenType() != PenType.SHAPE_DETECTOR) {
                setToolSelection(mImageButtonCanvasToolGroup, buttonDetectShapes, true);
                mCanvasView.getCanvasWriter().setCurrentPenType(PenType.SHAPE_DETECTOR);
            } else {
                setToolSelection(mImageButtonCanvasToolGroup, buttonDetectShapes, false);
                mCanvasView.getCanvasWriter().setCurrentPenType(PenType.WRITER);
            }
        });
        mImageButtonCanvasToolGroup.add(buttonDetectShapes);
    }


    /**
     * Create a preset pen from the current canvas settings (color and width)
     * @param fragmentActivity the activity to add the pen into
     * @param llayoutPenContainer the layout to add the pen into
     * @return a PresetPen
     */
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

    /**
     * Sets the onclick methods for the preset pens
     * @param buttonPresetPen the individual preset pen
     * @param llayoutPenContainer the container they are in
     */
    private void setPresetPenButtonListeners(PresetPenButton buttonPresetPen, LinearLayout llayoutPenContainer) {
        buttonPresetPen.setOnClickListener(v1 -> {
            buttonPresetPen.ddMenuColor.setSelection(buttonPresetPen.mddMenuColorIndex, true);
            buttonPresetPen.ddMenWeight.setSelection(buttonPresetPen.mddMenuWeightIndex, true);
            mCanvasView.getCanvasWriter().setCurrentPenType(PenType.WRITER);
            markerEnabled = false;
            int color = mCanvasView.getStrokeColor();
            int transparent = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
            mCanvasView.setStrokeColor(transparent);
            setOrToggleToolSelection(buttonPresetPen, false);
        });
        buttonPresetPen.setOnLongClickListener(view1 -> {
            llayoutPenContainer.removeView(buttonPresetPen);
//            mPresetPenButtons.remove(buttonPresetPen);
            return true;
        });
    }

    /**
     * Saves the preset pens as a json string in the shared preferences
     * @param editor the shared preferences to put the pens into
     */
    private void savePresetPensToSharedPreferences(SharedPreferences.Editor editor) {
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
