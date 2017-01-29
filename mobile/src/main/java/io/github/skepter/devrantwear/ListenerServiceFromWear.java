package io.github.skepter.devrantwear;

import android.os.AsyncTask;
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

import io.github.skepter.devrantwear.io.github.skepter.devrantwear.devrant.DevRantAccessor;
import io.github.skepter.devrantwear.io.github.skepter.devrantwear.devrant.Rant;

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
            Rant rant = getRantFromAccessor();
            sendToWatch(rant);
        }
    }

    private void sendToWatch(Rant rant) {
        new DataTask(rant).execute();
    }

    private Rant getRantFromAccessor() {
        return new DevRantAccessor().getRant();
    }
}

class DataTask extends AsyncTask<Node, Void, Void> {

    private final Rant rant;

    public DataTask (Rant rant) {
        this.rant = rant;
    }

    @Override
    protected Void doInBackground(Node... nodes) {

        PutDataMapRequest dataMap = PutDataMapRequest.create("/wear-path");
        dataMap.getDataMap().putString("rantID", String.valueOf(rant.getId()));
        dataMap.getDataMap().putString("rantContent", rant.getText());
        dataMap.getDataMap().putString("rantUsername", rant.getUsername());

        PutDataRequest request = dataMap.asPutDataRequest();

        DataApi.DataItemResult dataItemResult = Wearable.DataApi
                .putDataItem(googleApiClient, request).await();

        Log.d (ListenerServiceFromWear.LOG_TAG, dataItemResult.getStatus().getStatusMessage());
        return null;
    }
}
