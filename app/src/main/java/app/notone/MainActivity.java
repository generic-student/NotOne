package app.notone;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    String TAG = "NotOneMainActivity";
    HashMap<String, Integer> penColors = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas);

        getSupportActionBar().hide();
        penColors.put("RED", Color.RED);
        penColors.put("GREEN", Color.GREEN);
        penColors.put("BLUE", Color.BLUE);

        CanvasView canvasView = findViewById(R.id.canvasView);
        Spinner dropdownPenColor = findViewById(R.id.spinner_pen);
        ArrayAdapter<CharSequence> dropdownItems = ArrayAdapter.createFromResource(
                this, R.array.pen_colors, R.layout.pen_color_spinner_dropdown_field);

        dropdownItems.setDropDownViewResource(R.layout.pen_color_spinner_dropdown_items);
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