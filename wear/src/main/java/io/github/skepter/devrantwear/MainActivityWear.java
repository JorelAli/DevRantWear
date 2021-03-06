package io.github.skepter.devrantwear;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ProgressBar;
import android.widget.Toast;

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

import java.util.LinkedList;
import java.util.Queue;

public class MainActivityWear extends WearableActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener {

    public static final String LOG_TAG = "DevRantWear (Wear)";
    private static final int MAX_RANT_QUEUE = 10;

    private Node mNode;
    private GoogleApiClient mGoogleApiClient;
    private static final String WEAR_PATH = "/from-wear";
    private ProgressBar bar;
    private Queue<Rant> rantsQueue;
    private boolean displayRantOnReceive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.activity_main_wear);

        Log.d(LOG_TAG, "Starting MainActivityWear!");

        rantsQueue = new LinkedList<Rant>();

        //Builds the Google API Client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //This code might be unnecessary - but you know, it works with this... dunno if it works without
        bar = (ProgressBar) findViewById(R.id.progressBar);
        //bar.setProgress(20);
        findViewById(R.id.imageView).setAlpha(0);
        findViewById(R.id.textView).setAlpha(0);

    }

    private void addRantToQueue(Rant rant) {
        if(rantsQueue.size() != MAX_RANT_QUEUE) {
            rantsQueue.add(rant);
        } else {
            //Discard rant :(
        }
    }

    @Override /* KeyEvent.Callback */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_NAVIGATE_NEXT:
                return nextRant();
            case KeyEvent.KEYCODE_NAVIGATE_PREVIOUS:
                return nextRant();
        }
        // If you did not handle it, let it be handled by the next possible element as deemed by the Activity.
        return super.onKeyDown(keyCode, event);
    }

    private boolean nextRant() {
        boolean handled = true;
        displayNextRant();
        return handled;
    }

    private void displayNextRant() {
        if(rantsQueue.size() == 0) {
            bar.setAlpha(1);
            requestRandomRant();
            displayRantOnReceive = true;
            return;
        }
        Rant rant = rantsQueue.remove();
        if(rant.hasComments) {
            displayCard(rant.rantID, rant.rantContent, rant.username, rant.commentIDs, rant.commentBodys);
        } else {
            displayCard(rant.rantID, rant.rantContent, rant.username);
        }
        requestRandomRant();
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
                                for(int i = 0; i < MAX_RANT_QUEUE; i++) {
                                    requestRandomRant();
                                }
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
    private void displayCard(final String rantID, final String contents, final String username) {
        bar.setAlpha(0);

        GridViewPager gridViewPager = (GridViewPager) findViewById(R.id.gridViewPager);
        gridViewPager.setAdapter(new FragmentGridPagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getFragment(int row, int col) {
                switch(col) {
                    case 0:
                        String title = "Rant ID: "+ rantID;
                        //Testing to see if bold text renders properly in preparation for comment formatting
                        String content = "<b>" + username + ": </b>" + contents;
                        CardFragment card = CardFragment.create(title, Html.fromHtml(content));
                        card.setExpansionFactor(20.0F);
                        return card;
                    case 1:
                        return ActionFragment.create(R.drawable.ic_full_action, R.string.new_rant, new ActionFragment.Listener() {
                            @Override
                            public void onActionPerformed() {
                                displayNextRant();
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

    private void displayCard(final String rantID, final String contents, final String username, final String[] commentIDs, final String[] commentBodys) {
        bar.setAlpha(0);

        GridViewPager gridViewPager = (GridViewPager) findViewById(R.id.gridViewPager);
        gridViewPager.setAdapter(new FragmentGridPagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getFragment(int row, int col) {
                switch(col) {
                    case 0:
                        String title = "Rant ID: "+ rantID;
                        //Testing to see if bold text renders properly in preparation for comment formatting
                        String content = "<b>" + username + ": </b>" + contents;
                        return CardFragment.create(title, Html.fromHtml(content));
                    case 1:
                        StringBuilder builder = new StringBuilder("");
                        for(int i = 0; i < commentIDs.length; i++) {
                            builder.append("<b>" + commentIDs[i] + ": </b>");
                            builder.append(commentBodys[i]);
                            if(i != commentIDs.length - 1) {
                                builder.append("<br><br>");
                            }
                        }

                        CardFragment card = CardFragment.create("Comments", Html.fromHtml(builder.toString()));
                        card.setExpansionFactor(20.0F);
                        return card;
                    case 2:
                        return ActionFragment.create(R.drawable.ic_full_action, R.string.new_rant, new ActionFragment.Listener() {
                            @Override
                            public void onActionPerformed() {
                                displayNextRant();
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
                return 3;
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
                if(dataItem.getDataMap().getBoolean("networkDead")) {
                    Log.d(LOG_TAG, "Network dead message received");
                    findViewById(R.id.gridViewPager).setAlpha(0);
                    bar.setAlpha(0);
                    findViewById(R.id.imageView).setAlpha(1);
                    findViewById(R.id.textView).setAlpha(1);
                    return;
                } else {
                    addRantToQueue(new Rant(dataItem));
                    if(displayRantOnReceive) {
                        displayRantOnReceive = false;
                        displayNextRant();
                    }
                }
            }
        }
    }
}

