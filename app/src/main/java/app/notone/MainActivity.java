package app.notone;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import static androidx.navigation.Navigation.findNavController;

public class MainActivity extends AppCompatActivity {
    String TAG = "NotOneMainActivity";
    AppBarConfiguration mAppBarConfiguration;
    NavigationView mNavDrawerContainerNV;
    boolean mToolbarVisibility = true;

    /**
     * Main onCreate of the App
     * Set the Main Activity View
     *  Burger Menu and Title State
     *  Fragment Navigation
     *  Toolbar state
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        DrawerLayout mainActivityDrawer = findViewById(R.id.drawer_activity_main); // main base layout
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_main_host_fragment); // container of the fragments
        Toolbar canvasToolbar = findViewById(R.id.canavas_toolbar); // toolbar
        AppBarLayout appBar = findViewById(R.id.AppBar); // toolbar container
        mNavDrawerContainerNV = findViewById(R.id.navdrawercontainer_view); // drawer menu container
        NavController navGraphController = navHostFragment.getNavController(); // nav_graph of the app

        /* configure AppBar with burger and title */
        setSupportActionBar(canvasToolbar);
        mAppBarConfiguration = new AppBarConfiguration.Builder(navGraphController.getGraph()) // getGraph => topLevelDestinations
                .setOpenableLayout(mainActivityDrawer) // setDrawerLayout // define burger button for toplevel
                .build();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mainActivityDrawer, canvasToolbar, R.string.open, R.string.close); // add burger button for top level
        mainActivityDrawer.addDrawerListener(toggle); // add listener to it
        NavigationUI.setupActionBarWithNavController(this, navGraphController, mAppBarConfiguration); // add titles and burger from nav_graph to actionbar otherwise there will be the app title and no burger!
        NavigationUI.setupWithNavController(mNavDrawerContainerNV, navGraphController); // this will call onNavDestination(Selected||Changed) when a menu item is selected.

        /* catch menu clicks for setting actions, forward to navController for destination change */
        mNavDrawerContainerNV.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.open_file:
                    Log.i(TAG, "onNavigationItemSelected: Open File");
                    return true;
            }
            // needed as onDestinationChanged is not called when onNavigationItemSelected catches the menu item click event
            if (navGraphController.getGraph().findNode(menuItem.getItemId()) != null) {
                navGraphController.navigate(menuItem.getItemId());
                mainActivityDrawer.closeDrawers();
            }
            return true;
        });

        /* Button to hide the toolbar */
        FloatingActionButton fabToolbarVisibility = findViewById(R.id.button_toggle_toolbar);
        fabToolbarVisibility.setOnClickListener(view -> {
            toggleToolBarVisibility(appBar, fabToolbarVisibility);
        });

        /* set Title and Toolbar function by Fragment */
        navGraphController.addOnDestinationChangedListener((controller, destination, arguments) -> {
//            TextView tvTitle = ((TextView) findViewById(R.id.tv_fragment_title));
            HorizontalScrollView viewPenTools = findViewById(R.id.canvas_tools_pen);
            LinearLayout viewUnRedo = findViewById(R.id.canvas_tools_unredo);
            switch (destination.getId()) {
                case R.id.canvas_fragment:
                    findViewById(R.id.button_toggle_toolbar).setVisibility(View.VISIBLE);
                    viewPenTools.setVisibility(View.VISIBLE);
                    viewUnRedo.setVisibility(View.VISIBLE);
//                    tvTitle.setText("Zeichnen");
                    return; // dont reset toolbar

                case R.id.settings_fragment:
                case R.id.about_fragment:
                    findViewById(R.id.button_toggle_toolbar).setVisibility(View.GONE);
                    viewPenTools.setVisibility(View.GONE);
                    viewUnRedo.setVisibility(View.GONE);
//                    tvTitle.setText("Einstellungen");
//                    tvTitle.setText("About");
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
            fabToolbarVisibility.setImageResource(android.R.drawable.arrow_down_float);
        } else {
            mToolbarVisibility = true;
            appBar.animate().translationY(0);
            fabToolbarVisibility.animate().translationY(0);
            fabToolbarVisibility.setImageResource(android.R.drawable.arrow_up_float); // maybe rotate instead
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "onSupportNavigateUp");
        NavController navGraphController = findNavController(this, R.id.nav_main_host_fragment);
        return navGraphController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        NavController navController = Navigation.findNavController(this, R.id.nav_main_host_fragment);
        Log.d(TAG, "onOptionsItemSelected");
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }
}