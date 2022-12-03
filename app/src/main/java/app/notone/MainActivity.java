package app.notone;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import app.notone.core.CanvasView;
import app.notone.fragments.CanvasFragment;
import app.notone.io.CanvasExporter;
import app.notone.io.CanvasFileManager;
import app.notone.io.CanvasImporter;
import app.notone.io.PdfExporter;
import app.notone.io.PdfImporter;
import app.notone.views.NavigationDrawer;

import static androidx.navigation.Navigation.findNavController;

public class MainActivity extends AppCompatActivity {
    String TAG = "NotOneMainActivity";
    AppBarConfiguration mAppBarConfiguration;
    NavigationView mNavDrawerContainerNV;
    boolean mToolbarVisibility = true;
    NavigationDrawer mmainActivityDrawer;
    private Uri mUri;
    public static CanvasView mCanvasView = null;

    ActivityResultLauncher<String> mSavePdfDocument = registerForActivityResult(new ActivityResultContracts.CreateDocument("application/pdf"),
            uri -> {
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                PdfDocument doc = PdfExporter.exportPdfDocument(mCanvasView, (float)metrics.densityDpi / metrics.density, true);
                CanvasFileManager.savePdfDocument(this, uri, doc);
            });

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

        /* catch menu clicks for setting actions, forward to navController for destination change */
//        CanvasView aaaaaa = findViewById(R.id.canvasView);
//        mCanvasView = navHostFragment.getChildFragmentManager().getFragments().get(0).getActivity().findViewById(R.id.canvasView);
//        mCanvasView = CanvasFragment.mCanvasView;
        mNavDrawerContainerNV.setNavigationItemSelectedListener(menuItem -> {
            mCanvasView = CanvasFragment.mCanvasView;
            String canvasData = "";
            switch (menuItem.getItemId()) {
                case R.id.new_file:
                    /* create a new file at a chosen uri and open it in the current canvas */
                    Log.d(TAG, "onNavigationItemSelected: New File");
                    if(mCanvasView == null) {
                        return false;
                    }
                    CanvasFileManager.createNewFile(this, Uri.parse("")); // returns json containing uri after opening filepicer
                        /* The rest is done in activity result method */
                    return true;
                case R.id.open_file:
                    /* chose a existing file with uri and open it in the current canvas */
                    Log.d(TAG, "onNavigationItemSelected: Open File");
                    Uri uri = mCanvasView.getCurrentURI();
                    canvasData = CanvasFileManager.openCanvasFile(this, uri);
                    try {
                        CanvasImporter.initCanvasViewFromJSON(canvasData, mCanvasView, true);
                    } catch (JSONException e) {
                        Log.e(TAG, "onCreate: ", e);
                    }
                    mCanvasView.invalidate();
                    Toast.makeText(this, "opened a saved file", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.save_file:
                    /* save file to existing uri of current view || save as to new uri (should not happen as there shouldnt be any current canvases without uri) */
                    Log.d(TAG, "onNavigationItemSelected: Save File as JSON to shared prefs");
                    try {
                        canvasData = CanvasExporter.canvasViewToJSON(mCanvasView, true).toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return false;
                    }
                    Uri currentUri = mCanvasView.getCurrentURI();
                    if (currentUri != null) {
                        CanvasFileManager.saveCanvasFile(this, currentUri, canvasData);

                        Toast.makeText(this, "saved file", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    // TODO
//                    Uri uri = CanvasFileManager.saveAsCanvasFile(canvasData); // still contains the wrong uri
//                    mCanvasView.setUri(uri);
                    return true;
                case R.id.export:
                    mSavePdfDocument.launch("application/pdf");

                    Log.i(TAG, "onNavigationItemSelected: Export to pdf");
                    return true;
            }
            // needed as onDestinationChanged is not called when onNavigationItemSelected catches the menu item click event
            if (navGraphController.getGraph().findNode(menuItem.getItemId()) != null) {
                navGraphController.navigate(menuItem.getItemId());
                mmainActivityDrawer.closeDrawers();
            }
            return true;
        });

        Switch swAutoSave = mNavDrawerContainerNV.getMenu().findItem(R.id.drawer_switch_autosave).getActionView().findViewById(R.id.menu_switch);
        Switch swSync = mNavDrawerContainerNV.getMenu().findItem(R.id.drawer_switch_sync).getActionView().findViewById(R.id.menu_switch);
        swAutoSave.setChecked(sharedPreferences.getBoolean("autosave", false));
        swSync.setChecked(sharedPreferences.getBoolean("sync", false));
        swAutoSave.setOnCheckedChangeListener((compoundButton, b) -> spEditor.putBoolean("autosave", b).apply());
        swSync.setOnCheckedChangeListener((compoundButton, b) -> spEditor.putBoolean("sync", b).apply());


        /* Button to hide the toolbar */
        FloatingActionButton fabToolbarVisibility = findViewById(R.id.button_toggle_toolbar);
        fabToolbarVisibility.setOnClickListener(view -> {
            toggleToolBarVisibility(appBar, fabToolbarVisibility);
        });

        /* set Title and Toolbar functions by Fragment */
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
                    tvTitle.setText("DOCNAME"); // TODO replace with open document name
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
            toggleToolBarVisibility(appBar, fabToolbarVisibility);
        });
    }

    private void toggleToolBarVisibility(AppBarLayout appBar, FloatingActionButton fabToolbarVisibility) {
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

    /* Handle Back Navigation via Android Button and Back Arrow in toolbar */
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

    /* Handle Navigation from the drawer menu with navcontoller*/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        NavController navController = Navigation.findNavController(this, R.id.nav_main_host_fragment);
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        Log.d(TAG, "onActivityResult: Caught an Activity Result");
        if (requestCode == CanvasFileManager.CREATE_NEW_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Get URI of the created file from resultdata
            if (resultData != null) {
                mUri = resultData.getData();
                Log.d(TAG, "onActivityResult: Created a New File at: " + mUri);
                String canvasData = CanvasFileManager.newCanvasFile(mUri,1);
                try {
                    CanvasImporter.initCanvasViewFromJSON(canvasData, mCanvasView, true); // canvasView.currentURI = CanvasFileManager.getCurrentURI();
                } catch (JSONException e) {
                    Log.e(TAG, "new_file: ", e);
                }
                mCanvasView.invalidate();
                Toast.makeText(this, "created a new file", Toast.LENGTH_SHORT).show();

                // Persist permissions for File
                final int takeFlags = resultData.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(mUri, takeFlags);
            }
        }
    }

    private void requestFileAccessPermission() {
        // requesting permissions if not provided.
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, 200);
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
                    Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }
}