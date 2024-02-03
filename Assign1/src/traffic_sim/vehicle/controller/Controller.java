package traffic_sim.vehicle.controller;

import traffic_sim.Simulation;
import traffic_sim.map.RoadMap;
import traffic_sim.map.Road;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.vehicle.Vehicle;

import java.util.ArrayList;

public interface Controller {
    void tick(Vehicle v, Simulation sim, Road.Lane road, float delta);
    Intersection.Turn chooseTurn(Vehicle v, Simulation sim, ArrayList<Intersection.Turn> turns);
    Road.LaneChangeDecision laneChange(Vehicle v, Simulation sim, Road.Lane road);
}
