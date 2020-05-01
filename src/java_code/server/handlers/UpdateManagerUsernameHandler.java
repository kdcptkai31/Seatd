package java_code.server.handlers;

import com.google.gson.JsonObject;
import com.pubnub.api.PubNub;
import java_code.database.DBManager;
import java_code.server.MessageHandler;
import java_code.server.Server;

public class UpdateManagerUsernameHandler implements MessageHandler {

    Server server;

    public UpdateManagerUsernameHandler(Server server){this.server = server;}

    @Override
    public void handleMessage(JsonObject data, PubNub pubnub, String clientId) {

        DBManager.updateManagerUsername(data.get("venueID").getAsInt(), data.get("newUsername").getAsString());
        server.updateAdminPage();

    }
}
