package io.github.skepter.devrantwear;

/**
 * Created by Jorel on 31/01/2017.
 */

public class Rant {

    String rantID;
    String rantContent;
    String username;
    boolean hasComments;
    String[] commentIDs;
    String[] commentBodys;

    public Rant(String rantID, String rantContent, String username, boolean hasComments) {
        this.rantID = rantID;
        this.rantContent = rantContent;
        this.username = username;
        this.hasComments = hasComments;

    }

}
