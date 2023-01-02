package app.notone.ui;

import android.net.Uri;

public class CanvasFragmentSettings {
    private Uri mUri;
    private boolean mLoadPdf;
    private boolean mMarkerEnabled;

    public CanvasFragmentSettings(Uri uri, boolean loadPdf, boolean markerEnabled) {
        this.mUri = uri;
        this.mLoadPdf = loadPdf;
        this.mMarkerEnabled = markerEnabled;
    }

    public Uri getUri() {
        return mUri;
    }

    public void setUri(Uri mUri) {
        this.mUri = mUri;
    }

    public boolean isLoadPdf() {
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
}
