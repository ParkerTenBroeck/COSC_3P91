package traffic_sim.networking;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

/**
 * A class for handling authentication of clients
 * Has little
 */
public class Authentication {
    public static boolean authenticateClient(String user, String password, Socket client) throws IOException {
        var output = client.getOutputStream();

        var userBytes = user.getBytes();
        output.write((userBytes.length >> 8) & 0xFF);
        output.write(userBytes.length & 0xFF);
        output.write(userBytes);

        var passwordBytes = password.getBytes();
        output.write((passwordBytes.length >> 8) & 0xFF);
        output.write(passwordBytes.length & 0xFF);
        output.write(passwordBytes);

        output.flush();

        return client.getInputStream().read() == 69;
    }

    public boolean authenticateCheckClient(String username, String password) {
        return password.equals(users.get(username));
    }


    private final HashMap<String, String> users = new HashMap<>();

    public Authentication(){
        users.put("Parker", "bruh");
        users.put("TA", "supersecret");
    }
}
