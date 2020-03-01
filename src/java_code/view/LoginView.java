package java_code.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginView {

    @FXML
    TextField usernameField;
    @FXML
    TextField passwordField;
    @FXML
    Label errorText;

    /**
     * Initializes the scene.
     */
    @FXML
    protected void initialize(){

        SeatDApplication.setWindowSize(600, 390);
        errorText.setVisible(false);

    }

    /**
     * Checks if login info is present, if so, then it validates the info and logs in the user. This can either be a
     * venue manager or a SeatD admin.
     */
    public void onLoginClicked(){

        if(!usernameField.getText().equals("") && !passwordField.getText().equals("")){

            errorText.setVisible(false);
            if(usernameField.getText().equals("manager") && usernameField.getText().equals("manager")) {

                try {
                    SeatDApplication.getCoordinator().showManagerScene();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }else{

                errorText.setText("***invalid credentials***");
                usernameField.clear();
                passwordField.clear();
                errorText.setVisible(true);

            }

        }else{

            errorText.setText("***missing credentials***");
            errorText.setVisible(true);

        }

    }

    /**
     * Returns to the venue scene.
     */
    public void onCancelClicked(){

        try {
            SeatDApplication.getCoordinator().showVenueScene();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}