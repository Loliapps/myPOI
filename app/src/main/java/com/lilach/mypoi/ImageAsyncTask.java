package com.lilach.mypoi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageAsyncTask extends AsyncTask<String,Void,Bitmap> {

    private OnImageDownloadCompleteListener listener;



    public ImageAsyncTask(OnImageDownloadCompleteListener listener){
        this.listener = listener;
    }

    @Override
    protected Bitmap doInBackground(String... params) {

        InputStream in = null;
        Bitmap bitmap = null;

        try {

            URL url = new URL("https://maps.googleapis.com/maps/api/place/photo?maxwidth=120&photoreference="+params[0]+"&key=AIzaSyBiMBQfs2ZMMl5Lch-mU-RkoIquLQ-X0aU");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            in = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(in);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }


    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        listener.onImageDownloadComplete(bitmap);
    }
}
