package app.notone.ui.fragments;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.navigation.NavigationView;

import app.notone.R;
import app.notone.core.util.SettingsHolder;

/**
 * Works with the fragment_root_preferences PreferenceScreen to automatically set and update
 * preferences in the shared prefs
 * since it doesnt work in the xml the inputtype of the edittexts needs to be set here
 * @author Luca Hackel
 * @since 202212XX
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = "SettingsFragment";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_root_preferences, rootKey);

        EditTextPreference saveIntervalPreference = findPreference("saveintervall");
        if (saveIntervalPreference != null) {
            saveIntervalPreference.setOnBindEditTextListener(
                    editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
        }

        EditTextPreference syncIntervalPreference = findPreference("syncintervall");
            if (syncIntervalPreference != null) {
            syncIntervalPreference.setOnBindEditTextListener(
                    editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
        }
    }

    /**
     * set theme on updated setting
     * @param preference
     * @return
     */
    @Override
    public boolean onPreferenceTreeClick(@NonNull Preference preference) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        /* set app theme based on */
        boolean darkMode = sharedPreferences.getBoolean("darkmode", false);
        AppCompatDelegate.setDefaultNightMode(darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        SettingsHolder.update(sharedPreferences);

        /* update quicksettings switch in drawer menu */
        MaterialSwitch swAutoSave = ((NavigationView)getActivity().findViewById(R.id.navdrawercontainer_view))
               .getMenu().findItem(R.id.drawer_switch_autosave)
               .getActionView().findViewById(R.id.menu_switch);
        swAutoSave.setChecked(SettingsHolder.shouldAutoSaveCanvas());

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