package io.github.skepter.devrantwear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

/**
 * Created by Jorel on 29/01/2017.
 */

public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_screen);
        Log.d("DevRantWear (Wear)", "Showing splash");

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                initMainScreen();
            }
        }, 2000);

    }

    private void initMainScreen() {

        Log.d("DevRantWear (Wear)", "Starting main activity");
        Intent intent = new Intent(this, MainActivityWear.class);
        startActivity(intent);
        finish();
    }
}
