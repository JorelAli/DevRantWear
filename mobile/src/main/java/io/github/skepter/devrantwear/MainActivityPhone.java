package io.github.skepter.devrantwear;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class MainActivityPhone extends AppCompatActivity {

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

        Log.d(TAG, "Application started!");
        Log.d(TAG, "Network status: " + (isNetworkAvailable() ? "Good" : "Potato"));
        Toast.makeText(this, "devRantWear Service Started", Toast.LENGTH_LONG).show();
        finish();
    }
}
