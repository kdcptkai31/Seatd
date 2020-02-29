package java_code.view;

import java_code.controller.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * Handles how the scenes switch around in the main window.
 */
public class SceneCoordinator {

    private Stage window;
    private Controller controller;

    /**
     * Loads in the window obj, as well as initializes the controller for use through the application.
     * @param window
     */
    public SceneCoordinator(Stage window){

        this.window = window;
        controller = new Controller();

    }

    /**
     *
     * @return the controller
     */
    public Controller getController(){return controller;}

    /**
     * Loads the venue scene into the main window.
     * @throws IOException
     */
    public void showVenueScene() throws IOException {

        window.setScene(new Scene(FXMLLoader.load(new File("src/resources/layout/venue.fxml").toURL())));

    }

}
