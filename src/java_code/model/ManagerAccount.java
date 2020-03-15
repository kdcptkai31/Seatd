package java_code.model;

/**
 * Represents a manager account object.
 */
public class ManagerAccount {

    private String username;
    private String password;

    //Constructors
    public ManagerAccount(){this("", "");}
    public ManagerAccount(ManagerAccount obj){this(obj.username, obj.password);}
    public ManagerAccount(String username, String password){

        this.username = username;
        this.password = password;

    }

    public boolean checkPassword(String password){return this.password.equals(password);}

}
