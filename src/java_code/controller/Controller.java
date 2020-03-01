package java_code.controller;

import java_code.model.Patron;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Holds local information, will be removed once the server is working fully and correctly. Represents a single venue.
 */
public class Controller {

    public String venueName;
    public String venueType;
    public BlockingQueue<Patron> waitlist;  //This is thread safe
    public int waitPerPatron;

    /**
     * Loads defaults for the first sprint data.
     */
    public Controller(){

        venueName = "FRYING NEMO";
        venueType = "Fish and Chips";
        waitlist = new ArrayBlockingQueue<Patron>(100); //This is thread safe
        waitPerPatron = 15;


    }

}
