package app.notone;

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

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.navdrawer_container_view);
        NavController navController = ((NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_main_host_fragment)).getNavController();

        mAppBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        Set<Integer> topLevelDestinations = new HashSet<Integer>();
        topLevelDestinations.add(R.id.canvasFragment);
        topLevelDestinations.add( R.id.settingsFragment);

        mAppBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations)
                .setOpenableLayout(drawerLayout)
                .build();
        NavigationUI.setupActionBarWithNavController(this,
                navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        Log.d(TAG, "onCreate: AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = findNavController(this, R.id.nav_main_host_fragment);
        return navController.navigateUp() || super.onSupportNavigateUp();
//        return super.onSupportNavigateUp();
    }
}