package java_code.server;

import com.google.gson.JsonObject;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult;
import com.pubnub.api.models.consumer.pubsub.objects.PNMembershipResult;
import com.pubnub.api.models.consumer.pubsub.objects.PNSpaceResult;
import com.pubnub.api.models.consumer.pubsub.objects.PNUserResult;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Redirects incoming messages to their correct message handler for logic execution.
 */
public class MessageDelegator extends SubscribeCallback {

    private HashMap<String, MessageHandler> handlers;

    public MessageDelegator(){this.handlers = new HashMap<>();}

    /**
     * Adds handler to hashMap.
     * @param type
     * @param handler
     */
    public void addHandler(String type, MessageHandler handler) {
        handlers.put(type, handler);
    }


    /**
     * Sends the message to the right handler.
     * @param pubnub
     * @param message
     */
    @Override
    public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {

        String type = message.getMessage().getAsJsonObject().get("type").getAsString();
        JsonObject data = message.getMessage().getAsJsonObject().get("data").getAsJsonObject();

        data.addProperty("UUID", message.getPublisher());

        MessageHandler handler = handlers.get(type);

        if (handler != null) {
            handler.handleMessage(data, pubnub, message.getPublisher());
        }

    }

    @Override
    public void presence(@NotNull PubNub pubNub, @NotNull PNPresenceEventResult presence) {

        if (presence.getEvent().equals("join")) {

            if(presence.getOccupancy().equals(1))
                System.out.println("Server ID: ".concat(presence.getUuid()));
            else
                System.out.println("User: ".concat(presence.getUuid()).concat(" connected"));


        }
        else if(presence.getEvent().equals("leave")){

            System.out.println("User: ".concat(presence.getUuid()).concat(" disconnected"));

        }

    }

    @Override
    public void status(@NotNull PubNub pubNub, @NotNull PNStatus pnStatus) {

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
}
