package java_code.view;

import java_code.controller.ServerConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginView {

    ServerConnection conn;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private Label errorText;

    /**
     * Initializes the scene.
     */
    @FXML
    protected void initialize(){

        SeatDApplication.setWindowSize(600, 390);

        conn = ServerConnection.getInstance();
        conn.getLoggedInObservable().subscribe((onLoginChanged) -> {
            if (onLoginChanged.equals(true)) {
                Platform.runLater(() -> {
                    try {
                        SeatDApplication.getCoordinator().showManagerScene();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                Platform.runLater(() -> {
                    errorText.setText("***invalid credentials***");
                    errorText.setVisible(true);
                });
            }
        });

        Platform.runLater(()->{
            errorText.setVisible(false);
        });

    }

    /**
     * Checks if login info is present, if so, then it validates the info and logs in the user. This can either be a
     * venue manager or a SeatD admin.
     */
    public void onLoginClicked(){

        if(!usernameField.getText().equals("") && !passwordField.getText().equals("")){
            conn.login(usernameField.getText(), passwordField.getText());
            errorText.setVisible(false);

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