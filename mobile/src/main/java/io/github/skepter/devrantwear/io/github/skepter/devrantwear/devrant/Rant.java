package io.github.skepter.devrantwear.io.github.skepter.devrantwear.devrant;

/**
 * Created by Jorel on 29/01/2017.
 */

public class Rant {

    int id;
    String text;

    public int getNum_upvotes() {
        return num_upvotes;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public int getNum_downvotes() {
        return num_downvotes;
    }

    public int getScore() {
        return score;
    }

    public long getCreated_time() {
        return created_time;
    }

    public String getAttached_image() {
        return attached_image;
    }

    public int getNum_comments() {
        return num_comments;
    }

    public String[] getTags() {
        return tags;
    }

    public int getVote_state() {
        return vote_state;
    }

    public boolean isEdited() {
        return edited;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getUser_username() {
        return user_username;
    }

    public int getUser_score() {
        return user_score;
    }

    int num_upvotes;
    int num_downvotes;
    int score;
    long created_time;
    String attached_image;
    int num_comments;
    String[] tags;
    int vote_state;
    boolean edited;
    int user_id;
    String user_username;
    int user_score;

}
