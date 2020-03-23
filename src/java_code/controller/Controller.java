package java_code.controller;

/**
 * Holds local information.
 */
public class Controller {

    private String managerUsername;
    private int venueID;

    public Controller(){

        managerUsername = "";

    }

    public String getManagerUsername() {return managerUsername;}
    public int getVenueID(){return venueID;}
    public void setManagerUsername(String managerUsername){this.managerUsername = managerUsername;}
    public void setVenueID(int i){venueID = i;}

}
