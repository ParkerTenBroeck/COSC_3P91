package traffic_sim.vehicle.controller;

import traffic_sim.Simulation;
import traffic_sim.map.Road;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.vehicle.Vehicle;

import java.util.ArrayList;

/**
 * A controller that dictates how a vehicle makes decisions
 */
public interface Controller {
    /** Gets called every simulation tick
     *
     * @param v The vehicle being controlled
     * @param sim   The simulation this vehicle exists in
     * @param lane  The lane the provided vehicle is on
     * @param delta The simulation delta time in seconds
     */
    void tick(Vehicle v, Simulation sim, Road.Lane lane, float delta);

    /** Gets called when a vehicle is at the end of the road and can make a turn
     *
     * @param v The vehicle being controlled
     * @param sim  The simulation the vehicle is apart of
     * @param turns The turns that are available at the end of this lane
     * @return  The turn decision that was made, null if no turn was selected
     */
    Intersection.Turn chooseTurn(Vehicle v, Simulation sim, ArrayList<Intersection.Turn> turns);

    /** This is called every tick to ask if a Vehicle should change lanes
     *
     * @param v The vehicle being controlled
     * @param sim   The simulation the vehicle is apart of
     * @param lane  The lane the vehicle is on
     * @param left_vehicle_back_index   The index of the vehicle in the left lane that is behind this vehicle, -1 if the lane doesn't exist, > num of vehicles in lane if there is no vehicle behind
     * @param right_vehicle_back_index  The index of the vehicle in the right lane that is behind this vehicle, -1 if the lane doesn't exist, > num of vehicles in lane if there is no vehicle behind
     * @return  The lane change decision
     */
    Road.LaneChangeDecision laneChange(Vehicle v, Simulation sim, Road.Lane lane, int left_vehicle_back_index, int right_vehicle_back_index);
}
