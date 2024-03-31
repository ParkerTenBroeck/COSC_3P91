package traffic_sim.networking;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

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

        return client.getInputStream().read() == 69;
    }

    public boolean authenticateCheckClient(Socket client) throws IOException{
        var reader = new Reader(client.getInputStream());
        var username = reader.readString();
        var password = reader.readString();
        if (password.equals(users.get(username))){
            client.getOutputStream().write(69);
            return true;
        }else {
            client.getOutputStream().write(0);
            return false;
        }
    }


    private final HashMap<String, String> users = new HashMap<>();

    public Authentication(){
        users.put("Parker", "bruh");
        users.put("TA", "supersecret");
    }
}
