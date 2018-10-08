package com.lilach.mypoi;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import com.google.android.gms.maps.model.LatLng;



public class MainActivity extends AppCompatActivity {//implements OrientationChangeListener{

    private FrameLayout fragment_container, map_container;
    private boolean isLandscape;
    private MapActivity mapFragment;
    private MainActivityFragment mainActivityFragment;
    public PlaceObject selectedPlace;
    private BatteryReceiver batteryReceiver;
    private DBOpenHelper helper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        batteryReceiver = new BatteryReceiver();
        helper = new DBOpenHelper(this);

        if(savedInstanceState != null) {
            selectedPlace = savedInstanceState.getParcelable("selected_pObj");
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragment_container = (FrameLayout) findViewById(R.id.fragment_container);
        android.support.v4.app.FragmentManager manager = getSupportFragmentManager();

        mainActivityFragment = new MainActivityFragment();
        mapFragment = new MapActivity();

        if(savedInstanceState == null){
            manager.beginTransaction().add(R.id.fragment_container,mainActivityFragment).commit();
            if(findViewById(R.id.map_container) != null){
                isLandscape = true;
                manager.beginTransaction().add(R.id.map_container, mapFragment).commit();
            }
        }else{
            manager.beginTransaction().replace(R.id.fragment_container,mainActivityFragment).commit();
            if(findViewById(R.id.map_container) != null){
                isLandscape = true;
                mapFragment.getPlaceData(selectedPlace);
                manager.beginTransaction().replace(R.id.map_container, mapFragment).commit();
            }
        }


/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
    }


    @Override
    protected void onResume() {
        registerReceiver(itemClickedReceiver,new IntentFilter("item_clicked"));
        if(isLandscape){
            if(selectedPlace == null) {
                if(mainActivityFragment.places.size() > 0) {
                    mapFragment.getPlaceData(mainActivityFragment.places.get(0));
                }else{
                    if(mainActivityFragment.myLocation != null){
                        LatLng myPosition = new LatLng(mainActivityFragment.myLocation.getLatitude(), mainActivityFragment.myLocation.getLongitude());
                        PlaceObject currentPlace = new PlaceObject("","","you are here","",myPosition);
                        mapFragment.getPlaceData(currentPlace);
                    }
                }
            }
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(itemClickedReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("selected_pObj",selectedPlace);
        super.onSaveInstanceState(outState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this,SettingsActivity.class);
                // settingsIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(settingsIntent);
                break;
            case R.id.action_preferd:
                Intent intent = new Intent(this,PreferredActivity.class);
                startActivity(intent);
                break;
            case R.id.action_delete:
                helper.deleteAll(helper.PREFERRED_TABLE_NAME);
                break;
            case R.id.action_exit:
                break;
        }

        return true;
    }


    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

    }

    private BroadcastReceiver itemClickedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            selectedPlace = (PlaceObject) intent.getParcelableExtra("placeObject");
            mapFragment.getPlaceData(selectedPlace);

            if (isLandscape) {
                getSupportFragmentManager().beginTransaction().replace(R.id.map_container, mapFragment).commit();
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mapFragment).addToBackStack("B").commit();
            }
        }
    };




}
