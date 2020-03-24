package java_code.view;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

    ServerConnection conn;
    Controller controller;

    private String attemptedWaitlistAdd;

    /**
     * Initializes the scene.
     */
    @FXML
    protected void initialize(){

        conn = ServerConnection.getInstance();
        controller = SeatDApplication.getController();
        SeatDApplication.setToDefaultWindowSize();
        errorText.setVisible(false);
        attemptedWaitlistAdd = "";

        registerVenueAllDataListener();
        registerWaitlistAddListener();
        refresh();

    }

    /**
     * Refreshes the scene with the most current data for the currently selected venue.
     */
    public void refresh(){conn.refreshWaitListData();}

    /**
     * Adds the input information to the waitlist, if it is all there.
     */
    public void onAddToWaitlistClicked(){

        if(!nameField.getText().equals("") && !emailField.getText().equals("")){

            try{

                errorText.setVisible(false);
                attemptedWaitlistAdd = nameField.getText();
                conn.addToWaitlist(controller.getVenueID(), new Patron(nameField.getText(), emailField.getText()));

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
    public void registerVenueAllDataListener(){

        conn.getPubNub().addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus pnStatus) {}
            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {

                String type = message.getMessage().getAsJsonObject().get("type").getAsString();//Message type
                if(type.equals("updateAllData")){

                    //Extract waitTimes and waitSizes
                    JsonArray waitTimes = message.getMessage().getAsJsonObject().get("data").getAsJsonObject().
                            get("waitTimes").getAsJsonArray();
                    JsonArray waitSizes = message.getMessage().getAsJsonObject().get("data").getAsJsonObject().
                            get("waitSizes").getAsJsonArray();
                    JsonArray venueNames = message.getMessage().getAsJsonObject().get("data").getAsJsonObject().
                            get("venueNames").getAsJsonArray();
                    JsonArray venueTypes = message.getMessage().getAsJsonObject().get("data").getAsJsonObject().
                            get("venueTypes").getAsJsonArray();
                    Iterator<JsonElement> waitTimeIt = waitTimes.iterator();
                    Iterator<JsonElement> waitSizeIt = waitSizes.iterator();
                    Iterator<JsonElement> venueNameIt = venueNames.iterator();
                    Iterator<JsonElement> venueTypeIt = venueTypes.iterator();
                    ArrayList<Integer> waitlistTimes = new ArrayList<>();
                    ArrayList<Integer> waitListSizes = new ArrayList<>();
                    ArrayList<String> allVenueNames = new ArrayList<>();
                    ArrayList<String> allVenueTypes = new ArrayList<>();

                    while(waitTimeIt.hasNext() && waitSizeIt.hasNext() && venueNameIt.hasNext() && venueTypeIt.hasNext()){

                        waitlistTimes.add(waitTimeIt.next().getAsInt());
                        waitListSizes.add(waitSizeIt.next().getAsInt());
                        allVenueNames.add(venueNameIt.next().getAsString());
                        allVenueTypes.add(venueTypeIt.next().getAsString());

                    }

                    Platform.runLater(() -> {
                        waitTimeLabel.setText(Integer.toString(waitlistTimes.get(controller.getVenueID())));
                        waitlistSize.setText(Integer.toString(waitListSizes.get(controller.getVenueID())));
                        venueNameLabel.setText(allVenueNames.get(controller.getVenueID()));
                        venueTypeLabel.setText(allVenueTypes.get(controller.getVenueID()));

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

    /**
     * Handles verification message when a patron is trying to add to the waitlist.
     */
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

    /**
     * Returns the user to the list of venues when they click the "Go Back!" button.
     */
    public void onGoBackClicked(){

        try {
            SeatDApplication.getCoordinator().showVenueListScene();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
