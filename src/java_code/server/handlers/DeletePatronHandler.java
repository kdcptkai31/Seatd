package java_code.server.handlers;

import com.google.gson.JsonObject;
import com.pubnub.api.PubNub;
import java_code.database.DBManager;
import java_code.server.MessageHandler;
import java_code.server.Server;

public class DeletePatronHandler implements MessageHandler {

    Server server;

    public DeletePatronHandler(Server server){this.server = server;}

    @Override
    public void handleMessage(JsonObject data, PubNub pubnub, String clientId) {

        int venueID = data.get("venueID").getAsInt();
        String name = data.get("user_name").getAsString();
        String email = data.get("email").getAsString();

        if(DBManager.deletePatronFromVenueWaitlist(venueID, name, email)){

            server.updateCurrentVenueData();
            server.sendUpdateWaitlistData();
            server.sendUpdateWaitlist(venueID);
            server.updateManagerPage(data);

        }

    }
}
