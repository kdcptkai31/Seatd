package java_code.view;

import java_code.controller.Controller;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import javafx.scene.control.Label;

import java.io.IOException;

public class VenueView {

    Controller controller;
    @FXML
    Button loginButton;
    @FXML
    Label venueNameLabel;
    @FXML
    Label venueTypeLabel;
    @FXML
    Label waitTimeLabel;

    /**
     * Initializes the scene.
     */
    @FXML
    protected void initialize(){

        controller = SeatDApplication.getController();
        SeatDApplication.setToDefaultWindowSize();
        venueNameLabel.setText(controller.venueName);
        venueTypeLabel.setText(controller.venueType);
        waitTimeLabel.setText(Integer.toString(controller.waitlist.size() * controller.waitPerPatron));

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

}
