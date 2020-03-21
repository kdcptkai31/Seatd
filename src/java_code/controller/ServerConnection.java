package java_code.controller;

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
import java_code.observable.Observable;
import java_code.server.MessageDelegator;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

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
    private Observable<Boolean> loggedIn;
    private String attemptedUsername = "noName";

    private ServerConnection(){

        pnConfig = new PNConfiguration();
        pnConfig.setPublishKey(pubkey);
        pnConfig.setSubscribeKey(subkey);
        pubnub = new PubNub(pnConfig);

        loggedIn = new Observable<>();
        delegator = new MessageDelegator();

        pubnub.subscribe().channels(Arrays.asList("main")).withPresence().execute();

        waitForServerPub();
        registerLoginStatusListener();


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
                if (!type.equals("serverPub")) {
                    return;
                }

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
            public void signal(@NotNull PubNub pubNub, @NotNull PNSignalResult pnSignalResult) {

            }
            @Override
            public void user(@NotNull PubNub pubNub, @NotNull PNUserResult pnUserResult) {

            }
            @Override
            public void space(@NotNull PubNub pubNub, @NotNull PNSpaceResult pnSpaceResult) {

            }
            @Override
            public void membership(@NotNull PubNub pubNub, @NotNull PNMembershipResult pnMembershipResult) {

            }
            @Override
            public void messageAction(@NotNull PubNub pubNub, @NotNull PNMessageActionResult pnMessageActionResult) {

            }
        });

        this.pubnub.subscribe().channels(Arrays.asList("main")).withPresence().execute();

    }

    /**
     * Listener for login status
     */
    private void registerLoginStatusListener() {
        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {}
            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                String type = message.getMessage().getAsJsonObject().get("type").getAsString();//Message type
                JsonObject data = message.getMessage().getAsJsonObject().get("data").getAsJsonObject();

                if (!Arrays.asList("loggedIn", "badLogin").contains(type)) {
                    return;
                }

                String username = data.get("username").getAsString();

                if (type.equals("loggedIn") && username.equals(attemptedUsername)) {

                    loggedIn.set(true);

                }


                if (type.equals("badLogin") && username.equals(attemptedUsername)) {

                    loggedIn.set(false);
                    attemptedUsername = "noName";

                }

            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {

            }
            @Override
            public void signal(@NotNull PubNub pubNub, @NotNull PNSignalResult pnSignalResult) {

            }
            @Override
            public void user(@NotNull PubNub pubNub, @NotNull PNUserResult pnUserResult) {

            }
            @Override
            public void space(@NotNull PubNub pubNub, @NotNull PNSpaceResult pnSpaceResult) {

            }
            @Override
            public void membership(@NotNull PubNub pubNub, @NotNull PNMembershipResult pnMembershipResult) {

            }
            @Override
            public void messageAction(@NotNull PubNub pubNub, @NotNull PNMessageActionResult pnMessageActionResult) {

            }
        });
    }

    /**
     * Sends the "login" message with the username and password.
     * @param username
     * @param password
     */
    public void login(String username, String password) {

        attemptedUsername = username;
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

    //Getters
    public Observable<Boolean> getLoggedInObservable() {
        return loggedIn;
    }
    public PubNub getPubNub(){return pubnub;}

    //Setters
    public void setAttemptedUsername(String str){attemptedUsername = str;}


}
