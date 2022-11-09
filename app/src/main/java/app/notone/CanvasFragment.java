package app.notone;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.HashMap;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class CanvasFragment extends Fragment {
    // The onCreateView method is called when Fragment should create its View object hierarchy, via XML layout inflation.
    String TAG = "NotOneCanvasFragment";
    View mCanvasFragmentView;
    /**
     * @param inflater
     * @param parent
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        mCanvasFragmentView = inflater.inflate(R.layout.fragment_canvas, parent, false);
/* moved to onViewCreated*/

//        Spinner dropdownPenWeight = getActivity().findViewById(R.id.spinner_pen_weights);
//        ArrayAdapter<CharSequence> dropdownWeights = ArrayAdapter.createFromResource(
//                getActivity(), R.array.pen_weights, R.layout.spinner_dropdown_pen_field);
//        dropdownWeights.setDropDownViewResource(R.layout.spinner_dropdown_pen_items);
//        dropdownPenWeight.setAdapter(dropdownWeights); // set to spinner
//        dropdownPenWeight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                Log.d(TAG, "onItemSelected: " + adapterView.getItemAtPosition(i));
//                canvasView.setStrokeWeight(Float.parseFloat((String) adapterView.getItemAtPosition(i)));
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//            }
//        });


        return mCanvasFragmentView;
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @FunctionalInterface
    private interface onClickDropDownItem {
        void onClick(AdapterView adapterView, View view, int i, long l);
    }

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
        Button buttonEraser = fragmentActivity.findViewById(R.id.button_eraser);
        Button buttonUndo = fragmentActivity.findViewById(R.id.button_undo);
        Button buttonRedo = fragmentActivity.findViewById(R.id.button_redo);
        buttonEraser.setOnClickListener(v -> Log.d(TAG, "onClick: ERASE"));
        buttonUndo.setOnClickListener(v -> Log.d(TAG, "onClick: UNDO"));
        buttonRedo.setOnClickListener(v -> Log.d(TAG, "onClick: REDO"));
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
