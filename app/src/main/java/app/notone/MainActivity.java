package app.notone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
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
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import app.notone.core.PeriodicSaveHandler;
import app.notone.core.util.RecentCanvases;
import app.notone.core.util.SettingsHolder;
import app.notone.ui.fragments.CanvasFragment;
import app.notone.io.FileManager;
import app.notone.ui.ActivityResultLauncherProvider;
import app.notone.ui.NavigationDrawer;
import app.notone.ui.RecentCanvasSimpleExpandableListAdapterBuilder;

import static androidx.navigation.Navigation.findNavController;

/**
 * @author Luca Hackel
 * @since 202212XX
 */

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "NotOneMainActivity";

/* Programmatic Layout Variables */
    /* the view that contains the navdrawer menu */
    private NavigationView mNavDrawerView;
    /* the main activity of the app that contains the drawer and fragments */
    private NavigationDrawer mMainDrawerActivity;
    /* the expandable list view that contains the recent canvases */
    private ExpandableListView mSimExpListView;
    /* the adapter that holds the data for the expandable listview */
    private SimpleExpandableListAdapter mAdapter;

    /* contains the name of the current canvas used as title */
    public static String sCanvasName = "Unsaved Doc";
    /* contains the recently opened canvases */
    public static RecentCanvases sRecentCanvases = new RecentCanvases(4);
    /* if the toolbar is visible or hidden by the fab */
    boolean mToolbarVisibility = true;

//region Persistence

/* Persistence */
    /* holds the shared pref key for the recent canvases */
    private static final String RECENT_CANVASES_LIST_PREF_KEY = "recentfiles";

    /**
     * Store the recent canvases in shared prefs as json
     */
    private static void saveRecentCanvases2SharedPreferences(Context context) {
        if (MainActivity.sRecentCanvases.size() != 0) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(CanvasFragment.SHARED_PREFS_TAG, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String recentCanvasesString = sRecentCanvases.toJson().toString();

            Log.d(TAG, "onPause: storing recent files: " + recentCanvasesString);
            editor.putString(RECENT_CANVASES_LIST_PREF_KEY, recentCanvasesString);
            editor.apply();
        }
    }

    /**
     * load the recent canvases from the shared prefs
     */
    private static void loadRecentCanvasesFromSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(CanvasFragment.SHARED_PREFS_TAG, MODE_PRIVATE);
        String recentCanvasList = sharedPreferences.getString(RECENT_CANVASES_LIST_PREF_KEY, "");
        Log.d(TAG, "onResume: recentCanvasList: " + recentCanvasList + "replace old map: " + sRecentCanvases);
        try {
            sRecentCanvases = RecentCanvases.fromJson(new JSONObject(recentCanvasList), 4);
        } catch (ArrayIndexOutOfBoundsException | JSONException a) {
            Log.e(TAG, "onCreate: couldnt load recent files");
        }
    }

//endregion

//region Navigation Callbacks

    /**
     * Set up call back receivers that handle the ui for file handling
     */
    /* Callback Functions */
    private final ActivityResultLauncher<String> mSavePdfDocument = ActivityResultLauncherProvider.getExportPdfActivityResultLauncher(this);
    private final ActivityResultLauncher<String> mNewCanvasFile = ActivityResultLauncherProvider.getNewCanvasFileActivityResultLauncher(this);
    private final ActivityResultLauncher<String[]> mOpenCanvasFile = ActivityResultLauncherProvider.getOpenCanvasFileActivityResultLauncher(this);
    public final ActivityResultLauncher<String> mSaveAsCanvasFile = ActivityResultLauncherProvider.getSaveAsCanvasFileActivityResultLauncher(this);

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
                mMainDrawerActivity.openDrawer(GravityCompat.START);
        }
        return navGraphController.navigateUp() || super.onSupportNavigateUp();
    }

    /**
     * receive the result of a permission request and show the user the result
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {
            if (grantResults.length > 0) {
                boolean writeStoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStoragePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (writeStoragePermission && readStoragePermission) {
                    Toast.makeText(this, "Permissions Granted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission(s) Denied.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

//endregion

//region User Interface Setters

    /**
     * Set the Title in the Toolbar
     * shows the filename
     *
     * @param title usually the filename
     */
    public void setToolbarTitle(String title) {
        if (title == null || title.equals("")) {
            Log.e(TAG, "setToolbarTitle: uri is probably empty, save first");
            return;
        }
        TextView tvTitle = ((TextView) findViewById(R.id.tv_fragment_title));
        tvTitle.setText(title);
    }

    /**
     * Update the Files shown in the recent Canvases List view
     */
    public void updateRecentCanvasesExpListView() {
        String[][] recentCanvasList = sRecentCanvases.getFileList();
        Log.d(TAG, "updateExpListRecentFiles: " + Arrays.deepToString(recentCanvasList));
        mSimExpListView.invalidate();
        mSimExpListView.setAdapter((ExpandableListAdapter) null);
        mAdapter = RecentCanvasSimpleExpandableListAdapterBuilder.build(this, recentCanvasList);
        mSimExpListView.setAdapter(mAdapter);
        mSimExpListView.invalidateViews();
        ((BaseExpandableListAdapter) mAdapter).notifyDataSetInvalidated();
        mAdapter.notifyDataSetChanged();

        saveRecentCanvases2SharedPreferences(getApplicationContext());
    }

    /**
     * Toggle the visbility of the toolbar
     * Animate the transition and icon
     * Adapt to multi-window mode
     * used by the FAB
     */
    private void toggleToolbarVisibilityResponsively(AppBarLayout appBar, FloatingActionButton fabToolbarVisibility) {
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

//endregion

//region ANDROID Lifecycle

    /**
     * update Settings in the SettingsHolder
     * Store Recent Canvases List to SharedPreferences
     */
    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        SettingsHolder.update(PreferenceManager.getDefaultSharedPreferences(this));
        saveRecentCanvases2SharedPreferences(getApplicationContext());

        /* collapse explistview */
        mSimExpListView.collapseGroup(0);
        super.onPause();
    }

    /**
     * update Settings in the SettingsHolder
     * Reload Recent Canvases List from SharedPreferences
     */
    @Override
    protected void onStart() {
        SettingsHolder.update(PreferenceManager.getDefaultSharedPreferences(this));

        /* reload recent files */
        loadRecentCanvasesFromSharedPreferences(getApplicationContext());
        updateRecentCanvasesExpListView();

        /* collapse explistview */
        mSimExpListView.collapseGroup(0);
        super.onStart();
    }

    /**
     * Contains the Ui logic of the basic app framework
     * each fragment has additonal ui code
     *
     * Main onCreate of the App
     * init filestorage
     * set theme preference on first start
     * configure appbar
     * handle drawer menu clicks
     * drawer quick settings state
     * recent canvases list
     * fabbutton to hide toolbar
     * toolbar padding
     *
     * Main resource for implementation was:
     * https://developer.android.com/guide/navigation/navigation-ui
     * Additional information where found under:
     * https://developer.android.com/develop/ui/views/components/appbar
     * https://developer.android.com/reference/androidx/navigation/ui/NavigationUI
     * https://developer.android.com/guide/navigation/navigation-getting-started
     * Any missing info where covered with lots of invested time.
     *
     */
    @SuppressLint({"NonConstantResourceId", "UseSwitchCompatOrMaterialCode"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);


        /* Init File Storage */
        FileManager.requestFileAccessPermission(this);
        FirebaseStorage.getInstance();//.useEmulator("192.168.178.49", 9199); // debug database



        /* Set theme Preference on first start if it has never been set before */
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();

        boolean darkModeActive = (Configuration.UI_MODE_NIGHT_YES == (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)); // get system darkmode config
        if (!sharedPreferences.contains("darkmode")) {
            spEditor.putBoolean("darkmode", darkModeActive).apply();
        }
        SettingsHolder.update(sharedPreferences);

        darkModeActive = SettingsHolder.isDarkMode();
        AppCompatDelegate.setDefaultNightMode(darkModeActive ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);



        /* Init layouts and navigation vars */
        mMainDrawerActivity = findViewById(R.id.drawer_activity_main); // main base layout
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_main_host_fragment); // container of the fragments
        NavController navGraphController = navHostFragment.getNavController(); // nav_graph of the app
        mNavDrawerView = findViewById(R.id.navdrawercontainer_view); // drawer menu container
        AppBarLayout toolbarContainer = findViewById(R.id.AppBar); // toolbar container
        Toolbar toolbar = findViewById(R.id.toolbar); // toolbar



        /* Configure AppBar with burger button and title
        * getGraph enables finding of the top level destinations,
        * which have a burger button.
        * the NavController DestinationChange Callback gets activated
        * */
        setSupportActionBar(toolbar);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navGraphController.getGraph())
                .setOpenableLayout(mMainDrawerActivity) // setDrawerLayout // define burger button for toplevel
                .build();
        NavigationUI.setupActionBarWithNavController(this, navGraphController, appBarConfiguration); // add titles and burger from nav_graph to actionbar otherwise there will be the app title and no burger!
        NavigationUI.setupWithNavController(mNavDrawerView, navGraphController); // this will call onNavDestination(Selected||Changed) when a menu item is selected.



        /* Handle drawer menu clicks for file and quick settings actions,
         forward clicks to the navController for destination change */
        mNavDrawerView.setNavigationItemSelectedListener(menuItem -> {

            /* handle action button clicks */
            if (CanvasFragment.sCanvasView == null) {
                Log.e(TAG, "onCreate: Canvasview has not been initalized");
                return false;
            }
            switch (menuItem.getItemId()) {
                case R.id.open_server_file:
                    final Uri firebaseUri = Uri.parse("firebase");
                    //CanvasFragment.sFlags.setOpenFile(true);
                    FileManager.openCanvasFileFromUri(this, firebaseUri);
                    return false;
                /* create a new file at a chosen uri and open it in the current canvas */
                case R.id.new_file:
                    Log.d(TAG, "onNavigationItemSelected: New File");
                    //CanvasFragment.sFlags.setNewFile(true);
                    mNewCanvasFile.launch("canvasFile.json");
                    return false;
                /* chose a existing file with uri and open it in the current canvas */
                case R.id.open_file:
                    //CanvasFragment.sFlags.setOpenFile(true);
                    mOpenCanvasFile.launch(new String[]{"application/json"});
                    return false;
                /* save file to existing uri of current view */
                case R.id.save_file:
                    //check if the canvas has a uri.
                    //Open the 'save as' dialog if the canvas does not have a uri.
                    if (CanvasFragment.sCanvasView.getCurrentURI().equals(Uri.parse(""))) {
                        /* save as to new uri (should not happen as there shouldnt be any current canvases without uri) */
                        mSaveAsCanvasFile.launch("canvasFile.json");
                        return false;
                    }
                    FileManager.saveCanvasFileToUri(this, CanvasFragment.sCanvasView.getCurrentURI());
                    //CanvasFileManager.safeSave(this, getApplicationContext(), CanvasFragment.sCanvasView.getCurrentURI(), CanvasFragment.sCanvasView);
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
                mMainDrawerActivity.closeDrawers();
            }
            return true;
        });



        /* Set state of the drawer quick settings */
        Switch swAutoSave = mNavDrawerView.getMenu().findItem(R.id.drawer_switch_autosave).getActionView().findViewById(R.id.menu_switch);
        swAutoSave.setChecked(SettingsHolder.shouldAutoSaveCanvas());
        swAutoSave.setOnCheckedChangeListener((compoundButton, autoSave) -> {
            spEditor.putBoolean("autosave", autoSave).apply();
            SettingsHolder.update(sharedPreferences);

            if (!PeriodicSaveHandler.isInitialized()) {
                PeriodicSaveHandler.init(this);
            }

            if (autoSave) {
                PeriodicSaveHandler.getInstance().start();
            } else {
                PeriodicSaveHandler.getInstance().stop();
            }
        });
//        Not a MVP Feature
//        Switch swSync = mNavDrawerContainerNV.getMenu().findItem(R.id.drawer_switch_sync).getActionView().findViewById(R.id.menu_switch);
//        swSync.setChecked(sharedPreferences.getBoolean("sync", false));
//        swSync.setOnCheckedChangeListener((compoundButton, b) -> spEditor.putBoolean("sync", b).apply());



        /* Setup recent Canvases list */
        mSimExpListView = mNavDrawerView.getMenu().findItem(R.id.recent_files).getActionView().findViewById(R.id.exp_list_view);
        String[][] initialRecentFileNames = new String[][]{{"Default Value"}}; // string arrays for child entries
        mAdapter = RecentCanvasSimpleExpandableListAdapterBuilder.build(this, initialRecentFileNames);
        mSimExpListView.setAdapter(mAdapter);

        mSimExpListView.collapseGroup(0);

        mSimExpListView.setOnGroupClickListener((parent, v, groupPosition, id) -> {
            Log.d(TAG, "setOnGroupClickListener: " + Arrays.toString(initialRecentFileNames[0]));
            if (!parent.isGroupExpanded(groupPosition)) {
                findViewById(R.id.exp_list_view).setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 310));
                Log.d(TAG, "setNavigationItemSelectedListener: changed list height");
            } else {
                findViewById(R.id.exp_list_view).setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                Log.d(TAG, "setNavigationItemSelectedListener: changed list height");
            }
            return false;
        });

        mSimExpListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            String filename = mAdapter.getChild(groupPosition, childPosition).toString().replaceAll("\\{([A-Z])\\w+=", "").replaceAll("\\}", "");
            Log.d(TAG, "mSimpleExpandableListView.setOnChildClickListener: " + filename + sRecentCanvases.getByFilename(filename).mUri);
            FileManager.openCanvasFileFromUri(this, sRecentCanvases.getByFilename(filename).mUri);

            findViewById(R.id.exp_list_view).setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return false;
        });



        /* Setup Action of FloatingActionButton to hide the toolbar */
        FloatingActionButton fabToolbarVisibility = findViewById(R.id.button_toggle_toolbar);
        fabToolbarVisibility.setOnClickListener(view -> {
            toggleToolbarVisibilityResponsively(toolbarContainer, fabToolbarVisibility);
        });



        /* Set toolbar padding depending on splitscreen state */
        // in multiwindow mode the screen has a clear border and cant be behind system ui
        if (isInMultiWindowMode()) {
            toolbarContainer.setPadding(0, 0, 0, 0);

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
                    tvTitle.setText(sCanvasName); // TODO replace with open document name
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
            toggleToolbarVisibilityResponsively(toolbarContainer, fabToolbarVisibility);
        });
    }
//endregion
}