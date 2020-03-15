package java_code;

import java_code.server.Server;
import javafx.application.Application;
import java_code.view.SeatDApplication;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Application starts this way so that we can easily launch the server application with the same main, just add an if
 * statement to check if the args has a arbitrary "server" tag.
 */
public class Main{

    public static void main(String[] args){

        if(new ArrayList<>(Arrays.asList(args)).contains("server")){

            Server server = new Server();
            server.start();

        }else
            Application.launch(SeatDApplication.class, args);

    }
}
