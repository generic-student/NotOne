package app.notone.core.util;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

public class RecentCanvas {
    public String mName;
    public Uri mUri;
    public int mFileSize;

    public RecentCanvas(String mName, Uri mUri, int mFileSize) {
        this.mName = mName;
        this.mUri = mUri;
        this.mFileSize = mFileSize;
    }

    public static RecentCanvas fromJson(JSONObject json) {
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
