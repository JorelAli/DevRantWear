package io.github.skepter.devrantwear;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import io.github.skepter.devrantwear.io.github.skepter.devrantwear.devrant.Comment;
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

        if(googleApiClient == null) {
            Intent intent = new Intent(this, MainActivityPhone.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }


        if(messageEvent.getPath().equals(WEARPATH)) {
            String data = new String(messageEvent.getData());
            Log.d(LOG_TAG, "Received message: " + data);

            Log.d(LOG_TAG, "Looking for rant...");
            Rant rant = new DevRantAccessor().getRant();
            Comment[] comments = new DevRantAccessor().getComments(rant.getId());

            PutDataMapRequest dataMap = PutDataMapRequest.create("/wear-path");
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


            PutDataRequest request = dataMap.asPutDataRequest();

            DataApi.DataItemResult dataItemResult = Wearable.DataApi
                    .putDataItem(googleApiClient, request).await();

            Log.d (ListenerServiceFromWear.LOG_TAG, dataItemResult.getStatus().getStatusMessage());

        }
    }

}