package java_code;

import javafx.application.Application;
import java_code.view.SeatDApplication;

/**
 * Application starts this way so that we can easily launch the server application with the same main, just add an if
 * statement to check if the args has a arbitrary "server" tag.
 */
public class Main{

    public static void main(String[] args){

        Application.launch(SeatDApplication.class, args);

    }
}
