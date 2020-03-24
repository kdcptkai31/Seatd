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
        waitPerPatronField.setFocusTraversable(false);

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

                if(type.equals("badWaitPerPatronUpdate") &&
                        message.getMessage().getAsJsonObject().get("data").getAsJsonObject().get("venueID").
                                getAsInt() == controller.getVenueID()){

                    waitPerPatronField.clear();
                    waitPerPatronField.setFocusTraversable(false);

                }


                if(!type.equals("managerViewData"))
                    return;

                Platform.runLater(()->{waitlistView.getItems().clear();});

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
                if(!waitlistPatrons.isEmpty()){

                    for(int i = 0; i < waitlistPatrons.size(); i++)
                        waitlistViewList.add(Integer.toString(i + 1).concat(". ").
                                             concat(waitlistPatrons.get(i).toString()));

                    controller.setTmpWaitlistOrder(waitlistPatrons);

                }else
                    waitlistViewList.add("Nobody is on the waitlist your venue sucks.");

                Platform.runLater(()->{waitlistView.setItems(waitlistViewList);});

                conn.refreshWaitListData();

                //Run Extraction
                Platform.runLater(()->{
                    totalWaitTimeLabel.setText(String.valueOf(data.get("waitPerPatron").getAsInt() * waitlistPatrons.size()));
                    venueNameLabel.setText(data.get("name").getAsString());
                    waitPerPatronField.setPromptText(data.get("waitPerPatron").getAsString());
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

        Platform.runLater(()->{waitlistView.getItems().clear();});
        JsonObject data = message.getMessage().getAsJsonObject().get("data").getAsJsonObject();

        JsonArray names = data.get("names").getAsJsonArray();
        JsonArray emails = data.get("emails").getAsJsonArray();
        Iterator<JsonElement> nameElements = names.iterator();
        Iterator<JsonElement> emailElements = emails.iterator();
        ArrayList<Patron> patrons = new ArrayList<>();
        while(nameElements.hasNext() && emailElements.hasNext())
            patrons.add(new Patron(nameElements.next().getAsString(), emailElements.next().getAsString()));

        ObservableList<String> waitlistViewList = FXCollections.observableArrayList();
        if(!patrons.isEmpty()){

            for(int i = 0; i < patrons.size(); i++)
                waitlistViewList.add(Integer.toString(i + 1).concat(". ").
                        concat(patrons.get(i).toString()));

            controller.setTmpWaitlistOrder(patrons);

        }else
            waitlistViewList.add("Nobody is on the waitlist your venue sucks.");

        Platform.runLater(()->{
            waitlistView.setItems(waitlistViewList);
            totalWaitTimeLabel.setText(Integer.toString(data.get("waittime").getAsInt()));
        });

    }

    /**
     * Checks if a new wait time per patron was entered, if so, it updates the controller and refreshes the scene.
     */
    public void onUpdateWaitPerPatronClicked(){

        String entry = waitPerPatronField.getText();
        if(!entry.isEmpty() && !entry.equals((waitPerPatronField.getPromptText())) && Integer.valueOf(entry) > 0)
            conn.updateWaitPerPatron(controller.getVenueID(), Integer.parseInt(waitPerPatronField.getText()));

        waitPerPatronField.clear();

    }

    public void onDeleteClicked(){

        if(!waitlistView.getSelectionModel().isEmpty()){

            Patron tmp = getPatronFromStringAr(waitlistView.getSelectionModel().getSelectedItem().split(" | "));
            conn.deleteWaitListPatron(controller.getVenueID(), tmp.getName(), tmp.getEmail());

        }

    }

    /**
     * Moves the selected patron down one, if possible, in the temporary list.
     */
    public void onDownClicked(){

        if(waitlistView.getSelectionModel().isEmpty())
            return;

        Patron selected = getPatronFromStringAr(waitlistView.getSelectionModel().getSelectedItem().split(" | "));
        int selectedIndex = waitlistView.getSelectionModel().getSelectedIndex();

        if(selectedIndex == -1 || selectedIndex == controller.getTmpWaitlistOrder().size())
            return;

        Patron tmp = controller.getTmpWaitlistOrder().get(selectedIndex + 1);
        controller.getTmpWaitlistOrder().set(selectedIndex + 1, selected);
        controller.getTmpWaitlistOrder().set(selectedIndex, tmp);

        waitlistView.getItems().clear();
        ObservableList<String> waitlistViewList = FXCollections.observableArrayList();

        for(int i = 0; i < controller.getTmpWaitlistOrder().size(); i++)
            waitlistViewList.add(Integer.toString(i + 1).concat(". ").
                    concat(controller.getTmpWaitlistOrder().get(i).toString()));

        waitlistView.setItems(waitlistViewList);
        waitlistView.getSelectionModel().selectIndices(selectedIndex + 1);

    }

    /**
     * Moves the selected patron up one, if possible, in the temporary list.
     */
    public void onUpClicked(){

        if(waitlistView.getSelectionModel().isEmpty())
            return;

        Patron selected = getPatronFromStringAr(waitlistView.getSelectionModel().getSelectedItem().split(" | "));

        int selectedIndex = waitlistView.getSelectionModel().getSelectedIndex();
        if(selectedIndex < 1)
            return;

        Patron tmp = controller.getTmpWaitlistOrder().get(selectedIndex - 1);
        controller.getTmpWaitlistOrder().set(selectedIndex - 1, selected);
        controller.getTmpWaitlistOrder().set(selectedIndex, tmp);

        waitlistView.getItems().clear();
        ObservableList<String> waitlistViewList = FXCollections.observableArrayList();

        for(int i = 0; i < controller.getTmpWaitlistOrder().size(); i++)
            waitlistViewList.add(Integer.toString(i + 1).concat(". ").
                    concat(controller.getTmpWaitlistOrder().get(i).toString()));

        waitlistView.setItems(waitlistViewList);
        waitlistView.getSelectionModel().selectIndices(selectedIndex - 1);

    }

    /**
     * Saves the temporary list to the database.
     */
    public void onSavedListClicked(){

        conn.updateWaitListFromMove(controller.getVenueID(), controller.getTmpWaitlistOrder());

    }

    /**
     * Logs the manager out and returns them to the venue scene.
     */
    public void onLogoutButtonClicked(){

        try {
            SeatDApplication.getCoordinator().showVenueListScene();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Used to extract a Patron object from the list representation in the manager page, showing the current waitlist.
     * @param str
     * @return
     */
    private Patron getPatronFromStringAr(String[] str){

        String user_name = "";
        String email = "";
        boolean usernameRetieved = false;
        for(int i = 0; i < str.length; i++) {

            if (i > 0 && !usernameRetieved && !str[i].equals("|"))
                user_name = user_name.concat( " " + str[i]);

            if(usernameRetieved)
                email = email.concat(" " + str[i]);

            if(str[i].equals("|"))
                usernameRetieved = true;

        }

        return new Patron(user_name.trim(), email.trim());

    }

}
