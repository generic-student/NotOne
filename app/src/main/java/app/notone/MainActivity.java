package app.notone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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

        //convert the data to a byte array
        byte[] byte_data = Base64.decode(data, Base64.DEFAULT);

        //deserialize the data
        CanvasWriter writer = ObjectSerializer.deserialize(byte_data);
        if(writer == null) {
            return;
        }
        //init the writer
        writer.initDefaultPaint();
        for(Stroke stroke : writer.getStrokes()) {
            stroke.initPathFromPathPoints();
        }
        canvasView.setCanvasWriter(writer);
    }

    @Override
    protected void onPause() {
        //serialize the writer
        byte[] byte_data = ObjectSerializer.serialize(canvasView.getCanvasWriter());
        if(byte_data == null) {
            super.onPause();
            return;
        }

        // Storing data into SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("NotOneSharedPrefs", MODE_PRIVATE);

        // Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //Convert the stream to a string
        String data = Base64.encodeToString(byte_data, Base64.DEFAULT);

        // Write the byte stream to the preferences
        editor.putString("lastOpenedCanvasWriter", data);

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