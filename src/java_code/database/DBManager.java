package java_code.database;

import com.sun.mail.smtp.SMTPTransport;
import java_code.model.ManagerAccount;
import java_code.model.Patron;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
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

        String sql = "SELECT username, password, venue_id FROM manager_account m WHERE m.username = ?";

        try {

            PreparedStatement stmt  = connection.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                return new ManagerAccount(rs.getInt("venue_id"), rs.getString("username"),
                        rs.getString("password"));
            }
            return null;



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
     * Removes the first instance of a patron on the waitlist and sends them an email, effectively "serving" them.
     * @param venueID
     */
    public static void servePatron(int venueID){

        String sql = "SELECT * FROM waitlist WHERE cor_venue_id = ? LIMIT 1;";
        String sql2 = "DELETE FROM waitlist WHERE cor_venue_id = ? AND user_name = ? AND email = ?;";
        String sq123 = "SELECT name FROM venue WHERE venue_id = ?";
        try{
            //Gets the person on the top of the waitlist.
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, venueID);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            Patron tmp = new Patron(rs.getString("user_name"), rs.getString("email"));
            int tmpVenueId = rs.getInt("cor_venue_id");
            rs.close();

            //Deletes the person on top of the waitlist.s
            PreparedStatement stmt2 = connection.prepareStatement(sql2);
            stmt2.setInt(1, venueID);
            stmt2.setString(2, tmp.getName());
            stmt2.setString(3, tmp.getEmail());
            stmt2.executeUpdate();

            //Finds the venue name from the given venue id
            PreparedStatement stmt3 = connection.prepareStatement(sq123);
            stmt3.setInt(1, tmpVenueId);
            ResultSet rs3 = stmt3.executeQuery();
            rs3.next();
            String tmpVenueName = rs3.getString("name");
            System.out.println(tmpVenueName);
            sendEmail(tmp, tmpVenueName);
            rs3.close();
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

    /**
     * Returns all current venue names.
     * @return
     */
    public static Vector<String> getAllVenueNames(){

        String sql = "SELECT * FROM venue";

        try{
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            Vector<String> tmp = new Vector<>();
            while(rs.next())
                tmp.add(rs.getString("name"));

            return tmp;

        }catch (SQLException e){
            e.printStackTrace();
        }

        return null;

    }

    /**
     * Returns the current venue types.
     * @return
     */
    public static Vector<String> getAllVenueTypes(){

        String sql = "SELECT * FROM venue";

        try{
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            Vector<String> tmp = new Vector<>();
            while(rs.next())
                tmp.add(rs.getString("venue_type"));

            return tmp;

        }catch (SQLException e){
            e.printStackTrace();
        }

        return null;

    }

    /**
     * Returns the name and wait per patron value for the given venueID.
     * @param venueID
     * @return
     */
    public static Vector<String> getVenueNameAndWaitPerPatron(int venueID){

        String sql = "SELECT name, wait_per_patron FROM venue WHERE venue_id = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, venueID);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            Vector<String> tmp = new Vector<>();
            tmp.add(rs.getString("name"));
            tmp.add(String.valueOf(rs.getInt("wait_per_patron")));
            rs.close();
            return tmp;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * Returns the name and emails of every patron on the waitlist of a given venue.
     * @param venueID
     * @return
     */
    public static Vector<Patron> getWaitlistFromVenueID(int venueID){

        String sql = "SELECT user_name, email FROM waitlist WHERE cor_venue_id = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, venueID);
            ResultSet rs = stmt.executeQuery();
            Vector<Patron> tmp = new Vector<>();
            while(rs.next())
                tmp.add(new Patron(rs.getString("user_name"), rs.getString("email")));

            rs.close();
            return tmp;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * Updates a venue's wait per patron value.
     * @param venueID
     * @param waitPerPatronValue
     * @return
     */
    public static boolean updateVenueWaitPerPatron(int venueID, int waitPerPatronValue){

        String sql = "UPDATE venue SET wait_per_patron = ? WHERE venue_id = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, waitPerPatronValue);
            stmt.setInt(2, venueID);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * Updates a venue's name and type.
     * @param venueID
     * @param newName
     * @param newType
     */
    public static void updateVenueNameAndType(int venueID, String newName, String newType){

        String sql = "UPDATE venue SET name = ?, venue_type = ? WHERE venue_id = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, newName);
            stmt.setString(2, newType);
            stmt.setInt(3, venueID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Attempts to delete a patron from the given venue.
     * @param venueID
     * @param name
     * @param email
     * @return
     */
    public static boolean deletePatronFromVenueWaitlist(int venueID, String name, String email){

        String sql = "DELETE FROM waitlist WHERE cor_venue_id = ? AND user_name = ? AND email = ?";

        try {
            PreparedStatement stmt2 = connection.prepareStatement(sql);
            stmt2.setInt(1, venueID);
            stmt2.setString(2, name);
            stmt2.setString(3, email);
            stmt2.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * Sets the waitlist for a given venue.
     * @param venueID
     * @param list
     */
    public static void setWaitlistForVenue(int venueID, ArrayList<Patron> list){

        String sql = "DELETE FROM waitlist WHERE cor_venue_id = ?";

        try{
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, venueID);
            stmt.executeUpdate();

            for(int i = 0; i < list.size(); i++){

                String string = "INSERT INTO waitlist(cor_venue_id, user_name, email) VALUES(?, ?, ?)";

                PreparedStatement statement = connection.prepareStatement(string);
                statement.setInt(1, venueID);
                statement.setString(2, list.get(i).getName());
                statement.setString(3, list.get(i).getEmail());
                statement.executeUpdate();

            }

        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    /**
     * Sends an email to the patron who just got served, notifying them that their table is ready.
     * @param patron
     */
    private static void sendEmail(Patron patron, String venueName){

        // Setup mail server
        Properties prop = System.getProperties();
        prop.put("mail.smtp.host", "smtp-relay.sendinblue.com"); //optional, defined in SMTPTransport
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.port", "587"); // default port 25

        Session session = Session.getInstance(prop, null);
        Message msg = new MimeMessage(session);
        try {

            // from
            msg.setFrom(new InternetAddress("services@SeatD.com"));

            // to
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(patron.getEmail()));

            // subject
            msg.setSubject("SeatD - Your Table at " + venueName + " is Ready!");

            // content
            msg.setText("Hello ".concat(patron.getName()).concat(",\n\n" + "Your table at ").
                    concat(venueName).concat(" is ready. Please confirm with the host within 15 minutes you may lose ").
                    concat("your reservation. Thank you for using SeatD,\n\n - SeatD Admins"));

            //Date
            msg.setSentDate(new Date());

            //Send Email
            SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
            // connect
            t.connect("smtp-relay.sendinblue.com", "kdcptkai31@gmail.com", "N06pBhOgJ3RtCLrc");
            // send
            t.sendMessage(msg, msg.getAllRecipients());
            System.out.println("Response: " + t.getLastServerResponse());
            t.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
