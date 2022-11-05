package app.notone;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import static androidx.navigation.Navigation.findNavController;

public class MainActivity extends AppCompatActivity {
    String TAG = "NotOneMainActivity";
    AppBarConfiguration mAppBarConfiguration;
    NavigationView navDrawerContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar canvasToolbar = findViewById(R.id.canavastoolbar); // do depending on fragment
        setSupportActionBar(canvasToolbar);

        DrawerLayout mainActivityDrawerLayout = findViewById(R.id.drawer_layout_activity_main);
        navDrawerContainer = findViewById(R.id.navdrawercontainer_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_main_host_fragment);
        NavController navGraphController = navHostFragment.getNavController(); // nav_graph

        /* top levels dont display a back button*/
        mAppBarConfiguration = new AppBarConfiguration.Builder(navGraphController.getGraph()) // getGraph => topLevelDestinations
                .setOpenableLayout(mainActivityDrawerLayout) // setDrawerLayout // adds burger button for toplevel
                .build();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mainActivityDrawerLayout, canvasToolbar, R.string.open, R.string.close); // open with burger
        mainActivityDrawerLayout.addDrawerListener(toggle); // not needed?

        NavigationUI.setupActionBarWithNavController(this, navGraphController, mAppBarConfiguration); // add titles and burger from nav_graph to actionbar otherwise there will be the app title and no burger!
        NavigationUI.setupWithNavController(navDrawerContainer, navGraphController); // this will call onNavDestinationSelected when a menu item is selected.


        navGraphController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller,
                                             @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if (destination.getId() == R.id.settings_fragment) {
                    canvasToolbar.setVisibility(View.GONE);
                    findViewById(R.id.button_toggle_toolbar).setVisibility(View.GONE);
                } else {
                    canvasToolbar.setVisibility(View.VISIBLE);
                    findViewById(R.id.button_toggle_toolbar).setVisibility(View.VISIBLE);
//                setSupportActionBar(canvasToolbar); // breaks burger
                }
            }
        });


//        navDrawerContainer.setNavigationItemSelectedListener(item -> );

        Log.d(TAG, "onCreate: AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navGraphController = findNavController(this, R.id.nav_main_host_fragment);
        Log.d(TAG, "onSupportNavigateUp: FCDFFFFFFFFFFFFFFFFF");
        return navGraphController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        NavController navController = Navigation.findNavController(this, R.id.nav_main_host_fragment);
        Log.d(TAG, "onOptionsItemSelected: FCDFFFFFFFFFFFFFFFFF");
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: FCDFFFFFFFFFFFFFFFFF");
//        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.drawer_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

}