import traffic_sim.NetworkSystem;
import traffic_sim.Simulation;
import traffic_sim.excpetions.MapBuildingException;
import traffic_sim.io.Display;
import traffic_sim.io.Input;
import traffic_sim.io.TextView;
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
import traffic_sim.vehicle.controller.PlayerController;

import java.awt.*;
import java.io.*;

/*UML_HIDE*/
public class Main {

    public static void main(String[] args) throws Exception {

        var map = MapXmlTools.loadMap(new FileInputStream("res/road_map.xml"));
        MapXmlTools.saveMap(map, new FileWriter("saved_road_map.xml"));

        var is = (SourceIntersection)map.getIntersectionById("Source");

        for(var road : map.getRoads()){
            for(var lane : road.getLanes()){
                for(int i = 0; i < 3; i ++)
                    lane.addVehicle(new Car());
            }
        }

        Simulation simulation;
        Vehicle player;

        // set this to false for a GUI thats very incomplete
        boolean text = false;
        // show gui while being controlled by text mode
        boolean show_gui = true;
        if (text){
            var displayController = new TextView();
            simulation = new Simulation(map, displayController, show_gui);
            player = new Car(displayController, Color.MAGENTA);
        }else{
            simulation = new Simulation(map, new View());
            player = new Car(new PlayerController(simulation.getInput()), Color.MAGENTA);
        }

        if(!text || show_gui){
            if(text){
                simulation.setDebug(false);
                simulation.getView().setFollowing(player);
            }
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
                    if (sim.getInput().keyHeld('P')){
//                        try{
//                            MapXmlTools.saveMap(sim.getMap(), new FileWriter("savedmap.xml"));
//                        }catch (Exception ignore){}
                    }
                    sim.isPooled ^= sim.getInput().keyPressed('P');
//                    if (sim.getInput().keyPressed('R')){
//                        try{
//                            map.read(new FileReader("newmap.txt"));
//                        }catch (Exception ignore){}
//                    }
                    if (sim.getInput().keyPressed('V')){
                        sim.setDebug(!sim.getDebug());
                    }
                    if (sim.getInput().keyPressed('T')){
//                    is.toAdd(new Truck());
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
        }

        player.setSpeedMultiplier(1f);
        is.toAdd(player);
        is.toAdd(new Truck());

        simulation.addSystem(new NetworkSystem());

        simulation.run();
    }

    public static void createMap() throws MapBuildingException, IOException {
        var map = new RoadMap();
        var i1 = map.addIntersection(null, new TimedIntersection("", 0,0));
        var i2 = map.addIntersection(null, "", 10,0);
        var i3 = map.addIntersection(null, "", 10,10);
        var i4 = map.addIntersection(null, "", 0,10);
        var i5 = map.addIntersection(null, "", 0,20);
        var i6 = map.addIntersection(null, "", -10,10);
        var is = new SourceIntersection("Source", 0, -10);
        var id = new DrainIntersection("Drain", 10, 20);
        map.addIntersection("Source", is);
        map.addIntersection("Drain", id);

        var sr = map.linkIntersection(is, i1, null,"", 2);
        map.linkIntersection(i1, is, null, "", 2);
        var dr = map.linkIntersection(i3, id, null, "", 1);

        var r1 = map.linkIntersection(i1, i2, null, "", 1);
        var r2 = map.linkIntersection(i2, i3, null, "", 2);
        var r3 = map.linkIntersection(i3, i4, null, "", 1);
        var r4 = map.linkIntersection(i4, i1, null, "", 1);

        var r5 = map.linkIntersection(i4, i5, null, "", 1);

        var r6 = map.linkIntersection(i5, i6, null, "", 1);
        var r7 = map.linkIntersection(i6, i1, null, "", 1);

        var r8 = map.linkIntersection(i1, i6, null, "", 1);
        var r9 = map.linkIntersection(i6, i4, null, "", 1);

        map.addTurn(sr.getLane(0), r1.getLane(0), "Left");
        map.addTurn(sr.getLane(0), r8.getLane(0), "Right");
        map.addTurn(sr.getLane(1), r8.getLane(0), "Right");

        map.addTurn(r2.getLane(0), dr.getLane(0), "Forward");

        map.addTurn(r4.getLane(0), r8.getLane(0), "Left");
        map.addTurn(r8.getLane(0), r9.getLane(0), "Left");
        map.addTurn(r9.getLane(0), r4.getLane(0), "Left");
        map.addTurn(r9.getLane(0), r5.getLane(0), "Right");
        map.addTurn(r6.getLane(0), r9.getLane(0), "Right");


        map.addTurn(r1.getLane(0), r2.getLane(0), "Right");
        map.addTurn(r2.getLane(0), r3.getLane(0), "Right");
        map.addTurn(r3.getLane(0), r4.getLane(0), "Right");
        map.addTurn(r4.getLane(0), r1.getLane(0), "Right");

        map.addTurn(r3.getLane(0), r5.getLane(0), "Left");

        map.addTurn(r5.getLane(0), r6.getLane(0), "Right");
        map.addTurn(r6.getLane(0), r7.getLane(0), "Right");
        map.addTurn(r7.getLane(0), r1.getLane(0), "Right");


        MapXmlTools.saveMap(map, new FileWriter("simple_roadmap.xml"));
    }
}