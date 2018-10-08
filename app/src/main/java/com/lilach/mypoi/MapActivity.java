package com.lilach.mypoi;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

public class MapActivity extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Context context;
    private PlaceObject placeObject;
    private Marker marker;
    private LatLng placeLocation;
    private String zoom_val;
    private SharedPreferences sharedPreferences;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map_activity, container, false);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment));
        mapFragment.getMapAsync(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        zoom_val = sharedPreferences.getString(context.getResources().getString(R.string.map_zoom_option_key),"");

        if(savedInstanceState != null){
            placeLocation = savedInstanceState.getParcelable("placeLocation");
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("placeLocation",placeLocation);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mMap != null){
            zoom_val = sharedPreferences.getString(context.getResources().getString(R.string.map_zoom_option_key),"");
            if(!zoom_val.isEmpty()){
                if(placeLocation != null){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(placeLocation,Float.parseFloat(zoom_val)));
                }else {
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(Float.parseFloat(zoom_val)));
                }
            }else{
                mMap.setMinZoomPreference(10);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(placeObject != null) {
            placeLocation = placeObject.getLatLng();
            marker = mMap.addMarker(new MarkerOptions().position(placeLocation).title(placeObject.getTvName()));
            marker.showInfoWindow();
            if(!zoom_val.isEmpty()) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLocation, Float.parseFloat(zoom_val)));
            }else{
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLocation, 15f));
            }
            mMap.setMaxZoomPreference(20);
            mMap.setMinZoomPreference(5);
        }
    }


    public void getPlaceData (PlaceObject placeObject){
        this.placeObject = placeObject;

        if(marker != null){
            marker.remove();
        }

        if(mMap != null){
            LatLng placeLocation = placeObject.getLatLng();
            marker = mMap.addMarker(new MarkerOptions().position(placeLocation).title(placeObject.getTvName()));
            marker.showInfoWindow();
            mMap.moveCamera(CameraUpdateFactory.newLatLng(placeLocation));
            mMap.setMaxZoomPreference(20);
            mMap.setMinZoomPreference(14);
        }
    }

}
