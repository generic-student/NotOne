package app.notone.ui;

import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import app.notone.MainActivity;
import app.notone.io.FileManager;
import app.notone.ui.fragments.CanvasFragment;

/**
 * @author default-student
 * @since 202212XX
 */

public class ActivityResultLauncherProvider {

    @NonNull
    public static ActivityResultLauncher<String> getExportPdfActivityResultLauncher(MainActivity mainActivity) {
        return mainActivity.registerForActivityResult(new ActivityResultContracts.CreateDocument("application/pdf"), uri -> {
            if (uri == null) {
                Log.e(MainActivity.TAG, "mNewCanvasFile: file creation was aborted");
                return;
            }
            FileManager.exportPdfDocumentToUri(mainActivity, uri);
        });
    }

    @NonNull
    public static ActivityResultLauncher<String> getNewCanvasFileActivityResultLauncher(MainActivity mainActivity) {
        return mainActivity.registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"), uri -> {
            if (uri == null) {
                Log.e(MainActivity.TAG, "mNewCanvasFile: file creation was aborted");
                return;
            }
            FileManager.createNewCanvasFileAtUri(mainActivity, uri);
        });
    }

    @NonNull
    public static ActivityResultLauncher<String[]> getOpenCanvasFileActivityResultLauncher(MainActivity mainActivity) {
        return mainActivity.registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri == null) {
                Log.e(MainActivity.TAG, "mOpenCanvasFile: file opening was aborted");
                return;
            }
            FileManager.openCanvasFileFromUri(mainActivity, uri);
        });
    }

    @NonNull
    public static ActivityResultLauncher<String> getSaveAsCanvasFileActivityResultLauncher(MainActivity mainActivity) {
        return mainActivity.registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"), uri -> {
            if (uri == null) {
                Log.e(MainActivity.TAG, "mOpenCanvasFile: file creation was aborted");
                return;
            }
            FileManager.saveCanvasFileToUri(mainActivity, uri);
        });
    }

    @NonNull
    public static ActivityResultLauncher<String> getImportPdfActivityResultLauncher(Fragment canvasFragment) {
        return canvasFragment.registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri == null) {
                Log.e(CanvasFragment.TAG, "getImportPdfActivityResultLauncher: file selection was aborted");
                return;
            }
            FileManager.importPdfDocumentFromUri(canvasFragment, uri);
        });
    }
}
