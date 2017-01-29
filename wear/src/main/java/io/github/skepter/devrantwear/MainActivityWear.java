package io.github.skepter.devrantwear;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.text.Html;
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

    public static final String LOG_TAG = "DevRantWear (Wear)";

    private Node mNode;
    private GoogleApiClient mGoogleApiClient;
    private static final String WEAR_PATH = "/from-wear";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.activity_main_wear);



        Log.d(LOG_TAG, "Starting MainActivityWear!");

        //Builds the Google API Client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //TODO:
        //Add something to say "yeah we're connected" or "nah, dunno where that phone is at fam"
        //Loading spinny wheel icons?

        //Start implementing comments

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
                                requestRandomRant();
                            }
                        }
                        if(mNode == null) {
                            Log.d(LOG_TAG, "Not connected");
                        }
                    }
                });
    }

    //Actions to perform once connected to the phone
    private void requestRandomRant() {
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
                                Log.d(LOG_TAG, "Failed to send message to phone: " + sendMessageResult.getStatus().getStatusCode());
                            } else {
                                Log.d(LOG_TAG, "Message send to phone successfully");
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

        /*
        Planned design for grid:

        [rant] [Comments] [+ button to request new rant]
               [Comments]
               [Comments]
               [Comments]
         */

        DotsPageIndicator pages = new DotsPageIndicator(this);

        GridViewPager gridViewPager = (GridViewPager) findViewById(R.id.gridViewPager);
        pages.setPager(gridViewPager);
        gridViewPager.setAdapter(new FragmentGridPagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getFragment(int row, int col) {
                switch(col) {
                    case 0:
                        String title = "Rant ID: "+ contents[0];
                        //Testing to see if bold text renders properly in preparation for comment formatting
                        String content = "<b>" + contents[2] + ": </b>" + contents[1];
                        return CardFragment.create(title, Html.fromHtml(content));
                    case 1:
                        return ActionFragment.create(R.drawable.ic_full_action, R.string.new_rant, new ActionFragment.Listener() {
                            @Override
                            public void onActionPerformed() {
                                requestRandomRant();
                            }
                        });
                    default:
                        //This case should never occur.
                        Log.d(LOG_TAG, "Unexpected case found!");
                        return null;
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

            String eventUri = event.getDataItem().getUri().toString();

            if (eventUri.contains ("/wear-path")) {

                DataMapItem dataItem = DataMapItem.fromDataItem(event.getDataItem());
                String rantID = dataItem.getDataMap().getString("rantID");
                String rantContent = dataItem.getDataMap().getString("rantContent");
                String rantUsername = dataItem.getDataMap().getString("rantUsername");


                String[] data = new String[] {rantID, rantContent, rantUsername};
                displayCard(data);

            }
        }
    }
}

