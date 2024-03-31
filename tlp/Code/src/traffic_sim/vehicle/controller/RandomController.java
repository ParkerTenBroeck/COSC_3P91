package traffic_sim.vehicle.controller;

import traffic_sim.ConsoleUtils;
import traffic_sim.Simulation;
import traffic_sim.io.Display;
import traffic_sim.map.Road;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.vehicle.Vehicle;

import java.awt.*;
import java.util.ArrayList;

/**
 * A Vehicle controller that chooses its actions randomly
 */
public class RandomController implements Controller{
    boolean wantsToLaneChange = false;
    boolean wantsToChange2 = false;
    float lastDistance;
    @Override
    public void tick(Vehicle v, Simulation sim, Road.Lane lane, int laneIndex, boolean changedLanes, float delta) {
        if (lane.vehicleAt(laneIndex -1) != null){
            lastDistance = lane.distanceToNext(laneIndex);
            if (lastDistance <= 0.2500000001 ){
                wantsToLaneChange = true;
            }else{
                wantsToLaneChange = false;
            }
        }else{
            wantsToLaneChange = false;
        }

    }

    @Override
    public Intersection.Turn chooseTurn(Vehicle v, Simulation sim, Road.Lane current_lane, Intersection intersection, ArrayList<Intersection.Turn> turns) {
        if (turns == null || turns.isEmpty()) {
            wantsToChange2 = true;
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
    public  Road.LaneChangeDecision laneChange(Vehicle v, Simulation sim, Road.Lane lane, int current_index, int left_vehicle_back_index, int right_vehicle_back_index) {
        if (wantsToLaneChange || wantsToChange2){
            if (!lane.leftmost){
                if (lane.leftLane().distanceToNext(left_vehicle_back_index) > v.getSize()
                        && (lane.leftLane().vehicleAt(left_vehicle_back_index) == null || lane.leftLane().vehicleAt(left_vehicle_back_index).getDistanceAlongRoad() < v.getDistanceAlongRoadBack())){
                    return Road.LaneChangeDecision.WaitLeft;
                }
            }else if(!lane.rightmost){
                if (lane.rightLane().distanceToNext(right_vehicle_back_index) > v.getSize()
                        && (lane.rightLane().vehicleAt(right_vehicle_back_index) == null ||lane.rightLane().vehicleAt(right_vehicle_back_index).getDistanceAlongRoad() < v.getDistanceAlongRoadBack())){
                    return Road.LaneChangeDecision.WaitRight;
                }
            }
            return lane.leftmost?Road.LaneChangeDecision.Nothing:Road.LaneChangeDecision.WaitLeft;
        }else{
            return  Road.LaneChangeDecision.Nothing;
        }
    }

    @Override
    public void draw(Simulation sim, Vehicle v, float x, float y, float dx, float dy) {
        if(sim.getDebug()){
            sim.getView().setLayer(Display.Layer.Hud);
            sim.getView().setColor(Color.WHITE);
            sim.getView().drawString("d"+lastDistance, x, y);
            sim.getView().drawString("s"+v.getActualSpeed(), x, y+10/sim.getView().zoom);
        }
    }
}
