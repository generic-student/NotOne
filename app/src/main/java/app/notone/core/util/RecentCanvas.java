package app.notone.core.util;

import android.net.Uri;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Describes a Canvas by name, save location and file size
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public class RecentCanvas {
    /** Filename */
    public String mName;
    /** Save location*/
    public Uri mUri;
     /** Size of the file in bytes*/
    @Deprecated
    public int mFileSize;

    public RecentCanvas(String mName, Uri mUri, int mFileSize) {
        this.mName = mName;
        this.mUri = mUri;
        this.mFileSize = mFileSize;
    }

    /**
     * Builds an Instance from JSON
     * @param json JSONObject
     * @return Instance of RecentCanvas
     */
    public static RecentCanvas fromJson(@NonNull JSONObject json) {
        try {
            String name = json.getString("name");
            Uri uri = Uri.parse(json.getString("uri"));
            int fileSize = json.getInt("size");

            return new RecentCanvas(name, uri, fileSize);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a JSON representation of the data in this class
     * @return instance of JSONObject
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("name", mName);
            json.put("uri", mUri.toString());
            json.put("size", mFileSize);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }
}
