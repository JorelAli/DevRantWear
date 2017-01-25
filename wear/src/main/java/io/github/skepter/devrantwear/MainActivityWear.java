package io.github.skepter.devrantwear;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.util.Log;

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

public class MainActivityWear extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener {

    private void log(String m) {
        Log.i(this.getClass().getName(), m);
    }

    public static final String LOG_TAG = "DevRantWear (Wear)";

    private Node mNode;
    private GoogleApiClient mGoogleApiClient;
    private static final String WEAR_PATH = "/from-wear";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_wear);
        log("Starting application!");

        //Builds the Google API Client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //Allows Networking? (This might be unnecessary for the wear class)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

    }

    @Override
    //Once connected to the phone
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodes) {
                        for(Node node : nodes.getNodes()) {
                            if(node != null && node.isNearby()) {
                                //The name of the device connected (phone's Identifier)
                                mNode = node;
                                Log.d(LOG_TAG, "Connected to " + mNode.getDisplayName());
                                onceConnected();
                            }
                        }
                        if(mNode == null) {
                            Log.d(LOG_TAG, "Not connected");
                        }
                    }
                });
    }

    //Actions to perform once connected to the phone
    private void onceConnected() {
        sendMessage("Requesting Rant");
    }

    //Sends a message to the phone (Requesting Rant)
    private void sendMessage(String s) {
        if(mNode != null && mGoogleApiClient != null) {
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mNode.getId(), WEAR_PATH, s.getBytes())
                    .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                            if(!sendMessageResult.getStatus().isSuccess()) {
                                Log.d(LOG_TAG, "Failed message: " + sendMessageResult.getStatus().getStatusCode());
                            } else {
                                Log.d(LOG_TAG, "Message succeeded");
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

    //displays the rant
    private void displayCard(String[] contents) {
//        FragmentManager manager = getFragmentManager();
//        FragmentTransaction transaction = manager.beginTransaction();
//        CardFragment fragment = CardFragment.create(getString(R.string.card_title), data);
//        transaction.add(R.id.frame_layout, fragment);
//        transaction.commit();

        /*
        Planned design for grid:

        [rant] [Comments] [+ button to request new rant]
               [Comments]
               [Comments]
               [Comments]
         */

        GridViewPager gridViewPager = (GridViewPager) findViewById(R.id.gridViewPager);
        gridViewPager.setAdapter(new FragmentGridPagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getFragment(int row, int col) {
                switch(col) {
                    case 0:
                        String title = contents[0];
                        String content = contents[1];
                        return CardFragment.create(title, content);
                    case 1:
                        return ActionFragment.create(R.drawable.ic_cc_checkmark, R.string.new_rant, new ActionFragment.Listener() {
                            @Override
                            public void onActionPerformed() {
                                onceConnected();
                            }
                        });
                    default:
                        String title1 = contents[0];
                        String content1 = contents[1];
                        return CardFragment.create(title1, content1);
                }
            }

            @Override
            public int getRowCount() {
                return 1;
            }

            @Override
            public int getColumnCount(int rowNum) {
                return 2;
            }
        });

    }

    @Override
    //When data has been received from the phone
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event: dataEvents) {

            Log.d(LOG_TAG, "Event received: " + event.getDataItem().getUri());

            String eventUri = event.getDataItem().getUri().toString();

            if (eventUri.contains ("/wear-path")) {

                DataMapItem dataItem = DataMapItem.fromDataItem(event.getDataItem());
                String data[] = dataItem.getDataMap().getStringArray("contents");

                displayCard(data);


                Log.d(LOG_TAG, "Sending timeline to the listener");
            }
        }
    }
}

