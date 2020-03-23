package java_code.model;

/**
 * Represents a manager account object.
 */
public class ManagerAccount {

    private String username;
    private String password;
    private int venueID;

    //Constructors
    public ManagerAccount(){this(-1, "", "");}
    public ManagerAccount(ManagerAccount obj){this(obj.venueID, obj.username, obj.password);}
    public ManagerAccount(int venueID, String username, String password){

        this.venueID = venueID;
        this.username = username;
        this.password = password;

    }

    public boolean checkPassword(String password){return this.password.equals(password);}
    public int getVenueID(){return venueID;}

}
