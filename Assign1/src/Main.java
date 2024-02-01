import traffic_sim.Simulation;
import traffic_sim.map.intersection.DrainIntersection;
import traffic_sim.map.RoadMap;
import traffic_sim.map.intersection.SourceIntersection;
import traffic_sim.vehicle.Car;
import traffic_sim.vehicle.controller.PlayerController;

import java.awt.*;

public class Main {
    public static void main(String[] args) {
        var map = new RoadMap();
        var i1 = map.addIntersection("1", 0,0);
        var i2 = map.addIntersection("2", 10,0);
        var i3 = map.addIntersection("3", 10,10);
        var i4 = map.addIntersection("4", 0,10);
        var i5 = map.addIntersection("5", 0,20);
        var i6 = map.addIntersection("6", -10,10);
        var is = new SourceIntersection("Source", 0, -10);
        var id = new DrainIntersection("Drain", 10, 20);
        map.addIntersection(is);
        map.addIntersection(id);

        var sr = map.linkIntersection(is, i1, 3);
        map.linkIntersection(i1, is, 3);
        var dr = map.linkIntersection(i3, id, 1);

        var r1 = map.linkIntersection(i1, i2, 1);
        var r2 = map.linkIntersection(i2, i3, 2);
        var r3 = map.linkIntersection(i3, i4, 1);
        var r4 = map.linkIntersection(i4, i1, 1);

        var r5 = map.linkIntersection(i4, i5, 1);

        var r6 = map.linkIntersection(i5, i6, 1);
        var r7 = map.linkIntersection(i6, i1, 1);

        var r8 = map.linkIntersection(i1, i6, 1);
        var r9 = map.linkIntersection(i6, i4, 1);

        map.addTurn(sr.getLane(0), r1.getLane(0), "Left");
        map.addTurn(sr.getLane(0), r8.getLane(0), "Right");
        map.addTurn(sr.getLane(1), r8.getLane(0), "Right");
        map.addTurn(sr.getLane(2), r8.getLane(0), "Right");

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

        r1.setSpeedLimit(2.0f);

        var simulation = new Simulation(map);

        var player = new Car(new PlayerController(simulation.getInput()), Color.MAGENTA);
        map.init(player);

        simulation.getView().panX = 0;
        simulation.getView().panY = 0;
        simulation.getView().zoom = 21;

        simulation.addSystem((sim, tick) -> {
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
                sim.getView().setDebug(!sim.getView().getDebug());
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
        });

        simulation.run();


    }
}