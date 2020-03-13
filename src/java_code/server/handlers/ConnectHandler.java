package java_code.server.handlers;

import com.google.gson.JsonObject;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import java_code.server.MessageHandler;
import java_code.server.Server;

public class ConnectHandler implements MessageHandler {

    private Server server;

    public ConnectHandler(Server server){this.server = server;}

    @Override
    public void handleMessage(JsonObject data, PubNub pubnub, String clientId) {

        JsonObject msg = new JsonObject();
        msg.addProperty("type", "serverPub");

        JsonObject data1 = new JsonObject();
        data1.addProperty("publicKey", "");
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
