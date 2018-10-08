package com.lilach.mypoi;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class MyAsyncTask extends AsyncTask<String,Void,ArrayList<PlaceObject>> {

    private Context context;
    private OnAsyncTaskComplete onAsyncTaskComplete;


    public MyAsyncTask (Context context, OnAsyncTaskComplete onAsyncTaskComplete){
        this.context = context;
        this.onAsyncTaskComplete = onAsyncTaskComplete;
    }

    @Override
    protected ArrayList<PlaceObject> doInBackground(String... params) {

        String allData = "";
        ArrayList<PlaceObject> places = new ArrayList<>();

        try {
            URL url = new URL(params[0]);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);

            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line = null;

            while ((line = reader.readLine()) != null){
                allData += line;
            }
            Log.d("meir",allData);
            con.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            JSONObject object = new JSONObject(allData);
            JSONArray arr;

            if(object.has("candidates")) {
                arr = object.getJSONArray("candidates");
            }else{
                arr = object.getJSONArray("results");
            }
            String address;

            for(int j = 0; j < arr.length(); j++){

                if(arr.getJSONObject(j).has("formatted_address")){
                    address = arr.getJSONObject(j).getString("formatted_address");
                }else{
                    address = arr.getJSONObject(j).getString("vicinity");
                }

                String placeName = arr.getJSONObject(j).getString("name");
                String placeId = arr.getJSONObject(j).getString("place_id");
                JSONObject jGeometry = arr.getJSONObject(j).getJSONObject("geometry");
                JSONObject jLocation = jGeometry.getJSONObject("location");
                double lat = jLocation.getDouble("lat");
                double lng = jLocation.getDouble("lng");

                String img = "";
                if(arr.getJSONObject(j).has("photos")) {
                    JSONArray photos = arr.getJSONObject(j).getJSONArray("photos");
                    if (photos != null) {
                        String pString = photos.getJSONObject(0).getString("photo_reference");
                        if (pString != null) {
                            img = pString;
                        }
                    }
                }
                LatLng latLng = new LatLng(lat,lng);
                places.add(new PlaceObject(placeId,img,placeName,address,latLng));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return places;
    }

    @Override
    protected void onPostExecute(ArrayList<PlaceObject> places) {
        super.onPostExecute(places);
        onAsyncTaskComplete.onAsyncComplete(places);
    }
}
