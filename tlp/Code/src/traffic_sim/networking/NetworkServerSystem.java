package traffic_sim.networking;

import traffic_sim.Simulation;
import traffic_sim.map.Road;
import traffic_sim.map.intersection.SourceIntersection;
import traffic_sim.vehicle.Car;
import traffic_sim.vehicle.Vehicle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class NetworkServerSystem extends Simulation.SimSystem {
    private SourceIntersection source;
    private final ArrayList<Client> clients = new ArrayList<>();
    private final ArrayList<Client> newClients = new ArrayList<>();


    private final ArrayList<Road> newRoads = new ArrayList<>();
    private final ArrayList<Vehicle> newVehicles = new ArrayList<>();

    private int nextRoadId;
    private final HashMap<Road, Integer> roadIdMap = new HashMap<>();


    private int nextVehicleId;
    private final HashMap<Vehicle, Integer> vehicleIdMap = new HashMap<>();


    BufferedWriter vehicleBuf = new BufferedWriter();
    BufferedWriter deltaBuf = new BufferedWriter();
    BufferedWriter initBuf = new BufferedWriter(1<<20);

    java.net.ServerSocket socketServer;


    public NetworkServerSystem() {
        super(200);
        new Thread(() -> {
            try {
                socketServer = new ServerSocket(42069);
                while(true){
                    var client = new Client( socketServer.accept());
                    synchronized (newClients){
                        newClients.add(client);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();;
    }

    @Override
    public void init(Simulation sim) {
        this.source = (SourceIntersection) sim.getMap().getIntersectionById("Source");
    }

    private void clear(){
        this.vehicleBuf.clear();
        this.deltaBuf.clear();
        this.newRoads.clear();
        this.newVehicles.clear();
    }



    private int getRoadId(Road r){
        var id = this.roadIdMap.get(r);
        if (id == null){
            this.newRoads.add(r);
            this.roadIdMap.put(r, this.nextRoadId);
            id = this.nextRoadId;
            this.nextRoadId += 1;
        }
        return id;
    }

    private int getVehicleId(Vehicle v){
        var id = this.vehicleIdMap.get(v);
        if (id == null){
            this.newVehicles.add(v);
            this.vehicleIdMap.put(v, this.nextVehicleId);
            id = this.nextVehicleId;
            this.nextVehicleId += 1;
        }
        return id;
    }

    @Override
    public void run(Simulation sim, float delta) {
        this.clear();

        synchronized (newClients){
            for(var client : newClients){
                var source = (SourceIntersection)sim.getMap().getIntersectionById("Source");
                var vehicle = new Car(new NetworkController(client));
                try {
                    initBuf.clear();
                    // kind
                    initBuf.writeByte((byte) 1);
                    initBuf.writeInt(this.getVehicleId(vehicle));
                    ObjectOutputStream oos = new ObjectOutputStream(initBuf);
                    oos.writeObject(sim.getMap());
                    oos.flush();

                    try{
                        var list = new ArrayList<Vehicle>();
                        for (var road : sim.getMap().getRoads()){
                            for(var lane : road.getLanes()){
                                lane.inorder(list::add);
                            }
                        }
                        this.initBuf.writeInt(list.size());
                        oos = new ObjectOutputStream(initBuf);
                        for(var v : list){
                            this.initBuf.writeInt(getVehicleId(v));
                            oos.writeObject(v);
                        }
                        oos.flush();
                    }catch (Exception e){throw new RuntimeException(e);}

                    this.initBuf.writeInt(sim.getMap().getRoads().size());
                    for(var road : sim.getMap().getRoads()){
                        this.initBuf.writeInt(this.getRoadId(road));
                        this.initBuf.writeString(sim.getMap().getRoadId(road));
                    }

                    client.socket.getOutputStream().write(initBuf.getAllData(), 0, initBuf.getSize());

                    source.toAdd(vehicle);
                    clients.add(client);

                } catch (Exception e){
                    throw new RuntimeException(e);
                }

            }
            newClients.clear();
        }

        this.vehicleBuf.writeInt(sim.getMap().getRoads().size());
        for (var road : sim.getMap().getRoads()){
            this.vehicleBuf.writeInt(this.getRoadId(road));
            this.vehicleBuf.writeInt(road.getNumLanes());
            for(var lane : road.getLanes()){
                this.vehicleBuf.writeInt(lane.currentVehicles());
                lane.inorder((v) -> {
                    this.vehicleBuf.writeInt(this.getVehicleId(v));
                    this.vehicleBuf.writeFloat(v.getDistanceAlongRoad());
                });
            }
        }

        try{
            this.deltaBuf.writeInt(this.newVehicles.size());
            var out = new ObjectOutputStream(this.deltaBuf);
            for(var newVehicle : this.newVehicles){
                deltaBuf.writeInt(getVehicleId(newVehicle));
                out.writeObject(newVehicle);
            }
            out.flush();
        }catch (Exception e){throw new RuntimeException(e);}


        clients.removeIf((client) -> client.update(sim, this));
    }

    public static class Client{

        private final Socket socket;

        BufferedWriter writer = new BufferedWriter(512);

        public Client(Socket socket){
            this.socket = socket;
        }

        public boolean update(Simulation sim, NetworkServerSystem system){

            if(socket.isClosed()) return true;
            try{
//                var in = new ObjectInputStream(socket.getInputStream());
                var out = socket.getOutputStream();

                // write sim data
                writer.writeByte((byte) 2);
                writer.writeInt(sim.getSimTick());
                writer.writeFloat(sim.getFrameDelta());
                writer.writeLong(sim.getSimNanos());
                out.write(writer.getAllData(), 0, writer.getSize());
                writer.clear();


                // write vehicle data
                writer.writeByte((byte) 4);
                out.write(writer.getAllData(), 0, writer.getSize());
                out.write(system.vehicleBuf.getAllData(), 0, system.vehicleBuf.getSize());
                out.flush();
                writer.clear();

                // write delta data
                writer.writeByte((byte) 3);
                out.write(writer.getAllData(), 0, writer.getSize());
                out.write(system.deltaBuf.getAllData(), 0, system.deltaBuf.getSize());
                writer.clear();


//                if(in.available() >= 4){
//                    var num = in.readInt();
//                    for(int i = 0; i < num; i ++){
//                        System.out.print(in.readChar() + " ");
//                    }
//                    System.out.println();
//                }

            }catch (Exception e){
                try {
                    this.socket.close();
                } catch (IOException ignore) {
                }
                throw new RuntimeException(e);
//                return true;
            }
            return false;
        }
    }

}
