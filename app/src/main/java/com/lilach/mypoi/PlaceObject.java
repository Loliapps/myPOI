package com.lilach.mypoi;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class PlaceObject implements Parcelable {

    String placeId, img, tvName, tvAddress;
    LatLng latLng;


    public PlaceObject (String placeId, String img, String tvName, String tvAddress, LatLng latLng){

        this.placeId = placeId;
        this.img = img;
        this.tvName = tvName;
        this.tvAddress = tvAddress;
        this.latLng = latLng;
    }

    protected PlaceObject(Parcel in) {
        placeId = in.readString();
        img = in.readString();
        tvName = in.readString();
        tvAddress = in.readString();
        latLng = in.readParcelable(LatLng.class.getClassLoader());
    }

    public static final Creator<PlaceObject> CREATOR = new Creator<PlaceObject>() {
        @Override
        public PlaceObject createFromParcel(Parcel in) {
            return new PlaceObject(in);
        }

        @Override
        public PlaceObject[] newArray(int size) {
            return new PlaceObject[size];
        }
    };

    public String getImg() {
        return img;
    }

    public String getTvName() {
        return tvName;
    }

    public String getTvAddress() {
        return tvAddress;
    }

    public String getPlaceId() {
        return placeId;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(placeId);
        dest.writeString(img);
        dest.writeString(tvName);
        dest.writeString(tvAddress);
        dest.writeParcelable(latLng, flags);
    }
}
