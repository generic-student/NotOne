package app.notone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;

import org.json.JSONException;

import app.notone.io.CanvasExporter;
import app.notone.io.CanvasImporter;
import app.notone.io.ObjectSerializer;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName() + "_DEBUG";

    CanvasView canvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        canvasView = findViewById(R.id.canvasView);
        ((Button) findViewById(R.id.btn_red)).setOnClickListener(v -> {
            canvasView.undo();
        });

        ((Button) findViewById(R.id.btn_green)).setOnClickListener(v -> {
            canvasView.redo();
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPreferences = getSharedPreferences("NotOneSharedPrefs", MODE_PRIVATE);

        //load the data from the sharedPrefs
        String data = sharedPreferences.getString("lastOpenedCanvasWriter", "");

        try {
            CanvasImporter.initCanvasViewFromJSON(data, canvasView, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {

        String jsonString = "";
        try {
            jsonString = CanvasExporter.canvasViewToJSON(canvasView, true).toString(1);
            Log.d(LOG_TAG, jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Storing data into SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("NotOneSharedPrefs", MODE_PRIVATE);

        // Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Write the byte stream to the preferences
        editor.putString("lastOpenedCanvasWriter", jsonString);

        // write changes to file
        editor.commit();

        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putSerializable("CanvasWriter", canvasView.getCanvasWriter());

        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        CanvasWriter pen = (CanvasWriter) savedInstanceState.getSerializable("CanvasWriter");
        canvasView.setCanvasWriter(pen);


    }



}