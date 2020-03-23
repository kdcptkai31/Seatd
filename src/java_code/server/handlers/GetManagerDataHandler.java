package java_code.server.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import java_code.database.DBManager;
import java_code.model.Patron;
import java_code.server.MessageHandler;
import java_code.server.Server;

import java.util.Vector;

public class GetManagerDataHandler implements MessageHandler {

    private Server server;

    public GetManagerDataHandler(Server server){this.server = server;}

    @Override
    public void handleMessage(JsonObject data, PubNub pubnub, String clientId) {

        int venueID = data.get("venueID").getAsInt();
        Vector<String> nameAndWaitPerPatron = DBManager.getVenueNameAndWaitPerPatron(venueID);
        Vector<Patron> waitlistPatrons = DBManager.getWaitlistFromVenueID(venueID);

        if(nameAndWaitPerPatron == null || waitlistPatrons == null)
            return;
        
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "managerViewData");
        JsonObject data1 = new JsonObject();
        data1.addProperty("name", nameAndWaitPerPatron.get(0));
        data1.addProperty("waitPerPatron", nameAndWaitPerPatron.get(1));

        JsonArray names = new JsonArray();
        JsonArray emails = new JsonArray();
        for(int i = 0; i < waitlistPatrons.size(); i++){

            names.add(waitlistPatrons.get(i).getName());
            emails.add(waitlistPatrons.get(i).getEmail());

        }

        data1.add("waitlistNames", names);
        data1.add("waitlistEmails", emails);
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
