package app.notone.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import app.notone.R;

/**
 * @author default-student
 * @since 202212XX
 */

public class AboutFragment extends Fragment {

    /**
     * inflate fragment
     * handle the about button onclick programmatically
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        Button buttonAbout = view.findViewById(R.id.button_about_pages);
        buttonAbout.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/generic-student"))));

        return view;
    }
}