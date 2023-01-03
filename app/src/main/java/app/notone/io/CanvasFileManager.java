package app.notone.io;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import app.notone.MainActivity;
import app.notone.core.CanvasView;
import app.notone.core.util.RecentCanvas;
import app.notone.ui.fragments.CanvasFragment;

/**
 * @deprecated replaced by Filemanager
 * @author default-student
 * @since 202212XX
 */

@Deprecated
public class CanvasFileManager {
    private final static String TAG = "CanvasFilemanager";

    public static String open(Activity activity, Uri uri) {
        Log.d(TAG, "openCanvasFile: " + uri);
        String content = "";
        try {
            InputStream in = activity.getContentResolver().openInputStream(uri);
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuilder total = new StringBuilder();
            for (String line; (line = r.readLine()) != null; ) {
                total.append(line).append('\n');
            }
            content = total.toString();

            in.close();
            r.close();

        } catch (FileNotFoundException e) {
            Log.e(TAG, "openCanvasFile: ERROR File not Found");
        } catch (IOException e) {
            Log.e(TAG, "openCanvasFile: ERROR IOException");
        }
        return content;
    }

    public static void safeOpenCanvasFile(MainActivity mainActivity, Uri uri) {
        Log.d(TAG, "mOpenCanvasFile: Open File at: " + uri);

//        CanvasFragment.sCanvasView.setUri(uri);
//        try {
//            FileManager.load(mainActivity, CanvasFragment.sCanvasView, CanvasFragment.sSettings);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        CanvasFragment.sSettings.setUri(uri);
        if(uri.toString().equals("firebase")) {
            CanvasFragment.sCanvasView.setUri(uri);
            FileManager.loadFromFirebase(mainActivity, CanvasFragment.sCanvasView, CanvasFragment.sSettings);
        }
        else {
            String canvasData = CanvasFileManager.open(mainActivity, uri);
            new CanvasImporter.InitCanvasFromJsonTask().execute(new CanvasImporter.CanvasImportData(canvasData, CanvasFragment.sCanvasView, true, null));
        }
        CanvasFragment.sSettings.setOpenFile(true);

//        try {
//            CanvasImporter.initCanvasViewFromJSON(canvasData, CanvasFragment.sCanvasView, true);
//        } catch (JSONException e) {
//            Log.e(TAG, "mOpenCanvasFile: failed to open ", e);
//            Toast.makeText(mainActivity, "failed to parse file", Toast.LENGTH_SHORT).show();
//            return;
//        } catch (IllegalArgumentException i) {
//            Log.e(TAG, "mOpenCanvasFile: canvasFile was empty");
//            Toast.makeText(mainActivity, "file is empty", Toast.LENGTH_SHORT).show();
//            return;
//        }
        //reorder the recent canvases to have the active one as the first element

        CanvasFragment.sCanvasView.invalidate();

        if(!uri.toString().equals("firebase")) {
            FileManager.persistUriPermission(mainActivity.getContentResolver(), uri);
        }

        MainActivity.sCanvasName = FileManager.getFilenameFromUri(uri, mainActivity.getContentResolver());
        mainActivity.setToolbarTitle(MainActivity.sCanvasName);
        MainActivity.sRecentCanvases.add(new RecentCanvas(MainActivity.sCanvasName, uri, 0));
        mainActivity.updateRecentCanvasesExpListView();

        Toast.makeText(mainActivity, "opened a saved file", Toast.LENGTH_SHORT).show();
    }

//    public static void save(Activity activity, Uri uri, String json) {
//        try {
//            ParcelFileDescriptor pfd = activity.getContentResolver().
//                    openFileDescriptor(uri, "w");
//            FileOutputStream fileOutputStream =
//                    new FileOutputStream(pfd.getFileDescriptor());
//            fileOutputStream.write((json).getBytes());
//            // Let the document provider know you're done by closing the stream.
//            fileOutputStream.close();
//            pfd.close();
//        } catch (IOException e) {
//            Log.e(TAG, "saveCanvasFile: IOException; probably invalid permissions");
//        }
//    }

    public static void safeSave(MainActivity mainActivity, Context context, Uri uri, CanvasView canvasView) {
        Log.d(TAG, "mSaveAsCanvasFile to Json");
        // check for file access permissions || grant them, persistUriPermission() doesnt seem to work
        if (!FileManager.checkFileAccessPermission(context)) {
            Toast.makeText(mainActivity, "Permissions not granted", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "saveCanvasFile: Permissions not granted");
            return;
        }
        try {
            context.grantUriPermission(context.getPackageName(), uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } catch (SecurityException se) {
            /* permission for file access did not persist; user has to chose which file to override */
            Log.e(TAG, "saveCanvasFile: Failed to save file as it cant be accessed");
            mainActivity.mSaveAsCanvasFile.launch("canvasFile.json"); // this is recursive
            return;
        }

//        // save files
//        String canvasData = "";
//        try {
//            canvasData = CanvasExporter.canvasViewToJSON(canvasView, true).toString();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        CanvasFileManager.save(mainActivity, uri, canvasData);
        try {
            FileManager.save(mainActivity, canvasView);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MainActivity.sCanvasName = FileManager.getFilenameFromUri(uri, mainActivity.getContentResolver());
        mainActivity.setToolbarTitle(MainActivity.sCanvasName);

        Toast.makeText(mainActivity, "saved file", Toast.LENGTH_SHORT).show();
    }

    public static void save2PDF(Activity activity, Uri uri, PdfDocument doc) {
        try {
            ParcelFileDescriptor pfd = activity.getContentResolver().
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
}
