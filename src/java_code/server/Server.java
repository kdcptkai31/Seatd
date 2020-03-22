package java_code.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import java_code.database.DBManager;
import java_code.model.Patron;
import java_code.server.handlers.AddWaitlistHandler;
import java_code.server.handlers.ServerLoginHandler;

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
    private Vector<Integer> currentVenueWaits; //Represents the waittime for each venue, mod 15.
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
        //initialize venue wait times
        currentVenueWaits = new Vector<>();
        int tmp = dbManager.getVenueCount();
        for(int i = 0; i < tmp; i++)
            currentVenueWaits.add(0);

        timer = new Timer();

    }

    /**
     * Registers the message handlers and subscribes to the pubnub channel.
     */
    public void start(){

        delegator.addHandler("login", new ServerLoginHandler(this));
        delegator.addHandler("addWaitlist", new AddWaitlistHandler(this));
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

                updateCurrentVenueWaits();
                decrementVenueWaittimes();

                for(int i = 0; i < currentVenueWaits.size(); i++)
                    System.out.println(currentVenueWaits.get(i));
                System.out.println("---------");

                JsonObject msg = new JsonObject();
                msg.addProperty("type", "clockTick");

                JsonObject data = new JsonObject();
                data.add("data", getJSONArrayFromVector(currentVenueWaits));
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
        }, 1000, 60000);


    }

    /**
     * Updates the data structure with the current wait time for each venue.
     */
    public void updateCurrentVenueWaits(){

        Vector<Integer> waitlistSizes = dbManager.getAllWaitlistSizes();
        Vector<Integer> waitPerPatrons = dbManager.getAllWaitPerPatrons();
        if(waitlistSizes == null || waitPerPatrons == null)
            return;

        for(int i = 0; i < waitlistSizes.size(); i++){

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
     * Removes the patron on the top of the waitlist for the given venueID and sends them an email.
     * @param venueID
     */
    private void serveVenuePatron(int venueID){
        System.out.println("REMOVE WAITLIST");
        //Send email to this person, THEN execute the statement below
        dbManager.servePatron(venueID);


    }

    public JsonArray getJSONArrayFromVector(Vector<Integer> vector){

        JsonArray array = new JsonArray();
        for(int i = 0; i < vector.size(); i++)
            array.add(vector.get(i));

        return array;

    }

    public Vector<Integer> getCurrentVenueWaits(){return currentVenueWaits;}

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
