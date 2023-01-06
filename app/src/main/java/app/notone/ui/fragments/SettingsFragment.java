package app.notone.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import app.notone.R;
import app.notone.core.util.SettingsHolder;

/**
 * works with the fragment_root_preferences PreferenceScreen to automatically set and update
 * preferences in the shared prefs
 * @author Luca Hackel
 * @since 202212XX
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = "SettingsFragment";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_root_preferences, rootKey);
    }

    /**
     * set theme on updated setting
     * @param preference
     * @return
     */
    @Override
    public boolean onPreferenceTreeClick(@NonNull Preference preference) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // darkmode
        boolean darkMode = sharedPreferences.getBoolean("darkmode", false);
        AppCompatDelegate.setDefaultNightMode(darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        SettingsHolder.update(sharedPreferences);

        return super.onPreferenceTreeClick(preference);
    }

    /**
     * store settings in holder for easy access
     */
    @Override
    public void onStop() {
        super.onStop();

        SettingsHolder.update(PreferenceManager.getDefaultSharedPreferences(getContext()));
    }
}