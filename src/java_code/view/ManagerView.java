package java_code.view;

import java_code.controller.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;

public class ManagerView {

    Controller controller;

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

        controller = SeatDApplication.getController();
        SeatDApplication.setToDefaultWindowSize();
        refreshPage();

    }

    /**
     * Fetches the most recent data for a specific venue and loads it into the scene.
     */
    private void refreshPage(){

        venueNameLabel.setText(controller.venueName);
        waitPerPatronField.setPromptText(Integer.toString(controller.waitPerPatron));
        waitPerPatronField.setFocusTraversable(false);

        waitlistView.getItems().clear();
        ObservableList<String> waitlistPatrons = FXCollections.observableArrayList();

        if (controller.waitlist.size() > 0) {

            Object[] tmp = controller.waitlist.toArray();

            for (int i = 0; i < controller.waitlist.size(); i++)
                waitlistPatrons.add(Integer.toString(i + 1).concat(". ").concat(tmp[i].toString()));

            waitlistView.setItems(waitlistPatrons);

        } else {
            waitlistPatrons.add("Nobody is on the waitlist your venue sucks.");
        }

        totalWaitTimeLabel.setText(Integer.toString(waitlistPatrons.size() * controller.waitPerPatron));

    }

    /**
     * Checks if a new wait time per patron was entered, if so, it updates the controller and refreshes the scene.
     */
    public void onUpdateWaitPerPatronClicked(){

        if(!waitPerPatronField.getText().equals(Integer.toString(controller.waitPerPatron))) {

            controller.waitPerPatron = Integer.parseInt(waitPerPatronField.getText());
            refreshPage();
        }

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
