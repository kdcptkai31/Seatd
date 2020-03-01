package java_code.model;

/**
 * Represents a patron on a waitlist.
 */
public class Patron {

    private String name;
    private String email;

    public Patron(String name, String email){

        this.name = name;
        this.email = email;

    }

    /**
     * Returns the name and email of the patron in an organized way.
     * @return
     */
    @Override
    public String toString(){return name.concat(" | ".concat(email));}

    //Getters
    public String getName(){return name;}
    public String getEmail(){return email;}

}
