package app.notone.io;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CanvasFileManager {

    // Request code for selecting a PDF document.
    private static final int PICK_PDF_FILE = 2;

    public static String openCanvasFile(Activity activity, Uri uri) {
        String content = "";
        try {
//            ParcelFileDescriptor pfd = activity.getContentResolver().openFileDescriptor(uri, "r");
//            FileInputStream fileInputStream = new FileInputStream(pfd.getFileDescriptor());
//            openFile
//            String file = fileInputStream.read();

            InputStream in = activity.getContentResolver().openInputStream(uri);

            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuilder total = new StringBuilder();
            for (String line; (line = r.readLine()) != null; ) {
                total.append(line).append('\n');
            }

            content = total.toString();

            // Let the document provider know you're done by closing the stream.
            in.close();
            r.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
//        return "{\"scale\":0.09696853905916214,\"viewTransform\":[0.09696854,0,514.72797,0,0.09696854,511.8249,0,0,1],\"inverseViewTransform\":[10.312623,0,-5308.1953,0,10.312623,-5278.2573,0,0,1],\"writer\":{\"color\":-5242830,\"weight\":5,\"strokes\":[{\"color\":-65536,\"weight\":1,\"path\":[0,0,10,10]},{\"color\":-65536,\"weight\":1,\"path\":[0,0]},{\"color\":-5242830,\"weight\":5,\"path\":[4805.589,242.32764,4805.589,242.32764,4769.0215,269.75244,4763.536,283.46533,4761.708,283.46533,4771.7637,267.01025,4799.1895,234.1001,4837.586,182.90576,4895.1807,102.45947,4977.458,-14.5546875,5089.9053,-168.13623,5225.206,-342.74268,5369.6494,-544.7749,5511.3516,-748.63477,5655.795,-942.43945,5785.6104,-1109.7334,5894.4004,-1249.6013,5971.1934,-1347.4177,6026.959,-1422.3799,6067.1846,-1477.2302,6095.5234,-1508.312,6108.323,-1528.4238,6116.551,-1537.5654]}],\"actions\":[{\"actionType\":\"WRITE\",\"strokeId\":2}],\"undoneActions\":[]},\"uri\":\"content:\\/\\/com.android.providers.downloads.documents\\/document\\/72\"}";
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

    public static Uri saveAsCanvasFile(String canvasData) {
        // change uri in canvas data before writing to file
        return null;
    }
    public static void saveCanvasFile(Activity activity, Uri uri, String json) {
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
