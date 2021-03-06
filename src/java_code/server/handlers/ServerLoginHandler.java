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

        if(username.equals("admin") && password.equals("admin")) {

            sendAdminLogin(pubnub, username);
            return;

        }

        ManagerAccount manager = DBManager.getManagerAccountByUsername(username);
        if (manager == null || !manager.checkPassword(password)) {
            sendBadLogin(pubnub, username);
            return;
        }

        //implement check if already logged in in this method ^
        sendLoggedIn(pubnub, username, manager.getVenueID());
    }

    private void sendAdminLogin(PubNub pubNub, String username){

        JsonObject msg = new JsonObject();
        msg.addProperty("type", "adminLogin");

        JsonObject data = new JsonObject();
        data.addProperty("username", username);
        msg.add("data", data);

        try {
            pubNub.publish()
                    .channel("main")
                    .message(msg)
                    .sync();
            System.out.println(username.concat(" logged in"));
        } catch (PubNubException e) {
            e.printStackTrace();
        }

    }

    private void sendLoggedIn(PubNub pubnub, String username, int venueID) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "loggedIn");

        JsonObject data = new JsonObject();
        data.addProperty("username", username);
        data.addProperty("venueID", venueID);

        msg.add("data", data);

        try {
            pubnub.publish()
                    .channel("main")
                    .message(msg)
                    .sync();
            System.out.println(username.concat(" logged in"));
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
            System.out.println(username.concat(" tried to log in and failed"));
        } catch (PubNubException e) {
            e.printStackTrace();
        }
    }

}
