package traffic_sim.gamble;

import traffic_sim.map.Road;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.vehicle.Vehicle;

/**
 * A handler to deal with when a vehicle takes a gamble, deals with how reputation and health is dealt with
 */
public interface GambleHandler {
    /**
     * Called when a vehicles makes a valid lane change of any kind
     *
     * @param v             The vehicle who made a lane change
     * @param lane          The lane the vehicle is currently in
     * @param index         The index of the vehicle in the lane
     * @param wasSafe       Was the lane change made when the road was clear
     * @param laneChange    The kind of lane change decision that was made
     */
    void laneChange(Vehicle v, Road.Lane lane, int index, boolean wasSafe, Road.LaneChangeDecision laneChange);

    /**
     * Called when a vehicle makes a turn
     *
     * @param v     The vehicle who turned
     * @param from  The road turned off from
     * @param to    The turn that was chosen
     */
    void chooseTurn(Vehicle v, Road.Lane from, Intersection.Turn to);

    /**
     * Called when a vehicle makes a lane change into a lane that doesn't exist
     *
     * @param v   The vehicle who made an incorrect lane change
     * @param current   The lane the vehicle was currently in
     * @param delta     The delta time
     */
    void turnedIntoNonExistedLane(Vehicle v, Road.Lane current, float delta);
}
