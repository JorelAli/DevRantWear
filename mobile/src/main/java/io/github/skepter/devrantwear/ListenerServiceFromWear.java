package io.github.skepter.devrantwear;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import io.github.skepter.devrantwear.io.github.skepter.devrantwear.devrant.Comment;
import io.github.skepter.devrantwear.io.github.skepter.devrantwear.devrant.DevRantAccessor;
import io.github.skepter.devrantwear.io.github.skepter.devrantwear.devrant.Rant;


/**
 * Created by Jorel on 24/01/2017.
 */

public class ListenerServiceFromWear extends WearableListenerService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static GoogleApiClient googleApiClient;
    public static final String LOG_TAG = "DevRantWear (Device)";

    private static final String WEARPATH = "/from-wear";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.d(LOG_TAG, "I received a message!");

        if(googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Wearable.API)
                    .addApi(AppIndex.API).build();

            googleApiClient.connect();
        }


        if(messageEvent.getPath().equals(WEARPATH)) {
            Log.d(LOG_TAG, "Received message: " + new String(messageEvent.getData()));

            PutDataMapRequest dataMap = PutDataMapRequest.create("/wear-path");

            if(isNetworkAvailable()) {
                Log.d(LOG_TAG, "Looking for rant...");
                //Prefetch rants?
                Rant rant = new DevRantAccessor().getRant();
                if(rant != null) {
                    Comment[] comments = new DevRantAccessor().getComments(rant.getId());

                    dataMap.getDataMap().putBoolean("networkDead", false);

                    //Add rant info
                    dataMap.getDataMap().putString("rantID", String.valueOf(rant.getId()));
                    dataMap.getDataMap().putString("rantContent", rant.getText());
                    dataMap.getDataMap().putString("rantUsername", rant.getUsername());

                    //Add comment info
                    if(comments.length != 0) {
                        String[] commentIDs = new String[comments.length];
                        String[] commentBodys = new String[comments.length];
                        int i = 0;
                        for(Comment c : comments) {
                            commentIDs[i] = c.getUsername();
                            commentBodys[i] = c.getText();
                            i++;
                        }
                        dataMap.getDataMap().putStringArray("commentIDs", commentIDs);
                        dataMap.getDataMap().putStringArray("commentBodys", commentBodys);
                        dataMap.getDataMap().putBoolean("hasComments", true);
                    } else {
                        dataMap.getDataMap().putBoolean("hasComments", false);
                    }
                } else {
                    //Couldn't access devRant - couldn't access devRant
                    Log.d(LOG_TAG, "Couldn't access devRant");
                    dataMap.getDataMap().putBoolean("networkDead", true);
                }

            } else {
                Log.d(LOG_TAG, "Network dead. Not worth finding rant.");
                dataMap.getDataMap().putBoolean("networkDead", true);
            }

            //Send rant to watch
            PutDataRequest request = dataMap.asPutDataRequest();
            DataApi.DataItemResult dataItemResult = Wearable.DataApi
                    .putDataItem(googleApiClient, request).await();

            Log.d (ListenerServiceFromWear.LOG_TAG, dataItemResult.getStatus().getStatusMessage());
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                List<Node> connectedNodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes();
//                if(connectedNodes.size() == 0) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            TextView t = (TextView) findViewById(R.id.textView);
//                            t.setText("No devices found D:");
//                        }
//                    });
//                    return;
//                }
//                for(final Node node : connectedNodes) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            TextView t = (TextView) findViewById(R.id.textView);
//                            t.setText("Connected to device: " + node.getDisplayName());
//                        }
//                    });
//                }
//            }
//        }).start();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}