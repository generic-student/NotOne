package app.notone;

import android.graphics.pdf.PdfDocument;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import app.notone.fragments.CanvasFragment;
import app.notone.io.CanvasFileManager;
import app.notone.io.PdfExporter;

class ActivityResultLauncherFactory {

    @NonNull
    public static ActivityResultLauncher<String> getExportPdfActivityResultLauncher(MainActivity mainActivity) {
        return mainActivity.registerForActivityResult(new ActivityResultContracts.CreateDocument("application/pdf"),
                uri -> {
                    if (uri == null) {
                        Log.e(MainActivity.TAG, "mNewCanvasFile: file creation was aborted");
                        return;
                    }
                    DisplayMetrics metrics = mainActivity.getResources().getDisplayMetrics();
                    PdfDocument doc = PdfExporter.exportPdfDocument(MainActivity.mCanvasView, (float) metrics.densityDpi / metrics.density, true);
                    CanvasFileManager.savePdfDocument(mainActivity, uri, doc);
                });
    }

    @NonNull
    public static ActivityResultLauncher<String> getNewCanvasFileActivityResultLauncher(MainActivity mainActivity) {
        return mainActivity.registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"),
                uri -> {
                    if (uri == null) {
                        Log.e(MainActivity.TAG, "mNewCanvasFile: file creation was aborted");
                        return;
                    }
                    Log.d(MainActivity.TAG, "mNewCanvasFile: Created a New File at: " + uri);
                    CanvasFragment.mCanvasView.reset();
                    CanvasFragment.mCanvasView.setUri(uri);
                    MainActivity.mCanvasView = CanvasFragment.mCanvasView;

                    mainActivity.persistUriPermission(mainActivity.getIntent(), uri);

                    MainActivity.mCanvasName = MainActivity.getCanvasFileName(uri, mainActivity.getContentResolver());
                    mainActivity.setCanvasTitle(MainActivity.mCanvasName);
                    mainActivity.addToRecentFiles(MainActivity.mCanvasName, uri);
                    mainActivity.updateExpListRecentFiles();

                    Toast.makeText(mainActivity, "created a new file", Toast.LENGTH_SHORT).show();
                });
    }

    @NonNull
    public static ActivityResultLauncher<String[]> getOpenCanvasFileActivityResultLauncher(MainActivity mainActivity) {
        return mainActivity.registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri == null) {
                Log.e(mainActivity.TAG, "mOpenCanvasFile: file opening was aborted");
                return;
            }
            mainActivity.openCanvasFile(uri);
            mainActivity.persistUriPermission(mainActivity.getIntent(), uri);
        });
    }

    @NonNull
    public static ActivityResultLauncher<String> getSaveAsCanvasFileActivityResultLauncher(MainActivity mainActivity) {
        return mainActivity.registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"), uri -> {
            if (uri == null) {
                Log.e(MainActivity.TAG, "mOpenCanvasFile: file creation was aborted");
                return;
            }

            MainActivity.mCanvasName = MainActivity.getCanvasFileName(uri, mainActivity.getContentResolver());
            mainActivity.setCanvasTitle(MainActivity.mCanvasName);
            mainActivity.addToRecentFiles(MainActivity.mCanvasName, uri);
            mainActivity.updateExpListRecentFiles();

            mainActivity.saveCanvasFile(uri);
            MainActivity.mCanvasView.setUri(uri);

            mainActivity.persistUriPermission(mainActivity.getIntent(), uri);
            Toast.makeText(mainActivity, " saved file as: " + uri, Toast.LENGTH_SHORT).show();
        });
    }

    }
