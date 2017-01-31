package io.github.skepter.devrantwear;

import com.google.android.gms.wearable.DataMapItem;

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

    public Rant(DataMapItem dataItem) {
        rantID = dataItem.getDataMap().getString("rantID");
        rantContent = dataItem.getDataMap().getString("rantContent");
        username = dataItem.getDataMap().getString("rantUsername");
        hasComments = dataItem.getDataMap().getBoolean("hasComments");
        if(hasComments) {
            commentIDs = dataItem.getDataMap().getStringArray("commentIDs");
            commentBodys = dataItem.getDataMap().getStringArray("commentBodys");
            //displayCard(rantID, rantContent, rantUsername, commentIDs, commentBodys);
        } else {
            //displayCard(rantID, rantContent, rantUsername);
        }
    }

}
