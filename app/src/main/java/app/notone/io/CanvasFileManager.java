package app.notone.io;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;

import java.io.FileOutputStream;
import java.io.IOException;

public class CanvasFileManager {

    public static String openCanvasFile() {
        return null;
    }

    public static String newCanvasFile(Uri uri, int Type) {
        String canvas = "{\n" +
                "   \"scale\":0.09696853905916214,\n" +
                "   \"viewTransform\":[\n" +
                "      0.09696854,\n" +
                "      0,\n" +
                "      514.72797,\n" +
                "      0,\n" +
                "      0.09696854,\n" +
                "      511.8249,\n" +
                "      0,\n" +
                "      0,\n" +
                "      1\n" +
                "   ],\n" +
                "   \"inverseViewTransform\":[\n" +
                "      100,\n" +
                "      0,\n" +
                "      -173288.36,\n" +
                "      0,\n" +
                "      100,\n" +
                "      -55048.45,\n" +
                "      0,\n" +
                "      0,\n" +
                "      1\n" +
                "   ],\n" +
                "   \"writer\":{\n" +
                "      \"color\":-5242830,\n" +
                "      \"weight\":5,\n" +
                "      \"strokes\":[\n" +
                "         {\n" +
                "            \"color\":-65536,\n" +
                "            \"weight\":1,\n" +
                "            \"path\":[\n" +
                "\t\t\t0,0,\n" +
                "\t\t\t10,10\n" +
                "            ]\n" +
                "         },\n" +
                "         {\n" +
                "            \"color\":-65536,\n" +
                "            \"weight\":1,\n" +
                "            \"path\":[\n" +
                "\t\t\t0,0\n" +
                "            ]\n" +
                "         }\n" +
                "      ],\n" +
                "      \"actions\":[\n" +
                "         \n" +
                "      ],\n" +
                "      \"undoneActions\":[\n" +
                "         \n" +
                "      ]\n" +
                "   },\n" +
                "   \"uri\": \"" + uri + "\"\n" +
                "}";
        return canvas; // return new json containing uri
    }

    public static Uri saveasCanvasFile(String canvasData) {
        // change uri in canvas data before writing to file
        return null;
    }

    public static void saveCanvasFile(Uri currentUri, String canvasData) {
    }

    public void saveJSONtoFileStorage(){

    }

    public static final int CREATE_NEW_FILE_REQUEST_CODE = 56498;

    public static void createNewFile(Activity activity, Uri pickerInitialUri) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/text");
        intent.putExtra(Intent.EXTRA_TITLE, "note.json");

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document. (pickerInitialUri)
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        activity.startActivityForResult(intent, CREATE_NEW_FILE_REQUEST_CODE);
    }

    public static void alterDocument(Activity activity, Uri uri, String json) {
        try {
            ParcelFileDescriptor pfd = activity.getContentResolver().
                    openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write(("Overwritten at " + System.currentTimeMillis() +
                    "\n" + json).getBytes());
            // Let the document provider know you're done by closing the stream.
            fileOutputStream.close();
            pfd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
// https://developer.android.com/training/data-storage/shared/documents-files#java

}
