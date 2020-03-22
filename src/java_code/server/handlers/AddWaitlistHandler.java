package java_code.server.handlers;

import com.google.gson.JsonObject;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import java_code.model.Patron;
import java_code.server.MessageHandler;
import java_code.server.Server;

public class AddWaitlistHandler implements MessageHandler {

    private Server server;

    public AddWaitlistHandler(Server server){this.server = server;}

    @Override
    public void handleMessage(JsonObject data, PubNub pubnub, String clientId) {

        int venueID = data.get("venueID").getAsInt();
        String name = data.get("name").getAsString();
        String email = data.get("email").getAsString();

        if(server.addToWaitlist(venueID,new Patron(name, email))){

            sendGoodAdd(pubnub, name);
            server.updateCurrentVenueData();
            server.sendUpdateWaitlistData(pubnub);

        }else
            sendBadAdd(pubnub, name);

    }

    private void sendGoodAdd(PubNub pubnub, String name){

        JsonObject msg = new JsonObject();
        msg.addProperty("type", "goodWaitlistAdd");

        JsonObject data = new JsonObject();
        data.addProperty("name", name);
        msg.add("data", data);

        try {
            pubnub.publish()
                    .channel("main")
                    .message(msg)
                    .sync();
            System.out.println(name.concat(" was added to the waitlist"));
        } catch (PubNubException e) {
            e.printStackTrace();
        }

    }

    private void sendBadAdd(PubNub pubnub, String name){

        JsonObject msg = new JsonObject();
        msg.addProperty("type", "badWaitlistAdd");

        JsonObject data = new JsonObject();
        data.addProperty("name", name);
        msg.add("data", data);

        try {
            pubnub.publish()
                    .channel("main")
                    .message(msg)
                    .sync();
            System.out.println(name.concat(" tried to add to the waitlist and failed"));
        } catch (PubNubException e) {
            e.printStackTrace();
        }

    }

}
