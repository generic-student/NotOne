package app.notone.core.util;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Holds a list of RecentCanvas that were opened recently up to a max size.
 * The first index in the list is always the last element added.
 *
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public class RecentCanvases {
    /**
     * Tag for logging
     */
    private static final String TAG = RecentCanvas.class.getSimpleName();
    /**
     * List of {@link RecentCanvas}
     */
    private ArrayList<RecentCanvas> mRecentCanvases;
    /**
     * Max. amount of elements in the list
     */
    private int mMaxSize;

    public RecentCanvases(int maxSize) {
        this.mMaxSize = maxSize;
        this.mRecentCanvases = new ArrayList<>();
    }

    /**
     * Adds a RecentCanvas to the front of to the list.
     * When the list exceeds the maximum size after the insertion,
     * the last element is dropped.
     *
     * @param rc Instance of RecentCanvas
     */
    public void add(@NonNull RecentCanvas rc) {
        //check if an element with the same name already exists
        RecentCanvas recentCanvas =
                mRecentCanvases.stream().
                        filter(e -> e.mUri.equals(rc.mUri)).
                        findFirst().
                        orElse(null);
        //if it exists move it to the front
        if (recentCanvas != null) {
            mRecentCanvases.remove(recentCanvas);
            recentCanvas.mName = rc.mName;
            recentCanvas.mFileSize = rc.mFileSize;
            mRecentCanvases.add(0, recentCanvas);
            return;
        }

        //add the new element
        mRecentCanvases.add(0, rc);

        //delete the last element if the array exceeded its max size
        if (mRecentCanvases.size() > mMaxSize) {
            mRecentCanvases.remove(mRecentCanvases.size() - 1);
        }
    }

    public RecentCanvas get(int index) {
        return mRecentCanvases.get(index);
    }

    /**
     * Returns the first RecentCanvas found given a filename
     *
     * @param filename String
     * @return Instance of RecentCanvas when found else null
     */
    public RecentCanvas getByFilename(String filename) {
        return mRecentCanvases.stream().
                filter(r -> r.mName.equals(filename)).
                findFirst().
                orElse(null);
    }

    public int size() {
        return mRecentCanvases.size();
    }

    /**
     * Converts the list of RecentCanvas into a specialized format needed for
     * an adapter
     *
     * @return
     */
    public String[][] getFileList() {
        return new String[][]{mRecentCanvases.stream().
                map(e -> e.mName).
                toArray(String[]::new)};
    }

    /**
     * Create an instance of RecentCanvases from a JSON object and a given
     * max size
     *
     * @param json    JSONObject
     * @param maxSize Maximum size of elements
     * @return instance of RecentCanvases
     */
    public static RecentCanvases fromJson(JSONObject json, int maxSize) {
        RecentCanvases recent = new RecentCanvases(0);
        try {
            recent = new RecentCanvases(maxSize);

            JSONArray entries = json.getJSONArray("entries");
            //the elements need to be added in reverse order because they are
            // added to the front when using RecentCanvases.add
            for (int i = entries.length() - 1; i >= 0; i--) {
                RecentCanvas recentCanvas =
                        RecentCanvas.fromJson(entries.getJSONObject(i));
                if (recentCanvas != null) {
                    recent.add(recentCanvas);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return recent;
    }

    /**
     * Create a JSON representation of the data in this class
     *
     * @return Instance of JSONObject
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            JSONArray entries =
                    new JSONArray(mRecentCanvases.stream().
                            map(e -> e.toJson()).
                            collect(Collectors.toList()));
            json.put("entries", entries);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
