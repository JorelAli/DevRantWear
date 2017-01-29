package io.github.skepter.devrantwear.io.github.skepter.devrantwear.devrant;

import com.google.gson.JsonObject;

/**
 * Created by Jorel on 29/01/2017.
 */

public class Comment {

    String username;
    String text;

    public String getUsername() {
        return username;
    }

    public String getText() {
        return text;
    }

    public Comment(JsonObject json) {
        username = json.get("user_username").getAsString();
        text = json.get("body").getAsString();
    }

}
