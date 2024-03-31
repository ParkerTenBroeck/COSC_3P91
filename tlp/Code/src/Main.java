import traffic_sim.ConsoleUtils;
import traffic_sim.networking.NetworkClientSystem;
import traffic_sim.networking.NetworkServerSystem;
import traffic_sim.Simulation;
import traffic_sim.excpetions.MapBuildingException;
import traffic_sim.io.Display;
import traffic_sim.io.Input;
import traffic_sim.vehicle.controller.TextPlayerController;
import traffic_sim.io.View;
import traffic_sim.map.xml.MapXmlTools;
import traffic_sim.map.intersection.DrainIntersection;
import traffic_sim.map.RoadMap;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.map.intersection.SourceIntersection;
import traffic_sim.map.intersection.TimedIntersection;
import traffic_sim.vehicle.Car;
import traffic_sim.vehicle.Truck;
import traffic_sim.vehicle.Vehicle;
import traffic_sim.vehicle.controller.GUIPlayerController;

import java.awt.*;
import java.io.*;

/*UML_HIDE*/
public class Main {

    public static void main(String[] args) throws Exception {
//        if(true){
//            ConsoleUtils.enterRawMode();
//            ConsoleUtils.clear();
//            ConsoleUtils.moveCursor(0, 0);
//            while(true){
//                ConsoleUtils.show();
//                ConsoleUtils.moveCursor(0, 0);
//                ConsoleUtils.clear();
//                var read = ConsoleUtils.read();
//                ConsoleUtils.stylePrintln(read+"", ConsoleUtils.BasicBackground.Red, ConsoleUtils.Style.Underline);
//                while(ConsoleUtils.hasNext()){
//                    read = ConsoleUtils.read();
//                    ConsoleUtils.stylePrintln(read+"", ConsoleUtils.BasicBackground.Red, ConsoleUtils.Style.Underline);
//                }
//            }
//        }

        var item = args.length == 1? args[0].trim() : "__EMPTY";
        switch (item) {
            case "b" -> {
                new Thread(() -> {
                    try {
                        runServer();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                runClientTerm();
            }
            case "bg" -> {
                new Thread(() -> {
                    try {
                        runServer();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                runClientGUI();
            }
            case "s" -> runServer();
            case "cg" -> runClientGUI();
            case "c" -> runClientTerm();
            case "l" -> runLocal();
            case "lg" -> runLocalGraphical();
            default -> ConsoleUtils.println("""
                    Please provide a single argument
                    b:  Both Server + Client
                    bg: Both Server + Client Graphical
                    s:  Server
                    c:  Client
                    cg: Client Graphical
                    l:  Local (Not networked)
                    lg: Local Graphical (Not networked)
                    """
            );
        }
    }

    public static void runClientTerm() {
        var simulation = new Simulation();
        simulation.addSystem(new NetworkClientSystem(new TextPlayerController()));
        simulation.attachRawModeTUI();
        simulation.run();
    }

    public static void runClientGUI() {
        var simulation = new Simulation(new View());
        simulation.attachGUIRenderer();
        simulation.getView().panX = 0;
        simulation.getView().panY = 0;
        simulation.getView().zoom = 21;
        simulation.addSystem(new NetworkClientSystem(new GUIPlayerController(simulation.getInput())));
        simulation.addSystem(Simulation.SimSystem.simple(1, (sim, delta) -> {
             if (sim.getInput().keyHeld('D')) {
                 sim.getView().panX -= sim.getFrameDelta() * 300 * 1 / sim.getView().zoom;
             }
             if (sim.getInput().keyHeld('A')) {
                 sim.getView().panX += sim.getFrameDelta() * 300 * 1 / sim.getView().zoom;
             }
             if (sim.getInput().keyHeld('W')) {
                 sim.getView().panY += sim.getFrameDelta() * 300 * 1 / sim.getView().zoom;
             }
             if (sim.getInput().keyHeld('S')) {
                 sim.getView().panY -= sim.getFrameDelta() * 300 * 1 / sim.getView().zoom;
             }
             if (sim.getInput().keyHeld('Q')) {
                 sim.getView().zoom += sim.getFrameDelta() * sim.getView().zoom * 2;
             }
             if (sim.getInput().keyHeld('E')) {
                 sim.getView().zoom -= sim.getFrameDelta() * sim.getView().zoom * 2;
             }
             if (sim.getInput().keyPressed('V')){
                 sim.setDebug(!sim.getDebug());
             }
         }));
        simulation.run();
    }

    public static void runServer() throws Exception{
        var map = MapXmlTools.loadMap(new FileInputStream("res/road_map.xml"));
        var is = (SourceIntersection)map.getIntersectionById("Source");
        for(var road : map.getRoads()){
            for(var lane : road.getLanes()){
                for(int i = 0; i < 3; i ++)
                    lane.addVehicle(is.getRandom());
            }
        }
        var simulation = new Simulation(map);
        simulation.addSystem(new NetworkServerSystem());
        simulation.attachRealTimeSim();
        simulation.run();
    }


    public static void runLocal() throws Exception {

        var map = MapXmlTools.loadMap(new FileInputStream("res/road_map.xml"));

        var is = (SourceIntersection)map.getIntersectionById("Source");
        for(var road : map.getRoads()){
            for(var lane : road.getLanes()){
                for(int i = 0; i < 3; i ++)
                    lane.addVehicle(is.getRandom());
            }
        }

        var displayController = new TextPlayerController();
        var simulation = new Simulation(map);
        simulation.attachConstantTimeSteppedSim();
        simulation.attachRawModeTUI();

        var player = new Car(displayController, Color.MAGENTA);

        player.setSpeedMultiplier(1f);
        is.toAdd(player);
        simulation.run();
    }
    public static void runLocalGraphical() throws Exception{

        var map = MapXmlTools.loadMap(new FileInputStream("res/road_map.xml"));
        var is = (SourceIntersection)map.getIntersectionById("Source");

        for(var road : map.getRoads()){
            for(var lane : road.getLanes()){
                for(int i = 0; i < 3; i ++)
                    lane.addVehicle(is.getRandom());
            }
        }


        var simulation = new Simulation(map, new View());
        var player = new Car(new GUIPlayerController(simulation.getInput()), Color.MAGENTA);

        simulation.attachGUIRenderer();
        simulation.attachRealTimeSim();

        simulation.getView().panX = 0;
        simulation.getView().panY = 0;
        simulation.getView().zoom = 21;

        simulation.addSystem(new Simulation.SimSystem(1){

            Intersection held;
            @Override
            public void init(Simulation sim) {}

            @Override
            public void run(Simulation sim, float delta) {
                if (sim.getInput().keyHeld('D')){
                    sim.getView().panX -= sim.getFrameDelta()*300*1/sim.getView().zoom;
                }
                if (sim.getInput().keyHeld('A')){
                    sim.getView().panX += sim.getFrameDelta()*300*1/sim.getView().zoom;
                }
                if (sim.getInput().keyHeld('W')){
                    sim.getView().panY += sim.getFrameDelta()*300*1/sim.getView().zoom;
                }
                if (sim.getInput().keyHeld('S')){
                    sim.getView().panY -= sim.getFrameDelta()*300*1/sim.getView().zoom;
                }
                if (sim.getInput().keyHeld('Q')){
                    sim.getView().zoom += sim.getFrameDelta()*sim.getView().zoom*2;
                }
                if (sim.getInput().keyHeld('E')){
                    sim.getView().zoom -= sim.getFrameDelta()*sim.getView().zoom*2;
                }
                if (sim.getInput().keyPressed(' ')){
                    sim.setPaused(!sim.getPaused());
                }
                if (sim.getInput().keyPressed('V')){
                    sim.setDebug(!sim.getDebug());
                }
                if (sim.getInput().keyPressed('T')){
                    sim.tick(1f);
                }
                if (sim.getInput().keyPressed('1')){
                    sim.setSimulationMultiplier(1.0f);
                }
                if (sim.getInput().keyHeld('-')){
                    sim.setSimulationMultiplier(sim.getSimulationMultiplier() - 0.01f);
                }
                if (sim.getInput().keyHeld('=')){
                    sim.setSimulationMultiplier(sim.getSimulationMultiplier() + 0.01f);
                }
                if (sim.getInput().keyHeld('=') && sim.getInput().keyHeld(16)){
                    sim.setSimulationMultiplier(sim.getSimulationMultiplier() + 100.0f);
                }
                if(sim.getInput().mousePressed(Input.MouseKey.Left) | sim.getInput().mousePressed(Input.MouseKey.Right)){
                    this.held = null;
                    var x = sim.getView().getMouseMapX();
                    var y = sim.getView().getMouseMapY();
                    for(var intersection : sim.getMap().getIntersections()){
                        var xd = Math.abs(x - intersection.getX());
                        var yd = Math.abs(y - intersection.getY());
                        if(xd < 1 && yd < 1){
                            this.held = intersection;
                        }
                    }
                }
                if (sim.getInput().mousePressed(Input.MouseKey.Middle)){
                    try{
                        sim.getMap().addIntersection(null, "", sim.getView().getMouseMapX(), sim.getView().getMouseMapY());
                    }catch (MapBuildingException ignore){}
                }
                if(sim.getInput().mouseHeld(Input.MouseKey.Left)) {
                    if(held != null){
                        this.held.updatePosition(sim.getMap(),  sim.getView().getMouseMapX(), sim.getView().getMouseMapY());
                    }

                    try {
                        map.autoLinkTurns();
                    } catch (MapBuildingException ignore) {

                    }
                }
                if (sim.getInput().mouseReleased(Input.MouseKey.Right)){
                    var x = sim.getView().getMouseMapX();
                    var y = sim.getView().getMouseMapY();
                    Intersection other = null;
                    for(var intersection : sim.getMap().getIntersections()){
                        var xd = Math.abs(x - intersection.getX());
                        var yd = Math.abs(y - intersection.getY());
                        if(xd < 1 && yd < 1){
                            other = intersection;
                        }
                    }
                    if(other != null && held !=null){
                        var linked = sim.getMap().getLinked(held, other);
                        if (linked != null){
                            linked.addLanes(1);
                        }else try{
                            sim.getMap().linkIntersection(held, other, null, "", 1);
                        }catch (MapBuildingException ignore){}
                    }
                    this.held = null;
                }

                if(this.held != null){
                    sim.getView().setLayer(Display.Layer.TopLevel);
                    sim.getView().setColor(Color.YELLOW);
                    sim.getView().drawOval(held.getX(), held.getY(), 3f, 3f);
                    if(sim.getInput().mouseHeld(Input.MouseKey.Right)){
                        sim.getView().drawLine(held.getX(), held.getY(), sim.getView().getMouseMapX(), sim.getView().getMouseMapY());
                    }
                }


                if (sim.getInput().keyPressed('F')){
                    if (sim.getView().getFollowing() == null){
                        sim.getView().setFollowing(player);
                    }else{
                        sim.getView().setFollowing(null);
                    }
                }
                if (sim.getInput().keyPressed('R')){
                    if (!player.isOnRoad()){
                        is.toAdd(player);
                    }
                }
            }
        });

        player.setSpeedMultiplier(1f);
        is.toAdd(player);
        simulation.run();
    }
}