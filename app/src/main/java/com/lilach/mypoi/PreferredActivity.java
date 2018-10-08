package com.lilach.mypoi;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Map;

public class PreferredActivity extends AppCompatActivity implements OpenContextMenuListener {

    private ArrayList<PlaceObject> places = new ArrayList<>();
    private PlaceAdapter placeAdapter;
    private RecyclerView recyclerView;
    private boolean isPermitted;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location myLocation;
    private DBOpenHelper helper;
    private int itemViewPosition;
    private SharedPreferences sharedPreferences;
    private String distanceUnit;


    private static final String PREFERRED_ARRAY = "pref_array";
    private static final int LOCATION_REQUEST_CODE = 100;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferred);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(savedInstanceState != null) {
            places = savedInstanceState.getParcelableArrayList(PREFERRED_ARRAY);
        }else {
            helper = new DBOpenHelper(this);
            Map<String, ArrayList<PlaceObject>> m = helper.getLastSearchResults(helper.PREFERRED_TABLE_NAME);
            for(String key : m.keySet()){
                places = m.get(key);
            }
        }

        recyclerView = (RecyclerView) findViewById(R.id.preferredActivity_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkLocationPermission();
        distanceUnit = sharedPreferences.getString(getResources().getString(R.string.distance_unit),"");

        if(places.size() > 0){
            placeAdapter = new PlaceAdapter(this,places, null, this, distanceUnit);
            recyclerView.setAdapter(placeAdapter);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList(PREFERRED_ARRAY,places);
        super.onSaveInstanceState(outState);
    }



    private void checkLocationPermission() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    myLocation = location;
                                }
                            }
                        });
                isPermitted = true;
            }else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
                isPermitted = false;
            }
        }else{
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                myLocation = location;
                            }
                        }
                    });
            isPermitted = true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == LOCATION_REQUEST_CODE && grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    myLocation = location;
                                }
                            }
                        });
                isPermitted = true;
            }else{
                buildAlertDialog();
            }
        }
    }

    private void buildAlertDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage(getResources().getString(R.string.msg))
                .setNegativeButton(getResources().getString(R.string.negative_btn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(getResources().getString(R.string.positive_btn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });

        builder.create();
        builder.show();
    }


// -------------------------------- interface -----------------------------------



    @Override
    public void placeLongSelection(int position) {
        itemViewPosition = position;
        View itemView = recyclerView.getLayoutManager().findViewByPosition(position);
        registerForContextMenu(itemView);
    }




// -----------------------------  main menu  ----------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.removeItem(R.id.action_preferd);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this,SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.action_delete:
                helper.deleteAll(helper.PREFERRED_TABLE_NAME);
                placeAdapter.removeAll();
                break;
            case R.id.action_exit:
                break;
        }
        return true;
    }




// -----------------------------  context menu  ----------------------------------------


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.recycler_long_menu,menu);
        menu.removeItem(R.id.add_to_favorite);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        PlaceObject placeObject = places.get(itemViewPosition);

        switch (item.getItemId()){
            case R.id.share_intent:
                String textMsg = placeObject.getTvName() + "\n" +
                        placeObject.getTvAddress() +"\n"+
                        placeObject.getLatLng().latitude + "," + placeObject.getLatLng().longitude;
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, textMsg);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;

            case R.id.favorite_delete_item:
                helper.deleteOneItem(placeObject.getPlaceId());
                placeAdapter.itemRemoved(itemViewPosition);
                break;
        }

        return true;
    }




}
