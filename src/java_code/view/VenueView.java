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
import com.sun.mail.smtp.SMTPTransport;
import java_code.controller.Controller;
import java_code.controller.ServerConnection;
import java_code.model.Patron;
import javafx.application.Platform;
import javafx.fxml.FXML;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.jetbrains.annotations.NotNull;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.*;

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
    @FXML
    private Label processingLabel;

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
        processingLabel.setVisible(false);
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
                processingLabel.setVisible(true);
                attemptedWaitlistAdd = nameField.getText();
                conn.addToWaitlist(controller.getVenueID(), new Patron(nameField.getText(), emailField.getText()));

            }catch (Exception e){
                e.printStackTrace();
            }

        }else{

            errorText.setText("***missing credentials***");
            errorText.setVisible(true);
            processingLabel.setVisible(false);

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
                        if(!controller.tmpEmail.equals(""))
                            sendEmail();
                        controller.tmpName = "";
                        controller.tmpEmail = "";
                        processingLabel.setVisible(false);

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

                    controller.tmpName = nameField.getText();
                    controller.tmpEmail = emailField.getText();
                    attemptedWaitlistAdd = "";
                    nameField.clear();
                    emailField.clear();
                    errorText.setVisible(false);

                }
                if(type.equals("badWaitlistAdd")){

                    Platform.runLater(() -> {
                        processingLabel.setVisible(false);
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

    /**
     * Sends the user a verification email that they have been added to the waitlist with the current waittime.
     * @param
     */
    private void sendEmail(){

        // Setup mail server
        Properties prop = System.getProperties();
        prop.put("mail.smtp.host", "smtp-relay.sendinblue.com"); //optional, defined in SMTPTransport
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.port", "587"); // default port 25

        Session session = Session.getInstance(prop, null);
        Message msg = new MimeMessage(session);
        try {

            // from
            msg.setFrom(new InternetAddress("services@SeatD.com"));

            // to
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(controller.tmpEmail));

            // subject
            msg.setSubject("SeatD - Added to " + venueNameLabel.getText() + " Waitlist");

            // content
            msg.setText("Hello ".concat(controller.tmpName).concat(",\n\n" + "Reminder that you are on the ").
                        concat(venueNameLabel.getText()).concat(" waitlist, your current wait time is ").
                        concat(waitTimeLabel.getText()).concat(" minutes.\nYou will receive another email once your ").
                            concat("table is ready!\n\n - SeatD Admins")
                        );

            //Date
            msg.setSentDate(new Date());

            //Send Email
            SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
            // connect
            t.connect("smtp-relay.sendinblue.com", "kdcptkai31@gmail.com", "N06pBhOgJ3RtCLrc");
            // send
            t.sendMessage(msg, msg.getAllRecipients());
            System.out.println("Response: " + t.getLastServerResponse());
            t.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
