package app.notone.io;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

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
import app.notone.ui.CanvasFragmentFlags;
import app.notone.ui.fragments.CanvasFragment;

/**
 * Manages the file access of the app
 * A edited canvas can be saved and opened afterwards with its methods
 * save locations on the local device as well as firebase cloud are possible
 *
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @author Luca Hackel
 * @version 0.1
 * @since 0.1
 */
public class FileManager {
    /**
     * Tag for accessing the shared preferences
     */
    private static final String SHARED_PREFS_TAG = "NotOneSharedPrefs";
    /**
     * Tag for logging
     */
    private static final String TAG = FileManager.class.getSimpleName();

    //saving

    /**
     * General function for saving a CanvasView.
     * Depending on the uri it will be saved to the internal filsystem,
     * external filesystem or firebase. <p>
     * uri == null => internal filesystem <p>
     * uri == 'firebase' => firebase <p>
     * else => external filesystem
     */
    public static void save(Context context, CanvasView view) throws IOException, JSONException {
        String jsonString =
                CanvasExporter.canvasViewToJSON(view, true).toString();

        //differentiate between (Filesystem, Firebase, Cache)
        if (view.getCurrentURI() == null || view.getCurrentURI().toString().isEmpty()) {
            saveToInternalDir(context, jsonString);
            view.setSaved(true);
            return;
        }

        if (view.getCurrentURI().toString().equals("firebase")) {
            saveToFirebase(context, jsonString);
            view.setSaved(true);
            return;
        }

        saveToFilesystem(context, jsonString, view);
        view.setSaved(true);
    }

    /**
     * Saves a String to the internal filesystem as 'canvas.json'.
     * This is used for temporarily storing a Canvas while it has
     * not been assigned a filepath yet.
     *
     * @param context Application Context
     * @param data    Data to save
     * @throws IOException
     * @author Kai Titgens
     * @author kai.titgens@stud.th-owl.de
     */
    public static void saveToInternalDir(Context context, String data) throws IOException {
        Log.d(TAG, "Saving file to internal dir " + context.getFilesDir());

        File file = new File(context.getFilesDir(), "canvas.json");
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(data);
        bw.close();
    }

    /**
     * Saves a String to the firebase server as  FirebaseCanvas.json'.
     * This is used for storing one Canvas to Firebase.
     *
     * @param context Application Context
     * @param data    Data to save
     * @throws IOException
     * @author Kai Titgens
     * @author kai.titgens@stud.th-owl.de
     */
    public static void saveToFirebase(Context context, String data) {
        Log.d(TAG, "Saving file to firebase");

        String userId = getFirebaseUserId(context);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference userStorageReference =
                storageReference.child(String.format("%s/FirebaseCanvas.json"
                        , userId));

        UploadTask uploadTask =
                userStorageReference.putBytes(data.getBytes(StandardCharsets.UTF_8));
        uploadTask.addOnFailureListener(exception ->
                Log.d(TAG, "Failed to upload file.")
        ).addOnSuccessListener(
                taskSnapshot -> Log.d(TAG, "Successfully uploaded file.")
        );
    }

    /**
     * Saves a String to the filesystem given a uri
     * contained in the CanvasView
     *
     * @param context Application Context
     * @param data    Data to save
     * @param view    CanvasView containing the uri
     * @throws IOException
     * @author Luca Hackel
     */
    public static void saveToFilesystem(Context context, String data,
                                        CanvasView view) {
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

    /**
     * General function for loading a CanvasView from the internal filesystem,
     * external fileystem, firebase or for creating a new canvas. <p>
     * if the flags say its a new file => create a new file <p>
     * uri == null || uri == '' => internal filesystem <p>
     * uri == 'firebase' => firebase <p>
     * else => external filesystem
     *
     * @param context Application context
     * @param view    CanvasView to load the data into
     * @param flags   Flags set for the CanvasFragment
     * @throws IOException
     * @author Kai Titgens
     * @author kai.titgens@stud.th-owl.de
     */
    public static void load(@NonNull Context context,
                            @NonNull CanvasView view,
                            @NonNull CanvasFragmentFlags flags) throws IOException {
        if (flags.isNewFile()) {
            initNewFile(context, flags);
            return;
        }

        //differentiate between (Filesystem, Firebase, Cache)
        if (view.getCurrentURI() == null || view.getCurrentURI().toString().isEmpty()) {
            loadFromInternalDir(context, view, flags);
            //if the canvas got loaded from disk it means its saved on disk
            view.setSaved(true);
            return;
        }

        if (view.getCurrentURI().toString().equals("firebase")) {
            loadFromFirebase(context, view, flags);
            view.setSaved(true);
            return;
        }

        loadFromFilesystem(context, view, flags);
        view.setSaved(true);
    }

    /**
     * Loads a CanvasView from the internal directory 'canvas.json'.
     * This is used for loading the temporary file from the interal
     * directory.
     *
     * @param context Application context
     * @param view    CanvasView to laod the data into
     * @param flags   Flags set for the CanvasFragment
     * @throws IOException
     * @author Kai Titgens
     * @author kai.titgens@stud.th-owl.de
     */
    public static void loadFromInternalDir(Context context, CanvasView view,
                                           CanvasFragmentFlags flags) throws IOException {
        Log.d(TAG,
                "Loading file from internal directory " + context.getFilesDir());

        File file = new File(context.getFilesDir(), "canvas.json");
        if (!file.exists()) {
            throw new FileNotFoundException("Could not find the active canvas" +
                    ". Has the cache been cleared?");
        }

        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);

        StringBuilder content = new StringBuilder();
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            content.append(line);
        }

        new CanvasImporter.InitCanvasFromJsonTask().
                execute(new CanvasImporter.CanvasImportData(
                        content.toString(), view, true, flags));
    }

    /**
     * Loads a CanvasView from the firebase server.
     * It will always load 'FirebaseCanvas.json'.
     *
     * @param context Application Context
     * @param view    CanvasView to load the data into
     * @param flags   Flags set for the CanvasFragment
     * @author Kai Titgens
     * @author kai.titgens@stud.th-owl.de
     */
    public static void loadFromFirebase(Context context, CanvasView view,
                                        CanvasFragmentFlags flags) {
        Log.d(TAG, "Loading file from firebase.");

        String userId = getFirebaseUserId(context);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference userStorageReference =
                storageReference.child(String.format("%s/FirebaseCanvas.json"
                        , userId));

        Task<byte[]> downloadTask = userStorageReference.getBytes(10_000_000);
        downloadTask.addOnFailureListener(exception -> {
            Log.d(TAG, "Failed to retrieve file.");
            int errorCode = ((StorageException) exception).getErrorCode();
            //if the error is something else than 'file does not exist yet'
            //show the stacktrace
            if (errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) {
                exception.printStackTrace();
                return;
            }
            //create the file on firebase if the file does not exist yet
            try {
                save(context, view);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        });
        //init the canvas when it is loaded
        downloadTask.addOnSuccessListener(result -> {
            String content = new String(result, StandardCharsets.UTF_8);
            new CanvasImporter.InitCanvasFromJsonTask().
                    execute(new CanvasImporter.CanvasImportData(
                            content, view, true, flags));
        });
    }

    /**
     * Loads a CanvasView from the filesystem given a uri
     * included in the CanvasView.
     *
     * @param context Application Context
     * @param view    CanvasView to load the data into
     * @param flags   Flags set for the CanvasFragment
     * @throws IOException
     * @author Luca Hackel
     */
    public static void loadFromFilesystem(Context context, CanvasView view,
                                          CanvasFragmentFlags flags) throws IOException {
        Log.d(TAG, "Loading file from filesystem " + view.getCurrentURI());

        String content = "";
        InputStream in =
                context.getContentResolver().openInputStream(view.getCurrentURI());

        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        StringBuilder total = new StringBuilder();
        for (String line; (line = r.readLine()) != null; ) {
            total.append(line).append('\n');
        }

        content = total.toString();

        // Let the document provider know you're done by closing the stream.
        in.close();
        r.close();

        new CanvasImporter.InitCanvasFromJsonTask().
                execute(new CanvasImporter.CanvasImportData(
                        content, view, true, flags));
    }

    /**
     * Tell the application that it should persist the permission
     * for a specific uri. This is required so that a uri can be
     * accessed after the app has been closed
     *
     * @param contentResolver
     * @param uri
     * @author Luca Hackel
     */
    @SuppressLint("WrongConstant")
    public static void persistUriPermission(ContentResolver contentResolver,
                                            Uri uri) {
        int takeFlags =
                (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        contentResolver.takePersistableUriPermission(uri, takeFlags);
    }

    /**
     * Request access to the external storage
     *
     * @param mainActivity
     * @author Luca Hackel
     */
    public static void requestFileAccessPermission(MainActivity mainActivity) {
        // requesting permissions if not provided.
        ActivityCompat.requestPermissions(mainActivity,
                new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE},
                200);
    }

    /**
     * Check if the app has access to the external storage
     *
     * @param context
     * @return True if access was granted
     * @author Luca Hackel
     */
    public static boolean checkFileAccessPermission(Context context) {
        return (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    public static String getFirebaseUserId(Context context) {
        String userId = context.getSharedPreferences(SHARED_PREFS_TAG,
                Context.MODE_PRIVATE).getString("firebase-userid", "");
        if (userId.isEmpty()) {
            throw new MissingResourceException("Firebase userId could not be " +
                    "found", FileManager.class.getSimpleName(), "userId");
        }
        return userId;
    }

    /**
     * Get the filename from a Uri
     *
     * @return filename
     * @author Luca Hackel
     */
    public static String getFilenameFromUri(Uri uri,
                                            ContentResolver contentResolver) {
        String fileName = "Unsaved Document";
        String fileSize = "0";

        if (uri.toString().equals("firebase")) {
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
        return fileName; // fileName + " : " + fileSize; // to nerdy for
        // production
    }

    private static void initNewFile(Context context,
                                    CanvasFragmentFlags flags) {
        CanvasFragment.sCanvasView.reset();
        CanvasFragment.sCanvasView.setLoaded(true);
    }


    /////////////

    /**
     * Exports the currently active CanvasView to a Pdf and exports it
     * to the given uri.
     *
     * @param mainActivity
     * @param uri          Uri to export to
     * @author Kai Titgens
     * @author kai.titgens@stud.th-owl.de
     */
    public static void exportPdfDocumentToUri(MainActivity mainActivity,
                                              Uri uri) {
        DisplayMetrics metrics =
                mainActivity.getResources().getDisplayMetrics();
        PdfDocument doc =
                PdfExporter.exportPdfDocument(CanvasFragment.sCanvasView,
                        (float) metrics.densityDpi / metrics.density, true);

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
            Log.e(TAG, "savePdfDocument: IOException; probably invalid " +
                    "permissions");
        }
    }

    /**
     * Creates a new Canvas File at the given uri
     *
     * @param mainActivity
     * @param uri
     * @author Luca Hackel
     */
    public static void createNewCanvasFileAtUri(MainActivity mainActivity,
                                                Uri uri) {
        CanvasFragment.sFlags.setNewFile(true);
        CanvasFragment.sCanvasView.reset();
        CanvasFragment.sCanvasView.setUri(uri);

        //save the canvas to add the basic json layout to the file (otherwise
        // the created file is empty and cannot be reopened)
        try {
            FileManager.save(mainActivity.getApplicationContext(),
                    CanvasFragment.sCanvasView);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //persist the uri permission so that we can access the file at a
        // later date
        FileManager.persistUriPermission(mainActivity.getContentResolver(),
                uri);

        MainActivity.sCanvasName = FileManager.getFilenameFromUri(uri,
                mainActivity.getContentResolver());
        mainActivity.setToolbarTitle(MainActivity.sCanvasName);
        MainActivity.sRecentCanvases.add(
                new RecentCanvas(MainActivity.sCanvasName, uri, 0));
        mainActivity.updateRecentCanvasesExpListView();

        Toast.makeText(mainActivity, "created a new file",
                Toast.LENGTH_SHORT).show();
        CanvasFragment.sFlags.setNewFile(false);
    }

    /**
     * Opens a Canvas file from a given uri
     *
     * @param mainActivity
     * @param uri
     * @author Kai Titgens
     * @author kai.titgens@stud.th-owl.de
     * @author Luca Hackel
     */
    public static void openCanvasFileFromUri(MainActivity mainActivity,
                                             Uri uri) {
        CanvasFragment.sFlags.setOpenFile(true);
        CanvasFragment.sCanvasView.setUri(uri);

        try {
            load(mainActivity, CanvasFragment.sCanvasView,
                    CanvasFragment.sFlags);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!uri.toString().equals("firebase")) {
            persistUriPermission(mainActivity.getContentResolver(), uri);
        }

        MainActivity.sCanvasName = getFilenameFromUri(uri,
                mainActivity.getContentResolver());
        mainActivity.setToolbarTitle(MainActivity.sCanvasName);
        MainActivity.sRecentCanvases.add(
                new RecentCanvas(MainActivity.sCanvasName, uri, 0));
        mainActivity.updateRecentCanvasesExpListView();

        Toast.makeText(mainActivity, "opened a saved file",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Exports a CanvasView as JSON to a given uri
     *
     * @param mainActivity
     * @param uri
     * @author Kai Titgens
     * @author kai.titgens@stud.th-owl.de
     * @author Luca Hackel
     */
    public static void saveCanvasFileToUri(MainActivity mainActivity, Uri uri) {
        // check for file access permissions || grant them,
        // persistUriPermission() doesn't seem to work
        if (!FileManager.checkFileAccessPermission(mainActivity)) {
            Toast.makeText(mainActivity, "Permissions not granted",
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "saveCanvasFile: Permissions not granted");
            return;
        }

        try {
            mainActivity.grantUriPermission(mainActivity.getPackageName(),
                    uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } catch (SecurityException se) {
            /* permission for file access did not persist; user has to chose
            which file to override */
            Log.e(TAG, "saveCanvasFile: Failed to save file as it cant be " +
                    "accessed");
            mainActivity.mSaveAsCanvasFile.launch("canvasFile.json"); // this
            // is recursive
            return;
        }

        CanvasFragment.sCanvasView.setUri(uri);

        try {
            FileManager.save(mainActivity, CanvasFragment.sCanvasView);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MainActivity.sCanvasName = FileManager.getFilenameFromUri(uri,
                mainActivity.getContentResolver());
        mainActivity.setToolbarTitle(MainActivity.sCanvasName);
        MainActivity.sRecentCanvases.add(
                new RecentCanvas(MainActivity.sCanvasName, uri, 0));
        mainActivity.updateRecentCanvasesExpListView();

        if (!uri.toString().equals("firebase")) {
            FileManager.persistUriPermission(mainActivity.getContentResolver(), uri);
        }

        Toast.makeText(mainActivity, " saved file as: " + uri,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Imports a pdf document from a given uri into the active CanvasView
     *
     * @param canvasFragment
     * @param uri
     * @author Luca Hackel
     */
    public static void importPdfDocumentFromUri(Fragment canvasFragment,
                                                Uri uri) {
        CanvasFragment.sFlags.setLoadPdf(true);
        CanvasFragment.sCanvasView.resetViewMatrices();
        CanvasFragment.sCanvasView.setScale(1f);
        PdfImporter.fromUri(canvasFragment.getContext(), uri,
                CanvasFragment.sCanvasView.getPdfDocument());
    }
}
