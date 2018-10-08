package com.lilach.mypoi;

import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_data_sync,rootKey);
    }

}
