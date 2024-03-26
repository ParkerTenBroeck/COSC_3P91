package traffic_sim;

import traffic_sim.map.intersection.SourceIntersection;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class NetworkSystem extends Simulation.SimSystem {
    private SourceIntersection source;
    private final ArrayList<Client> clients = new ArrayList<>();

    java.net.ServerSocket socket;


    public NetworkSystem() {
        super(200);
        try {
            socket = new ServerSocket(42069);
//            socket.getChannel().configureBlocking(false);

//            socket.accept();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void init(Simulation sim) {
        this.source = (SourceIntersection) sim.getMap().getIntersectionById("Source");
    }

    @Override
    public void run(Simulation sim, float delta) {
        var data = new byte[0];
//        socket.
//        socket.accept()
        clients.removeIf((client) -> client.update(data));
    }

    private static class Client{

        Socket socket;


        public Client(){

        }

        public boolean update(byte[] data){
            if(socket.isClosed()) return true;
            try{

                var in = socket.getInputStream();
                var out = socket.getOutputStream();
                out.write(data);
                
            }catch (Exception e){
                try {
                    this.socket.close();
                } catch (IOException ignore) {
                }
                return true;
            }
            return false;
        }
    }
}
