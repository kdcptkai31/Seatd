package java_code.view;

import java_code.controller.Controller;
import java_code.model.Patron;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;

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
    @FXML
    Label waitlistSize;
    @FXML
    TextField nameField;
    @FXML
    TextField emailField;
    @FXML
    Label errorText;

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
        waitlistSize.setText(Integer.toString(controller.waitlist.size()));
        errorText.setVisible(false);

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

    /**
     * Adds the input information to the waitlist, if it is all there.
     */
    public void onAddToWaitlistClicked(){

        if(!nameField.getText().equals("") && !emailField.getText().equals("")){

            try{
                errorText.setVisible(false);
                controller.waitlist.put(new Patron(nameField.getText(), emailField.getText()));
                SeatDApplication.getCoordinator().showVenueScene();
            }catch (Exception e){
                e.printStackTrace();
            }


        }else{

            errorText.setText("***missing credentials***");
            errorText.setVisible(true);

        }

    }

}
