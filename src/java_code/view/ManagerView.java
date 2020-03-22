package java_code.view;

import java_code.controller.Controller;

import java_code.controller.ServerConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;

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
        refreshPage();

    }

    /**
     * Fetches the most recent data for a specific venue and loads it into the scene.
     */
    private void refreshPage(){

        venueNameLabel.setText("af");
        waitPerPatronField.setPromptText(Integer.toString(15));
        waitPerPatronField.setFocusTraversable(false);

        waitlistView.getItems().clear();
        ObservableList<String> waitlistPatrons = FXCollections.observableArrayList();

        if (0 > 0) {

            Object[] tmp = new Object[2]/*controller.waitlist.toArray()*/;

            for (int i = 0; i < 0; i++)
                waitlistPatrons.add(Integer.toString(i + 1).concat(". ").concat(tmp[i].toString()));

            waitlistView.setItems(waitlistPatrons);

        } else {
            waitlistPatrons.add("Nobody is on the waitlist your venue sucks.");
        }

        totalWaitTimeLabel.setText(Integer.toString(waitlistPatrons.size() * 15));

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
            SeatDApplication.getCoordinator().showVenueScene();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
