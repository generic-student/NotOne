package app.notone.core.util;

import android.content.SharedPreferences;

public class SettingsHolder {
    //visuals
    private static boolean sDarkMode = false;
    private static boolean sShowPdfBounds = false;
    private static boolean sTextMarkerOpacityAdding = true;

    //function
    private static boolean sRestrictedPanning = true;
    private static boolean sDrawerSwipeOpen = false;
    private static boolean sAutoSaveCanvas = false;
    private static int sAutoSaveCanvasIntervalSeconds = 0;
    private static boolean sSynchronizeWithServer = false;
    private static int sSynchronizeWithServerIntervalSeconds = 0;

    public static void update(SharedPreferences prefs) {
        //visuals
        sDarkMode = prefs.getBoolean("darkmode", false);
        sShowPdfBounds = prefs.getBoolean("pdfbounds", false);
        sTextMarkerOpacityAdding = prefs.getBoolean("textmarker", true);

        //function
        sRestrictedPanning =  prefs.getBoolean("twofingerpanning", false);
        sDrawerSwipeOpen =  prefs.getBoolean("drawerswipe", false);
        sAutoSaveCanvasIntervalSeconds =  Integer.parseInt(prefs.getString("saveintervall", "999999999"));
        sAutoSaveCanvas =  prefs.getBoolean("autosave", false);
        sSynchronizeWithServer =  prefs.getBoolean("sync", false);
        sSynchronizeWithServerIntervalSeconds =  Integer.parseInt(prefs.getString("syncintervall", "999999999"));

    }

    public static boolean isDarkMode() {
        return sDarkMode;
    }

    public static boolean isShowPdfBounds() {
        return sShowPdfBounds;
    }

    public static boolean isTextMarkerOpacityAdding() {
        return sTextMarkerOpacityAdding;
    }

    public static boolean isRestrictedPanning() {
        return sRestrictedPanning;
    }

    public static boolean isDrawerSwipeOpen() {
        return sDrawerSwipeOpen;
    }

    public static boolean isAutoSaveCanvas() {
        return sAutoSaveCanvas;
    }

    public static int getAutoSaveCanvasIntervalSeconds() {
        return sAutoSaveCanvasIntervalSeconds;
    }

    public static boolean isSynchronizeWithServer() {
        return sSynchronizeWithServer;
    }

    public static int getSynchronizeWithServerIntervalSeconds() {
        return sSynchronizeWithServerIntervalSeconds;
    }
}
