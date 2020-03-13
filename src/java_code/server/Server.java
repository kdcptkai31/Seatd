package java_code.server;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import java_code.server.handlers.ConnectHandler;
import java_code.server.handlers.ServerLoginHandler;

import java.util.Arrays;
import java.util.HashMap;

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

    public Server(){

        users = new HashMap<>();
        pnConfiguration = new PNConfiguration();
        pnConfiguration.setPublishKey(pubKey);
        pnConfiguration.setSubscribeKey(subKey);
        pubnub = new PubNub(pnConfiguration);
        delegator = new MessageDelegator();
        pubnub.addListener(delegator);

    }

    public void start(){

        delegator.addHandler("connect", new ConnectHandler(this));
        delegator.addHandler("login", new ServerLoginHandler(this));
        pubnub.subscribe().channels(Arrays.asList("main")).withPresence().execute();

    }

    public void addUser(String uuid, String username){
        users.put(uuid, username);
    }
    public void removeUser(String uuid){
        users.remove(uuid);
    }
    public String findUser(String uuid){
        return users.get(uuid);
    }

}
