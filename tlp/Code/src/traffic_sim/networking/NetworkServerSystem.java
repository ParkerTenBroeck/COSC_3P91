package traffic_sim.networking;

import traffic_sim.Simulation;
import traffic_sim.map.Road;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.map.intersection.SourceIntersection;
import traffic_sim.vehicle.Vehicle;
import traffic_sim.vehicle.controller.Controller;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class NetworkServerSystem extends Simulation.SimSystem {

    private SourceIntersection source;
    private final ArrayList<Client> clients = new ArrayList<>();
    private final ArrayList<Client> newClients = new ArrayList<>();



    private int nextRoadId = 0;
    private final HashMap<Road, Integer> roadIdMap = new HashMap<>();
    private final ArrayList<Road> newRoads = new ArrayList<>();

    private int nextIntersectionId = 0;
    private final HashMap<Intersection, Integer> intersectionIdMap = new HashMap<>();
    private final ArrayList<Intersection> newIntersections = new ArrayList<>();

    private int nextVehicleId = 0;
    private final HashMap<Vehicle, Integer> vehicleIdMap = new HashMap<>();
    private final ArrayList<Vehicle> newVehicles = new ArrayList<>();


    private final BufferedWriter vehicleBuf = new BufferedWriter();
    private final BufferedWriter deltaBuf = new BufferedWriter();
    private final BufferedWriter initBuf = new BufferedWriter(1<<20);

    private java.net.ServerSocket socketServer;

    private final AuthenticationDatabase auth = new AuthenticationDatabase();

    /**
     * Waits till the client sends a valid login attempt before adding it to the newClients list
     *
     * @param socket    The newly connected client socket
     */
    private void authLoginClient(Socket socket){
        new Thread(() -> {
            try{
                socket.setTcpNoDelay(true);
                var reader = new Reader(socket.getInputStream());
                var username = reader.readString();
                var password = reader.readString();
                while(!auth.authenticateReceiveFromClient(username, password)){
                    socket.getOutputStream().write(0);
                    socket.getOutputStream().flush();
                    username = reader.readString();
                    password = reader.readString();
                }
                socket.getOutputStream().write(69);
                socket.getOutputStream().flush();

                var player = source.getRandom();
                var client = new Client( player, socket );
                synchronized (newClients){
                    newClients.add(client);
                }
            }catch (Exception ignore){}
        }).start();
    }

    public NetworkServerSystem() {
        super(200);
        this.roadIdMap.put(null, -1);
        new Thread(() -> {
            try {
                socketServer = new ServerSocket(42069);
                while(true){
                    authLoginClient(socketServer.accept());
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

    /**
     * Clear the contents of all buffers
     */
    private void clear(){
        this.vehicleBuf.clear();
        this.deltaBuf.clear();
        this.newRoads.clear();
        this.newVehicles.clear();
    }


    /**
     * @param i The intersection whos ID we want
     * @return  A unique ID that represents this intersection
     */
    private int getIntersectionId(Intersection i){
        var id = this.intersectionIdMap.get(i);
        if (id == null){
            this.newIntersections.add(i);
            this.intersectionIdMap.put(i, this.nextIntersectionId);
            id = this.nextIntersectionId;
            this.nextIntersectionId += 1;
        }
        return id;
    }

    /**
     * @param r The road whos ID we want
     * @return  A unique ID that represents this road
     */
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

    /**
     * @param v The vehicle whos ID we want
     * @return  A unique ID that represents this vehicle
     */
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

    /**
     * Initializes the provided client, sends map data, vehicle data, ID maps and attaches
     * the client to a vehicle that is queued to be spawned.
     *
     * @param sim   The current simulation
     * @param client    The client we want to initialize
     * @throws IOException
     */
    private void initializeClient(Simulation sim, Client client) throws IOException {
        initBuf.clear();

        // send player vehicle data.
        initBuf.writeInt(this.getVehicleId(client.player));
        ObjectOutputStream oos = new ObjectOutputStream(initBuf);
        oos.writeObject(client.player);

        // write the map
        oos.writeObject(sim.getMap());
        oos.flush();

        // write vehicle id map.
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

        // write road ID map
        this.initBuf.writeInt(sim.getMap().getRoads().size());
        for(var road : sim.getMap().getRoads()){
            this.initBuf.writeInt(this.getRoadId(road));
            this.initBuf.writeString(sim.getMap().getRoadId(road));
        }

        // write intersection id map
        this.initBuf.writeInt(sim.getMap().getIntersections().size());
        for(var intersection : sim.getMap().getIntersections()){
            this.initBuf.writeInt(this.getIntersectionId(intersection));
            this.initBuf.writeString(sim.getMap().getIntersectionId(intersection));
        }

        client.socket.getOutputStream().write(initBuf.getAllData(), 0, initBuf.getSize());
        client.socket.getOutputStream().flush();

        source.toAdd(client.player);
        clients.add(client);
    }

    /**
     * Check for new connections and initialize them
     *
     * @param sim the current simulation
     */
    private void checkNew(Simulation sim){
        synchronized (newClients){
            for(var client : newClients){
                try {
                    this.initializeClient(sim, client);
                } catch (Exception ignore){}
            }
            newClients.clear();
        }
    }

    @Override
    public void run(Simulation sim, float delta) {
        this.clear();

        this.checkNew(sim);

        // write the vehicle position data to a buffer
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

        // write new vehicle data to the buffer
        try{
            this.deltaBuf.writeInt(this.newVehicles.size());
            var out = new ObjectOutputStream(this.deltaBuf);
            for(var newVehicle : this.newVehicles){
                deltaBuf.writeInt(getVehicleId(newVehicle));
                out.writeObject(newVehicle);
            }
            out.flush();
        }catch (IOException e){throw new RuntimeException(e);}


        clients.removeIf((client) -> client.update(sim, this));
    }

    /**
     * A client who as connected to this server. Can remotely control a vehicle from the network.
     */
    public static class Client  implements Controller{

        private final Socket socket;

        BufferedWriter writer = new BufferedWriter(512);

        private int chosenTurn = -1;
        private Road.LaneChangeDecision laneChangeDecision = Road.LaneChangeDecision.Nothing;
        private float speed = 1.0f;

        Road.Lane putInLane = null;
        private int rightVehicleBackIndex;
        private int leftVehicleBackIndex;
        private int currentIndex;
        private Road.Lane currentLane = null;

        private Road.Lane turnLane;
        private Intersection turnIntersection;

        private final Vehicle player;


        public Client(Vehicle player, Socket socket){
            this.player = player;
            this.player.setController(this);
            this.socket = socket;
        }

        /** Write the simulation data part of the packet.
         *
         * @param sim   The current simulation
         * @throws IOException
         */
        private void writeSimData(Simulation sim) throws IOException{
            writer.writeInt(sim.getSimTick());
            writer.writeFloat(sim.getFrameDelta());
            writer.writeLong(sim.getSimNanos());
        }

        /**
         * Writes lane data to the writer
         *
         * @param writer    Where we want to write the data to.
         * @param system    The network system this client is apart of
         * @param lane      The lane we want to write
         */
        private void writeLaneData(BufferedWriter writer, NetworkServerSystem system, Road.Lane lane){
            if(lane != null){
                writer.writeInt(system.roadIdMap.get(lane.road()));
                writer.writeInt(lane.getLane());
            }else{
                writer.writeInt(-1);
                writer.writeInt(-1);
            }
        }

        /**
         * Writes lane change information.
         *
         * @param system    The network system this client is apart of
         */
        private void writeLaneChangeData(NetworkServerSystem system) {
            writer.writeInt(rightVehicleBackIndex);
            writer.writeInt(leftVehicleBackIndex);
            writer.writeInt(currentIndex);
            writeLaneData(writer, system, currentLane);
            currentLane = null;
        }

        /**
         * Writes turning data/information.
         *
         * @param system    The network system this client is apart of.
         */
        private void writeTurnData(NetworkServerSystem system){
            // turn data
            if(turnLane != null){
                writer.writeInt(system.roadIdMap.get(turnLane.road()));
                writer.writeInt(turnLane.getLane());
                writer.writeInt(system.intersectionIdMap.get(turnIntersection));
                turnLane = null;
                turnIntersection = null;
            }else{
                writer.writeInt(-1);
                writer.writeInt(-1);
                writer.writeInt(-1);
            }
        }

        /**
         * Writes the player data
         *
         * @param system    The network system this client is apart of.
         */
        private void writePlayerData(NetworkServerSystem system) {
            writer.writeFloat(player.getHealth());
            writer.writeFloat(player.getActualSpeed());
            writer.writeFloat(player.getReputation());

            this.writeLaneChangeData(system);

            writeLaneData(writer, system, putInLane);
            putInLane = null;

            writeTurnData(system);
        }


        /**
         * Reads the client response
         *
         * @param in    Where we want to read the client response from
         * @throws IOException  If the in reader throws an IOException
         */
        private void readClientResponse(Reader in) throws IOException {
            speed = in.readFloat();
            if(Float.isNaN(speed) | Float.isInfinite(speed)) speed = 0;
            chosenTurn = in.readInt();
            switch(in.readByte()){
                case -3 -> laneChangeDecision = Road.LaneChangeDecision.ForceLeft;
                case -2 -> laneChangeDecision = Road.LaneChangeDecision.NudgeLeft;
                case -1 -> laneChangeDecision = Road.LaneChangeDecision.WaitLeft;
                default -> laneChangeDecision = Road.LaneChangeDecision.Nothing;
                case 1 -> laneChangeDecision = Road.LaneChangeDecision.WaitRight;
                case 2 -> laneChangeDecision = Road.LaneChangeDecision.NudgeRight;
                case 3 -> laneChangeDecision = Road.LaneChangeDecision.ForceRight;
            }
        }

        /**
         * Run this every simulation system tick. Receives and sends the update packets
         *
         * @param sim   The simulation this client is apart of
         * @param system    The network system this client is apart of
         * @return  True if this client should be removed from the list of active clients.
         */
        public boolean update(Simulation sim, NetworkServerSystem system){
            if(socket.isClosed()) return true;
            try{
                var in = new Reader(socket.getInputStream());
                var out = socket.getOutputStream();

                this.writeSimData(sim);
                this.writePlayerData(system);

                out.write(writer.getAllData(), 0, writer.getSize());
                writer.clear();

                // write delta data
                out.write(system.deltaBuf.getAllData(), 0, system.deltaBuf.getSize());

                // write vehicle data
                out.write(system.vehicleBuf.getAllData(), 0, system.vehicleBuf.getSize());
                out.flush();

                this.readClientResponse(in);
            }catch (Exception e){
                try {
                    this.socket.close();
                } catch (IOException ignore) {}
                return true;
            }
            return false;
        }


        @Override
        public void tick(Vehicle v, Simulation sim, Road.Lane lane, int laneIndex, boolean changedLanes, float delta) {
            if(socket.isClosed()) lane.removeVehicle(laneIndex);
            v.setSpeedMultiplier(this.speed);
        }

        @Override
        public Intersection.Turn chooseTurn(Vehicle v, Simulation sim, Road.Lane current_lane, Intersection intersection, ArrayList<Intersection.Turn> turns) {
//            turn
            if(this.chosenTurn == -1){

                this.turnIntersection = intersection;
                this.turnLane = current_lane;
                return null;
            }else{
                var tmp = this.chosenTurn;
                this.chosenTurn = -1;
                try{
                    return turns.get(tmp);
                }catch (Exception e){
                    this.turnIntersection = intersection;
                    this.turnLane = current_lane;
                    return null;
                }
            }
        }

        @Override
        public Road.LaneChangeDecision laneChange(Vehicle v, Simulation sim, Road.Lane lane, int current_index, int left_vehicle_back_index, int right_vehicle_back_index) {
            this.currentIndex = current_index;
            this.leftVehicleBackIndex = left_vehicle_back_index;
            this.rightVehicleBackIndex = right_vehicle_back_index;
            this.currentLane = lane;
            var tmp = laneChangeDecision;
            laneChangeDecision = null;
            return tmp;
        }

        @Override
        public void putInLane(Vehicle vehicle, Road.Lane lane) {
            this.putInLane = lane;
        }
    }
}
