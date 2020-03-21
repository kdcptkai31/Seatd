package java_code.view;

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
import java_code.controller.Controller;
import java_code.controller.ServerConnection;
import java_code.model.Patron;
import javafx.application.Platform;
import javafx.fxml.FXML;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

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

        //SET SELECTED VENUE BASED ON WHAT THEY CLICKED TO GET HERE, THIS IS A PLACEHOLDER
        selectedVenue = 0;

        registerWaitlistTimeListener();

    }

    /**
     * Refreshes the scene with the most current data.
     */
    public void refresh(){

        venueNameLabel.setText(controller.venueName);
        venueTypeLabel.setText(controller.venueType);
        waitTimeLabel.setText(Integer.toString(controller.waitlist.size() * controller.waitPerPatron));
        waitlistSize.setText(Integer.toString(controller.waitlist.size()));

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
                controller.waitlist.put(new Patron(nameField.getText(), emailField.getText()));
                SeatDApplication.getCoordinator().showVenueScene();
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
                JsonObject data = message.getMessage().getAsJsonObject().get("data").getAsJsonObject();

                if(type.equals("clockTick")){

                    Platform.runLater(() -> {
                        System.out.println("Waittime received: ".concat(data.get(String.valueOf(selectedVenue)).getAsString()));
                        waitTimeLabel.setText(data.get(String.valueOf(selectedVenue)).getAsString());
                    });



                }

            }
            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult pnPresenceEventResult) {

            }
            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult pnSignalResult) {

            }
            @Override
            public void user(@NotNull PubNub pubnub, @NotNull PNUserResult pnUserResult) {

            }
            @Override
            public void space(@NotNull PubNub pubnub, @NotNull PNSpaceResult pnSpaceResult) {

            }
            @Override
            public void membership(@NotNull PubNub pubnub, @NotNull PNMembershipResult pnMembershipResult) {

            }
            @Override
            public void messageAction(@NotNull PubNub pubnub, @NotNull PNMessageActionResult pnMessageActionResult) {

            }
        });

    }

}
