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
    public String tmpName;
    public String tmpEmail;

    //Used for managers
    private String managerUsername;
    private String managerPassword;
    private ArrayList<Patron> tmpWaitlistOrder;

    //Used for admins
    private Vector<String> venueNames;
    private Vector<String> venueTypes;
    private Vector<String> managerUsernames;

    /**
     * Constructor
     */
    public Controller(){

        managerUsername = "";
        managerPassword = "";
        tmpName = "";
        tmpEmail = "";
        tmpWaitlistOrder = new ArrayList<>();
        venueNames = new Vector<>();
        venueTypes = new Vector<>();
        managerUsernames = new Vector<>();

    }

    //Getters
    public String getManagerUsername() {return managerUsername;}
    public String getManagerPassword() { return managerPassword; }
    public int getVenueID(){return venueID;}
    public ArrayList<Patron> getTmpWaitlistOrder(){return tmpWaitlistOrder;}
    public Vector<String> getVenueNames(){return venueNames;}
    public Vector<String> getVenueTypes(){return venueTypes;}
    public Vector<String> getManagerUsernames(){return managerUsernames;}

    //Setters
    public void setManagerUsername(String managerUsername){this.managerUsername = managerUsername;}
    public void setManagerPassword(String managerPassword) { this.managerPassword = managerPassword; }
    public void setVenueID(int i){venueID = i;}
    public void setTmpWaitlistOrder(ArrayList<Patron> vector){tmpWaitlistOrder = vector;}
    public void setVenueNames(Vector<String> venueNames) {this.venueNames = venueNames;}
    public void setVenueTypes(Vector<String> venueTypes) {this.venueTypes = venueTypes;}
    public void setManagerUsernames(Vector<String> venueUsernames){this.managerUsernames = venueUsernames;}
}
