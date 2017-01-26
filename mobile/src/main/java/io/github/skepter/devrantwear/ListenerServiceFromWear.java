package io.github.skepter.devrantwear;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;

import static android.R.attr.bitmap;
import static io.github.skepter.devrantwear.MainActivityPhone.googleApiClient;

/**
 * Created by Jorel on 24/01/2017.
 */

public class ListenerServiceFromWear extends WearableListenerService {

    public static final String LOG_TAG = "DevRantWear (Device)";

    private static final String WEARPATH = "/from-wear";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.d(LOG_TAG, "I received a message!");


        if(messageEvent.getPath().equals(WEARPATH)) {
            String data = new String(messageEvent.getData());
            Log.d(LOG_TAG, "Received message: " + data);


            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            Log.d(LOG_TAG, "Looking for rant...");
            Object[] rant = getRandomRant();

            Log.d(LOG_TAG, "Found a rant!");
            Log.d(LOG_TAG, "Rant: " + Arrays.toString(rant));

            sendToWatch(rant);

        }
    }

    private void sendToWatch(Object[] contents) {
        new DataTask(contents).execute();
    }

    public Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Object[] getRandomRant() {
        HttpURLConnection connection;
        InputStream inputStream;
        String rantID = "";
        String rantContent = "";
        String rantImageURL = "";
        Bitmap image = null;

        try {
            // Create the URL and connection, get the input stream.
            connection = (HttpURLConnection) new URL("https://www.devrant.io/api/devrant/rants/surprise?app=3").openConnection();
            inputStream = connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream();
            Scanner s = new Scanner(inputStream).useDelimiter("\\A");
            String result = s.hasNext() ? s.next() : "";
            s.close();
            Log.d(LOG_TAG, "Rant received: " + result);

            //Terrible parsing going on here (Use GSON FOR GOODNESS SAKE!!!)
            rantID = result.substring(result.indexOf("\"id\":"), result.indexOf(",\"text")).substring(5);
            rantContent = rantContent.replace("\\n", "\n");
            rantContent = rantContent.replace("\\", "");
            rantContent = result.substring(result.indexOf("text\":\""), result.indexOf("\",\"num_upvotes")).substring(7);

            if(result.contains("\"attached_image\":\"\",\"num_comments")) {
                rantImageURL = null;
            } else {
                rantImageURL = result.substring(result.indexOf("{\"url\":\""), result.indexOf("\",\"width\"")).substring(8);
                rantImageURL = rantImageURL.replace("\\/", "/");
                Log.d(LOG_TAG, "Retrieving Image from: " + rantImageURL);
                image = getBitmapFromURL(rantImageURL);
                rantContent = rantContent + "\n\nSee image below";
            }

            inputStream.close();
            connection.disconnect();
        } catch (IOException i) {
            i.printStackTrace();
        }
        return new Object[] {rantID, rantContent, image};
    }
}

class DataTask extends AsyncTask<Node, Void, Void> {

    private final Object[] contents;

    public DataTask (Object[] contents) {
        this.contents = contents;
    }

    @Override
    protected Void doInBackground(Node... nodes) {

        PutDataMapRequest dataMap = PutDataMapRequest.create("/wear-path");
        dataMap.getDataMap().putString("rantID", String.valueOf(contents[0]));
        dataMap.getDataMap().putString("rantContent", String.valueOf(contents[1]));
        if(contents[2] != null) {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            Bitmap img = (Bitmap) contents[2];
            img.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            dataMap.getDataMap().putByteArray("bitmapImage", byteStream.toByteArray());
        } else {
            dataMap.getDataMap().putByteArray("bitmapImage", new byte[] {});
        }
//        dataMap.getDataMap().putString("rantID", String.valueOf(contents[0]));
//        dataMap.getDataMap().putStringArray("contents", contents);

        PutDataRequest request = dataMap.asPutDataRequest();

        DataApi.DataItemResult dataItemResult = Wearable.DataApi
                .putDataItem(googleApiClient, request).await();



        Log.d (ListenerServiceFromWear.LOG_TAG, dataItemResult.getStatus().getStatusMessage());
        return null;
    }
}
