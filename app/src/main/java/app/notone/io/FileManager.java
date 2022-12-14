package app.notone.io;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.MissingResourceException;
import java.util.UUID;

import app.notone.core.CanvasView;
import kotlin.NotImplementedError;

public class FileManager {
    private static final String SHARED_PREFS_TAG = "NotOneSharedPrefs";
    private static final String TAG = FileManager.class.getSimpleName();

    //saving

    public static void save(Context context, CanvasView view) throws IOException, JSONException {
        String jsonString = CanvasExporter.canvasViewToJSON(view, true).toString();

        //differentiate between (Filesystem, Firebase, Cache)
        if(view.getCurrentURI() == null || view.getCurrentURI().toString().isEmpty()) {
            saveToCache(context, jsonString);
            return;
        }

        if(view.getCurrentURI().toString().equals("firebase")) {
            saveToFirebase(context, jsonString);
            return;
        }

        saveToFilesystem(context, jsonString, view);
    }

    public static void saveToCache(Context context, String data) throws IOException {
        Log.d(TAG, "Saving file to cache " + context.getFilesDir());

        File file = new File(context.getFilesDir(), "canvas.json");
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(data);
        bw.close();
    }

    public static void saveToFirebase(Context context, String data) {
        Log.d(TAG, "Saving file to firebase");

        String userId = context.getSharedPreferences(SHARED_PREFS_TAG, Context.MODE_PRIVATE).getString("firebase-userid", "");
        if(userId.isEmpty()) {
            throw new MissingResourceException("Firebase userId could not be found", FileManager.class.getSimpleName(), "userId");
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference userStorageReference = storageReference.child(String.format("%s/canvas.json", userId));

        UploadTask uploadTask  = userStorageReference.putBytes(data.getBytes(StandardCharsets.UTF_8));
        uploadTask.addOnFailureListener(exception ->
            Log.d(TAG, "Failed to upload file.")
        ).addOnSuccessListener(
            taskSnapshot -> Log.d(TAG, "Successfully uploaded file.")
        );
    }

    public static void saveToFilesystem(Context context, String data, CanvasView view) {
        Log.d(TAG, "Saving file to filesystem " + view.getCurrentURI());

        Uri uri = view.getCurrentURI();
        try {
            ParcelFileDescriptor pfd = context.getContentResolver().
                    openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write((data).getBytes());
            // Let the document provider know you're done by closing the stream.
            fileOutputStream.close();
            pfd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //loading

    public static void load(Context context, CanvasView view) throws IOException {
        //differentiate between (Filesystem, Firebase, Cache)
        if(view.getCurrentURI() == null || view.getCurrentURI().toString().isEmpty()) {
            loadFromCache(context, view);
            return;
        }

        if(view.getCurrentURI().toString().equals("firebase")) {
            loadFromFirebase(context, view);
            return;
        }

        loadFromFilesystem(context, view);
    }

    public static void loadFromCache(Context context, CanvasView view) throws IOException {
        Log.d(TAG, "Loading file from cache " + context.getFilesDir());

        File file = new File(context.getFilesDir(), "canvas.json");
        if (!file.exists()) {
            throw new FileNotFoundException("Could not find the active canvas. Has the cache been cleared?");
        }

        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);

        StringBuilder content = new StringBuilder();
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            content.append(line);
        }

        new CanvasImporter.InitCanvasFromJsonTask().execute(new CanvasImporter.CanvasImportData(content.toString(), view, true));
    }

    public static void loadFromFirebase(Context context, CanvasView view) {
        Log.d(TAG, "Loading file from firebase.");

        throw new NotImplementedError("Not implemented");
    }

    public static void loadFromFilesystem(Context context, CanvasView view) throws IOException {
        Log.d(TAG, "Loading file from filesystem " + view.getCurrentURI());

        String content = "";
        InputStream in = context.getContentResolver().openInputStream(view.getCurrentURI());

        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        StringBuilder total = new StringBuilder();
        for (String line; (line = r.readLine()) != null; ) {
            total.append(line).append('\n');
        }

        content = total.toString();

        // Let the document provider know you're done by closing the stream.
        in.close();
        r.close();

        new CanvasImporter.InitCanvasFromJsonTask().execute(new CanvasImporter.CanvasImportData(content, view, true));
    }

}
