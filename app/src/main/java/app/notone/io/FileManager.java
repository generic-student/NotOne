package app.notone.io;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

import app.notone.MainActivity;
import app.notone.core.CanvasView;
import kotlin.NotImplementedError;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * @author generic-student and default-student
 * @since 202212XX
 */


public class FileManager {
    private static final String SHARED_PREFS_TAG = "NotOneSharedPrefs";
    private static final String TAG = FileManager.class.getSimpleName();

    //saving

    public static void save(Context context, CanvasView view) throws IOException, JSONException {
        String jsonString = CanvasExporter.canvasViewToJSON(view, true).toString();

        //differentiate between (Filesystem, Firebase, Cache)
        if(view.getCurrentURI() == null || view.getCurrentURI().toString().isEmpty()) {
            saveToCache(context, jsonString);
            view.setSaved(true);
            return;
        }

        if(view.getCurrentURI().toString().equals("firebase")) {
            saveToFirebase(context, jsonString);
            view.setSaved(true);
            return;
        }

        saveToFilesystem(context, jsonString, view);
        view.setSaved(true);
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
            //if the canvas got loaded from disk it means its saved on disk
            view.setSaved(true);
            return;
        }

        if(view.getCurrentURI().toString().equals("firebase")) {
            loadFromFirebase(context, view);
            view.setSaved(true);
            return;
        }

        loadFromFilesystem(context, view);
        view.setSaved(true);
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

    @SuppressLint("WrongConstant")
    public static void persistUriPermission(ContentResolver contentResolver, Uri uri) {
        int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        contentResolver.takePersistableUriPermission(uri, takeFlags);
    }

    public static void requestFileAccessPermission(MainActivity mainActivity) {
        // requesting permissions if not provided.
        ActivityCompat.requestPermissions(mainActivity, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, 200);
    }

    public static boolean checkFileAccessPermission(Context context) {
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    public static String getFilenameFromUri(Uri uri, ContentResolver contentResolver) {
        String fileName = "Unsaved Document";
        String fileSize = "0";
        try {
            Cursor cursor =
                    contentResolver.query(uri, null, null, null, null);
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            cursor.moveToFirst();
            fileName = cursor.getString(nameIndex);
            fileSize = Long.toString(cursor.getLong(sizeIndex));
            cursor.close();
        } catch (NullPointerException n) {
            Log.e(TAG, "getCanvasFileName: couldnt extract Filename from uri");
        }
        return fileName; // fileName + " : " + fileSize; // to nerdy for production
    }
}
