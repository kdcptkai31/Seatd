package java_code.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java_code.controller.Controller;

import java.io.IOException;

public class VenueView {

    Controller controller;

    @FXML
    Button loginButton;

    /**
     * Initializes the scene.
     */
    @FXML
    protected void initialize(){

        controller = SeatDApplication.getController();
        SeatDApplication.setToDefaultWindowSize();

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
