package app.notone;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CanvasView canvasView = findViewById(R.id.canvasView);
        ((Button) findViewById(R.id.btn_red)).setOnClickListener(v -> {
            canvasView.undo();
        });

        ((Button) findViewById(R.id.btn_green)).setOnClickListener(v -> {
            canvasView.redo();
        });
    }
}