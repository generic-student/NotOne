package app.notone.core.util;

import android.content.SharedPreferences;

/**
 * This class represents the state of the settings in the settings page.
 * The member variables are updated according to the values stored in the
 * shared preferences.
 *
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public class SettingsHolder {
    //visuals
    /**
     * True if dark mode is enabled
     */
    private static boolean sDarkMode = false;
    /**
     * True if a border should be drawn around the area that will be
     * affected when exporting the canvas as a pdf
     */
    private static boolean sShowPdfBounds = false;
    /**
     * True if the text-marker strokes should be combined to avoid layering
     */
    private static boolean sTextMarkerOpacityAdding = true;

    //function
    /**
     * True if the user can only pan with two or more fingers
     */
    private static boolean sRestrictedPanning = true;
    /**
     * True if the user swipe the drawer menu open from the side
     */
    private static boolean sDrawerSwipeOpen = false;
    /**
     * True if the canvas is automatically saved
     */
    private static boolean sAutoSaveCanvas = false;
    /**
     * Amount of Seconds before the canvas is saved
     */
    private static int sAutoSaveCanvasIntervalSeconds = 0;
    /**
     * True if the canvases should be synced with the server
     */
    private static boolean sSynchronizeWithServer = false;
    /**
     * Amount of Seconds before the canvas is synchronized
     */
    private static int sSynchronizeWithServerIntervalSeconds = 0;

    /**
     * Update the values in the SettingsHolder from the shared preferences
     *
     * @param prefs SharedPreferences to take the values from
     */
    public static void update(SharedPreferences prefs) {
        //visuals
        sDarkMode = prefs.getBoolean("darkmode", false);
        sShowPdfBounds = prefs.getBoolean("pdfbounds", false);
        sTextMarkerOpacityAdding = prefs.getBoolean("textmarker", true);

        //function
        sRestrictedPanning = prefs.getBoolean("twofingerpanning", false);
        sDrawerSwipeOpen = prefs.getBoolean("drawerswipe", false);
        sAutoSaveCanvasIntervalSeconds = Integer.parseInt(prefs.getString(
                "saveintervall", "999999999"));
        sAutoSaveCanvas = prefs.getBoolean("autosave", false);
        sSynchronizeWithServer = prefs.getBoolean("sync", false);
        sSynchronizeWithServerIntervalSeconds =
                Integer.parseInt(
                        prefs.getString("syncintervall", "999999999"));

    }

    public static boolean isDarkMode() {
        return sDarkMode;
    }

    public static boolean shouldShowPdfBounds() {
        return sShowPdfBounds;
    }

    public static boolean isTextMarkerOpacityAdding() {
        return sTextMarkerOpacityAdding;
    }

    public static boolean isPanningRestricted() {
        return sRestrictedPanning;
    }

    public static boolean isDrawerSwipeOpen() {
        return sDrawerSwipeOpen;
    }

    public static boolean shouldAutoSaveCanvas() {
        return sAutoSaveCanvas;
    }

    public static int getAutoSaveCanvasIntervalSeconds() {
        return sAutoSaveCanvasIntervalSeconds;
    }

    public static boolean shouldSynchronizeWithServer() {
        return sSynchronizeWithServer;
    }

    public static int getSynchronizeWithServerIntervalSeconds() {
        return sSynchronizeWithServerIntervalSeconds;
    }
}
