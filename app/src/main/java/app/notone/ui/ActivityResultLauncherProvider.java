package app.notone.ui;

import android.graphics.pdf.PdfDocument;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import app.notone.MainActivity;
import app.notone.core.util.RecentCanvas;
import app.notone.io.CanvasFileManager;
import app.notone.io.FileManager;
import app.notone.io.PdfExporter;
import app.notone.io.PdfImporter;
import app.notone.ui.fragments.CanvasFragment;

public class ActivityResultLauncherProvider {

    @NonNull
    public static ActivityResultLauncher<String> getExportPdfActivityResultLauncher(MainActivity mainActivity) {
        return mainActivity.registerForActivityResult(new ActivityResultContracts.CreateDocument("application/pdf"), uri -> {
            if (uri == null) {
                Log.e(MainActivity.TAG, "mNewCanvasFile: file creation was aborted");
                return;
            }
            DisplayMetrics metrics = mainActivity.getResources().getDisplayMetrics();
            PdfDocument doc = PdfExporter.exportPdfDocument(MainActivity.sCanvasView, (float) metrics.densityDpi / metrics.density, true);
            CanvasFileManager.save2PDF(mainActivity, uri, doc);
        });
    }

    @NonNull
    public static ActivityResultLauncher<String> getNewCanvasFileActivityResultLauncher(MainActivity mainActivity) {
        return mainActivity.registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"), uri -> {
            if (uri == null) {
                Log.e(MainActivity.TAG, "mNewCanvasFile: file creation was aborted");
                return;
            }
            Log.d(MainActivity.TAG, "mNewCanvasFile: Created a New File at: " + uri);
            CanvasFragment.sCanvasView.reset();
            CanvasFragment.sCanvasView.setUri(uri);
            MainActivity.sCanvasView = CanvasFragment.sCanvasView;

            FileManager.persistUriPermission(mainActivity.getContentResolver(), uri);

            MainActivity.sCanvasName = FileManager.getFilenameFromUri(uri, mainActivity.getContentResolver());
            mainActivity.setToolbarTitle(MainActivity.sCanvasName);
            MainActivity.sRecentCanvases.add(new RecentCanvas(MainActivity.sCanvasName, uri, 0));
            mainActivity.updateRecentCanvasesExpListView();

            Toast.makeText(mainActivity, "created a new file", Toast.LENGTH_SHORT).show();
        });
    }

    @NonNull
    public static ActivityResultLauncher<String[]> getOpenCanvasFileActivityResultLauncher(MainActivity mainActivity) {
        return mainActivity.registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri == null) {
                Log.e(MainActivity.TAG, "mOpenCanvasFile: file opening was aborted");
                return;
            }
            CanvasFileManager.safeOpenCanvasFile(mainActivity, uri);
            FileManager.persistUriPermission(mainActivity.getContentResolver(), uri);
        });
    }

    @NonNull
    public static ActivityResultLauncher<String> getSaveAsCanvasFileActivityResultLauncher(MainActivity mainActivity) {
        return mainActivity.registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"), uri -> {
            if (uri == null) {
                Log.e(MainActivity.TAG, "mOpenCanvasFile: file creation was aborted");
                return;
            }

            MainActivity.sCanvasName = FileManager.getFilenameFromUri(uri, mainActivity.getContentResolver());
            mainActivity.setToolbarTitle(MainActivity.sCanvasName);
            MainActivity.sRecentCanvases.add(new RecentCanvas(MainActivity.sCanvasName, uri, 0));
            mainActivity.updateRecentCanvasesExpListView();

            CanvasFileManager.safeSave(mainActivity, mainActivity.getApplicationContext(), uri, MainActivity.sCanvasView);
            MainActivity.sCanvasView.setUri(uri);

            FileManager.persistUriPermission(mainActivity.getContentResolver(), uri);
            Toast.makeText(mainActivity, " saved file as: " + uri, Toast.LENGTH_SHORT).show();
        });
    }

    @NonNull
    public static ActivityResultLauncher<String> getImportPdfActivityResultLauncher(Fragment canvasFragment) {
        return canvasFragment.registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri == null) {
                Log.e(CanvasFragment.TAG, "getImportPdfActivityResultLauncher: file selection was aborted");
                return;
            }
            CanvasFragment.sCanvasView.resetViewMatrices();
            CanvasFragment.sCanvasView.setScale(1f);
            CanvasFragment.sIsLoadingPdfPages = true;
            PdfImporter.fromUri(canvasFragment.getContext(), uri, CanvasFragment.sCanvasView.getPdfDocument());
            CanvasFragment.sCanvasView.invalidate();
        });
    }
}
