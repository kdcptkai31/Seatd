package java_code.server.handlers;

import com.google.gson.JsonObject;
import com.pubnub.api.PubNub;
import java_code.server.MessageHandler;
import java_code.server.Server;

public class GetWaitlistDataHandler implements MessageHandler {

    private Server server;

    public GetWaitlistDataHandler(Server server){this.server = server;}

    @Override
    public void handleMessage(JsonObject data, PubNub pubnub, String clientId) {

        server.updateCurrentVenueData();
        server.sendUpdateWaitlistData();

    }
}
