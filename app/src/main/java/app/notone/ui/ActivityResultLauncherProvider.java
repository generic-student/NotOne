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
 * Provides the ActivityResultLaunchers for the MainActivity and UI
 * They enable the save and open functionality in the navDrawer
 *
 * @author Luca Hackel
 * @since 202212XX
 */

public class ActivityResultLauncherProvider {

    /**
     * Provides an ActivityResultLauncher that opens a CreateDocument
     * Activity to chose a uri.
     * This uri is used as save location for the pdf export.
     *
     * @param mainActivity app context
     * @return ActivityResultLauncher
     */
    @NonNull
    public static ActivityResultLauncher<String> getExportPdfActivityResultLauncher(
            MainActivity mainActivity) {
        return mainActivity.registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/pdf"), uri -> {
            if (uri == null) {
                Log.e(MainActivity.TAG, "mNewCanvasFile: file creation was " +
                        "aborted");
                return;
            }
            FileManager.exportPdfDocumentToUri(mainActivity, uri);
        });
    }

    /**
     * Provides an ActivityResultLauncher that opens a CreateDocument
     * Activity to chose a uri.
     * The new canvas file is created at this location.
     *
     * @param mainActivity app context
     * @return
     */
    @NonNull
    public static ActivityResultLauncher<String> getNewCanvasFileActivityResultLauncher(
            MainActivity mainActivity) {
        return mainActivity.registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/json"), uri -> {
            if (uri == null) {
                Log.e(MainActivity.TAG, "mNewCanvasFile: file creation was " +
                        "aborted");
                return;
            }
            FileManager.createNewCanvasFileAtUri(mainActivity, uri);
        });
    }

    /**
     * Provides an ActivityResultLauncher that opens a OpenDocument Activity
     * to chose the uri.
     * The canvas file is opened from this uri.
     *
     * @param mainActivity app context
     * @return
     */
    @NonNull
    public static ActivityResultLauncher<String[]>
    getOpenCanvasFileActivityResultLauncher(MainActivity mainActivity) {
        return mainActivity.registerForActivityResult(
                new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri == null) {
                Log.e(MainActivity.TAG, "mOpenCanvasFile: file opening was " +
                        "aborted");
                return;
            }
            FileManager.openCanvasFileFromUri(mainActivity, uri);
        });
    }

    /**
     * Provides an ActivityResultLauncher that opens a CreateDocument
     * Activity to chose the uri.
     * The currently opened canvas is saved to this uri.
     *
     * @param mainActivity app context
     * @return
     */
    @NonNull
    public static ActivityResultLauncher<String>
    getSaveAsCanvasFileActivityResultLauncher(MainActivity mainActivity) {
        return mainActivity.registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/json"), uri -> {
            if (uri == null) {
                Log.e(MainActivity.TAG, "mOpenCanvasFile: file creation was " +
                        "aborted");
                return;
            }
            FileManager.saveCanvasFileToUri(mainActivity, uri);
        });
    }

    /**
     * Provides an ActivityResultLauncher that opens a GetContent Activity to
     * chose the uri.
     * A PDF is imported from this uri into the canvas.
     *
     * @param canvasFragment
     * @return
     */
    @NonNull
    public static ActivityResultLauncher<String>
    getImportPdfActivityResultLauncher(Fragment canvasFragment) {
        return canvasFragment.registerForActivityResult(
                new ActivityResultContracts.GetContent(), uri -> {
            if (uri == null) {
                Log.e(CanvasFragment.TAG, "getImportPdfActivityResultLauncher" +
                        ": file selection was aborted");
                return;
            }
            FileManager.importPdfDocumentFromUri(canvasFragment, uri);
        });
    }
}
