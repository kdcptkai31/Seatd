package java_code.view;

import java_code.controller.Controller;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class SeatDApplication extends Application {

    private static SceneCoordinator coordinator;
    private static Stage Window;

    //Constants
    final private static int defaultWidth = 600;
    final private static int defaultHeight = 500;

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initializes the window and loads the main venue scene.
     * @param primaryStage
     */
    @Override
    public void start(Stage primaryStage) {

        Window = primaryStage;
        Window.setTitle("SeatD");
        Window.setWidth(400);
        Window.setHeight(600);
        Window.show();
        Window.setResizable(false);

        coordinator = new SceneCoordinator(Window);

        try {
            coordinator.showVenueListScene();
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

    /**
     * Changes the window dimensions.
     * @param width
     * @param height
     */
    public static void setWindowSize(int width, int height){

        Window.setWidth(width);
        Window.setHeight(height);

    }

    /**
     * Changes the window dimensions to the default size.
     */
    public static void setToDefaultWindowSize(){ setWindowSize(defaultWidth, defaultHeight);}

    /**
     * Closes the application on window closure.
     */
    @Override
    public void stop(){coordinator.onExitRequested();}

}
