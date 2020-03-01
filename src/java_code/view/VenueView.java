package java_code.view;

import java_code.controller.Controller;
import java_code.model.Patron;
import javafx.fxml.FXML;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;

public class VenueView {

    Controller controller;
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

    /**
     * Initializes the scene.
     */
    @FXML
    protected void initialize(){

        controller = SeatDApplication.getController();
        SeatDApplication.setToDefaultWindowSize();
        refresh();
        errorText.setVisible(false);

    }

    /**
     * Refreshes the scene with the most current data.
     */
    public void refresh(){

        venueNameLabel.setText(controller.venueName);
        venueTypeLabel.setText(controller.venueType);
        waitTimeLabel.setText(Integer.toString(controller.waitlist.size() * controller.waitPerPatron));
        waitlistSize.setText(Integer.toString(controller.waitlist.size()));

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
