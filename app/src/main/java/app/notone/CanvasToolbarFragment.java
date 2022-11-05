package app.notone;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.HashMap;

import androidx.fragment.app.Fragment;

class CanvasToolbarFragment extends Fragment {
    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    String TAG = "NotOneCanvasFragment";
    HashMap<String, Integer> penColors = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        View view = inflater.inflate(R.layout.toolbar_canvas, parent, false);
        Log.d(TAG, "onCreateView: BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
        penColors.put("RED", Color.RED);
        penColors.put("GREEN", Color.GREEN);
        penColors.put("BLUE", Color.BLUE);

//        CanvasView canvasView = getActivity().findViewById(R.id.canvasView);
        Spinner dropdownPenColor = view.findViewById(R.id.spinner_pen);
        ArrayAdapter<CharSequence> dropdownItems = ArrayAdapter.createFromResource(
                getActivity(), R.array.pen_colors, R.layout.pen_color_spinner_dropdown_field);

        dropdownItems.setDropDownViewResource(R.layout.pen_color_spinner_dropdown_items);
        dropdownPenColor.setAdapter(dropdownItems); // set to spinner

//        class ItemSelectedListener implements
        dropdownPenColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: " + adapterView.getItemAtPosition(i));
//                canvasView.setStrokeColor(penColors.get(adapterView.getItemAtPosition(i)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return view;
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        // EditText etFoo = (EditText) view.findViewById(R.id.etFoo);

    }
}
