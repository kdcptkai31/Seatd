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
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

public class VenueListView {

    ServerConnection conn;
    Controller controller;
    private Vector<String> localVenueNames;

    @FXML
    private ComboBox<String> sortBox;
    @FXML
    private ListView<String> venueList;

    /**
     * Initializes the scene.
     */
    @FXML
    protected void initialize(){

        SeatDApplication.setWindowSize(400, 600);
        controller = SeatDApplication.getController();
        conn = ServerConnection.getInstance();
        localVenueNames = new Vector<>();

        venueList.setFocusTraversable(false);
        ObservableList<String> sortBoxOptions = FXCollections.observableArrayList();
        sortBoxOptions.add("Alphabetical");
        sortBoxOptions.add("Shortest Wait");
        sortBoxOptions.add("Most Popular");
        sortBox.setItems(sortBoxOptions);
        sortBox.getSelectionModel().selectFirst();

        conn.getVenueListData();
        registerGetVenueListData();

    }

    /**
     * Listens for the data coming from the server to fill the objects on the venue list page. Based on the current
     * sortBox selection, the data will be sorted. It can currently be sorted alphabetically or by shortest waittime.
     */
    private void registerGetVenueListData(){

        conn.getPubNub().addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus pnStatus) {}
            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {

                String type = message.getMessage().getAsJsonObject().get("type").getAsString();//Message type

                if(!type.equals("venueListData"))
                    return;

                Platform.runLater(()->{venueList.getItems().clear();});

                //Extract data
                JsonObject data = message.getMessage().getAsJsonObject().get("data").getAsJsonObject();
                JsonArray names = data.get("names").getAsJsonArray();
                JsonArray types = data.get("types").getAsJsonArray();
                JsonArray waits = data.get("waits").getAsJsonArray();
                Iterator<JsonElement> nameElement = names.iterator();
                Iterator<JsonElement> typeElement = types.iterator();
                Iterator<JsonElement> waitElement = waits.iterator();

                Vector<String> vectorNames = new Vector<>();
                Vector<String> vectorTypes = new Vector<>();
                Vector<Integer> vectorWaits = new Vector<>();

                while(nameElement.hasNext() && typeElement.hasNext() && waitElement.hasNext()){

                    vectorNames.add(nameElement.next().getAsString());
                    vectorTypes.add(typeElement.next().getAsString());
                    vectorWaits.add(waitElement.next().getAsInt());

                }

                //Saves a local copy of the venue order for venueID index purposes.
                localVenueNames = new Vector<>(vectorNames);
                ObservableList<String> venueViewList = FXCollections.observableArrayList();
                if(!vectorNames.isEmpty()){

                    Vector<String> finalListStrings = new Vector<>();
                    for(int i = 0; i < vectorNames.size(); i++) {
                        finalListStrings.add(vectorNames.get(i).concat(" | ").concat(vectorTypes.get(i).
                                concat(" | WaitTime: ").concat(String.valueOf(vectorWaits.get(i)))));
                    }

                    switch (sortBox.getSelectionModel().getSelectedIndex()){

                        //Sorts alphabetically
                        case 0:
                            Collections.sort(finalListStrings);
                            break;

                        //Sorts by shortest waittime
                        case 1:
                            for(int i = 0; i < finalListStrings.size(); i++){

                                for(int j = i + 1; j < finalListStrings.size(); j++){

                                    if(vectorWaits.get(i) > vectorWaits.get(j)){

                                        int tmpInt = vectorWaits.get(i);
                                        vectorWaits.setElementAt(vectorWaits.get(j), i);
                                        vectorWaits.setElementAt(tmpInt, j);

                                        String tmpStr = finalListStrings.get(i);
                                        finalListStrings.setElementAt(finalListStrings.get(j), i);
                                        finalListStrings.setElementAt(tmpStr, j);

                                    }
                                }
                            }

                            break;

                        //Sorts by most popular
                        case 2:
                            for(int i = 0; i < finalListStrings.size(); i++){

                                for(int j = i + 1; j < finalListStrings.size(); j++){

                                    if(vectorWaits.get(i) > vectorWaits.get(j)){

                                        int tmpInt = vectorWaits.get(i);
                                        vectorWaits.setElementAt(vectorWaits.get(j), i);
                                        vectorWaits.setElementAt(tmpInt, j);

                                        String tmpStr = finalListStrings.get(i);
                                        finalListStrings.setElementAt(finalListStrings.get(j), i);
                                        finalListStrings.setElementAt(tmpStr, j);

                                    }
                                }
                            }
                            Collections.reverse(finalListStrings);

                            break;

                    }

                    for(int i = 0; i < vectorNames.size(); i++)
                        venueViewList.add(finalListStrings.get(i));

                }else
                    venueViewList.add("NO VENUES FOUND");


                Platform.runLater(()->{
                    venueList.setItems(venueViewList);
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

    public void onSortMethodChanged(){
        System.out.println("RUNS");
        conn.getVenueListData();}

    /**
     * Brings the user to the login page.
     */
    public void onloginClicked(){

        try {
            SeatDApplication.getCoordinator().showLoginScene();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Sends the user to the venue page for the venue they selected in the list.
     */
    public void onGoToWaitlistClicked(){

        if(venueList.getSelectionModel().isEmpty())
            return;

        //Get Venue Name From Selection
        String[] selection = venueList.getSelectionModel().getSelectedItem().split(" | ");
        String venueName = "";
        boolean nameFound = false;
        for(int i = 0; i < selection.length; i++){

            if(selection[i].equals("|"))
                nameFound = true;

            if(!nameFound && i == 0)
                venueName = venueName.concat(selection[i]);

            if(!nameFound && i > 0)
                venueName = venueName.concat(" " + selection[i]);

        }

        controller.setVenueID(localVenueNames.indexOf(venueName));
        try {
            SeatDApplication.getCoordinator().showVenueScene();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
