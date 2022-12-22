package app.notone.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

import app.notone.core.util.SettingsHolder;

/**
 * a custom drawer enable and disable swiping to open the drawer based on preferences
 * @author default-student
 * @since 202212XX
 */

public class NavigationDrawer extends DrawerLayout {

    boolean mSwipeOpenEnabled;

    public NavigationDrawer(@NonNull Context context) {
        super(context);
        mSwipeOpenEnabled = SettingsHolder.isDrawerSwipeOpen();
    }

    public NavigationDrawer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mSwipeOpenEnabled = SettingsHolder.isDrawerSwipeOpen();
    }

    public NavigationDrawer(Context context, AttributeSet attributeSet, int disp) {
        super(context, attributeSet, disp);
        mSwipeOpenEnabled = SettingsHolder.isDrawerSwipeOpen();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mSwipeOpenEnabled && !isDrawerVisible(Gravity.LEFT)) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }
}
