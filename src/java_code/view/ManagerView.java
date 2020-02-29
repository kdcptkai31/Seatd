package java_code.view;

import java_code.controller.Controller;
import javafx.fxml.FXML;

public class ManagerView {

    Controller controller;

    /**
     * Initializes the scene.
     */
    @FXML
    protected void initialize(){

        controller = SeatDApplication.getController();
        SeatDApplication.setToDefaultWindowSize();

    }

}
