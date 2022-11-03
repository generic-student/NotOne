package app.notone;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    String TAG = "NotOneMainActivity";
    CanvasView canvasView;
    HashMap<String, Integer> penColors = new HashMap<String, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas);

        CanvasView canvasView = findViewById(R.id.canvasView);

        penColors.put("RED", Color.RED);
        penColors.put("GREEN", Color.GREEN);
        penColors.put("BLUE", Color.BLUE);
//        ((Button) findViewById(R.id.btn_red)).setOnClickListener(v -> {
//            canvasView.setStrokeColor(Color.RED);
//            canvasView.setStrokeWeight(10);
//        });
//
//        ((Button) findViewById(R.id.btn_green)).setOnClickListener(v -> {
//            canvasView.setStrokeColor(Color.GREEN);
//            canvasView.setStrokeWeight(100);
//        });

        Spinner dropdownPenColor = (Spinner) findViewById(R.id.spinner_pen_color);
        ArrayAdapter<CharSequence> dropdownItems = ArrayAdapter.createFromResource(
                this, R.array.pen_colors, R.layout.pen_color_spinner_dropdown_field); // load dropdown items

        dropdownItems.setDropDownViewResource(R.layout.pen_color_spinner_dropdown_items); // layout for all items
        dropdownPenColor.setAdapter(dropdownItems); // set to spinner

        class ItemSelectedListener implements AdapterView.OnItemSelectedListener {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: " + adapterView.getItemAtPosition(i));
                canvasView.setStrokeColor(penColors.get(adapterView.getItemAtPosition(i)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        }
        dropdownPenColor.setOnItemSelectedListener(new ItemSelectedListener());
    }
}