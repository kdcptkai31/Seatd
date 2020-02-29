package java_code.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java_code.controller.Controller;

public class VenueView {

    Controller controller;

    @FXML
    Label testLabel;

    @FXML
    protected void initialize(){

        controller = SeatDApplication.getController();


    }

}
