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

import io.github.skepter.devrantwear.io.github.skepter.devrantwear.devrant.DevRantAccessor;
import io.github.skepter.devrantwear.io.github.skepter.devrantwear.devrant.RawRant;

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

            Log.d(LOG_TAG, "Looking for rant...");
            RawRant rant = getRantFromAccessor();
            sendToWatch(rant);
            //String[] rant = getRandomRant();

//            Log.d(LOG_TAG, "Found a rant!");
//            Log.d(LOG_TAG, "Rant: " + Arrays.toString(rant));

//            sendToWatch(rant);

        }
    }

    private void sendToWatch(RawRant rant) {
        new DataTask(rant).execute();
    }

    private void getComments(String rantID) {
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL("https://www.devrant.io/api/devrant/rants/" + rantID + "?app=3").openConnection();
        } catch(Exception e) {

        }
    }

    private RawRant getRantFromAccessor() {
        return new DevRantAccessor().getRant();
    }

    /**
    Returns [rantID, rant]
     */
    @Deprecated
    private String[] getRandomRant() {
        HttpURLConnection connection;
        InputStream inputStream;
        String rantID = "";
        String rantContent = "";

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

            inputStream.close();
            connection.disconnect();
        } catch (IOException i) {
            i.printStackTrace();
        }
        return new String[] {rantID, rantContent};
    }
}

class DataTask extends AsyncTask<Node, Void, Void> {

    private final RawRant rant;

    public DataTask (RawRant rant) {
        this.rant = rant;
    }

    @Override
    protected Void doInBackground(Node... nodes) {

        PutDataMapRequest dataMap = PutDataMapRequest.create("/wear-path");
        dataMap.getDataMap().putString("rantID", String.valueOf(rant.getRant().getId()));
        dataMap.getDataMap().putString("rantContent", rant.getRant().getText());

        PutDataRequest request = dataMap.asPutDataRequest();

        DataApi.DataItemResult dataItemResult = Wearable.DataApi
                .putDataItem(googleApiClient, request).await();

        Log.d (ListenerServiceFromWear.LOG_TAG, dataItemResult.getStatus().getStatusMessage());
        return null;
    }
}
