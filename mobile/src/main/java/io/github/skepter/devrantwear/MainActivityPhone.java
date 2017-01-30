package io.github.skepter.devrantwear;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class MainActivityPhone extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static GoogleApiClient googleApiClient;
    public static Context myContext;

    public final static String TAG = "MainActivityPhone";

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_phone);

        myContext = this.getApplicationContext();
        
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .addApi(AppIndex.API).build();

        googleApiClient.connect();

        Log.d(TAG, "Application started!");
        Log.d(TAG, "Network status: " + (isNetworkAvailable() ? "Good" : "Potato"));
    }

    private void check(final GoogleApiClient client) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Node> connectedNodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes();
                if(connectedNodes.size() == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView t = (TextView) findViewById(R.id.textView);
                            t.setText("No devices found D:");
                        }
                    });
                    return;
                }
                for(final Node node : connectedNodes) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView t = (TextView) findViewById(R.id.textView);
                            t.setText("Connected to device: " + node.getDisplayName());
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        check(googleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

}
