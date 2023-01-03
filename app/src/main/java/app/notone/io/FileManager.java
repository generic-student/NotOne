package app.notone.io;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
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
import java.nio.charset.StandardCharsets;
import java.util.MissingResourceException;

import app.notone.MainActivity;
import app.notone.core.CanvasView;
import app.notone.core.util.RecentCanvas;
import app.notone.ui.CanvasFragmentSettings;
import app.notone.ui.fragments.CanvasFragment;
import kotlin.NotImplementedError;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * @author generic-student and default-student
 * @since 202212XX
 */

/**
 *
 */
public class FileManager {
    private static final String SHARED_PREFS_TAG = "NotOneSharedPrefs";
    private static final String TAG = FileManager.class.getSimpleName();

    //saving

    public static void save(Context context, CanvasView view) throws IOException, JSONException {
        String jsonString = CanvasExporter.canvasViewToJSON(view, true).toString();

        //differentiate between (Filesystem, Firebase, Cache)
        if(view.getCurrentURI() == null || view.getCurrentURI().toString().isEmpty()) {
            saveToInternalDir(context, jsonString);
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

    public static void saveToInternalDir(Context context, String data) throws IOException {
        Log.d(TAG, "Saving file to internal dir " + context.getFilesDir());

        File file = new File(context.getFilesDir(), "canvas.json");
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(data);
        bw.close();
    }

    public static void saveToFirebase(Context context, String data) {
        Log.d(TAG, "Saving file to firebase");

        String userId = "kai";//context.getSharedPreferences(SHARED_PREFS_TAG, Context.MODE_PRIVATE).getString("firebase-userid", "");
        if(userId.isEmpty()) {
            throw new MissingResourceException("Firebase userId could not be found", FileManager.class.getSimpleName(), "userId");
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference userStorageReference = storageReference.child(String.format("%s/FirebaseCanvas.json", userId));

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

    public static void load(@NonNull Context context, @NonNull CanvasView view, @NonNull CanvasFragmentSettings settings) throws IOException {
        if(settings.isOpenFile()) {
            return;
        }

        if(settings.isNewFile()) {
            initNewFile(context, settings);
            return;
        }

        //differentiate between (Filesystem, Firebase, Cache)
        if(view.getCurrentURI() == null || view.getCurrentURI().toString().isEmpty()) {
            loadFromInternalDir(context, view, settings);
            //if the canvas got loaded from disk it means its saved on disk
            view.setSaved(true);
            return;
        }

        if(view.getCurrentURI().toString().equals("firebase")) {
            loadFromFirebase(context, view, settings);
            view.setSaved(true);
            return;
        }

        loadFromFilesystem(context, view, settings);
        view.setSaved(true);
    }

    public static void loadFromInternalDir(Context context, CanvasView view, CanvasFragmentSettings settings) throws IOException {
        Log.d(TAG, "Loading file from internal directory " + context.getFilesDir());

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

        new CanvasImporter.InitCanvasFromJsonTask().execute(new CanvasImporter.CanvasImportData(content.toString(), view, true, settings));
    }

    public static void loadFromFirebase(Context context, CanvasView view, CanvasFragmentSettings settings) {
        Log.d(TAG, "Loading file from firebase.");

        String userId = "kai";//context.getSharedPreferences(SHARED_PREFS_TAG, Context.MODE_PRIVATE).getString("firebase-userid", "");
        if(userId.isEmpty()) {
            throw new MissingResourceException("Firebase userId could not be found", FileManager.class.getSimpleName(), "userId");
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference userStorageReference = storageReference.child(String.format("%s/FirebaseCanvas.json", userId));

        Task<byte[]> downloadTask = userStorageReference.getBytes(10_000_000);
        downloadTask.addOnFailureListener(exception -> {
            Log.d(TAG, "Failed to retrieve file.");
            int errorCode = ((StorageException) exception).getErrorCode();
            try {
                save(context, view);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        });
        downloadTask.addOnSuccessListener(result -> {
            String content = new String(result, StandardCharsets.UTF_8);
            new CanvasImporter.InitCanvasFromJsonTask().execute(new CanvasImporter.CanvasImportData(content, view, true, settings));
        });
    }

    public static void loadFromFilesystem(Context context, CanvasView view, CanvasFragmentSettings settings) throws IOException {
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

        new CanvasImporter.InitCanvasFromJsonTask().execute(new CanvasImporter.CanvasImportData(content, view, true, settings));
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

        if(uri.toString().equals("firebase")) {
            return "FirebaseCanvas.json";
        }

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

    private static void initNewFile(Context context, CanvasFragmentSettings settings) {
        CanvasFragment.sCanvasView.reset();
        CanvasFragment.sCanvasView.setLoaded(true);
    }


    /////////////
    public static void exportPdfDocumentToUri(MainActivity mainActivity, Uri uri) {
        DisplayMetrics metrics = mainActivity.getResources().getDisplayMetrics();
        PdfDocument doc = PdfExporter.exportPdfDocument(CanvasFragment.sCanvasView, (float) metrics.densityDpi / metrics.density, true);

        try {
            ParcelFileDescriptor pfd = mainActivity.getContentResolver().
                    openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            doc.writeTo(fileOutputStream);
            // Let the document provider know you're done by closing the stream.
            fileOutputStream.close();
            pfd.close();
        } catch (IOException e) {
            Log.e(TAG, "savePdfDocument: IOException; probably invalid permissions");
        }
    }

    public static void createNewCanvasFileAtUri(MainActivity mainActivity, Uri uri) {
        Log.d(MainActivity.TAG, "mNewCanvasFile: Created a New File at: " + uri);
        CanvasFragment.sCanvasView.setUri(uri);

        //save the canvas to add the basic json layout to the file (otherwise the created file is empty and cannot be reopened)
        try {
            FileManager.save(mainActivity.getApplicationContext(), CanvasFragment.sCanvasView);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //persist the uri permission so that we can access the file at a later date
        FileManager.persistUriPermission(mainActivity.getContentResolver(), uri);

        MainActivity.sCanvasName = FileManager.getFilenameFromUri(uri, mainActivity.getContentResolver());
        mainActivity.setToolbarTitle(MainActivity.sCanvasName);
        MainActivity.sRecentCanvases.add(new RecentCanvas(MainActivity.sCanvasName, uri, 0));
        mainActivity.updateRecentCanvasesExpListView();

        Toast.makeText(mainActivity, "created a new file", Toast.LENGTH_SHORT).show();
        CanvasFragment.sSettings.setNewFile(false);
    }

    public static void openCanvasFileFromUri(MainActivity mainActivity, Uri uri) {
        CanvasFragment.sCanvasView.setUri(uri);
        //TODO: embed deprecated call to CanvasFileManager in this function
        CanvasFileManager.safeOpenCanvasFile(mainActivity, uri);
        //FileManager.openCanvasFile(mainActivity, uri);
        if(!uri.toString().equals("firebase")) {
            FileManager.persistUriPermission(mainActivity.getContentResolver(), uri);
        }
    }

    public static void saveCanvasFileToUri(MainActivity mainActivity, Uri uri) {
        MainActivity.sCanvasName = FileManager.getFilenameFromUri(uri, mainActivity.getContentResolver());
        mainActivity.setToolbarTitle(MainActivity.sCanvasName);
        MainActivity.sRecentCanvases.add(new RecentCanvas(MainActivity.sCanvasName, uri, 0));
        mainActivity.updateRecentCanvasesExpListView();

        //TODO: embed deprecated call to CanvasFileManager in this function
        CanvasFileManager.safeSave(mainActivity, mainActivity.getApplicationContext(), uri, CanvasFragment.sCanvasView);
        CanvasFragment.sCanvasView.setUri(uri);

        FileManager.persistUriPermission(mainActivity.getContentResolver(), uri);
        Toast.makeText(mainActivity, " saved file as: " + uri, Toast.LENGTH_SHORT).show();
    }

    public static void importPdfDocumentFromUri(Fragment canvasFragment, Uri uri) {
        CanvasFragment.sCanvasView.resetViewMatrices();
        CanvasFragment.sCanvasView.setScale(1f);
        PdfImporter.fromUri(canvasFragment.getContext(), uri, CanvasFragment.sCanvasView.getPdfDocument());
    }
}
