package java_code.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import java_code.database.DBManager;
import java_code.model.Patron;
import java_code.server.handlers.*;

import java.util.*;

/**
 * The running server. Will handle the delegation of all incoming messages by sending them to the correct handler, where
 * the logic of that message will be executed.
 */
public class Server {

    private static String pubKey = "pub-c-01d2cd3b-e3bb-4881-a9ac-e896e0476911";
    private static String subKey = "sub-c-d3b9c360-5fee-11ea-b7ea-f2f107c29c38";
    private PubNub pubnub;
    private PNConfiguration pnConfiguration;
    private MessageDelegator delegator;
    private HashMap<String, String> users;
    private DBManager dbManager;
    private Vector<Integer> currentVenueWaits; //Represents the waittime for each venue, mod its wait per patron.
    private Vector<Integer> currentVenueWaitSizes; //Represents the waitlist size for each venue
    private Vector<String> currentVenueNames;
    private Vector<String> currentVenueTypes;
    private Timer timer;

    /**
     * Constructor
     */
    public Server(){

        pnConfiguration = new PNConfiguration();
        pnConfiguration.setPublishKey(pubKey);
        pnConfiguration.setSubscribeKey(subKey);
        pubnub = new PubNub(pnConfiguration);
        delegator = new MessageDelegator();
        pubnub.addListener(delegator);

        users = new HashMap<>();
        dbManager = new DBManager();
        currentVenueWaits = new Vector<>();
        currentVenueWaitSizes = new Vector<>();
        currentVenueNames = new Vector<>();
        currentVenueTypes = new Vector<>();
        int tmp = dbManager.getVenueCount();
        for(int i = 0; i < tmp; i++) {

            currentVenueWaits.add(0);
            currentVenueWaitSizes.add(0);
            currentVenueNames.add("");
            currentVenueTypes.add("");

        }

        timer = new Timer();

    }

    /**
     * Registers the message handlers and subscribes to the pubnub channel.
     */
    public void start(){

        delegator.addHandler("login", new ServerLoginHandler(this));
        delegator.addHandler("addWaitlist", new AddWaitlistHandler(this));
        delegator.addHandler("getWaitlistData", new GetWaitlistDataHandler(this));
        delegator.addHandler("getManagerData", new GetManagerDataHandler(this));
        delegator.addHandler("updateWaitPerPatron", new UpdateWaitPerPatronHandler(this));
        delegator.addHandler("deletePatronFromWaitlist", new DeletePatronHandler(this));
        pubnub.subscribe().channels(Arrays.asList("main")).withPresence().execute();
        startClock();

    }

    /**
     * Sends a message every 60 seconds to all clients with an updated waitlist time for each venue.
     */
    private void startClock(){

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                updateCurrentVenueData();
                decrementVenueWaittimes();
                sendUpdateWaitlistData();

            }
        }, 1000, 60000); //Runs every minute

    }

    /**
     * Updates the data structure with the current wait time for each venue.
     */
    public void updateCurrentVenueData(){

        currentVenueNames = dbManager.getAllVenueNames();
        currentVenueTypes = dbManager.getAllVenueTypes();
        Vector<Integer> waitlistSizes = dbManager.getAllWaitlistSizes();
        Vector<Integer> waitPerPatrons = dbManager.getAllWaitPerPatrons();
        if(waitlistSizes == null || waitPerPatrons == null)
            return;

        for(int i = 0; i < waitlistSizes.size(); i++){

            currentVenueWaitSizes.setElementAt(waitlistSizes.get(i), i);

            if(currentVenueWaits.get(i) % waitPerPatrons.get(i) == 0)
                currentVenueWaits.setElementAt((currentVenueWaits.get(i) % waitPerPatrons.get(i))+
                        waitlistSizes.get(i) *  waitPerPatrons.get(i), i);
            else
                currentVenueWaits.setElementAt((currentVenueWaits.get(i) % waitPerPatrons.get(i)) +
                        ((waitlistSizes.get(i) - 1) * waitPerPatrons.get(i)), i);

        }

    }

    /**
     * Decrements each waittime value in the data structure by 1, representing a minute passing on the clock.
     * If a waittime hits it's wait per patron value,(example: waitperpatron = 10, clock hits 20, 10, etc), the
     * person on the top of that venue's waitlist is removed and "served", updating the waitlist.
     */
    private void decrementVenueWaittimes(){

        Vector<Integer> waitlistSizes = dbManager.getAllWaitlistSizes();
        Vector<Integer> waitPerPatrons = dbManager.getAllWaitPerPatrons();

        if(waitlistSizes == null || waitPerPatrons == null)
            return;

        for(int i = 0; i < currentVenueWaits.size(); i++){

            if(waitlistSizes.get(i) != 0){

                if(currentVenueWaits.get(i) != 0)
                    currentVenueWaits.setElementAt(currentVenueWaits.get(i) - 1 ,i);

                if(currentVenueWaits.get(i) % waitPerPatrons.get(i) == 0)
                    serveVenuePatron(i);

            }

        }

    }

    /**
     * Sends a message to all clients with updated data for their venues
     * @param
     */
    public void sendUpdateWaitlistData(){

        JsonObject msg = new JsonObject();
        msg.addProperty("type", "updateAllData");

        JsonObject data = new JsonObject();
        data.add("waitTimes", getJsonArrayFromIntegerVector(getCurrentVenueWaits()));
        data.add("waitSizes", getJsonArrayFromIntegerVector(getCurrentVenueWaitSizes()));
        data.add("venueNames", getJsonArrayFromStringVector(getCurrentVenueNames()));
        data.add("venueTypes", getJsonArrayFromStringVector(getCurrentVenueTypes()));
        msg.add("data", data);

        try {
            pubnub.publish()
                    .channel("main")
                    .message(msg)
                    .sync();
        } catch (PubNubException e) {
            e.printStackTrace();
        }

    }

    /**
     * Removes the patron on the top of the waitlist for the given venueID and sends them an email.
     * @param venueID
     */
    private void serveVenuePatron(int venueID){
        System.out.println("REMOVE FROM WAITLIST");
        //Send email to this person, THEN execute the statement below
        dbManager.servePatron(venueID);
        currentVenueWaitSizes.setElementAt(currentVenueWaitSizes.get(venueID) - 1, venueID);
        sendUpdateWaitlist(venueID);


    }

    /**
     * Changes data structure format from a Integer vector to a JsonArray
     * @param vector
     * @return
     */
    public JsonArray getJsonArrayFromIntegerVector(Vector<Integer> vector){

        JsonArray array = new JsonArray();
        for(int i = 0; i < vector.size(); i++)
            array.add(vector.get(i));

        return array;

    }

    public JsonArray getJsonArrayFromStringVector(Vector<String> vector){

        JsonArray array = new JsonArray();
        for(int i = 0; i < vector.size(); i++)
            array.add(vector.get(i));

        return array;

    }

    /**
     * Updates all venue clients that are viewing the waitlist, when it is updated.
     * @param venueID
     */
    public void sendUpdateWaitlist(int venueID){

        JsonObject msg = new JsonObject();
        msg.addProperty("type", "updateWaitlist");

        JsonObject data = new JsonObject();
        Vector<Patron> tmp = DBManager.getWaitlistFromVenueID(venueID);
        Vector<String> names = new Vector<>();
        Vector<String> emails = new Vector<>();
        for(int i = 0; i < tmp.size(); i++){

            names.add(tmp.get(i).getName());
            emails.add(tmp.get(i).getEmail());

        }

        data.addProperty("venueID", venueID);
        data.addProperty("waittime", getCurrentVenueWaits().get(venueID));
        data.add("names", getJsonArrayFromStringVector(names));
        data.add("emails", getJsonArrayFromStringVector(emails));
        msg.add("data", data);

        try {
            pubnub.publish()
                    .channel("main")
                    .message(msg)
                    .sync();
        } catch (PubNubException e) {
            e.printStackTrace();
        }

    }

    /**
     * Sends all needed data to update the manager page for a certain venue.
     * @param data
     */
    public void updateManagerPage(JsonObject data){

        int venueID = data.get("venueID").getAsInt();
        Vector<String> nameAndWaitPerPatron = DBManager.getVenueNameAndWaitPerPatron(venueID);
        Vector<Patron> waitlistPatrons = DBManager.getWaitlistFromVenueID(venueID);

        if(nameAndWaitPerPatron == null || waitlistPatrons == null)
            return;

        JsonObject msg = new JsonObject();
        msg.addProperty("type", "managerViewData");
        JsonObject data1 = new JsonObject();
        data1.addProperty("name", nameAndWaitPerPatron.get(0));
        data1.addProperty("waitPerPatron", nameAndWaitPerPatron.get(1));

        JsonArray names = new JsonArray();
        JsonArray emails = new JsonArray();
        for(int i = 0; i < waitlistPatrons.size(); i++){

            names.add(waitlistPatrons.get(i).getName());
            emails.add(waitlistPatrons.get(i).getEmail());

        }

        data1.add("waitlistNames", names);
        data1.add("waitlistEmails", emails);
        msg.add("data", data1);

        try {
            pubnub.publish()
                    .channel("main")
                    .message(msg)
                    .sync();
        } catch (PubNubException e) {
            e.printStackTrace();
        }

    }

    public Vector<Integer> getCurrentVenueWaits(){return currentVenueWaits;}
    public Vector<Integer> getCurrentVenueWaitSizes(){return currentVenueWaitSizes;}
    public Vector<String> getCurrentVenueNames(){return currentVenueNames;}
    public Vector<String> getCurrentVenueTypes(){return currentVenueTypes;}

    /**
     * Adds a currently running client's uuid to send specific messages to them.
     * @param uuid
     * @param username
     */
    public void addUser(String uuid, String username){users.put(uuid, username);}

    /**
     * Adds the user to the correct waitlist.
     * @param venueID
     * @param patron
     * @return
     */
    public boolean addToWaitlist(int venueID, Patron patron){return dbManager.addToWaitlist(venueID, patron);}

    /**
     * Removes a client's uuid when they close the application.
     * @param uuid
     */
    public void removeUser(String uuid){users.remove(uuid);}

}
