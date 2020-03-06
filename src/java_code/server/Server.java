package java_code.server;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import java_code.server.handlers.ServerLoginHandler;

import java.util.Arrays;
import java.util.HashMap;

/**
 * The running server. Will handle the delegation of all incoming messages by sending them to the correct handler, where
 * the logic of that message will be executed.
 */
public class Server {

    private String pubKey = System.getenv("PUBNUB_PUBLISH");
    private String subKey = System.getenv("PUBNUB_SUBSCRIBE");
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
