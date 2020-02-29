package java_code.view;

import java_code.controller.Controller;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class SeatDApplication extends Application {

    private static SceneCoordinator coordinator;
    private static Stage Window;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {



        Window = primaryStage;
        Window.setTitle("SeatD");
        Window.setWidth(600);
        Window.setHeight(800);
        Window.show();
        Window.setResizable(false);

        coordinator = new SceneCoordinator(Window);

        try {
            coordinator.showVenueScene();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Allows the controller to be accessed by each scene.
     * @return
     */
    public static Controller getController(){return coordinator.getController();}

    /**
     * Allows the scene coordinator to be called from each scene view.
     * @return
     */
    public static SceneCoordinator getCoordinator(){return coordinator;}

    public static void setWindowSize(double width, double height){

        Window.setWidth(width);
        Window.setHeight(height);

    }

}
