package app.notone;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;

import app.notone.fragments.CanvasFragment;
import app.notone.io.FileManager;

public class PeriodicSaveHandler {
    private int mInterval = 5000;
    private Handler mHandler;
    private Runnable mPeriodicSaveRunner;

    private Context mContext;

    public PeriodicSaveHandler(Context context) {
        mContext = context;
        mHandler = new Handler();

        initRunnable();
    }

    public PeriodicSaveHandler(Context context, int interval) {
        mContext = context;
        mInterval = interval;
        mHandler = new Handler();

        initRunnable();
    }

    private void initRunnable() {
        mPeriodicSaveRunner = () -> {
            try {
                FileManager.save(mContext, CanvasFragment.mCanvasView);
                Log.d("PeriodicSaveHandler", "Saved canvas");
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mHandler.postDelayed(mPeriodicSaveRunner, mInterval);
            }
        };
    }

    public void start() {
        mPeriodicSaveRunner.run();
    }

    public void stop() {
        mHandler.removeCallbacks(mPeriodicSaveRunner);
    }
}
