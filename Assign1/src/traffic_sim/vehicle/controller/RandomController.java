package traffic_sim.vehicle.controller;

import traffic_sim.Simulation;
import traffic_sim.map.Road;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.vehicle.Vehicle;

import java.util.ArrayList;

/**
 * A Vehicle controller that chooses its actions randomly
 */
public class RandomController implements Controller{
    @Override
    public void tick(Vehicle v, Simulation sim, Road.Lane lane, float delta) {}

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
    public  Road.LaneChangeDecision laneChange(Vehicle v, Simulation sim, Road.Lane lane, int left_vehicle_back_index, int right_vehicle_back_index) {
        return  Road.LaneChangeDecision.Nothing;
    }
}
