package app.notone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
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
import app.notone.core.util.StringUriFixedSizeStack;
import app.notone.fragments.CanvasFragment;
import app.notone.io.CanvasExporter;
import app.notone.io.CanvasFileManager;
import app.notone.io.CanvasImporter;
import app.notone.io.PdfExporter;
import app.notone.views.NavigationDrawer;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static androidx.navigation.Navigation.findNavController;

public class MainActivity extends AppCompatActivity {
    private static final String RECENT_FILES_PREF_KEY = "recentfiles";
    private static CanvasView mCanvasView = null;
    private static String mCanvasName = "Unsaved Doc";
    String TAG = "NotOneMainActivity";
    AppBarConfiguration mAppBarConfiguration;
    NavigationView mNavDrawerContainerNV;
    NavigationDrawer mmainActivityDrawer;

    ExpandableListView mSimpleExpandableListView;
    SimpleExpandableListAdapter mAdapter;
    String[][] mRecentFileNames = {{}};
    private List<List<Map<String, String>>> mChildData;

    boolean mToolbarVisibility = true;
    StringUriFixedSizeStack<String, Uri> mNameUriMap = new StringUriFixedSizeStack<String, Uri>(5);

    ActivityResultLauncher<String> mSavePdfDocument = registerForActivityResult(new ActivityResultContracts.CreateDocument("application/pdf"), uri -> {
        if (uri == null) {
            Log.e(TAG, "mNewCanvasFile: file creation was aborted");
            return;
        }
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        PdfDocument doc = PdfExporter.exportPdfDocument(mCanvasView, (float) metrics.densityDpi / metrics.density, true);
        CanvasFileManager.savePdfDocument(this, uri, doc);
    });

    ActivityResultLauncher<String> mNewCanvasFile = registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json") {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, @NonNull String input) {
            Intent intent = super.createIntent(context, input);
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, getFilesDir()); // inital uri
            return intent;
        }
    }, uri -> {
        if (uri == null) {
            Log.e(TAG, "mNewCanvasFile: file creation was aborted");
            return;
        }
        Log.d(TAG, "mNewCanvasFile: Created a New File at: " + uri);
        String canvasData = CanvasFileManager.initNewFile(uri, 1);
        try {
            CanvasImporter.initCanvasViewFromJSON(canvasData, mCanvasView, true);
        } catch (JSONException e) {
            Log.e(TAG, "new_file: ", e);
        }
        mCanvasView.invalidate();

        persistUriPermission(getIntent(), uri);

        mCanvasName = getCanvasFileName(uri);
        setCanvasTitle(mCanvasName);
        addToRecentFiles(mCanvasName, uri);

        Toast.makeText(this, "created a new file", Toast.LENGTH_SHORT).show();
    });

    ActivityResultLauncher<String[]> mOpenCanvasFile = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
        if (uri == null) {
            Log.e(TAG, "mOpenCanvasFile: file opening was aborted");
            return;
        }
        openCanvasFile(uri);
    });

    private void saveCanvasFile(Uri uri) {
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
        Log.d(TAG, "onCreate: save file: " + uri);
        CanvasFileManager.saveCanvasFile(this, uri, canvasData);

        mCanvasName = getCanvasFileName(uri);
        setCanvasTitle(mCanvasName);

        Toast.makeText(this, "saved file", Toast.LENGTH_SHORT).show();
        return;
    }    ActivityResultLauncher<String> mSaveAsCanvasFile = registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"), uri -> {
        if (uri == null) {
            Log.e(TAG, "mOpenCanvasFile: file creation was aborted");
            return;
        }
        mCanvasName = getCanvasFileName(uri);
        setCanvasTitle(mCanvasName);
        addToRecentFiles(mCanvasName, uri);

        saveCanvasFile(uri);
        mCanvasView.setUri(uri);

        persistUriPermission(getIntent(), uri);
        Toast.makeText(this, " saved file as: " + uri, Toast.LENGTH_SHORT).show();
    });

    private void openCanvasFile(Uri uri) {
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
        mCanvasView.invalidate();

        persistUriPermission(getIntent(), uri);
        mCanvasName = getCanvasFileName(uri);
        setCanvasTitle(mCanvasName);
        addToRecentFiles(mCanvasName, uri);

        Toast.makeText(this, "opened a saved file", Toast.LENGTH_SHORT).show();
    }

    private void addToRecentFiles(String mCanvasName, Uri uri) {
        mNameUriMap.push(mCanvasName, uri);
        Log.d(TAG, "addToRecentFiles: " + mNameUriMap);
    }

    private String getCanvasFileName(Uri uri) {
        String FileName = "";
        String FileSize = "";
        try {
            Cursor returnCursor =
                    getContentResolver().query(uri, null, null, null, null);
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

    private void setCanvasTitle(String title) {
        if (title == null || title.equals("")) {
            Log.e(TAG, "setCanvasTitle: uri is probably empty, save first");
            return;
        }
        TextView tvTitle = ((TextView) findViewById(R.id.tv_fragment_title));
        tvTitle.setText(title);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        if (mNameUriMap.size() != 0) {
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(CanvasFragment.SHARED_PREFS_TAG, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Log.d(TAG, "onPause: storing recent files: " + mNameUriMap.toStringCereal());
            editor.putString(RECENT_FILES_PREF_KEY, mNameUriMap.toStringCereal());

            editor.apply();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: Loading Recent Files");
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(CanvasFragment.SHARED_PREFS_TAG, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        String recentFiles = sharedPreferences.getString(RECENT_FILES_PREF_KEY, "");
        Log.d(TAG, "onCreate: recentFiles: " + recentFiles + mNameUriMap);
        try {
            mNameUriMap = new StringUriFixedSizeStack<String, Uri>(5, sharedPreferences.getString(RECENT_FILES_PREF_KEY, ""));
        } catch (ArrayIndexOutOfBoundsException a) {
            Log.e(TAG, "onCreate: couldnt load recent files");
        }

        List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
        String groupItems[] = {"Recent Files"};

        mRecentFileNames = new String[][]{mNameUriMap.keySet().toArray(new String[0])};
        mChildData = new ArrayList<List<Map<String, String>>>();
        initExpListData(groupItems, groupData, mChildData);

        mSimpleExpandableListView.invalidate();
        mSimpleExpandableListView.setAdapter((ExpandableListAdapter) null);
        mAdapter = createSimpleExpListAdapter(
                new String[] {"Recent Files"});
        mSimpleExpandableListView.setAdapter(mAdapter);
        mSimpleExpandableListView.invalidateViews();
        ((BaseExpandableListAdapter)mAdapter).notifyDataSetInvalidated();
        mAdapter.notifyDataSetChanged();
        super.onResume();
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

        setContentView(R.layout.activity_main);

        requestFileAccessPermission();

        /* set theme Preference on first start if it has never been set before */
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        boolean darkMode = (Configuration.UI_MODE_NIGHT_YES == (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK));
        if (!sharedPreferences.contains("darkmode"))
            spEditor.putBoolean("darkmode", darkMode).apply();
        darkMode = sharedPreferences.getBoolean("darkmode", false);
        AppCompatDelegate.setDefaultNightMode(darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        /* base layouts for all navigations */
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
        swAutoSave.setChecked(sharedPreferences.getBoolean("autosave", false));
//        swSync.setChecked(sharedPreferences.getBoolean("sync", false));
        swAutoSave.setOnCheckedChangeListener((compoundButton, b) -> spEditor.putBoolean("autosave", b).apply());
//        swSync.setOnCheckedChangeListener((compoundButton, b) -> spEditor.putBoolean("sync", b).apply());

        /* populate recents list */
        mSimpleExpandableListView = mNavDrawerContainerNV.getMenu().findItem(R.id.recent_files).getActionView().findViewById(R.id.exp_list_view);
        // string arrays for group and child items
        String groupItems[] = {"Recent Files"};
//        String[][] recentFileNames = {{"Dog", "Cat", "Tiger", "Tiger", "AAA", "EEEEEEEE", "CCCCCC"}};
        mRecentFileNames = new String[][]{{"Dog", "Cat", "Tiger", "Tiger", "AAA", "EEEEEEEE", "CCCCCC"}};
        mAdapter = createSimpleExpListAdapter(groupItems);
        mSimpleExpandableListView.setAdapter(mAdapter);

        mSimpleExpandableListView.setOnGroupClickListener((parent, v, groupPosition, id) -> {
            mSimpleExpandableListView.invalidate();
            mAdapter.notifyDataSetInvalidated();
            mAdapter.notifyDataSetChanged();
            Log.d(TAG, "setOnGroupClickListener: " + Arrays.toString(mRecentFileNames[0]) + " CHILDDATA:" + mChildData);
            if (!parent.isGroupExpanded(groupPosition)) {
                findViewById(R.id.exp_list_view).setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300));
                Log.d(TAG, "setNavigationItemSelectedListener: changed list height");
            } else {
                findViewById(R.id.exp_list_view).setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                Log.d(TAG, "setNavigationItemSelectedListener: changed list height");
            } //groupItems[groupPosition]
            return false;
        });
        mSimpleExpandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            Log.d(TAG, "mSimpleExpandableListView.setOnChildClickListener: " + mRecentFileNames[groupPosition][childPosition] + mNameUriMap.get(mRecentFileNames[groupPosition][childPosition]));
//            openCanvasFile(nameUriMap.get(recentFileNames[groupPosition][childPosition]));
            return false;
        });

        /* FAButton to hide the toolbar */
        FloatingActionButton fabToolbarVisibility = findViewById(R.id.button_toggle_toolbar);
        fabToolbarVisibility.setOnClickListener(view -> {
            toggleToolbarVisibility(appBar, fabToolbarVisibility);
        });

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

    private SimpleExpandableListAdapter createSimpleExpListAdapter(String[] groupItems) {
        // create lists for group and child items
        List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
        mChildData = new ArrayList<List<Map<String, String>>>();

        initExpListData(groupItems, groupData, mChildData);

        // define arrays for displaying data in Expandable list view
        String groupFrom[] = {TAG};
        int groupTo[] = {R.id.listGroupTitle};
        String childFrom[] = {TAG};
        int childTo[] = {R.id.listItemText};

        // Set up the adapter
        return mAdapter = new SimpleExpandableListAdapter(this, groupData,
                R.layout.list_group,
                groupFrom, groupTo,
                mChildData, R.layout.list_item,
                childFrom, childTo) {
            @Override
            public void registerDataSetObserver(DataSetObserver observer) {
                super.registerDataSetObserver(observer);
                Log.d(TAG, "registerDataSetObserver: DATA changed");
            }

            @Override
            public void notifyDataSetChanged() {
                Log.d(TAG, "notifyDataSetChanged: AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                super.notifyDataSetChanged();
            }
        };
    }

    private void initExpListData(String[] groupItems, List<Map<String, String>> groupData, List<List<Map<String, String>>> childData) {
        // add data in group and child list
        for (int i = 0; i < groupItems.length; i++) {
            Map<String, String> curGroupMap = new HashMap<String, String>();
            groupData.add(curGroupMap);
            curGroupMap.put(TAG, groupItems[i]);

            List<Map<String, String>> children = new ArrayList<Map<String, String>>();
            for (int j = 0; j < mRecentFileNames[i].length; j++) {
                Map<String, String> curChildMap = new HashMap<String, String>();
                children.add(curChildMap);
                curChildMap.put(TAG, mRecentFileNames[i][j]);
            }
            childData.add(children);
        }
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
            appBar.animate().translationY(-appBar.getHeight());
            fabToolbarVisibility.animate().translationY(-appBar.getHeight());
            fabToolbarVisibility.animate().rotation(180);
        } else {
            mToolbarVisibility = true;
            appBar.animate().translationY(0);
            fabToolbarVisibility.animate().translationY(0);
            fabToolbarVisibility.animate().rotation(0);
        }
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

    /**
     * Handle Navigation from the drawer menu with navcontoller
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        NavController navController = Navigation.findNavController(this, R.id.nav_main_host_fragment);
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }

    private void requestFileAccessPermission() {
        // requesting permissions if not provided.
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, 200);

    }

    private boolean checkFileAccessPermission() {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    //    @SuppressLint("WrongConstant") // breaks it ?!
    private void persistUriPermission(Intent intent, Uri uri) {
        int takeFlags = intent.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
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


}