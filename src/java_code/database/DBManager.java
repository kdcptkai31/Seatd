package java_code.database;

import java_code.model.ManagerAccount;

import java.sql.*;

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
    private void checkIfDatabaseExists(){

        // SQL operation for creating a new venue table
        String sqlVenue = "CREATE TABLE IF NOT EXISTS venue (\n"
                + "    venue_id INTEGER PRIMARY KEY,\n"
                + "    name TEXT NOT NULL UNIQUE,\n"
                + "    venue_type TEXT NOT NULL UNIQUE,\n"
                + "    wait_per_patron REAL NOT NULL\n"
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

}
