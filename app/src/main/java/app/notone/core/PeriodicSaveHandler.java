package app.notone.core;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import app.notone.core.util.SettingsHolder;
import app.notone.ui.fragments.CanvasFragment;

//sigleton instance
public class PeriodicSaveHandler {
    private static final String TAG = PeriodicSaveHandler.class.getSimpleName();
    private static PeriodicSaveHandler sInstance = null;

    private Handler mHandler;
    private Runnable mPeriodicSaveRunner;
    boolean mIsRunning = false;

    private Context mContext;

    public static void init(Context context) {
        sInstance = new PeriodicSaveHandler(context);
    }

    public static boolean isInitialized() {
        return sInstance != null;
    }

    public static PeriodicSaveHandler getInstance() {
        return sInstance;
    }

    private PeriodicSaveHandler(Context context) {
        mContext = context;
        mHandler = new Handler();

        initRunnable();
    }

    private void initRunnable() {
        mPeriodicSaveRunner = () -> {
            try {
                if(CanvasFragment.sCanvasView.isLoaded()) {
                    //FileManager.save(mContext, CanvasFragment.mCanvasView);
                    Log.d(TAG, "Saved canvas to " + CanvasFragment.sCanvasView.getCurrentURI() + ".");
                } else {
                    Log.d(TAG, "Canvas is not fully loaded yet.");
                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
            } finally {
                if(SettingsHolder.isAutoSaveCanvas()) {
                    mHandler.postDelayed(mPeriodicSaveRunner, SettingsHolder.getAutoSaveCanvasIntervalSeconds() * 1000);
                } else {
                    Log.d(TAG, "Stopped PeriodicSaveHandler.");
                }
            }
        };
    }

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

    public boolean isRunning() {
        return mIsRunning;
    }
}
