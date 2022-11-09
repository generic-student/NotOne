package app.notone.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import app.notone.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_root_preferences, rootKey);
    }
}