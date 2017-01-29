package io.github.skepter.devrantwear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import java.util.concurrent.TimeUnit;

/**
 * Created by Jorel on 29/01/2017.
 */

public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_screen);
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(3));

        Log.d("DevRantWear (Wear)", "Showing splash and starting next activity");

        Intent intent = new Intent(this, MainActivityWear.class);
        startActivity(intent);
        finish();
    }
}
