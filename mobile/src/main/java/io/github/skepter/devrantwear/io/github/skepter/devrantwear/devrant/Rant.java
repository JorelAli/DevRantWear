package io.github.skepter.devrantwear.io.github.skepter.devrantwear.devrant;

import com.google.gson.JsonObject;

/**
 * Created by Jorel on 29/01/2017.
 */

public class Rant {

    int id;
    String text;
    String username;
    boolean image;

    public int getId() {
        return id;
    }

    public boolean hasImage() {
        return image;
    }

    public String getText() {
        return text;
    }

    public String getUsername() {
        return username;
    }

    public Rant(JsonObject json) {
        JsonObject rant = json.get("rant").getAsJsonObject();
        id = rant.get("id").getAsInt();
        text = rant.get("text").getAsString();
        username = rant.get("user_username").getAsString();
        image = rant.get("attached_image").isJsonObject();
    }

}
