package com.lilach.mypoi;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class DBOpenHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "pointOfInterestDB1.db";
    private static final int DB_VERSION = 1;

    public static final String SEARCH_RESULT_TABLE_NAME = "search_results";
    public static final String PREFERRED_TABLE_NAME = "preferd";


    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_ADRESS = "adress";
    private static final String COLUMN_LONGITUDE = "longitute";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_PICTURE = "picture";
    private static final String COLUMN_PLACE_ID = "place_id";
    private static final String COLUMN_SEARCH_TXT = "search_text";


    private static final String CREATE_SEARCH_RESULT_TABLE_CHAT = "CREATE TABLE IF NOT EXISTS " + SEARCH_RESULT_TABLE_NAME
            + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_SEARCH_TXT + " TEXT DEFAULT NULL, "
            + COLUMN_NAME + " TEXT NOT NULL, "
            + COLUMN_ADRESS + " TEXT NOT NULL, "
            + COLUMN_PLACE_ID + " TEXT NOT NULL, "
            + COLUMN_LONGITUDE + " TEXT DEFAULT NULL,"
            + COLUMN_LATITUDE + " TEXT DEFAULT NULL,"
            + COLUMN_PICTURE + " TEXT DEFAULT NULL)";


    //create second table
    private static final String CREATE_PREFERD_TABLE_CHAT = "CREATE TABLE IF NOT EXISTS " + PREFERRED_TABLE_NAME
            + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_SEARCH_TXT + " TEXT DEFAULT NULL, "
            + COLUMN_NAME + " TEXT NOT NULL, "
            + COLUMN_ADRESS + " TEXT NOT NULL, "
            + COLUMN_PLACE_ID + " TEXT NOT NULL, "
            + COLUMN_LONGITUDE + " TEXT DEFAULT NULL,"
            + COLUMN_LATITUDE + " TEXT DEFAULT NULL,"
            + COLUMN_PICTURE + " TEXT DEFAULT NULL)";


    public DBOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SEARCH_RESULT_TABLE_CHAT);
        db.execSQL(CREATE_PREFERD_TABLE_CHAT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertPlace(String tableName, @Nullable String search_txt, String place_name, String place_address, String place_id, String lat, String lng, String img) {

        if (search_txt != null) {
            String sql = "INSERT INTO " + tableName + " ("
                    + COLUMN_SEARCH_TXT + ","
                    + COLUMN_NAME + ","
                    + COLUMN_ADRESS + ","
                    + COLUMN_PLACE_ID + ","
                    + COLUMN_LATITUDE + ","
                    + COLUMN_LONGITUDE + ","
                    + COLUMN_PICTURE + ") VALUES (?,?,?,?,?,?,?);";

            SQLiteDatabase database = getWritableDatabase();
            SQLiteStatement stmt = database.compileStatement(sql);
            stmt.bindString(1, search_txt);
            stmt.bindString(2, place_name);
            stmt.bindString(3, place_address);
            stmt.bindString(4, place_id);
            stmt.bindString(5, lat);
            stmt.bindString(6, lng);
            stmt.bindString(7, img);
            stmt.execute();

            database.close();
        }
    }


    public Map<String,ArrayList<PlaceObject>> getLastSearchResults(String tableName){

        ArrayList<PlaceObject> placeObjects = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        String sql = "SELECT * FROM " + tableName;

        Cursor cursor = database.rawQuery(sql,null);
        String search_et = "txt";

        if(cursor.getCount() > 0){

            cursor.moveToFirst();
            search_et = cursor.getString(cursor.getColumnIndex(COLUMN_SEARCH_TXT));
            String placeName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
            String placeAddress = cursor.getString(cursor.getColumnIndex(COLUMN_ADRESS));
            String placeId = cursor.getString(cursor.getColumnIndex(COLUMN_PLACE_ID));
            double lat = Double.parseDouble(cursor.getString(cursor.getColumnIndex(COLUMN_LATITUDE)));
            double lng = Double.parseDouble(cursor.getString(cursor.getColumnIndex(COLUMN_LONGITUDE)));
            String img = cursor.getString(cursor.getColumnIndex(COLUMN_PICTURE));

            placeObjects.add(new PlaceObject(placeId,img,placeName,placeAddress,new LatLng(lat,lng)));

            while (cursor.moveToNext()) {
                placeName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                placeAddress = cursor.getString(cursor.getColumnIndex(COLUMN_ADRESS));
                placeId = cursor.getString(cursor.getColumnIndex(COLUMN_PLACE_ID));
                lat = Double.parseDouble(cursor.getString(cursor.getColumnIndex(COLUMN_LATITUDE)));
                lng = Double.parseDouble(cursor.getString(cursor.getColumnIndex(COLUMN_LONGITUDE)));
                img = cursor.getString(cursor.getColumnIndex(COLUMN_PICTURE));

                placeObjects.add(new PlaceObject(placeId,img,placeName,placeAddress,new LatLng(lat,lng)));
            }
        }
        database.close();

        HashMap<String,ArrayList<PlaceObject>> m = new HashMap<>();
        m.put(search_et,placeObjects);
        return m;

    }


    public void deleteAll (String tableName){

        String sql = "DELETE FROM " +  tableName;
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL(sql);
        database.close();

    }

    public void deleteOneItem (String place_id){

        String sql = "DELETE FROM " + PREFERRED_TABLE_NAME + " WHERE " + COLUMN_PLACE_ID + "='" + place_id +"'";
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL(sql);
        database.close();
    }


}

