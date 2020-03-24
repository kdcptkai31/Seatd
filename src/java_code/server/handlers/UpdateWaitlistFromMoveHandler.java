package java_code.server.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pubnub.api.PubNub;
import java_code.database.DBManager;
import java_code.model.Patron;
import java_code.server.MessageHandler;
import java_code.server.Server;

import java.util.ArrayList;
import java.util.Iterator;

public class UpdateWaitlistFromMoveHandler implements MessageHandler {

    Server server;

    public UpdateWaitlistFromMoveHandler(Server server){this.server = server;}

    @Override
    public void handleMessage(JsonObject data, PubNub pubnub, String clientId) {

        JsonArray names = data.get("names").getAsJsonArray();
        JsonArray emails = data.get("emails").getAsJsonArray();
        Iterator<JsonElement> nameElements = names.iterator();
        Iterator<JsonElement> emailElements = emails.iterator();
        ArrayList<Patron> patrons = new ArrayList<>();
        while(nameElements.hasNext() && emailElements.hasNext())
            patrons.add(new Patron(nameElements.next().getAsString(), emailElements.next().getAsString()));

        DBManager.setWaitlistForVenue(data.get("venueID").getAsInt(), patrons);

        server.updateCurrentVenueData();
        server.sendUpdateWaitlistData();
        server.sendUpdateWaitlist(data.get("venueID").getAsInt());
        server.updateManagerPage(data);

    }
}
