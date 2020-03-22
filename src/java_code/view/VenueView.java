package java_code.view;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult;
import com.pubnub.api.models.consumer.pubsub.objects.PNMembershipResult;
import com.pubnub.api.models.consumer.pubsub.objects.PNSpaceResult;
import com.pubnub.api.models.consumer.pubsub.objects.PNUserResult;
import java_code.controller.Controller;
import java_code.controller.ServerConnection;
import java_code.model.Patron;
import javafx.application.Platform;
import javafx.fxml.FXML;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class VenueView {

    Controller controller;
    ServerConnection conn;
    @FXML
    private Label venueNameLabel;
    @FXML
    private Label venueTypeLabel;
    @FXML
    private Label waitTimeLabel;
    @FXML
    private Label waitlistSize;
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private Label errorText;

    private int selectedVenue;
    private String attemptedWaitlistAdd;

    /**
     * Initializes the scene.
     */
    @FXML
    protected void initialize(){

        conn = ServerConnection.getInstance();
        controller = SeatDApplication.getController();
        SeatDApplication.setToDefaultWindowSize();
        refresh();
        errorText.setVisible(false);

        //SET SELECTED VENUE BASED ON WHAT THEY CLICKED TO GET HERE, THIS IS A PLACEHOLDER*********************
        selectedVenue = 0;

        attemptedWaitlistAdd = "";

        registerWaitlistTimeListener();
        registerWaitlistAddListener();

    }

    /**
     * Refreshes the scene with the most current data.
     */
    public void refresh(){

        venueNameLabel.setText(controller.venueName);
        venueTypeLabel.setText(controller.venueType);
        waitTimeLabel.setText(Integer.toString(controller.waitlist.size() * controller.waitPerPatron));
        waitlistSize.setText(Integer.toString(controller.waitlist.size()));

        JsonObject msg = new JsonObject();
        msg.addProperty("type", "getWaitlistData");

        JsonObject data = new JsonObject();
        msg.add("data", data);

        try {
            conn.getPubNub().publish()
                    .channel("main")
                    .message(msg)
                    .sync();
            System.out.println("send me waitlist data please");
        } catch (PubNubException e) {
            e.printStackTrace();
        }

    }

    /**
     * Loads the login scene.
     */
    public void onLoginClicked(){

        try {
            SeatDApplication.getCoordinator().showLoginScene();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Adds the input information to the waitlist, if it is all there.
     */
    public void onAddToWaitlistClicked(){

        if(!nameField.getText().equals("") && !emailField.getText().equals("")){

            try{

                errorText.setVisible(false);
                attemptedWaitlistAdd = nameField.getText();
                conn.addToWaitlist(selectedVenue, new Patron(nameField.getText(), emailField.getText()));

            }catch (Exception e){
                e.printStackTrace();
            }


        }else{

            errorText.setText("***missing credentials***");
            errorText.setVisible(true);

        }

    }

    /**
     * Listens for the server's update on the specific wait time of the venue the user has selected.
     */
    public void registerWaitlistTimeListener(){

        conn.getPubNub().addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus pnStatus) {}
            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {

                String type = message.getMessage().getAsJsonObject().get("type").getAsString();//Message type
                if(type.equals("clockTick")){

                    //Extract waitTimes and waitSizes
                    JsonArray waitTimes = message.getMessage().getAsJsonObject().get("data").getAsJsonObject().get("waitTimes").
                                     getAsJsonArray();
                    JsonArray waitSizes = message.getMessage().getAsJsonObject().get("data").getAsJsonObject().get("waitSizes").
                            getAsJsonArray();
                    Iterator<JsonElement> waitTimeIt = waitTimes.iterator();
                    Iterator<JsonElement> waitSizeIt = waitSizes.iterator();
                    ArrayList<Integer> waitlistTimes = new ArrayList<>();
                    ArrayList<Integer> waitListSizes = new ArrayList<>();

                    while(waitTimeIt.hasNext() && waitSizeIt.hasNext()){

                        JsonElement ele = waitTimeIt.next();
                        waitlistTimes.add(ele.getAsInt());
                        JsonElement ele2 = waitSizeIt.next();
                        waitListSizes.add(ele2.getAsInt());

                    }

                    Platform.runLater(() -> {
                        waitTimeLabel.setText(Integer.toString(waitlistTimes.get(selectedVenue)));
                        waitlistSize.setText(Integer.toString(waitListSizes.get(selectedVenue)));
                    });

                }

            }
            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult pnPresenceEventResult) {}
            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult pnSignalResult) {}
            @Override
            public void user(@NotNull PubNub pubnub, @NotNull PNUserResult pnUserResult) {}
            @Override
            public void space(@NotNull PubNub pubnub, @NotNull PNSpaceResult pnSpaceResult) {}
            @Override
            public void membership(@NotNull PubNub pubnub, @NotNull PNMembershipResult pnMembershipResult) {}
            @Override
            public void messageAction(@NotNull PubNub pubnub, @NotNull PNMessageActionResult pnMessageActionResult) {}
        });

    }

    public void registerWaitlistAddListener(){

        conn.getPubNub().addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus pnStatus) {}
            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {

                String type = message.getMessage().getAsJsonObject().get("type").getAsString();//Message type
                if (!Arrays.asList("goodWaitlistAdd", "badWaitlistAdd").contains(type))
                    return;

                String name = message.getMessage().getAsJsonObject().get("data").getAsJsonObject().get("name").getAsString();
                if(!attemptedWaitlistAdd.equals(name))
                    return;

                if(type.equals("goodWaitlistAdd")){

                    attemptedWaitlistAdd = "";
                    nameField.clear();
                    emailField.clear();
                    errorText.setVisible(false);

                }
                if(type.equals("badWaitlistAdd")){

                    Platform.runLater(() -> {
                        errorText.setText("***error-waitlist addition failed***");
                        errorText.setVisible(true);
                    });

                }

            }
            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult pnPresenceEventResult) {}
            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult pnSignalResult) {}
            @Override
            public void user(@NotNull PubNub pubnub, @NotNull PNUserResult pnUserResult) {}
            @Override
            public void space(@NotNull PubNub pubnub, @NotNull PNSpaceResult pnSpaceResult) {}
            @Override
            public void membership(@NotNull PubNub pubnub, @NotNull PNMembershipResult pnMembershipResult) {}
            @Override
            public void messageAction(@NotNull PubNub pubnub, @NotNull PNMessageActionResult pnMessageActionResult) {}
        });

    }

}
