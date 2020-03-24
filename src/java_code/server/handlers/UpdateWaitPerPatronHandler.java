package java_code.server.handlers;

import com.google.gson.JsonObject;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import java_code.database.DBManager;
import java_code.server.MessageHandler;
import java_code.server.Server;

public class UpdateWaitPerPatronHandler implements MessageHandler {

    Server server;

    public UpdateWaitPerPatronHandler(Server server){this.server = server;}

    @Override
    public void handleMessage(JsonObject data, PubNub pubnub, String clientId) {

        int venueID = data.get("venueID").getAsInt();
        int waitPerPatronValue = data.get("wppValue").getAsInt();

        if(DBManager.updateVenueWaitPerPatron(venueID, waitPerPatronValue)){

            server.updateManagerPage(data);
            server.updateCurrentVenueData();
            server.sendUpdateWaitlistData();
            server.updateVenueListData();

        }else{//WaitPerPatron not updated

            JsonObject msg = new JsonObject();
            msg.addProperty("type", "badWaitPerPatronUpdate");
            JsonObject data1 = new JsonObject();
            data.addProperty("venueID", venueID);
            msg.add("data", data1);

            try {
                pubnub.publish()
                        .channel("main")
                        .message(msg)
                        .sync();
            } catch (PubNubException e) {
                e.printStackTrace();
            }

        }

    }
}
