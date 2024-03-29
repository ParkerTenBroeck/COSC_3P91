package traffic_sim.networking;

import traffic_sim.Simulation;
import traffic_sim.map.Road;
import traffic_sim.map.RoadMap;
import traffic_sim.vehicle.Vehicle;
import traffic_sim.vehicle.controller.Controller;

import javax.swing.*;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

public class NetworkClientSystem extends Simulation.SimSystem {


    private final HashMap<Integer, Vehicle> vehicleIdMap = new HashMap<>();
    private final HashMap<Integer, Road> roadIdMap = new HashMap<>();

    private final Controller playerController;

    private Socket server;
    public NetworkClientSystem(Controller controller) {
        super(10);
        this.playerController = controller;
    }

    @Override
    public void init(Simulation sim) {
        String message = "Enter IP/URL";
        while(true){
            String path = JOptionPane.showInputDialog(message, "localhost");
            try{
                server = new Socket(InetAddress.getByName(path), 42069);
                break;
            }catch (Exception ignore){}
            message = "Invalid, Enter IP/URL";
        }

        try{
            var in = new Reader(server.getInputStream());
            var kind = in.readByte();
            if(kind != 1){
                throw new RuntimeException("nbruh");
            }

            var myid = in.readInt();

            var oin = new ObjectInputStream(in);
            sim.setMap((RoadMap)oin.readObject());
            var vehicles = in.readInt();
            oin = new ObjectInputStream(in);
            for(int i = 0; i < vehicles; i ++){
                var vid = in.readInt();
                this.vehicleIdMap.put(vid, (Vehicle) oin.readObject());
            }

            var roads = in.readInt();
            for(int i = 0; i < roads; i ++){
                var rid = in.readInt();
                String id = in.readString();
//                System.out.println(id);
                roadIdMap.put(rid, sim.getMap().getRoadById(id));
            }

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run(Simulation sim, float delta) {
        if(this.server.isInputShutdown()) throw new RuntimeException();
        try{
            var in = new Reader(this.server.getInputStream());
            for(int bruh = 0; bruh < 3; bruh ++){
                switch(in.readByte()){
                    case 2 -> {
                        sim.setTick(in.readInt());
                        var ignore = in.readFloat();
                        sim.setSimNanos(in.readLong());

                    }
                    case 3 -> {
                        var vehicles = in.readInt();
                        var oin = new ObjectInputStream(in);
                        for(int i = 0; i < vehicles; i ++){
                            var vid = in.readInt();
                            if(!this.vehicleIdMap.containsKey(vid))
                                this.vehicleIdMap.put(vid, (Vehicle) oin.readObject());
                        }
                    }
                    case 4 -> {

                        var roads = in.readInt();
                        for(int r = 0; r < roads; r ++){
                            var rid = in.readInt();
                            var road = this.roadIdMap.get(rid);
                            var lanes = in.readInt();
                            for(int l = 0; l < lanes; l ++){
                                var lane = road.getLane(l);
                                var stuff = lane.getVehicles();
                                stuff.clear();
                                var vehicles = in.readInt();
                                for(int v = 0; v < vehicles; v ++){
                                    var vid = in.readInt();
                                    var distance = in.readFloat();
                                    var vehicle = this.vehicleIdMap.get(vid);
                                    if(vehicle != null){
                                        vehicle.setDistanceAlongRoad(distance);
                                        stuff.add(vehicle);
                                    }
                                }
                            }
                        }
                    }
                    default -> throw new RuntimeException("Invalid kind");
                }
            }
        }catch (Exception e){throw new RuntimeException(e);}
    }
}
