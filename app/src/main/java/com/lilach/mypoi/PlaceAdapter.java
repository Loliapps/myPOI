package com.lilach.mypoi;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {

    private Context context;
    private ArrayList<PlaceObject> places;
    private Location myLocation;
    private OpenContextMenuListener listener;
    private String distanceUnit;


    public PlaceAdapter (Context context, ArrayList<PlaceObject> places, Location myLocation, OpenContextMenuListener listener, String distanceUnit){
        this.context = context;
        this.places = places;
        this.myLocation = myLocation;
        this.listener = listener;
        this.distanceUnit = distanceUnit;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView rvImage;
        TextView tvName, tvAddress, tvDistance;


        public ViewHolder(View itemView) {
            super(itemView);
            rvImage = (ImageView) itemView.findViewById(R.id.recyclerView_image);
            tvName  = (TextView) itemView.findViewById(R.id.recyclerView_name);
            tvAddress = (TextView) itemView.findViewById(R.id.recyclerView_address);
            tvDistance = (TextView) itemView.findViewById(R.id.recyclerView_distance);
        }


    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_item,null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final PlaceObject placeObject = places.get(position);

        Picasso.get().load("https://maps.googleapis.com/maps/api/place/photo?maxwidth=120&photoreference=" + placeObject.getImg() + "&key=AIzaSyBiMBQfs2ZMMl5Lch-mU-RkoIquLQ-X0aU")
                .error(R.drawable.default_img).into(holder.rvImage);

        holder.tvName.setText(placeObject.getTvName());
        holder.tvAddress.setText(placeObject.getTvAddress());

        Location locationA = new Location("locationA");
        locationA.setLatitude(placeObject.getLatLng().latitude);
        locationA.setLongitude(placeObject.getLatLng().longitude);

        if(myLocation != null) {
            if(!distanceUnit.equals("miles")) {
                float distance = myLocation.distanceTo(locationA)/1000;
                holder.tvDistance.setText(String.valueOf(distance)+ " km");
            }else {
                float distance = myLocation.distanceTo(locationA)/1609.344f;
                holder.tvDistance.setText(String.valueOf(distance)+ " mile");
            }
        }else{
            holder.tvDistance.setText("Distance: unKnown");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("item_clicked");
                intent.putExtra("placeObject",placeObject);
                context.sendBroadcast(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.placeLongSelection(position);
                return false;
            }
        });
    }


    @Override
    public int getItemCount() {
        return places.size();
    }


    public void itemRemoved(int position){
        places.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,places.size());
    }

    public void removeAll(){
        places = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setMyLocation(Location location){
        myLocation = location;
        notifyDataSetChanged();
    }

}
