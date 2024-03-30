package traffic_sim.networking;

import traffic_sim.Simulation;
import traffic_sim.map.Road;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.map.intersection.SourceIntersection;
import traffic_sim.vehicle.Car;
import traffic_sim.vehicle.Vehicle;
import traffic_sim.vehicle.controller.Controller;

import java.io.IOException;
import java.io.ObjectOutputStream;
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


    BufferedWriter vehicleBuf = new BufferedWriter();
    BufferedWriter deltaBuf = new BufferedWriter();
    BufferedWriter initBuf = new BufferedWriter(1<<20);

    java.net.ServerSocket socketServer;


    public NetworkServerSystem() {
        super(200);
        this.roadIdMap.put(null, -1);
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


    private int getIntersectionId(Intersection r){
        var id = this.intersectionIdMap.get(r);
        if (id == null){
            this.newIntersections.add(r);
            this.intersectionIdMap.put(r, this.nextIntersectionId);
            id = this.nextIntersectionId;
            this.nextIntersectionId += 1;
        }
        return id;
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
                var vehicle = new Car(client);
                try {
                    initBuf.clear();
                    // kind
                    initBuf.writeByte((byte) 1);
                    initBuf.writeInt(this.getVehicleId(vehicle));
                    ObjectOutputStream oos = new ObjectOutputStream(initBuf);
                    oos.writeObject(vehicle);
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

                    this.initBuf.writeInt(sim.getMap().getIntersections().size());
                    for(var intersection : sim.getMap().getIntersections()){
                        this.initBuf.writeInt(this.getIntersectionId(intersection));
                        this.initBuf.writeString(sim.getMap().getIntersectionId(intersection));
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


        public Client(Socket socket){
            this.socket = socket;
        }

        public boolean update(Simulation sim, NetworkServerSystem system){

            if(socket.isClosed()) return true;
            try{
                var in = new Reader(socket.getInputStream());
                var out = socket.getOutputStream();

                // write sim data
                writer.writeByte((byte) 2);
                writer.writeInt(sim.getSimTick());
                writer.writeFloat(sim.getFrameDelta());
                writer.writeLong(sim.getSimNanos());


                writer.writeInt(rightVehicleBackIndex);
                writer.writeInt(leftVehicleBackIndex);
                writer.writeInt(currentIndex);

                if(currentLane != null){
                    writer.writeInt(system.roadIdMap.get(currentLane.road()));
                    writer.writeInt(currentLane.getLane());
                    currentLane = null;
                }else{
                    writer.writeInt(-1);
                    writer.writeInt(-1);
                }


                if(putInLane != null){
                    writer.writeInt(system.roadIdMap.get(putInLane.road()));
                    writer.writeInt(putInLane.getLane());
                    putInLane = null;
                }else{
                    writer.writeInt(-1);
                    writer.writeInt(-1);
                }



                // intersections
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
//                writer.write

                out.write(writer.getAllData(), 0, writer.getSize());
                writer.clear();


                // write delta data
                writer.writeByte((byte) 3);
                out.write(writer.getAllData(), 0, writer.getSize());
                out.write(system.deltaBuf.getAllData(), 0, system.deltaBuf.getSize());
                writer.clear();

                // write vehicle data
                writer.writeByte((byte) 4);
                out.write(writer.getAllData(), 0, writer.getSize());
                out.write(system.vehicleBuf.getAllData(), 0, system.vehicleBuf.getSize());
                out.flush();
                writer.clear();


                speed = in.readFloat();
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

            }catch (Exception e){
                try {
                    this.socket.close();
                } catch (IOException ignore) {
                }
                return true;
            }
            return false;
        }


        @Override
        public void tick(Vehicle v, Simulation sim, Road.Lane lane, int laneIndex, boolean changedLanes, float delta) {
            v.setSpeedMultiplier(this.speed);
        }

        @Override
        public Intersection.Turn chooseTurn(Vehicle v, Simulation sim, Road.Lane current_lane, Intersection intersection, ArrayList<Intersection.Turn> turns) {
//            turn
            this.turnIntersection = intersection;
            this.turnLane = current_lane;
            if(this.chosenTurn == -1){
                return null;
            }else{
                var tmp = this.chosenTurn;
                this.chosenTurn = -1;
                try{
                    return turns.get(tmp);
                }catch (Exception e){
                    return null;
                }
            }
        }

        @Override
        public Road.LaneChangeDecision laneChange(Vehicle v, Simulation sim, Road.Lane lane, int current_index, int left_vehicle_back_index, int right_vehicle_back_index) {
            this.currentIndex = current_index;
            this.leftVehicleBackIndex = left_vehicle_back_index;
            this.rightVehicleBackIndex = right_vehicle_back_index;
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
