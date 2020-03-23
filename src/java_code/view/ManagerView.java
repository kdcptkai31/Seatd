package java_code.view;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class ManagerView {

    Controller controller;
    ServerConnection conn;

    @FXML
    private ListView<String> waitlistView;
    @FXML
    private Label venueNameLabel;
    @FXML
    private TextField waitPerPatronField;
    @FXML
    private Label totalWaitTimeLabel;

    /**
     * Initializes the scene.
     */
    @FXML
    protected void initialize(){

        conn = ServerConnection.getInstance();
        controller = SeatDApplication.getController();
        SeatDApplication.setToDefaultWindowSize();
        registerManagerPageDataListener();
        conn.getManagerPageData(controller.getVenueID());

    }

    /**
     * Handles all messages that will update the management page dynamically.
     */
    public void registerManagerPageDataListener(){

        conn.getPubNub().addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus pnStatus) {}
            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {

                String type = message.getMessage().getAsJsonObject().get("type").getAsString();//Message type
                if(type.equals("updateAllData"))
                    updateWaitTime(message);

                if(type.equals("updateWaitlist") &&
                        message.getMessage().getAsJsonObject().get("data").getAsJsonObject().get("venueID").
                                getAsInt() == controller.getVenueID())
                    updateWaitlistList(message);

                if(!type.equals("managerViewData"))
                    return;

                JsonObject data = message.getMessage().getAsJsonObject().get("data").getAsJsonObject();

                //Extract data: into (venueName, waitPerPatronValue, waitlistPatrons)
                JsonArray waitlistNames = data.get("waitlistNames").getAsJsonArray();
                JsonArray waitlistEmails = data.get("waitlistEmails").getAsJsonArray();
                Iterator<JsonElement> waitNameIt = waitlistNames.iterator();
                Iterator<JsonElement> waitEmailIt = waitlistEmails.iterator();
                ArrayList<Patron> waitlistPatrons = new ArrayList<>();
                while(waitNameIt.hasNext() && waitEmailIt.hasNext())
                    waitlistPatrons.add(new Patron(waitNameIt.next().getAsString(), waitEmailIt.next().getAsString()));

                ObservableList<String> waitlistViewList = FXCollections.observableArrayList();
                waitlistView.getItems().clear();
                if(!waitlistPatrons.isEmpty()){

                    for(int i = 0; i < waitlistPatrons.size(); i++)
                        waitlistViewList.add(Integer.toString(i + 1).concat(". ").
                                             concat(waitlistPatrons.get(i).toString()));

                }else
                    waitlistViewList.add("Nobody is on the waitlist your venue sucks.");

                waitlistView.setItems(waitlistViewList);
                conn.refreshWaitListData();

                //Run Extraction
                Platform.runLater(()->{
                    totalWaitTimeLabel.setText(String.valueOf(data.get("waitPerPatron").getAsInt() * waitlistPatrons.size()));
                    venueNameLabel.setText(data.get("name").getAsString());
                    waitPerPatronField.setPromptText(data.get("waitPerPatron").getAsString());
                    waitPerPatronField.setFocusTraversable(false);
                });

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
     * Runs when a waitlist is updated, or its wait time.
     * @param message
     */
    private void updateWaitTime(PNMessageResult message){

        JsonArray waitTimes = message.getMessage().getAsJsonObject().get("data").getAsJsonObject().
                get("waitTimes").getAsJsonArray();
        Iterator<JsonElement> waitTimeIt = waitTimes.iterator();
        ArrayList<Integer> waitlistTimes = new ArrayList<>();
        while(waitTimeIt.hasNext())
            waitlistTimes.add(waitTimeIt.next().getAsInt());

        Platform.runLater(()->{totalWaitTimeLabel.setText(Integer.toString(waitlistTimes.get(controller.getVenueID())));});

    }

    /**
     * Runs when a person is added to the waitlist, to update all relevant management pages.
     * @param message
     */
    private void updateWaitlistList(PNMessageResult message){

        JsonObject data = message.getMessage().getAsJsonObject().get("data").getAsJsonObject();

        JsonArray names = data.get("names").getAsJsonArray();
        JsonArray emails = data.get("emails").getAsJsonArray();
        Iterator<JsonElement> nameElements = names.iterator();
        Iterator<JsonElement> emailElements = emails.iterator();
        ArrayList<Patron> patrons = new ArrayList<>();
        while(nameElements.hasNext() && emailElements.hasNext())
            patrons.add(new Patron(nameElements.next().getAsString(), emailElements.next().getAsString()));

        ObservableList<String> waitlistViewList = FXCollections.observableArrayList();
        waitlistView.getItems().clear();
        if(!patrons.isEmpty()){

            for(int i = 0; i < patrons.size(); i++)
                waitlistViewList.add(Integer.toString(i + 1).concat(". ").
                        concat(patrons.get(i).toString()));

        }else
            waitlistViewList.add("Nobody is on the waitlist your venue sucks.");

        waitlistView.setItems(waitlistViewList);

        Platform.runLater(()->{totalWaitTimeLabel.setText(Integer.toString(data.get("waittime").getAsInt()));});

    }

    /**
     * Checks if a new wait time per patron was entered, if so, it updates the controller and refreshes the scene.
     */
    public void onUpdateWaitPerPatronClicked(){

//        if(!waitPerPatronField.getText().equals(Integer.toString(controller.waitPerPatron))) {
//
//            controller.waitPerPatron = Integer.parseInt(waitPerPatronField.getText());
//            refreshPage();
//        }

        waitPerPatronField.clear();

    }

    /**
     * Logs the manager out and returns them to the venue scene.
     */
    public void onLogoutButtonClicked(){

        try {
            controller.setVenueID(-1);
            SeatDApplication.getCoordinator().showVenueScene();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
