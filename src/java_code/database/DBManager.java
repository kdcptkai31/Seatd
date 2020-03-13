package java_code.database;

import java_code.model.ManagerAccount;

/**
 * Manages the server's access to the main database.
 */
public class DBManager {

    public DBManager(){



    }


    public static ManagerAccount getManagerAccountByUsername(String username){

        //Parse db for the manager account
        return new ManagerAccount("manager", "manager");//Dummy login info

    }

}
