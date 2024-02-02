package traffic_sim.map.intersection;

import traffic_sim.Simulation;
import traffic_sim.map.RoadMap;
import traffic_sim.vehicle.Car;
import traffic_sim.vehicle.Vehicle;

import java.util.ArrayList;

public class SourceIntersection extends Intersection{

    private final ArrayList<Vehicle> toAdd = new ArrayList<>();

    public SourceIntersection(String name, float x, float y) {
        super(name, x, y);
    }

    @Override
    public void tick(Simulation sim, RoadMap map, double delta) {
        var outgoing = map.outgoing(this);
        if (outgoing == null) return;
        if (!outgoing.isEmpty()){
            var random = outgoing.get((int) (outgoing.size()*Math.random()));
            if (random.getNumLanes() > 0){
                var lane = (int)(random.getNumLanes()*Math.random());
                if (random.getSpace(lane) > 2.0){
                    if (toAdd.isEmpty()){
                        random.getLane(lane).addVehicle(new Car());
                    }else{
                        random.getLane(lane).addVehicle(toAdd.remove(0));
                    }
                }
            }
        }
    }

    public void toAdd(Vehicle player) {
        toAdd.add(player);
    }
}
