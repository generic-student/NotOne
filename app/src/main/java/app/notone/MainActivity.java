package app.notone;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    CanvasView canvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas);

        CanvasView canvasView = findViewById(R.id.canvasView);
//        ((Button) findViewById(R.id.btn_red)).setOnClickListener(v -> {
//            canvasView.setStrokeColor(Color.RED);
//            canvasView.setStrokeWeight(10);
//        });
//
//        ((Button) findViewById(R.id.btn_green)).setOnClickListener(v -> {
//            canvasView.setStrokeColor(Color.GREEN);
//            canvasView.setStrokeWeight(100);
//        });

        Spinner spinner = (Spinner) findViewById(R.id.spinner_pen_color);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.pen_colors, R.layout.pen_color_spinner_dropdown_field); // load dropdown items
        adapter.setDropDownViewResource(R.layout.pen_color_spinner_dropdown); // layout for all items
        spinner.setAdapter(adapter); // set to spinner
    }
}