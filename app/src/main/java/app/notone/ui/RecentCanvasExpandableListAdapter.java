package app.notone.ui;

import android.content.Context;
import android.widget.SimpleExpandableListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.notone.R;

/**
 * @author default-student
 * @since 202212XX
 */


public class RecentCanvasExpandableListAdapter {
    private final static String TAG = "BaseExpandableListAdapter";

    public static SimpleExpandableListAdapter getRecentCanvasExpListAdapter(Context context, String[][] recentFileNames) { // TODO switch to constructor
        // create lists for group and child items
        String[] groupItems = {"Recent Files"};
        List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
        List<List<Map<String, String>>> listItemData = new ArrayList<List<Map<String, String>>>();

        // add data in group and child list
        for (int i = 0; i < groupItems.length; i++) {
            Map<String, String> curGroupMap = new HashMap<String, String>();
            groupData.add(curGroupMap);
            curGroupMap.put(TAG, groupItems[i]);

            List<Map<String, String>> children = new ArrayList<Map<String, String>>();
            for (int j = 0; j < recentFileNames[i].length; j++) {
                Map<String, String> curChildMap = new HashMap<String, String>();
                children.add(curChildMap);
                curChildMap.put(TAG, recentFileNames[i][j]);
            }
            listItemData.add(children);
        }

        // define arrays for displaying data in Expandable list view
        String[] groupFrom = {TAG};
        int[] groupTo = {R.id.listGroupTitle};
        String[] childFrom = {TAG};
        int[] childTo = {R.id.listItemText};

        // Set up and return adapter
        return new SimpleExpandableListAdapter(context, groupData,
                R.layout.exp_list_group,
                groupFrom, groupTo,
                listItemData, R.layout.exp_list_item,
                childFrom, childTo);
    }
}
