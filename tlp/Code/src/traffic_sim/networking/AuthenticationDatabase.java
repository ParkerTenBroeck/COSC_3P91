package traffic_sim.networking;

import java.util.HashMap;

/**
 * A class for handling authentication of clients and storing user accounts
 */
public class AuthenticationDatabase {

    /**
     * @param username  The username of the connecting client
     * @param password  The password of the connecting client
     * @return  true if the password and username are correct. false otherwise
     */
    public boolean authenticateReceiveFromClient(String username, String password) {
        return password.equals(users.get(username));
    }


    private final HashMap<String, String> users = new HashMap<>();

    public AuthenticationDatabase(){
        users.put("Parker", "bruh");
        users.put("TA", "supersecret");
        users.put("James", "you_are_my_only_sunshine");
        users.put("Brett", "femboys:3");
        users.put("Robson", "nice");
    }
}
