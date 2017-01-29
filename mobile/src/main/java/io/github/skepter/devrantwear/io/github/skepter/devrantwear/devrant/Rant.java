package io.github.skepter.devrantwear.io.github.skepter.devrantwear.devrant;

import com.google.gson.JsonObject;

/**
 * Created by Jorel on 29/01/2017.
 */

public class Rant {

    int id;
    String text;

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public Rant(JsonObject json) {
        JsonObject rant = json.get("rant").getAsJsonObject();
        id = rant.get("id").getAsInt();
        text = rant.get("text").getAsString();
    }

}
