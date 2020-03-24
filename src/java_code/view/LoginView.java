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
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;

public class LoginView {

    ServerConnection conn;
    Controller controller;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private Label errorText;

    private String attemptedUsername;

    /**
     * Initializes the scene.
     */
    @FXML
    protected void initialize(){

        SeatDApplication.setWindowSize(600, 390);
        controller = SeatDApplication.getController();
        conn = ServerConnection.getInstance();
        attemptedUsername = "";
        registerLoginStatusListener();

        Platform.runLater(()->{
            errorText.setVisible(false);
        });

    }

    /**
     * Checks if login info is present, if so, then it validates the info and logs in the user. This can either be a
     * venue manager or a SeatD admin.
     */
    public void onLoginClicked(){

        if(!usernameField.getText().equals("") && !passwordField.getText().equals("")){

            attemptedUsername = usernameField.getText();
            conn.login(usernameField.getText(), passwordField.getText());
            errorText.setVisible(false);

        }else{

            errorText.setText("***missing credentials***");
            errorText.setVisible(true);

        }

    }

    /**
     * Listener for login status
     */
    private void registerLoginStatusListener() {
        conn.getPubNub().addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {}
            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                String type = message.getMessage().getAsJsonObject().get("type").getAsString();//Message type
                JsonObject data = message.getMessage().getAsJsonObject().get("data").getAsJsonObject();

                if(type.equals("adminLogin") && attemptedUsername.equals(data.get("username").getAsString())){

                    attemptedUsername = "";
                    Platform.runLater(() -> {
                        try {
                            SeatDApplication.getCoordinator().showAdminScene();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                }


                if (!Arrays.asList("loggedIn", "badLogin").contains(type))
                    return;

                String username = data.get("username").getAsString();
                if (type.equals("loggedIn") && username.equals(attemptedUsername)) {

                    controller.setVenueID(data.get("venueID").getAsInt());
                    controller.setManagerUsername(attemptedUsername);
                    attemptedUsername = "";
                    Platform.runLater(() -> {
                        try {
                            SeatDApplication.getCoordinator().showManagerScene();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                }

                if (type.equals("badLogin") && username.equals(attemptedUsername)) {

                    Platform.runLater(() -> {
                        errorText.setText("***invalid credentials***");
                        errorText.setVisible(true);
                    });
                    attemptedUsername = "";

                }

            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {}
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
    }

    /**
     * Returns to the venue scene.
     */
    public void onCancelClicked(){

        try {
            SeatDApplication.getCoordinator().showVenueListScene();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}