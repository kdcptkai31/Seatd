package java_code.model;

import java.util.UUID;

/**
 * Represents a manager account object.
 */
public class ManagerAccount {

    private UUID id;
    private String username;
    private String password;

    //Constructors
    public ManagerAccount(){this(UUID.randomUUID(), "", "");}
    public ManagerAccount(ManagerAccount obj){this(obj.id, obj.username, obj.password);}
    public ManagerAccount(UUID id, String username, String password){

        this.id = id;
        this.username = username;
        this.password = password;

    }

    public boolean checkPassword(String password){

        return this.password.equals(password);

    }

}
