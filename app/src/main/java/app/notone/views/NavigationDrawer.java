package app.notone.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;

/* a custom drawer enable and disable swiping to open the drawer based on preferences */
public class NavigationDrawer extends DrawerLayout {

    boolean mSwipeOpenEnabled;

    public NavigationDrawer(@NonNull Context context) {
        super(context);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mSwipeOpenEnabled = sharedPreferences.getBoolean("drawerswipe", false);
    }

    public NavigationDrawer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mSwipeOpenEnabled = sharedPreferences.getBoolean("drawerswipe", false);
    }

    public NavigationDrawer(Context context, AttributeSet attributeSet, int disp) {
        super(context, attributeSet, disp);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mSwipeOpenEnabled = sharedPreferences.getBoolean("drawerswipe", false);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mSwipeOpenEnabled && !isDrawerVisible(Gravity.LEFT)) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }
}
