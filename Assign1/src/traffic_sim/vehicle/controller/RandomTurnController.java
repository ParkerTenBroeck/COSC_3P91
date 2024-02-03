package traffic_sim.vehicle.controller;

import traffic_sim.Simulation;
import traffic_sim.map.RoadMap;
import traffic_sim.map.Road;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.vehicle.Vehicle;

import java.util.ArrayList;

public class RandomTurnController implements Controller{
    @Override
    public void tick(Vehicle v, Simulation sim, Road.Lane road, float delta) {}

    @Override
    public Intersection.Turn chooseTurn(Vehicle v, Simulation sim, ArrayList<Intersection.Turn> turns) {
        if (turns == null || turns.isEmpty()) {
            return null;
        }else{
            var random = (int)(Math.random() * turns.size());
            if (turns.get(random).canTurn(v)){
                return turns.get(random);
            }else{
                return null;
            }
        }
    }

    @Override
    public  Road.LaneChangeDecision laneChange(Vehicle v, Simulation sim, Road.Lane road) {
        return  Road.LaneChangeDecision.Nothing;
    }
}
