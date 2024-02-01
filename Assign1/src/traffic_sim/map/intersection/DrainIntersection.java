package traffic_sim.map.intersection;

import traffic_sim.map.RoadMap;

public class DrainIntersection extends Intersection{
    public DrainIntersection(String name, float x, float y) {
        super(name, x, y);
    }

    @Override
    public void tick(RoadMap map, double delta) {
        var incoming = map.incoming(this);
        if (incoming == null) return;
        for(var road : incoming){
            for(var lane : road.getLanes()){
                var removed = lane.removeEnd();
                if (removed != null) removed.removeFromRoad();
            }
        }
    }
}
