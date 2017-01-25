package io.github.skepter.devrantwear;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

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
            String[] rant = getRandomRant();

            Log.d(LOG_TAG, "Found a rant!");
            Log.d(LOG_TAG, "Rant: " + rant);

            sendToWatch(rant);

        }
    }

    private void sendToWatch(String[] contents) {
        new DataTask(contents).execute();
    }

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
            //System.out.println(result);

            //Terrible parsing going on here (Use GSON FOR GOODNESS SAKE!!!)
            rantID = result.substring(result.indexOf("\"id\":"), result.indexOf(",\"text")).substring(5);
            rantContent = result.substring(result.indexOf("text\":\""), result.indexOf("\",\"num_upvotes")).substring(7);
            rantContent = rantContent.replace("\\n", "\n");
            inputStream.close();
            connection.disconnect();
        } catch (IOException i) {
            i.printStackTrace();
        }
        return new String[] {rantID, rantContent};
    }
}

class DataTask extends AsyncTask<Node, Void, Void> {

    private final String[] contents;

    public DataTask (String[] contents) {
        this.contents = contents;
    }

    @Override
    protected Void doInBackground(Node... nodes) {

        PutDataMapRequest dataMap = PutDataMapRequest.create("/wear-path");
        dataMap.getDataMap().putStringArray("contents", contents);

        PutDataRequest request = dataMap.asPutDataRequest();

        DataApi.DataItemResult dataItemResult = Wearable.DataApi
                .putDataItem(googleApiClient, request).await();



        Log.d (ListenerServiceFromWear.LOG_TAG, dataItemResult.getStatus().getStatusMessage());
        return null;
    }
}
