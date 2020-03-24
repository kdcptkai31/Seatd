package java_code.controller;

import java_code.model.Patron;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Holds local information.
 */
public class Controller {

    private String managerUsername;
    private int venueID;
    private ArrayList<Patron> tmpWaitlistOrder;

    public Controller(){

        managerUsername = "";
        tmpWaitlistOrder = new ArrayList<>();

    }

    public String getManagerUsername() {return managerUsername;}
    public int getVenueID(){return venueID;}
    public ArrayList<Patron> getTmpWaitlistOrder(){return tmpWaitlistOrder;}
    public void setManagerUsername(String managerUsername){this.managerUsername = managerUsername;}
    public void setVenueID(int i){venueID = i;}
    public void setTmpWaitlistOrder(ArrayList<Patron> vector){tmpWaitlistOrder = vector;}

}
