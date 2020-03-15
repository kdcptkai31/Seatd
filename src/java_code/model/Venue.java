package java_code.model;

import java.util.concurrent.BlockingQueue;

/**
 * Will represent a venue. (Will take over "Controller"'s function once server is implemented)
 */
public class Venue {

    public String venueName;
    public String venueType;
    public BlockingQueue<Patron> waitlist;  //This is thread safe
    public int waitPerPatron;

}
