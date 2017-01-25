package io.github.skepter.devrantwear;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.scorpiac.javarant.DevRant;
import com.scorpiac.javarant.Rant;
import com.scorpiac.javarant.Sort;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import static com.google.android.gms.wearable.DataMap.TAG;

public class MainActivityWear extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener {

    private TextView mTextView;

    private void log(String m) {
        Log.i(this.getClass().getName(), m);
    }

    public static final String WEARABLE_MAIN = "WearableMain";

    private Node mNode;
    private GoogleApiClient mGoogleApiClient;
    private static final String WEAR_PATH = "/from-wear";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_wear);
        log("Starting application!");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        //Rant[] rants = DevRant.getRants(Sort.ALGO, 1, 0);
//        Rant randomRant = DevRant.surprise();
//
//        FragmentManager manager = getFragmentManager();
//        FragmentTransaction transaction = manager.beginTransaction();
//        CardFragment fragment = CardFragment.create(getString(R.string.card_title), randomRant.getContent());
//        transaction.add(R.id.frame_layout, fragment);
//        transaction.commit();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //your codes here

//        log("Looking for rant...");
//        String rant = randomRant();
//        //Rant randomRant = DevRant.surprise();
//        log("Found a rant!");
//        log("Rant: " + rant);
//        //log("Rant info:" + randomRant.getContent());
//
//        FragmentManager manager = getFragmentManager();
//        FragmentTransaction transaction = manager.beginTransaction();
//        CardFragment fragment = CardFragment.create(getString(R.string.card_title), rant);
//        //CardFragment fragment = CardFragment.create(getString(R.string.card_title), randomRant.getContent());
//        transaction.add(R.id.frame_layout, fragment);
//        transaction.commit();

    }
//
//    private String randomRant() {
//        HttpURLConnection connection;
//        InputStream inputStream;
//        String out = "";
//
//        try {
//            // Create the URL and connection, get the input stream.
//            connection = (HttpURLConnection) new URL("https://www.devrant.io/api/devrant/rants/surprise?app=3").openConnection();
//            inputStream = connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream();
//            Scanner s = new Scanner(inputStream).useDelimiter("\\A");
//            String result = s.hasNext() ? s.next() : "";
//            s.close();
//            //System.out.println(result);
//            out = result.substring(result.indexOf("text\":\""), result.indexOf("\",\"num_upvotes")).substring(7);
//            out = out.replace("\\n", "\n");
//            inputStream.close();
//            connection.disconnect();
//        } catch (IOException i) {
//            i.printStackTrace();
//        }
//        return out;
//    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodes) {
                        for(Node node : nodes.getNodes()) {
                            if(node != null && node.isNearby()) {
                                mNode = node;
                                Log.d(WEARABLE_MAIN, "Connected to " + mNode.getDisplayName());
                                onceConnected();
                            }
                        }
                        if(mNode == null) {
                            Log.d(WEARABLE_MAIN, "Not connected");
                        }
                    }
                });
    }

    private void onceConnected() {
        sendMessage("Sending message to phone");
    }

    private void sendMessage(String s) {
        if(mNode != null && mGoogleApiClient != null) {
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mNode.getId(), WEAR_PATH, s.getBytes())
                    .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                            if(!sendMessageResult.getStatus().isSuccess()) {
                                Log.d(WEARABLE_MAIN, "Failed message: " + sendMessageResult.getStatus().getStatusCode());
                            } else {
                                Log.d(WEARABLE_MAIN, "Message succeeded");
                            }
                        }
                    });
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    private void displayCard(String[] contents) {
//        FragmentManager manager = getFragmentManager();
//        FragmentTransaction transaction = manager.beginTransaction();
//        CardFragment fragment = CardFragment.create(getString(R.string.card_title), data);
//        transaction.add(R.id.frame_layout, fragment);
//        transaction.commit();

        GridViewPager gridViewPager = (GridViewPager) findViewById(R.id.gridViewPager);
        gridViewPager.setAdapter(new FragmentGridPagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getFragment(int row, int col) {
                String title = contents[0];
                String content = contents[1];
                return CardFragment.create(title, content);
            }

            @Override
            public int getRowCount() {
                return 1;
            }

            @Override
            public int getColumnCount(int rowNum) {
                return 1;
            }
        });

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event: dataEvents) {

            Log.d("[DEBUG] onDataChanged",
                    "Event received: " + event.getDataItem().getUri());

            String eventUri = event.getDataItem().getUri().toString();

            if (eventUri.contains ("/myapp/myevent")) {

                DataMapItem dataItem = DataMapItem.fromDataItem (event.getDataItem());
                String data[] = dataItem.getDataMap().getStringArray("contents");

                displayCard(data);


                Log.d("[DEBUG] onDataChanged", "Sending timeline to the listener");
            }
        }
    }
}

