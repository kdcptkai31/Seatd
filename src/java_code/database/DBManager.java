package java_code.database;

import java_code.model.ManagerAccount;
import java_code.model.Patron;

import java.sql.*;
import java.util.Vector;

/**
 * Manages the server's access to the main SeatD database.
 */
public class DBManager {

    private static Connection connection = getConnection();

    public DBManager(){

        checkIfDatabaseExists();
        //insertDefaultDatabaseInformation()---dummy data for the project

    }

    /**
     * Creates a connection to the database file.
     * @return
     */
    public static Connection getConnection(){

        try {
            return DriverManager.getConnection("jdbc:sqlite:src/db/seatd.db");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Checks if the database file has information already, if not it creates the needed tables.
     */
    private static void checkIfDatabaseExists(){

        // SQL operation for creating a new venue table
        String sqlVenue = "CREATE TABLE IF NOT EXISTS venue (\n"
                + "    venue_id INTEGER PRIMARY KEY,\n"
                + "    name TEXT NOT NULL UNIQUE,\n"
                + "    venue_type TEXT NOT NULL,\n"
                + "    wait_per_patron INTEGER NOT NULL\n"
                + ");";

        try {
            Statement stmt = connection.createStatement();
            stmt.execute(sqlVenue);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // SQL operation for creating a new manager account table
        String sqlManager = "CREATE TABLE IF NOT EXISTS manager_account (\n"
                + "    manager_id INTEGER PRIMARY KEY,\n"
                + "    username TEXT NOT NULL,\n"
                + "    password TEXT NOT NULL,\n"
                + "    venue_id INTEGER NOT NULL UNIQUE\n"
                //+ "    ,FOREIGN KEY (venue_id) REFERENCES venue (id) ON DELETE SET NULL"
                + ");";

        try {
            Statement stmt = connection.createStatement();
            stmt.execute(sqlManager);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // SQL operation for creating a new waitlist table
        String sqlWaitlist = "CREATE TABLE IF NOT EXISTS waitlist (\n"
                + "    cor_venue_id INTEGER NOT NULL,\n"
                + "    user_name TEXT NOT NULL,\n"
                + "    email TEXT NOT NULL\n"
                + ");";

        try {
            Statement stmt = connection.createStatement();
            stmt.execute(sqlWaitlist);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param username
     * @return the ManagerAccount object to the corresponding username. Returns null if it does not exist.
     */
    public static ManagerAccount getManagerAccountByUsername(String username){

        String sql = "SELECT username, password FROM manager_account m WHERE m.username = ?";

        try {

            PreparedStatement stmt  = connection.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                return new ManagerAccount(rs.getString("username"), rs.getString("password"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * Returns the number of people on the waitlist for each venue.
     * @return
     */
    public static Vector<Integer> getAllWaitlistSizes(){

        String sql = "SELECT cor_venue_id FROM waitlist";

        try{
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            Vector<Integer> tmp = new Vector<>();
            while(rs.next())
                tmp.add(rs.getInt("cor_venue_id"));

            if(!tmp.isEmpty()){
                Vector<Integer> results = new Vector<>();
                int venueSize = getVenueCount();
                for(int i = 0; i < venueSize; i++)
                    results.add(0);

                for(int i = 0; i < tmp.size(); i++)
                    results.setElementAt(results.get(tmp.elementAt(i)) + 1, tmp.elementAt(i));

                return results;

            }

            return null;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * Returns the wait per patron value for each venue in the database.
     * @return
     */
    public static Vector<Integer> getAllWaitPerPatrons(){

        String sql = "SELECT wait_per_patron FROM venue";

        try{
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            Vector<Integer> tmp = new Vector<>();
            while(rs.next())
                tmp.add(rs.getInt("wait_per_patron"));

            return tmp;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * Adds a patron to the venue they are trying to add themselves to.
     * @param venueID
     * @param patron
     * @return true if it is successful, false if not.
     */
    public static boolean addToWaitlist(int venueID, Patron patron){

        String sql = "INSERT INTO waitlist(cor_venue_id, user_name, email) VALUES(?, ?, ?)";

        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, venueID);
            stmt.setString(2, patron.getName());
            stmt.setString(3, patron.getEmail());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;

    }

    /**
     * Removes the first instance of a patron on the waitlist, effectively "serving" them.
     * @param venueID
     */
    public static void servePatron(int venueID){

        String sql = "SELECT * FROM waitlist WHERE cor_venue_id = ? LIMIT 1;";
        String sql2 = "DELETE FROM waitlist WHERE cor_venue_id = ? AND user_name = ? AND email = ?;";
        try{
            //Gets the person on the top of the waitlist.
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, venueID);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            Patron tmp = new Patron(rs.getString("user_name"), rs.getString("email"));
            rs.close();

            //Deletes the person on top of the waitlist.s
            PreparedStatement stmt2 = connection.prepareStatement(sql2);
            stmt2.setInt(1, venueID);
            stmt2.setString(2, tmp.getName());
            stmt2.setString(3, tmp.getEmail());
            stmt2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Returns the number of venues in the database.
     * @return
     */
    public static int getVenueCount(){

        String sql = "SELECT COUNT(*) FROM venue";

        try{
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            return rs.getInt("COUNT(*)");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;

    }

}
