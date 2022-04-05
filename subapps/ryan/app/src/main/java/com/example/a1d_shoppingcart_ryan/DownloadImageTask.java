package com.example.a1d_shoppingcart_ryan;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    customListener listener;

    public interface customListener {
        // https://guides.codepath.com/android/Creating-Custom-Listeners
        public void postExecute(Bitmap result);
    }

    DownloadImageTask(customListener callback) {
        this.listener = callback;
    }

    // https://stackoverflow.com/questions/2471935/how-to-load-an-imageview-by-url-in-android
    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        this.listener.postExecute(result);
    }
}
