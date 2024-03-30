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
    private Vehicle player;

    BufferedWriter writer = new BufferedWriter(512);


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

            player = this.vehicleIdMap.get(myid);

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


            Road.Lane putInLane = null;
            int rightVehicleBackIndex = 0;
            int leftVehicleBackIndex = 0;
            int currentIndex = 0;
            Road.Lane currentLane = null;

            boolean requestLaneChange = false;

            for(int bruh = 0; bruh < 3; bruh ++){
                switch(in.readByte()){
                    case 2 -> {
                        sim.setTick(in.readInt());
                        var ignore = in.readFloat();

                        rightVehicleBackIndex = in.readInt();
                        leftVehicleBackIndex = in.readInt();
                        currentIndex = in.readInt();
                        {
                            var rid = in.readInt();
                            var lane = in.readInt();
                            var road = this.roadIdMap.get(rid);
                            if(road != null){
                                putInLane = road.getLane(lane);
                            }
                        }
                        {
                            var rid = in.readInt();
                            var lane = in.readInt();
                            var road = this.roadIdMap.get(rid);
                            if(road != null){
                                currentLane = road.getLane(lane);
                            }
                        }
//                        requestLaneChange = in.readByte() == 1;

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


            if(putInLane != null)
                playerController.putInLane(player, putInLane);

            playerController.chooseTurn(player, sim, null, null);
            var decision = playerController.laneChange(player, sim, currentLane, currentIndex, leftVehicleBackIndex, rightVehicleBackIndex);


            writer.clear();
            writer.writeFloat(player.getSpeedMultiplier());
            writer.writeInt(-1);
            switch(decision){
                case ForceLeft -> writer.writeByte((byte) -3);
                case NudgeLeft -> writer.writeByte((byte) -2);
                case WaitLeft -> writer.writeByte((byte) -1);
                case Nothing -> writer.writeByte((byte) 0);
                case WaitRight -> writer.writeByte((byte) 1);
                case NudgeRight -> writer.writeByte((byte) 2);
                case ForceRight -> writer.writeByte((byte) 3);
            }

            this.server.getOutputStream().write(writer.getAllData(), 0, writer.getSize());

        }catch (Exception e){throw new RuntimeException(e);}
    }
}
