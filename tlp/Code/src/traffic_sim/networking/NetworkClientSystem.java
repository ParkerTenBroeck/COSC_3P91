package traffic_sim.networking;

import traffic_sim.Simulation;
import traffic_sim.map.Road;
import traffic_sim.map.RoadMap;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.vehicle.Vehicle;
import traffic_sim.vehicle.controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class NetworkClientSystem extends Simulation.SimSystem {


    private final HashMap<Integer, Vehicle> vehicleIdMap = new HashMap<>();
    private final HashMap<Integer, Road> roadIdMap = new HashMap<>();
    private final HashMap<Integer, Intersection> intersectionIdMap = new HashMap<>();

    private final Controller playerController;
    private Vehicle player;

    BufferedWriter writer = new BufferedWriter(512);


    private Socket server;
    public NetworkClientSystem(Controller controller) {
        super(10);
        this.playerController = controller;
    }


    private String[] authThing(String title){
        var logininformation = new String[2];

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel(title), BorderLayout.NORTH);
        JPanel label = new JPanel(new GridLayout(0, 1, 2, 2));
        label.add(new JLabel("Username", SwingConstants.RIGHT));
        label.add(new JLabel("Password", SwingConstants.RIGHT));
        panel.add(label, BorderLayout.WEST);

        JPanel controls = new JPanel(new GridLayout(0, 1, 2, 2));
        JTextField username = new JTextField();
        controls.add(username);
        JPasswordField password = new JPasswordField();
        controls.add(password);
        panel.add(controls, BorderLayout.CENTER);

        var result = JOptionPane.showConfirmDialog(null, panel, "login", JOptionPane.OK_CANCEL_OPTION);
        if(result == 2) System.exit(1);
        
        logininformation[0] = username.getText();
        logininformation[1] = new String(password.getPassword());
        return logininformation;
}

    @Override
    public void init(Simulation sim) {

        String message = "Enter IP/URL";
        outer:
        while(true){
            String path;
            if(sim.getView() == null){
                System.out.print(message + ": ");
                try{
                    path = new Scanner(System.in).nextLine().trim();
                }catch (Exception ignore){ path = "";}
            }else{
                path = JOptionPane.showInputDialog(message, "localhost");
                if(path == null) System.exit(1);
            }
            try{
                server = new Socket(InetAddress.getByName(path), 42069);
                this.server.setTcpNoDelay(true);
                try{
                    var authMessage = "Enter Username/Password";
                    while(true){
                        String username;
                        String password;
                        if(sim.getView() == null){
                            System.out.println(authMessage);
                            try{
                                System.out.print("Username: ");
                                username = new Scanner(System.in).nextLine().trim();
                                System.out.print("Password: ");
                                password = new Scanner(System.in).nextLine().trim();
                            }catch (Exception e) {throw new RuntimeException(e);}
                        }else{
                            var results = authThing(authMessage);
                            username = results[0];
                            password = results[1];
                        }
                        if(Authentication.authenticateClient(username, password, server)){
                            break outer;
                        }else{
                            authMessage = "Incorrect Attempt: Enter Username/Password";
                        }
                    }
                }catch (Exception e){
                    message = "Failed to Authenticate: Enter IP/URL";
                }
            }catch (Exception ignore){
                message = "Failed to Connect, Enter IP/URL";
            }
        }

        try{
            var in = new Reader(server.getInputStream());
            var kind = in.readByte();
            if(kind != 1){
                throw new RuntimeException("nbruh");
            }

            var myid = in.readInt();

            var oin = new ObjectInputStream(in);
            player = (Vehicle)oin.readObject();
            if(sim.getView() != null) sim.getView().setFollowing(player);
            this.vehicleIdMap.put(myid, player);
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
                roadIdMap.put(rid, sim.getMap().getRoadById(id));
            }

            var intersections = in.readInt();
            for(int i = 0; i < intersections; i ++){
                var iid = in.readInt();
                String id = in.readString();
                intersectionIdMap.put(iid, sim.getMap().getIntersectionById(id));
            }

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run(Simulation sim, float delta) {
        if(this.server.isInputShutdown()) throw new RuntimeException();
        try{
            // make client fps independent of server updates
//            if(this.server.getInputStream().available() > 0) {
                var in = new Reader(this.server.getInputStream());


                Road.Lane putInLane = null;
                int rightVehicleBackIndex = -1;
                int leftVehicleBackIndex = -1;
                int currentIndex = -1;
                Road.Lane currentLane = null;
                int intersectionId = -1;
                int turnsRid = -1;
                int turnsLane = -1;


                for (int bruh = 0; bruh < 3; bruh++) {
                    switch (in.readByte()) {
                        case 2 -> {
                            sim.setTick(in.readInt());
                            delta = in.readFloat();
                            sim.setSimNanos(in.readLong());

                            player.setHealth(in.readFloat());
                            player.setActualSpeed(in.readFloat());
                            player.setReputation(in.readFloat());

                            rightVehicleBackIndex = in.readInt();
                            leftVehicleBackIndex = in.readInt();
                            currentIndex = in.readInt();

                            {
                                var rid = in.readInt();
                                var lane = in.readInt();
                                var road = this.roadIdMap.get(rid);
                                if (road != null) {
                                    currentLane = road.getLane(lane);
                                }
                            }

                            {
                                var rid = in.readInt();
                                var lane = in.readInt();
                                var road = this.roadIdMap.get(rid);
                                if (road != null) {
                                    putInLane = road.getLane(lane);
                                }
                            }

                            turnsRid = in.readInt();
                            turnsLane = in.readInt();
                            intersectionId = in.readInt();

                        }
                        case 3 -> {
                            var vehicles = in.readInt();
                            var oin = new ObjectInputStream(in);
                            for (int i = 0; i < vehicles; i++) {
                                var vid = in.readInt();
                                if (!this.vehicleIdMap.containsKey(vid))
                                    this.vehicleIdMap.put(vid, (Vehicle) oin.readObject());
                                else {
                                    oin.readObject();
                                }

                            }
                        }
                        case 4 -> {
                            var roads = in.readInt();
                            for (int r = 0; r < roads; r++) {
                                var rid = in.readInt();
                                var road = this.roadIdMap.get(rid);
                                var lanes = in.readInt();
                                for (int l = 0; l < lanes; l++) {
                                    var lane = road.getLane(l);
                                    lane.empty();
                                    var vehicles = in.readInt();
                                    for (int v = 0; v < vehicles; v++) {
                                        var vid = in.readInt();
                                        var distance = in.readFloat();
                                        var vehicle = this.vehicleIdMap.get(vid);
                                        if (vehicle != null) {
                                            vehicle.setDistanceAlongRoad(distance);
//                                        stuff.add(vehicle);
                                            lane.addVehicle(vehicle);
                                        }
                                    }
                                }
                            }
                        }
                        default -> throw new RuntimeException("Invalid kind");
                    }
                }


                if (putInLane != null)
                    playerController.putInLane(player, putInLane);


                var turn = -1;
                if (intersectionId != -1) {
                    var intersection = intersectionIdMap.get(intersectionId);
                    var currentLaneTurn = roadIdMap.get(turnsRid).getLane(turnsLane);
                    var turns = intersection.getTurns(currentLaneTurn);
                    var chosenTurn = playerController.chooseTurn(player, sim, currentLaneTurn, intersection, turns);
                    if (chosenTurn != null) {
                        turn = turns.indexOf(chosenTurn);
                    }
                }


                Road.LaneChangeDecision decision = Road.LaneChangeDecision.Nothing;
                if (currentLane != null) {
                    decision = playerController.laneChange(player, sim, currentLane, currentIndex, leftVehicleBackIndex, rightVehicleBackIndex);
                }
                writer.clear();
                writer.writeFloat(player.getSpeedMultiplier());
                writer.writeInt(turn);
                switch (decision) {
                    case ForceLeft -> writer.writeByte((byte) -3);
                    case NudgeLeft -> writer.writeByte((byte) -2);
                    case WaitLeft -> writer.writeByte((byte) -1);
                    case Nothing -> writer.writeByte((byte) 0);
                    case WaitRight -> writer.writeByte((byte) 1);
                    case NudgeRight -> writer.writeByte((byte) 2);
                    case ForceRight -> writer.writeByte((byte) 3);
                }

                playerController.tick(player, sim, currentLane, currentIndex, false, delta);

                this.server.getOutputStream().write(writer.getAllData(), 0, writer.getSize());
                this.server.getOutputStream().flush();
//            }
        }catch (Exception e){throw new RuntimeException(e);}
    }
}
