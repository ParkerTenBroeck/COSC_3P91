package traffic_sim.map.intersection;

import traffic_sim.Simulation;

/**
 * An intersection that removes all the vehicles that get to the end
 */
public class DrainIntersection extends Intersection{
    public DrainIntersection(String name, float x, float y) {
        super(name, x, y);
    }

    @Override
    public void tick(Simulation sim, double delta) {
        var incoming = sim.getMap().incoming(this);
        if (incoming == null) return;
        for(var road : incoming){
            for(var lane : road.getLanes()){
                var removed = lane.removeLastVehicle();
                if (removed != null) removed.removeFromRoad();
            }
        }
    }
}
