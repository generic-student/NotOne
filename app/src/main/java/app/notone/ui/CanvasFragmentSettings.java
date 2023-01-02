package app.notone.ui;

import android.net.Uri;

public class CanvasFragmentSettings {
    private Uri mUri;
    private boolean mLoadPdf;
    private boolean mMarkerEnabled;
    private boolean mNewFile;
    private boolean mOpenFile;

    public CanvasFragmentSettings() {
        this.mUri = null;
        this.mLoadPdf = false;
        this.mMarkerEnabled = false;
        this.mNewFile = false;
        this.mOpenFile = false;
    }

    public CanvasFragmentSettings(Uri uri, boolean loadPdf, boolean markerEnabled, boolean newFile, boolean openFile) {
        this.mUri = uri;
        this.mLoadPdf = loadPdf;
        this.mMarkerEnabled = markerEnabled;
        this.mNewFile = newFile;
        this.mOpenFile = openFile;
    }

    public Uri getUri() {
        return mUri;
    }

    public void setUri(Uri mUri) {
        this.mUri = mUri;
    }

    public boolean shouldLoadPdf() {
        return mLoadPdf;
    }

    public void setLoadPdf(boolean mLoadPdf) {
        this.mLoadPdf = mLoadPdf;
    }

    public boolean isMarkerEnabled() {
        return mMarkerEnabled;
    }

    public void setMarkerEnabled(boolean mMarkerEnabled) {
        this.mMarkerEnabled = mMarkerEnabled;
    }

    public boolean isNewFile() {
        return mNewFile;
    }

    public void setNewFile(boolean mNewFile) {
        this.mNewFile = mNewFile;
    }

    public boolean isOpenFile() {
        return mOpenFile;
    }

    public void setOpenFile(boolean mOpenFile) {
        this.mOpenFile = mOpenFile;
    }
}
