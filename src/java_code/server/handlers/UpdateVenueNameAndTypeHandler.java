package java_code.server.handlers;

import com.google.gson.JsonObject;
import com.pubnub.api.PubNub;
import java_code.database.DBManager;
import java_code.server.MessageHandler;
import java_code.server.Server;

public class UpdateVenueNameAndTypeHandler implements MessageHandler {

    Server server;

    public UpdateVenueNameAndTypeHandler(Server server){this.server = server;}

    @Override
    public void handleMessage(JsonObject data, PubNub pubnub, String clientId) {

        DBManager.updateVenueNameAndType(data.get("venueID").getAsInt(), data.get("newName").getAsString(),
                                         data.get("newType").getAsString());
        server.updateAdminPage();

    }
}
