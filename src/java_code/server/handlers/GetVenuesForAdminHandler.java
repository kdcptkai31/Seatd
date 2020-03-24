package java_code.server.handlers;

import com.google.gson.JsonObject;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import java_code.database.DBManager;
import java_code.server.MessageHandler;
import java_code.server.Server;

import java.util.Vector;

public class GetVenuesForAdminHandler implements MessageHandler {

    Server server;

    public GetVenuesForAdminHandler(Server server){this.server = server;}

    @Override
    public void handleMessage(JsonObject data, PubNub pubnub, String clientId) {

        server.updateAdminPage();

    }
}
