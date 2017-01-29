package io.github.skepter.devrantwear.io.github.skepter.devrantwear.devrant;

import android.os.StrictMode;
import android.util.Log;

import com.google.gson.Gson;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Jorel on 28/01/2017.
 */

/*
Some kind of class to access devRant

JavaRant is available, but I would rather learn how to use
GSON by myself now and then be able to apply it in the future.

Also, JavaRant uses Java 8 with lambda expressions which are
not compatible with Android (unfortunately).
 */
public class DevRantAccessor {


    public RawRant getRant() {

        HttpURLConnection connection;
        InputStream inputStream;
        try {
            Log.d("DevRantAccessor", "Retrieving rant...");
            connection = (HttpURLConnection) new URL("https://www.devrant.io/api/devrant/rants/surprise?app=3").openConnection();
            inputStream = connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream();
            Scanner s = new Scanner(inputStream).useDelimiter("\\A");
            String result = s.hasNext() ? s.next() : "";
            s.close();
            Log.d("DevRantAccessor", "Received raw rant: " + result);
            result = result.replaceAll("\n", "\n");
            result = result.replaceAll("\\\\", "");
            Log.d("DevRantAccessor", "Parsed rant: " + result);

            Gson gson = new Gson();
            return gson.fromJson(result, RawRant.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


}
