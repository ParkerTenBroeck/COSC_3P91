package traffic_sim.vehicle;

import traffic_sim.Simulation;
import traffic_sim.io.Display;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.map.Road;
import traffic_sim.vehicle.controller.Controller;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * A form of transportation that can use Raods, Change lanes, and make turns.
 */
public abstract class Vehicle implements Serializable {

    private float health = 1.0f;
    protected float reputation = 1.0f;
    private transient float distanceAlongRoad = 0.0f;
    private float lastX;
    private float lastY;
    private transient boolean onRoad;
    /*UML_RAW_OUTER Vehicle "1" *-- "0..1" Controller: Each Vehicle can potentially have one Controller*/
    private transient Controller controller;

    protected float speedMultiplier;
    protected float actualSpeed = 0.0f;
    private final float size;

    public Vehicle(Controller controller, float size){
        this.size = size;
        this.controller = controller;
    }

    /**
     * @param controller    The new controller to control this vehicle
     */
    public void setController(Controller controller){
        this.controller = controller;
    }

    /**
     * @return the current reputation of this vehicle
     */
    public float getReputation() { return this.reputation; }

    /**
     * @param reputation    The value we want to update this vehicles reputation to
     */
    public void setReputation(float reputation) {
        this.reputation = Math.max(0, reputation);
    }

    /**
     * @return the current health of this vehicle
     */
    public float getHealth() { return this.health; }

    /**
     * @param health the new value of health we want
     */
    public void setHealth(float health) {
        this.health = Math.max(0, health);
    }

    /**
     * @return  if this vehicle is alive (health > 0)
     */
    public boolean isAlive() { return this.health > 0; }

    /**
     * @return  The speed multiplier for this vehicle. a multiplier of 1.0 means it always goes the speed limit exactly
     */
    public float getSpeedMultiplier() { return this.speedMultiplier; }

    /**
     * @param speedMultiplier   The new value for the speed multiplier
     */
    public void setSpeedMultiplier(float speedMultiplier) { this.speedMultiplier = speedMultiplier; }

    /**
     * @return the size (length) of this vehicle
     */
    public float getSize() {
        return size;
    }

    /**
     * @return  A value that represents how far along the road the front of this vehicle is. is normally in the range 0 to road.length where road is the parent road
     */
    public float getDistanceAlongRoad() {
        return distanceAlongRoad;
    }

    /**
     * @return  A value that represents how far along the road the back of this vehicle is. is normally in the range 0 to road.length where road is the parent road
     */
    public float getDistanceAlongRoadBack() {
        return this.distanceAlongRoad - this.size;
    }

    /**
     * @param distanceAlongRoad The new distance along the road this vehicle should be
     */
    public void setDistanceAlongRoad(float distanceAlongRoad){
        this.distanceAlongRoad = distanceAlongRoad;
    }

    /**
     * @return  The actual speed of the vehicle
     */
    public float getActualSpeed(){
        return this.actualSpeed;
    }

    /** This gets ran every simulation tick and is responsible for ticking the controller and updating the vehicles
     * position
     *
     * @param sim the simulation this vehicle is apart of
     * @param lane  the lane this vehicle is on
     * @param delta the simulation time delta in seconds
     */
    public void tick(Simulation sim, Road.Lane lane, int laneIndex, boolean changedLanes, float delta) {
        var old = this.distanceAlongRoad;
        this.distanceAlongRoad += lane.road().getSpeedLimit() * delta * this.getSpeedMultiplier();
        this.distanceAlongRoad = Math.min(lane.remainingSpace()-0.01f, this.distanceAlongRoad);
        if(controller != null) controller.tick(this, sim, lane, laneIndex, changedLanes, delta);
        if(delta != 0)
            this.actualSpeed = (this.distanceAlongRoad - old) / delta;
    }

    /** Gets called every time this vehicle can make a turn, return null of no turn should be made
     *
     * @param sim   the simulation this vehicle is apart of
     * @param turns The turns available for this vehicle to make
     * @return  The turn this vehicle has decided on, null if none
     */
    public Intersection.Turn chooseTurn(Simulation sim, Intersection intersection, ArrayList<Intersection.Turn> turns){
        if(controller != null) return controller.chooseTurn(this, sim, intersection, turns);
        return null;
    }

    /**
     * This is called every tick to ask if a Vehicle should change lanes
     *
     * @param sim                      The simulation the vehicle is apart of
     * @param lane                     The lane the vehicle is on
     * @param current_index
     * @param left_vehicle_back_index  The index of the vehicle in the left lane that is behind this vehicle, -1 if the lane doesn't exist, > num of vehicles in lane if there is no vehicle behind
     * @param right_vehicle_back_index The index of the vehicle in the right lane that is behind this vehicle, -1 if the lane doesn't exist, > num of vehicles in lane if there is no vehicle behind
     * @return The lane change decision
     */
    public Road.LaneChangeDecision changeLane(Simulation sim, Road.Lane lane, int current_index, int left_vehicle_back_index, int right_vehicle_back_index){
        if(controller != null) return controller.laneChange(this, sim, lane, current_index, left_vehicle_back_index, right_vehicle_back_index);
        return  Road.LaneChangeDecision.Nothing;
    }


    /** Gets called when a vehicle gets put on a new lane
     *
     * @param lane the lane this vehicle was put on
     */
    public void putInLane(Road.Lane lane){
        if(controller != null) controller.putInLane(this, lane);
        this.onRoad = true;
    }

    /**
     * gets called when a vehicle is removed entirely from a road and not put back on
     */
    public void removeFromRoad(){
        this.onRoad = false;
    }

    /**
     * @return  if this vehicle is currently on a road
     */
    public boolean isOnRoad(){
        return this.onRoad;
    }

    /**
     * Draws the vehicle to the screen
     *
     * @param sim   the simulation this vehicle is apart of
     * @param x     the X component of this vehicles position
     * @param y     the Y component of this vehicles position
     * @param dx    the directional vector x component
     * @param dy    the directional vector y component
     */
    public void draw(Simulation sim, float x, float y, float dx, float dy){
        if(sim.getDebug()){
            sim.getView().setLayer(Display.Layer.Hud);
            sim.getView().setColor(Color.WHITE);
            sim.getView().drawString("h" + this.getHealth(), x, y+-10/sim.getView().zoom);
            sim.getView().drawString("r" + this.getReputation(), x, y+-20/sim.getView().zoom);
        }
        if(this.controller!=null)this.controller.draw(sim, this, x, y, dx, dy);
    }

    /** Updates the cached position of this vehicle to the given values, position is in map space
     *
     * @param x the X component of the position
     * @param y the Y component of the position
     */
    public void updatePosition(float x, float y){
        this.lastX = x;
        this.lastY = y;
    }

    /**
     * @return  the most recently calculated X position of this vehicle in map space
     */
    public float getLastX(){
        return this.lastX;
    }

    /**
     * @return  the most recently calculated Y position of this vehicle in map space
     */
    public float getLastY(){
        return this.lastY;
    }
}
