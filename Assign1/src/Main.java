import traffic_sim.Simulation;
import traffic_sim.io.Display;
import traffic_sim.io.Input;
import traffic_sim.map.intersection.DrainIntersection;
import traffic_sim.map.RoadMap;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.map.intersection.SourceIntersection;
import traffic_sim.map.intersection.TimedIntersection;
import traffic_sim.vehicle.Car;
import traffic_sim.vehicle.Truck;
import traffic_sim.vehicle.controller.PlayerController;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        var map = new RoadMap();
        var i1 = map.addIntersection(new TimedIntersection("1", 0,0));
        var i2 = map.addIntersection("2", 10,0);
        var i3 = map.addIntersection("3", 10,10);
        var i4 = map.addIntersection("4", 0,10);
        var i5 = map.addIntersection("5", 0,20);
        var i6 = map.addIntersection("6", -10,10);
        var is = new SourceIntersection("Source", 0, -10);
        var id = new DrainIntersection("Drain", 10, 20);
        map.addIntersection(is);
        map.addIntersection(id);

        var sr = map.linkIntersection(is, i1, "", 2);
        map.linkIntersection(i1, is, "", 2);
        var dr = map.linkIntersection(i3, id, "", 1);

        var r1 = map.linkIntersection(i1, i2, "", 1);
        var r2 = map.linkIntersection(i2, i3, "", 2);
        var r3 = map.linkIntersection(i3, i4, "", 1);
        var r4 = map.linkIntersection(i4, i1, "", 1);

        var r5 = map.linkIntersection(i4, i5, "", 1);

        var r6 = map.linkIntersection(i5, i6, "", 1);
        var r7 = map.linkIntersection(i6, i1, "", 1);

        var r8 = map.linkIntersection(i1, i6, "", 1);
        var r9 = map.linkIntersection(i6, i4, "", 1);

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

        map.write(new FileWriter("bruh.txt"));

        map = new RoadMap();
        map.read(new Scanner(new File("bruh.txt")));

        r1.setSpeedLimit(2.0f);

        var simulation = new Simulation(map);

        var player = new Car(new PlayerController(simulation.getInput()), Color.MAGENTA);
        player.setSpeedMultiplier(0.1f);
        is.toAdd(player);
        is.toAdd(new Truck());

        simulation.getView().panX = 0;
        simulation.getView().panY = 0;
        simulation.getView().zoom = 21;

        simulation.addSystem(new Simulation.SimSystem(1){

            Intersection held;
            @Override
            public void init(Simulation sim) {}

            @Override
            public void run(Simulation sim, float delta) {
                if (sim.getInput().keyHeld('d')){
                    sim.getView().panX -= sim.getTrueDelta()*7;
                }
                if (sim.getInput().keyHeld('a')){
                    sim.getView().panX += sim.getTrueDelta()*7;
                }
                if (sim.getInput().keyHeld('w')){
                    sim.getView().panY += sim.getTrueDelta()*7;
                }
                if (sim.getInput().keyHeld('s')){
                    sim.getView().panY -= sim.getTrueDelta()*7;
                }
                if (sim.getInput().keyHeld('q')){
                    sim.getView().zoom += sim.getTrueDelta()*sim.getView().zoom*2;
                }
                if (sim.getInput().keyHeld('e')){
                    sim.getView().zoom -= sim.getTrueDelta()*sim.getView().zoom*2;
                }
                if (sim.getInput().keyPressed(' ')){
                    sim.setPaused(!sim.getPaused());
                }
                if (sim.getInput().keyPressed('v')){
                    sim.setDebug(!sim.getDebug());
                }
                if (sim.getInput().keyPressed('t')){
                    is.toAdd(new Truck());
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
                if (sim.getInput().keyHeld('+')){
                    sim.setSimulationMultiplier(sim.getSimulationMultiplier() + 100.0f);
                }
                if(sim.getInput().mousePressed(Input.MouseKey.Left) | sim.getInput().mousePressed(Input.MouseKey.Right)){

                    this.held = null;
                    var x = sim.getView().getScreenX();
                    var y = sim.getView().getScreenY();
                    for(var intersection : sim.getMap().getIntersections()){
                        var xd = Math.abs(x - intersection.getX());
                        var yd = Math.abs(y - intersection.getY());
                        if(xd < 1 && yd < 1){
                            this.held = intersection;
                        }
                    }
                }
                if (sim.getInput().mousePressed(Input.MouseKey.Middle)){
                    sim.getMap().addIntersection("", sim.getView().getScreenX(), sim.getView().getScreenY());
                }
                if(sim.getInput().mouseHeld(Input.MouseKey.Left)) {
                    if(held != null){
                        this.held.updatePosition(sim.getMap(),  sim.getView().getScreenX(), sim.getView().getScreenY());
                    }
                }
                if (sim.getInput().mouseReleased(Input.MouseKey.Right)){
                    var x = sim.getView().getScreenX();
                    var y = sim.getView().getScreenY();
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
                        }else
                            sim.getMap().linkIntersection(held, other, "", 1);
                    }
                    this.held = null;
                }

                if(this.held != null){
                    sim.getView().setLayer(Display.Layer.TopLevel);
                    sim.getView().setColor(Color.YELLOW);
                    sim.getView().drawOval(held.getX(), held.getY(), 3f, 3f);
                    if(sim.getInput().mouseHeld(Input.MouseKey.Right)){
                        sim.getView().drawLine(held.getX(), held.getY(), sim.getView().getScreenX(), sim.getView().getScreenY());
                    }
                }


                if (sim.getInput().keyPressed('f')){
                    if (sim.getView().getFollowing() == null){
                        sim.getView().setFollowing(player);
                    }else{
                        sim.getView().setFollowing(null);
                    }
                }
                if (sim.getInput().keyPressed('r')){
                    if (!player.isOnRoad()){
                        is.toAdd(player);
                    }
                }
            }
        });

        simulation.run();


    }
}