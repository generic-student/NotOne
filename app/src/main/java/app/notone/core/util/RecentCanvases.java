package app.notone.core.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.stream.Collectors;

import app.notone.R;

public class RecentCanvases {
    private ArrayList<RecentCanvas> mRecentCanvases;
    private int mMaxSize;

    public RecentCanvases(int maxSize) {
        this.mMaxSize = maxSize;
        this.mRecentCanvases = new ArrayList<>();
    }

    public void add(RecentCanvas rc) {
        mRecentCanvases.add(rc);
        if(mRecentCanvases.size() > mMaxSize) {
            mRecentCanvases.remove(0);
        }
    }

    public RecentCanvas get(int index) {
        return mRecentCanvases.get(index);
    }

    public RecentCanvas getByFilename(String filename) {
        return mRecentCanvases.stream().filter(r -> r.mName.equals(filename)).findFirst().orElse(null);
    }

    public int size() {
        return mRecentCanvases.size();
    }

    public String[][] getFileList() {
       return new String[][]{mRecentCanvases.stream().map(e -> e.mName).collect(Collectors.toList()).toArray(new String[0])};
//        String[][] data = new String[2][mRecentCanvases.size()];
//        for(int i = 0; i < mRecentCanvases.size(); i++) {
//            data[i] = new String[]{mRecentCanvases.get(i).mName, mRecentCanvases.get(i).mUri.toString()};
//        }
//        return data;
//        //return mRecentCanvases.stream().map(r -> new String[]{r.mName, r.mUri.toString()}).toArray(String[][]::new);
    }

    public static RecentCanvases fromJson(JSONObject json) {
        RecentCanvases recent = new RecentCanvases(0);
        try {
            final int maxSize = json.getInt("maxSize");
            recent = new RecentCanvases(maxSize);

            JSONArray entries = json.getJSONArray("entries");
            for(int i = 0; i < entries.length(); i++) {
                RecentCanvas recentCanvas = RecentCanvas.fromJson(entries.getJSONObject(i));
                if(recentCanvas != null) {
                    recent.add(recentCanvas);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return recent;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("maxSize", mMaxSize);
            JSONArray entries = new JSONArray(mRecentCanvases.stream().map(e -> e.toJson()).collect(Collectors.toList()));
            json.put("entries", entries);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
