package com.lilach.mypoi;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;

public class SettingsActivity extends PreferredActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    private SettingsFragment settingsFragment ;
    private SharedPreferences sharedPref;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content,settingsFragment).commit();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Preference preference = settingsFragment.getPreferenceScreen().findPreference(key);
        // Set summary to be the user-description for the selected value
        if (preference instanceof ListPreference) {
            preference.setSummary(((ListPreference) preference).getEntry());
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        Preference distancePreference = settingsFragment.getPreferenceScreen().findPreference(getResources().getString(R.string.distance_unit));
        Preference mapPreference = settingsFragment.getPreferenceScreen().findPreference(getResources().getString(R.string.map_zoom_option_key));
        distancePreference.setSummary(sharedPref.getString(getResources().getString(R.string.distance_unit),"km"));
        String summary = "";
        String zoomVal = sharedPref.getString(getResources().getString(R.string.map_zoom_option_key),"");
        switch (zoomVal){
            case "5":
                summary = "Continent";
                break;
            case "7":
                summary = "Country";
                break;
            case "10":
                summary = "City";
                break;
            case "15":
                summary = "Street";
                break;
            case "20":
                summary = "Building";
                break;
        }
        mapPreference.setSummary(summary);

        settingsFragment.getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        settingsFragment.getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }


}
