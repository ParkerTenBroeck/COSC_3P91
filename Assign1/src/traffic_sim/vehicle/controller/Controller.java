package traffic_sim.vehicle.controller;

import traffic_sim.map.RoadMap;
import traffic_sim.map.Road;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.vehicle.Vehicle;

import java.util.ArrayList;

public interface Controller {
    void tick(Vehicle v, RoadMap map, Road road, float delta);
    Intersection.Turn chooseTurn(Vehicle v, ArrayList<Intersection.Turn> turns);
    int laneChange(Vehicle v, RoadMap map, Road.Lane road);
}
