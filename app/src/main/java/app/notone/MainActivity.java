package app.notone;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.navigation.NavigationView;

import java.util.HashSet;
import java.util.Set;

import static androidx.navigation.Navigation.findNavController;

public class MainActivity extends AppCompatActivity {
    String TAG = "NotOneMainActivity";
    AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.canavastoolbar);
        setSupportActionBar(toolbar);

        DrawerLayout mainActivityDrawerLayout = findViewById(R.id.drawer_layout_activity_main); // contains everything enables the drawer
        NavigationView navDrawerContainer = findViewById(R.id.navdrawercontainer_view); // contains the drawer menu
        NavController navController = ((NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_main_host_fragment)).getNavController(); // contains the fragments underneath the toolbar that are changed when set via drawer menu


        /* top levels dont display a back button*/
//        Set<Integer> topLevelDestinations = new HashSet<Integer>();
//        topLevelDestinations.add(R.id.canvasFragment);
//        topLevelDestinations.add( R.id.settingsFragment);
        mAppBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()) //topLevelDestinations
                .setOpenableLayout(mainActivityDrawerLayout) // adds burger button for toplevel
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration); // add titles from navgraph to actionbar
        NavigationUI.setupWithNavController(navDrawerContainer, navController); // this will call onNavDestinationSelected when a menu item is selected.

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mainActivityDrawerLayout, toolbar, R.string.open, R.string.close);

        mainActivityDrawerLayout.addDrawerListener(toggle);
//        navDrawerContainer.setNavigationItemSelectedListener(item -> );

        Log.d(TAG, "onCreate: AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = findNavController(this, R.id.nav_main_host_fragment);
        return navController.navigateUp() || super.onSupportNavigateUp();
//        return super.onSupportNavigateUp();
    }
}