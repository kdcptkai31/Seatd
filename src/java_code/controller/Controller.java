package java_code.controller;

import java_code.model.Patron;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Holds local client information.
 */
public class Controller {

    //Used for everyone
    private int venueID;
    public String tmpEmail;

    //Used for managers
    private String managerUsername;
    private ArrayList<Patron> tmpWaitlistOrder;

    //Used for admins
    private Vector<String> venueNames;
    private Vector<String> venueTypes;

    /**
     * Constructor
     */
    public Controller(){

        managerUsername = "";
        tmpEmail = "";
        tmpWaitlistOrder = new ArrayList<>();
        venueNames = new Vector<>();
        venueTypes = new Vector<>();

    }

    //Getters
    public String getManagerUsername() {return managerUsername;}
    public int getVenueID(){return venueID;}
    public ArrayList<Patron> getTmpWaitlistOrder(){return tmpWaitlistOrder;}
    public Vector<String> getVenueNames(){return venueNames;}
    public Vector<String> getVenueTypes(){return venueTypes;}

    //Setters
    public void setManagerUsername(String managerUsername){this.managerUsername = managerUsername;}
    public void setVenueID(int i){venueID = i;}
    public void setTmpWaitlistOrder(ArrayList<Patron> vector){tmpWaitlistOrder = vector;}
    public void setVenueNames(Vector<String> venueNames) {this.venueNames = venueNames;}
    public void setVenueTypes(Vector<String> venueTypes) {this.venueTypes = venueTypes;}
}
