package java_code.server.handlers;

import com.google.gson.JsonObject;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import java_code.database.DBManager;
import java_code.model.ManagerAccount;
import java_code.server.MessageHandler;
import java_code.server.Server;

public class ServerLoginHandler implements MessageHandler {

    private Server server;

    public ServerLoginHandler(Server server){ this.server = server;}

    @Override
    public void handleMessage(JsonObject data, PubNub pubnub, String clientId) {

        String username = data.get("username").getAsString();
        String password = data.get("password").getAsString();


        ManagerAccount player = DBManager.getManagerAccountByUsername(username);
        if (player == null) {
            sendBadLogin(pubnub, username);
            return;
        }

        if (!player.checkPassword(password)) {
            sendBadLogin(pubnub, username);
            return;
        }

        server.addUser(data.get("UUID").getAsString(), username);
        sendLoggedIn(pubnub, username);
    }

    private void sendLoggedIn(PubNub pubnub, String username) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "loggedIn");

        JsonObject data = new JsonObject();
        data.addProperty("username", username);

        msg.add("data", data);

        try {
            pubnub.publish()
                    .channel("main")
                    .message(msg)
                    .sync();
        } catch (PubNubException e) {
            e.printStackTrace();
        }
    }

    private void sendBadLogin(PubNub pubnub, String username) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "badLogin");

        JsonObject data = new JsonObject();
        data.addProperty("username", username);
        msg.add("data", data);

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
