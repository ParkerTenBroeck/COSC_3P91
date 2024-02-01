package traffic_sim.vehicle.controller;

import traffic_sim.io.Input;
import traffic_sim.map.RoadMap;
import traffic_sim.map.Road;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.vehicle.Vehicle;

import java.util.ArrayList;

public class PlayerController implements Controller{
    private final Input input;

    public PlayerController(Input input){
        this.input = input;
    }
    @Override
    public void tick(Vehicle v, RoadMap map, Road road, float delta) {
        if (this.input.keyHeld('i')){

            v.setSpeedMultiplier(v.getSpeedMultiplier() + delta * 0.1f);
        }
        if (this.input.keyHeld('k')){
            v.setSpeedMultiplier(v.getSpeedMultiplier() - delta * 0.1f);
        }
        v.setSpeedMultiplier(Float.min(v.getSpeedMultiplier(), 1.5f));
        v.setSpeedMultiplier(Float.max(v.getSpeedMultiplier(), 0));
    }

    @Override
    public Intersection.Turn chooseTurn(Vehicle v, ArrayList<Intersection.Turn> turns) {
        if (turns == null || turns.isEmpty()) {
            return null;
        }else{
            var random = (int)(Math.random() * turns.size());
            if (turns.get(random).canFit(v)){
                return turns.get(random);
            }else{
                return null;
            }
        }
    }

    @Override
    public int laneChange(Vehicle v, RoadMap map, Road.Lane road) {
        return 0;
    }
}