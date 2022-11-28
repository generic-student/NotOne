package app.notone.io;

import android.net.Uri;

public class CanvasFileManager {

    public static String openCanvasFile() {
        return null;
    }

    public static String newCanvasFile(int Type) {
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
                "\t\t\t0,0\n" +
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
                "   \"uri\": \"a\"\n" +
                "}";
        return canvas; // return new json containing uri
    }

    public static Uri saveasCanvasFile(String canvasData) {
        // change uri in canvas data before writing to file
        return null;
    }

    public static void saveCanvasFile(Uri currentUri, String canvasData) {
    }
}
