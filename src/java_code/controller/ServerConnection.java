package java_code.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.presence.PNHereNowChannelData;
import com.pubnub.api.models.consumer.presence.PNHereNowOccupantData;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult;
import com.pubnub.api.models.consumer.pubsub.objects.PNMembershipResult;
import com.pubnub.api.models.consumer.pubsub.objects.PNSpaceResult;
import com.pubnub.api.models.consumer.pubsub.objects.PNUserResult;
import java_code.model.Patron;
import java_code.server.MessageDelegator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

/**
 * Singleton Class that represents the client's connection to the server via PubNub.
 */
public class ServerConnection {

    private static String pubkey = "pub-c-01d2cd3b-e3bb-4881-a9ac-e896e0476911";
    private static String subkey = "sub-c-d3b9c360-5fee-11ea-b7ea-f2f107c29c38";
    private static ServerConnection instance;
    private MessageDelegator delegator;
    private PubNub pubnub;
    private PNConfiguration pnConfig;

    private ServerConnection(){

        pnConfig = new PNConfiguration();
        pnConfig.setPublishKey(pubkey);
        pnConfig.setSubscribeKey(subkey);
        pubnub = new PubNub(pnConfig);
        delegator = new MessageDelegator();

        pubnub.subscribe().channels(Arrays.asList("main")).withPresence().execute();

        waitForServerPub();

    }

    /**
     * Returns an instance of the ServerConnection.
     * @return
     */
    public static ServerConnection getInstance() {

        if (instance == null)
            instance = new ServerConnection();

        return instance;
    }

    /**
     * Handles outputting the PubNub presence info to the console.
     */
    private void waitForServerPub() {
        this.pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {}

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {

                String type = message.getMessage().getAsJsonObject().get("type").getAsString();
                if (!type.equals("serverPub"))
                    return;

            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {

                if (presence.getEvent().equals("join")) {
                    System.out.println("User Joined");
                    presence.getHereNowRefresh();
                    presence.getUuid(); // 175c2c67-b2a9-470d-8f4b-1db94f90e39e
                    presence.getTimestamp(); // 1345546797
                    presence.getOccupancy(); // 2

                }
                else if(presence.getEvent().equals("leave")){
                    System.out.println("User Left");
                    presence.getUuid(); // 175c2c67-b2a9-470d-8f4b-1db94f90e39e
                    presence.getTimestamp(); // 1345546797
                    presence.getOccupancy(); // 2
                }
                else{
                    System.out.println("User Timed out");
                    presence.getUuid(); // 175c2c67-b2a9-470d-8f4b-1db94f90e39e
                    presence.getTimestamp(); // 1345546797
                    presence.getOccupancy(); // 2
                }

                hereNow();

            }

            @Override
            public void signal(@NotNull PubNub pubNub, @NotNull PNSignalResult pnSignalResult) {}
            @Override
            public void user(@NotNull PubNub pubNub, @NotNull PNUserResult pnUserResult) {}
            @Override
            public void space(@NotNull PubNub pubNub, @NotNull PNSpaceResult pnSpaceResult) {}
            @Override
            public void membership(@NotNull PubNub pubNub, @NotNull PNMembershipResult pnMembershipResult) {}
            @Override
            public void messageAction(@NotNull PubNub pubNub, @NotNull PNMessageActionResult pnMessageActionResult) {}
        });

        this.pubnub.subscribe().channels(Arrays.asList("main")).withPresence().execute();

    }

    /**
     * Sends the "login" message with the username and password.
     * @param username
     * @param password
     */
    public void login(String username, String password) {

        JsonObject msg = new JsonObject();
        msg.addProperty("type", "login");

        JsonObject data = new JsonObject();
        data.addProperty("username", username);
        data.addProperty("password", password);
        msg.add("data", data);

        try {
            this.pubnub.publish()
                    .channel("main")
                    .message(msg)
                    .sync();
        } catch (PubNubException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the "addToWaitlist" message with the venueID, name, and email.
     * @param venueID
     * @param patron
     */
    public void addToWaitlist(int venueID, Patron patron){

        JsonObject msg = new JsonObject();
        msg.addProperty("type", "addWaitlist");

        JsonObject data = new JsonObject();
        data.addProperty("venueID", venueID);
        data.addProperty("name", patron.getName());
        data.addProperty("email", patron.getEmail());
        msg.add("data", data);

        try {
            this.pubnub.publish()
                    .channel("main")
                    .message(msg)
                    .sync();
        } catch (PubNubException e) {
            e.printStackTrace();
        }

    }

    /**
     * Runs whenever someone joins or leaves the "main" pubnub channel.
     */
    public void hereNow(){

        this.pubnub.hereNow()
                .channels(Arrays.asList("main"))
                .includeUUIDs(true)
                .async((result, status) -> {

                    if (status.isError())
                        return;

                    for (PNHereNowChannelData channelData : result.getChannels().values()) {

                        System.out.println("---");
                        System.out.println("channel:" + channelData.getChannelName());
                        System.out.println("occupancy: " + channelData.getOccupancy());
                        System.out.println("occupants:");
                        for (PNHereNowOccupantData occupant : channelData.getOccupants())
                            System.out.println("uuid: " + occupant.getUuid() + " state: " + occupant.getState());

                    }

                });

    }

    /**
     * Sends a message to the server asking for a refresh on all waitlist data for clients.
     */
    public void refreshWaitListData(){

        JsonObject msg = new JsonObject();
        msg.addProperty("type", "getWaitlistData");

        JsonObject data = new JsonObject();
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

    /**
     * Sends a message requesting the server to send the manager page data for a specific venue.
     * @param venueID
     */
    public void getManagerPageData(int venueID){

        JsonObject msg = new JsonObject();
        msg.addProperty("type", "getManagerData");

        JsonObject data = new JsonObject();
        data.addProperty("venueID", venueID);
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

    /**
     * Sends a message requesting to update the wait per patron value for the given venue.
     * @param venueID
     */
    public void updateWaitPerPatron(int venueID, int waitPerPatronValue){

        JsonObject msg = new JsonObject();
        msg.addProperty("type", "updateWaitPerPatron");

        JsonObject data = new JsonObject();
        data.addProperty("venueID", venueID);
        data.addProperty("wppValue", waitPerPatronValue);
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

    /**
     * Sends a message to delete a Patron from a specific venue's waitlist.
     * @param venueID
     * @param user_name
     * @param email
     */
    public void deleteWaitListPatron(int venueID, String user_name, String email){

        JsonObject msg = new JsonObject();
        msg.addProperty("type", "deletePatronFromWaitlist");

        JsonObject data = new JsonObject();
        data.addProperty("venueID", venueID);
        data.addProperty("user_name", user_name);
        data.addProperty("email", email);
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

    /**
     * Sends the updated list for a venue to the server.
     * @param venueID
     * @param list
     */
    public void updateWaitListFromMove(int venueID, ArrayList<Patron> list){

        Vector<String> names = new Vector<>();
        Vector<String> emails = new Vector<>();
        for(int i = 0; i < list.size(); i++){

            names.add(list.get(i).getName().trim());
            emails.add(list.get(i).getEmail().trim());

        }

        JsonObject msg = new JsonObject();
        msg.addProperty("type", "updateWaitlistFromMove");

        JsonObject data = new JsonObject();
        data.addProperty("venueID", venueID);
        data.add("names", getJsonArrayFromStringVector(names));
        data.add("emails", getJsonArrayFromStringVector(emails));
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

    /**
     * Sends a message to the server asking for a list of all venues and their types.
     */
    public void getVenuesForAdmin(){

        JsonObject msg = new JsonObject();
        msg.addProperty("type", "getVenuesForAdmin");

        JsonObject data = new JsonObject();
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

    /**
     * Sends a message to the server to change the venue name and type info.
     * @param venueID
     * @param newName
     * @param newType
     */
    public void updateVenueInfo(int venueID, String newName, String newType){

        JsonObject msg = new JsonObject();
        msg.addProperty("type", "updateVenueNameAndType");

        JsonObject data = new JsonObject();
        data.addProperty("venueID", venueID);
        data.addProperty("newName", newName);
        data.addProperty("newType", newType);
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

    /**
     * Requests the list of venues and their waittimes from the server.
     * @param
     */
    public void getVenueListData(){

        JsonObject msg = new JsonObject();
        msg.addProperty("type", "getVenueListData");

        JsonObject data = new JsonObject();
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

    public JsonArray getJsonArrayFromStringVector(Vector<String> vector){

        JsonArray array = new JsonArray();
        for(int i = 0; i < vector.size(); i++)
            array.add(vector.get(i));

        return array;

    }

    //Getters
    public PubNub getPubNub(){return pubnub;}

}
