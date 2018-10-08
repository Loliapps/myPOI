package com.lilach.mypoi;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivityFragment extends Fragment implements View.OnClickListener, TextWatcher,
        OnAsyncTaskComplete, OpenContextMenuListener, SharedPreferences.OnSharedPreferenceChangeListener,
        LocationListener{

    public ArrayList<PlaceObject> places = new ArrayList<>();
    private int itemViewPosition;
    private PlaceAdapter placeAdapter;
    private RecyclerView recyclerView;
    private EditText search_et;
    private Button search_by_text, search_by_position;
    private boolean isPermitted;
    private Context context;
    private String searchString;
    private DBOpenHelper helper;
    public Location myLocation;
    private LocationManager locationManager;
    private FusedLocationProviderClient mFusedLocationClient;
    private SharedPreferences sharedPreferences;
    private String distanceUnit;

    private static final String ET_SEARCH = "searchEt";
    private static final String SEARCH_RESULTS = "arrResult";
    private static final int LOCATION_REQUEST_CODE = 100;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        View view = inflater.inflate(R.layout.fragment_main,container,false);
        helper = new DBOpenHelper(context);
        search_by_position = view.findViewById(R.id.button_search_by_position);
        search_by_text = view.findViewById(R.id.button_search_by_text);
        search_et = view.findViewById(R.id.editText_search_parameter);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        if(savedInstanceState != null) {
            if(savedInstanceState.getString(ET_SEARCH) != null) {
                searchString = savedInstanceState.getString(ET_SEARCH);
            }
            if(savedInstanceState.getParcelableArrayList(SEARCH_RESULTS) != null){
                places = savedInstanceState.getParcelableArrayList(SEARCH_RESULTS);
            }
        }else{
            Map<String,ArrayList<PlaceObject>> m = helper.getLastSearchResults(helper.SEARCH_RESULT_TABLE_NAME);
            for (String k : m.keySet()){
                searchString = k;
            }
            places = m.get(searchString);
        }

        if(!searchString.equals("txt")) {
            search_et.setText(searchString);
        }

        search_by_text.setOnClickListener(this);
        search_by_position.setOnClickListener(this);
        search_et.addTextChangedListener(this);

        return view;

    }


    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLocationPermission();

        distanceUnit = sharedPreferences.getString(getResources().getString(R.string.distance_unit),"");

        if(places.size() > 0){
            placeAdapter = new PlaceAdapter(context,places, myLocation,MainActivityFragment.this,distanceUnit);
            recyclerView.setAdapter(placeAdapter);
            placeAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPause() {
        locationManager.removeUpdates(this);
        super.onPause();
    }



    private void checkLocationPermission() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
               && getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    myLocation = location;
                                }
                            }
                        });
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
                isPermitted = true;
            }else {
                getActivity().requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
                isPermitted = false;
            }
        }else{
            Log.d("meir","check permission");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                myLocation = location;
                                Log.d("meir","check permission location = "+ myLocation);
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
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    myLocation = location;
                                }
                            }
                        });
                isPermitted = true;
            }else{
                buildAlertDialog(getResources().getString(R.string.msg));
            }
        }
    }

    private void buildAlertDialog(final String msg) {



        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setMessage(msg)
                .setNegativeButton(getResources().getString(R.string.negative_btn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(getResources().getString(R.string.positive_btn), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(msg.equals(getResources().getString(R.string.msg))){
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            context.startActivity(intent);
                        }else {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            context.startActivity(intent);
                        }
                        dialog.dismiss();
                    }
                });

                builder.create();
                builder.show();
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        searchString = search_et.getText().toString().trim();
        outState.putString(ET_SEARCH, searchString);
        outState.putParcelableArrayList(SEARCH_RESULTS,places);
        super.onSaveInstanceState(outState);
    }




// ------------------------------------------------- listeners ---------------------------------------------


    @Override
    public void onClick(View v) {

        String url = "";
        String params = "";
        String etRequest = search_et.getText().toString().trim();

        getLocationFromProviders();

        if(checkInternetConnection()) {

            helper.deleteAll(helper.SEARCH_RESULT_TABLE_NAME);

            String[] allWords = etRequest.split(" ");
            for (int i = 0; i < allWords.length; i++) {
                if (allWords[i].length() > 0) {
                    if (i == (allWords.length - 1)) {
                        params += allWords[i];
                    } else {
                        params += allWords[i] + "%20";
                    }
                }
            }

            params = params.replaceAll("\\.", "");


            switch (v.getId()) {

                case R.id.button_search_by_text:
                    url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + params + "&key=API_KEY";
                    //url = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input="+params+"&inputtype=textquery&fields=place_id,photos,formatted_address,name,geometry&key=AIzaSyBiMBQfs2ZMMl5Lch-mU-RkoIquLQ-X0aU";
                    new MyAsyncTask(context, this).execute(url);
                    break;

                case R.id.button_search_by_position:

                    if(myLocation != null) {
                        url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + myLocation.getLatitude() + "," + myLocation.getLongitude() + "&radius=10000&keyword=" + params + "&key=API_KEY";
                        new MyAsyncTask(context, this).execute(url);
                    }else{
                        if(isPermitted){

                            if(myLocation == null){
                                buildAlertDialog(getResources().getString(R.string.no_gps));
                            }else{
                                url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + myLocation.getLatitude() + "," + myLocation.getLongitude() + "&radius=10000&keyword=" + params + "&key=API_KEY";
                                new MyAsyncTask(context, this).execute(url);
                            }

                        }else {
                            buildAlertDialog(getResources().getString(R.string.msg));
                        }
                    }
                    break;

            }

        }
    }


    private void getLocationFromProviders(){

        List<String> providersList = locationManager.getProviders (true);
        if(providersList != null) {
            for(String provider : providersList) {
                if(isPermitted) {
                    myLocation = locationManager.getLastKnownLocation(provider);
                }
            }
        }
    }

    private boolean checkInternetConnection() {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isNetworkActive = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isNetworkActive;
    }





//  ---------------------------------------   edit text listener  --------------------------------------

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(s.length() > 0){
            search_by_text.setEnabled(true);
        }else{
            search_by_text.setEnabled(false);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }


    @Override
    public void onAsyncComplete(final ArrayList<PlaceObject> places) {

        this.places = places;

        if(isPermitted){

            if (myLocation != null) {
                placeAdapter = new PlaceAdapter(context,places, myLocation,MainActivityFragment.this, distanceUnit);
                recyclerView.setAdapter(placeAdapter);
            }else{
                placeAdapter = new PlaceAdapter(context,places, null,MainActivityFragment.this, distanceUnit);
                recyclerView.setAdapter(placeAdapter);
            }

        }else{
            placeAdapter = new PlaceAdapter(context,places, null, MainActivityFragment.this, distanceUnit);
            recyclerView.setAdapter(placeAdapter);
        }

        String search_txt = search_et.getText().toString().trim();

        helper = new DBOpenHelper(context);
        for(int i = 0 ; i < places.size(); i++){
            String lat = String.valueOf(places.get(i).getLatLng().latitude);
            String lng = String.valueOf(places.get(i).getLatLng().longitude);
            String placeName = places.get(i).getTvName().replaceAll(","," ");
            String placeAddress = places.get(i).getTvAddress().replaceAll(","," ");
            String placeId = places.get(i).getPlaceId();

            helper.insertPlace(helper.SEARCH_RESULT_TABLE_NAME,search_txt,placeName,placeAddress,placeId,lat,lng, places.get(i).getImg());
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.recycler_long_menu,menu);
        menu.removeItem(R.id.favorite_delete_item);
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

            case R.id.add_to_favorite:
                helper.insertPlace(helper.PREFERRED_TABLE_NAME,
                                   "pref",
                                    placeObject.getTvName(),
                                    placeObject.getTvAddress(),
                                    placeObject.getPlaceId(),
                                    String.valueOf(placeObject.getLatLng().latitude),
                                    String.valueOf(placeObject.getLatLng().longitude),
                                    placeObject.getImg());
                break;
        }

        return true;
    }


    @Override
    public void placeLongSelection(int position) {
        itemViewPosition = position;
        View itemView = recyclerView.getLayoutManager().findViewByPosition(position);
        registerForContextMenu(itemView);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(final String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(context,provider + " is disabled",Toast.LENGTH_SHORT).show();
    }
}
