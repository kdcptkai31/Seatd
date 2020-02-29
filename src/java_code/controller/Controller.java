package java_code.controller;

import java_code.model.Patron;

import java.util.Vector;

/**
 * Holds local information, will be removed once the server is working fully and correctly.
 */
public class Controller {

    public String venueName;
    public String venueType;
    public Vector<Patron> waitlist;
    public int waitPerPatron;

    /**
     * Loads defaults for the first sprint data.
     */
    public Controller(){

        venueName = "FRYING NEMO";
        venueType = "Fish and Chips";
        waitlist = new Vector<>();
        waitPerPatron = 15;


    }

}
