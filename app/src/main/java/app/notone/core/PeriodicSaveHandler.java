package app.notone.core;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import app.notone.core.util.SettingsHolder;
import app.notone.ui.fragments.CanvasFragment;

/**
 * Singleton class for handling the periodic saving of the currently active
 * Canvas in the background.
 * This was inspired by the Stackoverflow answer from Rajesh. K
 * https://stackoverflow.com/questions/6425611/android-run-a-task-periodically
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public class PeriodicSaveHandler {
    /** Tag for logging */
    private static final String TAG = PeriodicSaveHandler.class.getSimpleName();
    /** Singleton instance */
    private static PeriodicSaveHandler sInstance = null;

    /** Periodically starts the Runnable for saving the canvas */
    private Handler mHandler;
    /** Runnable for saving the canvas */
    private Runnable mPeriodicSaveRunner;
    /** True if the PeriodicSaveHandler is running */
    boolean mIsRunning = false;

    /** Application context */
    private Context mContext;

    /**
     * Initializes the singleton instance
     * @param context Application context
     */
    public static void init(Context context) {
        sInstance = new PeriodicSaveHandler(context);
    }

    /**
     * Determines if the singleton instance is fully initialized.
     * It is fully initialized if the instance is not null.
     * @return True if it is initialized
     */
    public static boolean isInitialized() {
        return sInstance != null;
    }

    /**
     * Returns the singleton instance
     * @return PeriodicSaveHandler
     */
    public static PeriodicSaveHandler getInstance() {
        return sInstance;
    }

    private PeriodicSaveHandler(Context context) {
        mContext = context;
        mHandler = new Handler();

        initRunnable();
    }

    /**
     * Initializes the Runnable and assigns the function that will
     * be periodically called.
     * The interval in which it will be called is stored in the
     * {@link SettingsHolder} and is determined in the settings page by the user.
     */
    private void initRunnable() {
        mPeriodicSaveRunner = () -> {
            try {
                if(CanvasFragment.sCanvasView.isLoaded()) {
                    //FileManager.save(mContext, CanvasFragment.mCanvasView);
                    Log.d(TAG, "Saved canvas to " + CanvasFragment.sCanvasView.getCurrentURI() + ".");
                } else {
                    Log.d(TAG, "Canvas is not fully loaded yet.");
                }
            } finally {
                if(SettingsHolder.shouldAutoSaveCanvas()) {
                    mHandler.postDelayed(mPeriodicSaveRunner, SettingsHolder.getAutoSaveCanvasIntervalSeconds() * 1000);
                } else {
                    Log.d(TAG, "Stopped PeriodicSaveHandler.");
                }
            }
        };
    }

    /**
     * Starts the PeriodicSaveHandler in the background
     */
    public void start() {
        if(isRunning()) {
            Log.d(TAG, "PeriodicSaveHandler is already running.");
            return;
        }
        Log.d(TAG, "Starting PeriodicSaveHandler.");
        mPeriodicSaveRunner.run();
        Log.d(TAG, "Started PeriodicSaveHandler.");
        mIsRunning = true;
    }

    /**
     * Stops the PeriodicSaveHandler
     */
    public void stop() {
        if(!isRunning()) {
            Log.d(TAG, "PeriodicSaveHandler is not running.");
            return;
        }
        Log.d(TAG, "Stopping PeriodicSaveHandler.");
        mHandler.removeCallbacks(mPeriodicSaveRunner);
        Log.d(TAG, "Stopped PeriodicSaveHandler.");
        mIsRunning = false;
    }

    /**
     * Checks if the PeriodicSaveHandler is currently running
     * @return True if it is running
     */
    public boolean isRunning() {
        return mIsRunning;
    }
}
