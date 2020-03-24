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
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class AdminView {

    ServerConnection conn;
    Controller controller;

    @FXML
    private ListView<String> venueListView;

    @FXML
    private TextField changeNameField;
    @FXML
    private TextField changeTypeField;

    /**
     * Initializes the scene.
     */
    @FXML
    protected void initialize(){

        SeatDApplication.setWindowSize(600, 500);
        controller = SeatDApplication.getController();
        conn = ServerConnection.getInstance();

        conn.getVenuesForAdmin();
        registerGetVenuesListener();
        venueListView.setFocusTraversable(false);
        changeNameField.setFocusTraversable(false);
        changeTypeField.setFocusTraversable(false);
        changeNameField.setPromptText("select a venue");
        changeTypeField.setPromptText("select a venue");

    }

    /**
     * Fills the venue list with the venue data from the server. Also saves the data locally for future use.
     */
    private void registerGetVenuesListener(){

        conn.getPubNub().addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus pnStatus) {}
            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {

                String type = message.getMessage().getAsJsonObject().get("type").getAsString();//Message type

                if(!type.equals("venueDataForAdmin"))
                    return;

                Platform.runLater(()->{venueListView.getItems().clear();});

                JsonObject data = message.getMessage().getAsJsonObject().get("data").getAsJsonObject();
                JsonArray names = data.get("names").getAsJsonArray();
                JsonArray types = data.get("types").getAsJsonArray();
                Iterator<JsonElement> nameElement = names.iterator();
                Iterator<JsonElement> typeElement = types.iterator();
                ObservableList<String> venueViewList = FXCollections.observableArrayList();
                Vector<String> vectorNames = new Vector<>();
                Vector<String> vectorTypes = new Vector<>();

                while(nameElement.hasNext() && typeElement.hasNext()){

                    String tmpName = nameElement.next().getAsString();
                    String tmpType = typeElement.next().getAsString();
                    vectorNames.add(tmpName);
                    vectorTypes.add(tmpType);
                    venueViewList.add(tmpName.concat(" | ").concat(tmpType));

                }

                controller.setVenueNames(vectorNames);
                controller.setVenueTypes(vectorTypes);

                Platform.runLater(()->{
                    venueListView.setItems(venueViewList);
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
     * Fills the text fields' prompt text with the selected venue values.
     */
    public void onVenueListClicked(){

        if(venueListView.getSelectionModel().isEmpty())
            return;

        int selectedIndex = venueListView.getSelectionModel().getSelectedIndex();
        changeNameField.setPromptText(controller.getVenueNames().get(selectedIndex));
        changeTypeField.setPromptText(controller.getVenueTypes().get(selectedIndex));

    }

    /**
     * If changes were made, venue name and type will be saved to the database.
     */
    public void onSaveChangesClicked(){

        if(changeNameField.getText().isEmpty() && changeTypeField.getText().isEmpty())
            return;

        int selectedVenue = venueListView.getSelectionModel().getSelectedIndex();
        String newVenueName;
        String newVenueType;

        if(changeNameField.getText().isEmpty()){

            newVenueName = controller.getVenueNames().get(selectedVenue);
            newVenueType = changeTypeField.getText();

        }else if(changeTypeField.getText().isEmpty()){

            newVenueName = changeNameField.getText();
            newVenueType = controller.getVenueTypes().get(selectedVenue);

        }else{

            newVenueName = changeNameField.getText();
            newVenueType = changeTypeField.getText();

        }

        conn.updateVenueInfo(selectedVenue, newVenueName, newVenueType);
        venueListView.getSelectionModel().clearSelection();
        changeNameField.clear();
        changeTypeField.clear();

    }

    /**
     * Returns the user to the venue scene when they choose to logout.
     */
    public void onLogoutClicked(){

        try {
            SeatDApplication.getCoordinator().showVenueScene();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
