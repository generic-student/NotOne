package app.notone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import app.notone.core.CanvasView;
import app.notone.core.util.RecentCanvas;
import app.notone.core.util.RecentCanvases;
import app.notone.core.util.SettingsHolder;
import app.notone.fragments.CanvasFragment;
import app.notone.io.CanvasExporter;
import app.notone.io.CanvasFileManager;
import app.notone.io.CanvasImporter;
import app.notone.views.NavigationDrawer;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static androidx.navigation.Navigation.findNavController;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "NotOneMainActivity";
    public static CanvasView mCanvasView = null;
    protected static String mCanvasName = "Unsaved Doc";
    AppBarConfiguration mAppBarConfiguration;
    NavigationView mNavDrawerContainerNV;
    NavigationDrawer mmainActivityDrawer;
    ExpandableListView mSimpleExpandableListView;
    SimpleExpandableListAdapter mAdapter;

    private static final String RECENT_FILES_PREF_KEY = "recentfiles";
    public static RecentCanvases sRecentCanvases = new RecentCanvases(4);

    boolean mToolbarVisibility = true;

    ActivityResultLauncher<String> mSavePdfDocument = ActivityResultLauncherFactory.getNewPdfDocumentActivityResultLauncher(this);

    ActivityResultLauncher<String> mNewCanvasFile = ActivityResultLauncherFactory.getNewCanvasFileActivityResultLauncher(this);

    ActivityResultLauncher<String[]> mOpenCanvasFile = ActivityResultLauncherFactory.getOpenCanvasFileActivityResultLauncher(this);

    ActivityResultLauncher<String> mSaveAsCanvasFile = ActivityResultLauncherFactory.getSaveAsCanvasFileActivityResultLauncher(this);

//region File stuff
    void saveCanvasFile(Uri uri) {
        Log.d(TAG, "mSaveAsCanvasFile to Json");
        // check for file access permissions || grant them, persistUriPermission() doesnt seem to work
        if (!checkFileAccessPermission()) {
            Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "saveCanvasFile: Permissions not granted");
            return;
        }
        try {
            getApplicationContext().grantUriPermission(getApplicationContext().getPackageName(), uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } catch (SecurityException se) {
            /* permission did not persist; user has to chose which file to override */
            Log.e(TAG, "saveCanvasFile: Failed to save file as it cant be accessed");
            mSaveAsCanvasFile.launch("canvasFile.json"); // this is recursive
            return;
        }

        // save files
        String canvasData = "";
        try {
            canvasData = CanvasExporter.canvasViewToJSON(mCanvasView, true).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CanvasFileManager.saveCanvasFile(this, uri, canvasData);

        mCanvasName = getCanvasFileName(uri, getContentResolver());
        setCanvasTitle(mCanvasName);

        Toast.makeText(this, "saved file", Toast.LENGTH_SHORT).show();
        return;
    }

    void openCanvasFile(Uri uri) {
        mCanvasView = CanvasFragment.mCanvasView;
        Log.d(TAG, "mOpenCanvasFile: Open File at: " + uri);

        String canvasData = CanvasFileManager.openCanvasFile(this, uri);
        try {
            CanvasImporter.initCanvasViewFromJSON(canvasData, mCanvasView, true);
        } catch (JSONException e) {
            Log.e(TAG, "mOpenCanvasFile: failed to open ", e);
            Toast.makeText(this, "failed to parse file", Toast.LENGTH_SHORT).show();
            return;
        } catch (IllegalArgumentException i) {
            Log.e(TAG, "mOpenCanvasFile: canvasFile was empty");
            Toast.makeText(this, "file is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        //reorder the recent canvases to have the active one as the first element

        mCanvasView.invalidate();

        persistUriPermission(getIntent(), uri);
        mCanvasName = getCanvasFileName(uri, getContentResolver());
        setCanvasTitle(mCanvasName);
        addToRecentFiles(mCanvasName, uri);
        updateExpListRecentFiles();

        Toast.makeText(this, "opened a saved file", Toast.LENGTH_SHORT).show();
    }

    protected static String getCanvasFileName(Uri uri, ContentResolver contentResolver) {
        String FileName = "";
        String FileSize = "";
        try {
            Cursor returnCursor =
                    contentResolver.query(uri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            FileName = returnCursor.getString(nameIndex);
            FileSize = Long.toString(returnCursor.getLong(sizeIndex));
        } catch (NullPointerException n) {
            Log.e(TAG, "getCanvasFileName: couldnt extract Filename from uri");
            FileName = "Unsaved Document";
            FileSize = "0";
        }
        return FileName + " : " + FileSize;
    }

    void setCanvasTitle(String title) {
        if (title == null || title.equals("")) {
            Log.e(TAG, "setCanvasTitle: uri is probably empty, save first");
            return;
        }
        TextView tvTitle = ((TextView) findViewById(R.id.tv_fragment_title));
        tvTitle.setText(title);
    }

    private void saveRecentCanvases() {
        if (sRecentCanvases.size() != 0) {
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(CanvasFragment.SHARED_PREFS_TAG, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            //Log.d(TAG, "onPause: storing recent files: " + mNameUriMap.toStringCereal());
            String recentCanvasesString = sRecentCanvases.toJson().toString();
            Log.d(TAG, "onPause: storing recent files: " + recentCanvasesString);
            //editor.putString(RECENT_FILES_PREF_KEY, mNameUriMap.toStringCereal());
            editor.putString(RECENT_FILES_PREF_KEY, recentCanvasesString);

            editor.apply();
        }
    }

    void addToRecentFiles(String mCanvasName, Uri uri) {
        //mNameUriMap.push(mCanvasName, uri);
        sRecentCanvases.add(new RecentCanvas(mCanvasName, uri, 0));
        //Log.d(TAG, "addToRecentFiles: " + mCanvasName + mNameUriMap);
        Log.d(TAG, "addToRecentFiles: " + mCanvasName + sRecentCanvases);
    }

    void updateExpListRecentFiles() {
        //String[][] recentFileList = new String[][]{mNameUriMap.keySet().toArray(new String[0])};
        String[][] recentFileList = sRecentCanvases.getFileList();
        Log.d(TAG, "updateExpListRecentFiles: " + Arrays.deepToString(recentFileList));
        mSimpleExpandableListView.invalidate();
        mSimpleExpandableListView.setAdapter((ExpandableListAdapter) null);
        mAdapter = createSimpleExpListAdapterForRecentFiles(
                recentFileList);
        mSimpleExpandableListView.setAdapter(mAdapter);
        mSimpleExpandableListView.invalidateViews();
        ((BaseExpandableListAdapter) mAdapter).notifyDataSetInvalidated();
        mAdapter.notifyDataSetChanged();

        saveRecentCanvases();
    }
    private void requestFileAccessPermission() {
        // requesting permissions if not provided.
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, 200);

    }

    private boolean checkFileAccessPermission() {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }
    //    @SuppressLint("WrongConstant") // breaks it ?!
    @SuppressLint("WrongConstant")
    protected void persistUriPermission(Intent intent, Uri uri) {
        //int takeFlags = intent.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        // Check for the freshest data.
        getContentResolver().takePersistableUriPermission(uri, takeFlags);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {
            if (grantResults.length > 0) {

                // after requesting permissions we are showing
                // users a toast message of permission granted.
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (writeStorage && readStorage) {
                    Toast.makeText(this, "Permission Granted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }
//endregion

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        /* save recent files */
        //if (mNameUriMap.size() != 0) {
        SettingsHolder.update(PreferenceManager.getDefaultSharedPreferences(this));
        saveRecentCanvases();
        super.onPause();
    }

    @Override
    protected void onStart() {
        /* reload recent files */
        SettingsHolder.update(PreferenceManager.getDefaultSharedPreferences(this));
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(CanvasFragment.SHARED_PREFS_TAG, MODE_PRIVATE);
        String recentFiles = sharedPreferences.getString(RECENT_FILES_PREF_KEY, "");
        //Log.d(TAG, "onResume: recentFiles: " + recentFiles + "replace old map: " + mNameUriMap);
        Log.d(TAG, "onResume: recentFiles: " + recentFiles + "replace old map: " + sRecentCanvases);
        try {
            //mNameUriMap = new StringUriFixedSizeStack(4, recentFiles);
            sRecentCanvases = RecentCanvases.fromJson(new JSONObject(recentFiles), 4);
        } catch (ArrayIndexOutOfBoundsException | JSONException a) {
            Log.e(TAG, "onCreate: couldnt load recent files");
        }

        updateExpListRecentFiles();
        super.onStart();
    }

    /**
     * Main onCreate of the App
     * Set the Main Activity View
     * Burger Menu and Title State
     * Fragment Navigation
     * Toolbar state
     *
     * @param savedInstanceState
     */
    @SuppressLint({"NonConstantResourceId", "UseSwitchCompatOrMaterialCode"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate Main");
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_main);

        requestFileAccessPermission();

        FirebaseStorage.getInstance().useEmulator("192.168.178.49", 9199);

        /* set theme Preference on first start if it has never been set before */
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();

        boolean darkMode = (Configuration.UI_MODE_NIGHT_YES == (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK));
        if (!sharedPreferences.contains("darkmode"))
            spEditor.putBoolean("darkmode", darkMode).apply();
        SettingsHolder.update(sharedPreferences);

        darkMode = SettingsHolder.isDarkMode();
        AppCompatDelegate.setDefaultNightMode(darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        /* init layouts and navigation stuff */
        mmainActivityDrawer = findViewById(R.id.drawer_activity_main); // main base layout
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_main_host_fragment); // container of the fragments
        Toolbar toolbar = findViewById(R.id.toolbar); // toolbar
        AppBarLayout appBar = findViewById(R.id.AppBar); // toolbar container
        mNavDrawerContainerNV = findViewById(R.id.navdrawercontainer_view); // drawer menu container
        NavController navGraphController = navHostFragment.getNavController(); // nav_graph of the app

        /* configure AppBar with burger and title */
        setSupportActionBar(toolbar);
        mAppBarConfiguration = new AppBarConfiguration.Builder(navGraphController.getGraph()) // getGraph => topLevelDestinations
                .setOpenableLayout(mmainActivityDrawer) // setDrawerLayout // define burger button for toplevel
                .build();

        NavigationUI.setupActionBarWithNavController(this, navGraphController, mAppBarConfiguration); // add titles and burger from nav_graph to actionbar otherwise there will be the app title and no burger!
        NavigationUI.setupWithNavController(mNavDrawerContainerNV, navGraphController); // this will call onNavDestination(Selected||Changed) when a menu item is selected.

        /* catch menu clicks for setting actions, forward clicks to the navController for destination change */
        mNavDrawerContainerNV.setNavigationItemSelectedListener(menuItem -> {
            mCanvasView = CanvasFragment.mCanvasView;

            /* handle action button clicks */
            if (mCanvasView == null) {
                Log.e(TAG, "onCreate: Canvasview has not been initalized");
                return false;
            }
            switch (menuItem.getItemId()) {
                /* create a new file at a chosen uri and open it in the current canvas */
                case R.id.new_file:
                    Log.d(TAG, "onNavigationItemSelected: New File");
                    mNewCanvasFile.launch("canvasFile.json");
                    return false;
                /* chose a existing file with uri and open it in the current canvas */
                case R.id.open_file:
                    mOpenCanvasFile.launch(new String[]{"application/json"});
                    return false;
                /* save file to existing uri of current view */
                case R.id.save_file:
                    if (mCanvasView.getCurrentURI().equals(Uri.parse(""))) { // shouldnt happen
                        /* save as to new uri (should not happen as there shouldnt be any current canvases without uri) */
                        mSaveAsCanvasFile.launch("canvasFile.json");
                        return false;
                    }
                    saveCanvasFile(mCanvasView.getCurrentURI());
                    return false;
                /* export a file to pdf */
                case R.id.export:
                    mSavePdfDocument.launch("exported.pdf");
                    Log.i(TAG, "onNavigationItemSelected: Export to pdf");
                    return false;
                case R.id.recent_files:
                    return false;
            }


            /* forward click to the navigation controller if a navigation item is clicked*/
            if (navGraphController.getGraph().findNode(menuItem.getItemId()) != null) {
                navGraphController.navigate(menuItem.getItemId()); // onOptionsItemSelected
                mmainActivityDrawer.closeDrawers();
            }
            return true;
        });
        /* set state of the drawer quick settings */
        Switch swAutoSave = mNavDrawerContainerNV.getMenu().findItem(R.id.drawer_switch_autosave).getActionView().findViewById(R.id.menu_switch);
//        Switch swSync = mNavDrawerContainerNV.getMenu().findItem(R.id.drawer_switch_sync).getActionView().findViewById(R.id.menu_switch);
        swAutoSave.setChecked(SettingsHolder.isAutoSaveCanvas());
//        swSync.setChecked(sharedPreferences.getBoolean("sync", false));
        swAutoSave.setOnCheckedChangeListener((compoundButton, b) -> {
            spEditor.putBoolean("autosave", b).apply();
            SettingsHolder.update(sharedPreferences);
        });
//        swSync.setOnCheckedChangeListener((compoundButton, b) -> spEditor.putBoolean("sync", b).apply());

        /* populate recents list */
        mSimpleExpandableListView = mNavDrawerContainerNV.getMenu().findItem(R.id.recent_files).getActionView().findViewById(R.id.exp_list_view);
        // string arrays for group and child items
        String[][] mrecentfilenames = new String[][]{{"Default Value"}};
        mAdapter = createSimpleExpListAdapterForRecentFiles(mrecentfilenames);
        mSimpleExpandableListView.setAdapter(mAdapter);

        mSimpleExpandableListView.setOnGroupClickListener((parent, v, groupPosition, id) -> {

            Log.d(TAG, "setOnGroupClickListener: " + Arrays.toString(mrecentfilenames[0]));
            if (!parent.isGroupExpanded(groupPosition)) {
                findViewById(R.id.exp_list_view).setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300));
                Log.d(TAG, "setNavigationItemSelectedListener: changed list height");
            } else {
                findViewById(R.id.exp_list_view).setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                Log.d(TAG, "setNavigationItemSelectedListener: changed list height");
            }
            return false;
        });
        mSimpleExpandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            String filename = mAdapter.getChild(groupPosition, childPosition).toString().replaceAll("\\{([A-Z])\\w+=", "").replaceAll("\\}", "");
            Log.d(TAG, "mSimpleExpandableListView.setOnChildClickListener: " + filename + sRecentCanvases.getByFilename(filename).mUri);
            openCanvasFile(sRecentCanvases.getByFilename(filename).mUri);
            findViewById(R.id.exp_list_view).setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return false;
        });

        /* FAButton to hide the toolbar */
        FloatingActionButton fabToolbarVisibility = findViewById(R.id.button_toggle_toolbar);
        fabToolbarVisibility.setOnClickListener(view -> {
            toggleToolbarVisibility(appBar, fabToolbarVisibility);
        });

        /* set toolbar padding depending on splitscreen */
        if (isInMultiWindowMode()) {
            appBar.setPadding(0, 0, 0, 0);

            float density = getApplicationContext().getResources().getDisplayMetrics().density;
            CoordinatorLayout.LayoutParams lp = new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.WRAP_CONTENT, CoordinatorLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.END;
            lp.setMargins(0, (int) (78 * density), (int) (16 * density), 0);
            fabToolbarVisibility.setLayoutParams(lp);
        }
        /* set Title and Toolbar functions depending on Fragment */
        navGraphController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            TextView tvTitle = ((TextView) findViewById(R.id.tv_fragment_title));
            LinearLayout viewCanvasToolsContainer = findViewById(R.id.canvas_tools_container);
            LinearLayout viewUnRedo = findViewById(R.id.canvas_tools_unredo);
            switch (destination.getId()) {
                case R.id.canvas_fragment:
                    findViewById(R.id.button_toggle_toolbar).setVisibility(View.VISIBLE);
                    viewCanvasToolsContainer.setVisibility(View.VISIBLE);
                    viewUnRedo.setVisibility(View.VISIBLE);
                    tvTitle.setVisibility(View.VISIBLE);
                    tvTitle.setText(mCanvasName); // TODO replace with open document name
                    return; // dont reset toolbar

                case R.id.settings_fragment:
                case R.id.about_fragment:
                    findViewById(R.id.button_toggle_toolbar).setVisibility(View.GONE);
                    viewCanvasToolsContainer.setVisibility(View.GONE);
                    viewUnRedo.setVisibility(View.GONE);
                    tvTitle.setVisibility(View.GONE);
                    break;
                default:
                    throw new IllegalStateException("Destination changed to unexpected value: " + destination.getId());
            }
            mToolbarVisibility = false; // to toggle to right state
            toggleToolbarVisibility(appBar, fabToolbarVisibility);
        });
    }

    /**
     * Handle Navigation from the drawer menu with navcontoller
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        NavController navController = Navigation.findNavController(this, R.id.nav_main_host_fragment);
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }

    /**
     * Back Navigation via Android Button and Back Arrow in toolbar gets handled here
     */
    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "onSupportNavigateUp");
        NavController navGraphController = findNavController(this, R.id.nav_main_host_fragment);
        switch (navGraphController.getCurrentDestination().getId()) {
            case R.id.settings_fragment:
            case R.id.about_fragment:
                Log.d(TAG, "onOptionsItemSelected: Navigate Up");
                navGraphController.navigateUp();
                return true;

            case R.id.canvas_fragment:
                mmainActivityDrawer.openDrawer(GravityCompat.START);
        }
        return navGraphController.navigateUp() || super.onSupportNavigateUp();
    }

    private SimpleExpandableListAdapter createSimpleExpListAdapterForRecentFiles(
            String[][] recentFileNames) {
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

        // Set up the adapter
        return mAdapter = new SimpleExpandableListAdapter(this, groupData,
                R.layout.list_group,
                groupFrom, groupTo,
                listItemData, R.layout.list_item,
                childFrom, childTo) {
        };
    }

    /**
     * for the fab that hides the toolbar
     *
     * @param appBar
     * @param fabToolbarVisibility
     */
    private void toggleToolbarVisibility(AppBarLayout appBar, FloatingActionButton fabToolbarVisibility) {
        if (mToolbarVisibility) {
            mToolbarVisibility = false;
            int offset = 39; // add a offset to the toolbar height, as its partly hidden behind the android statusbar
            if (isInMultiWindowMode()) {
                offset = 0;
            }
            appBar.animate().translationY(-appBar.getHeight() + offset);
            fabToolbarVisibility.animate().translationY(-appBar.getHeight() + offset);
            fabToolbarVisibility.animate().rotation(180);
        } else {
            mToolbarVisibility = true;
            appBar.animate().translationY(0);
            fabToolbarVisibility.animate().translationY(0);
            fabToolbarVisibility.animate().rotation(0);
        }
    }
}