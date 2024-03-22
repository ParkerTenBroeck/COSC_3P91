package traffic_sim.map.intersection;

import traffic_sim.Simulation;
import traffic_sim.io.Display;
import traffic_sim.map.RoadMap;
import traffic_sim.map.Road;
import traffic_sim.vehicle.Vehicle;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * An Intersection that exists at some coordinate X,Y. It also holds a name and the turns it can take.
 */
public abstract class Intersection {

    protected float x;
    protected float y;
    protected final String name;

    /*UML_RAW_OUTER Lane "1" o-- "n" Turn: A Lane can have many, one or no turns that it can take*/
    protected final HashMap<Road.Lane, ArrayList<Turn>> turns = new HashMap<>();


    public Intersection(String name, float x, float y){
        this.name = name;
        this.x = x;
        this.y = y;
    }

    /** Ticks the Intersection, This will be ran every simulation tick
     *
     * @param sim   The simulation
     * @param delta The simulation delta in seconds
     */
    public void tick(Simulation sim, double delta){}


    /** Calculates the distance between this intersection and another intersection
     *
     * @param other The other Intersection
     * @return  The calculated distance
     */
    public float distance(Intersection other){
        var xdiff = this.x - other.x;
        var ydiff = this.y - other.y;
        return (float) Math.sqrt(xdiff*xdiff + ydiff*ydiff);
    }

    /**
     * @return  The X coord of this intersection
     */
    public float getX() {
        return x;
    }

    /**
     * @return  The Y coord of this intersection
     */
    public float getY() {
        return y;
    }

    /**
     * @return  The name of this intersection
     */
    public String getName() {
        return name;
    }

    /** Adds a turning option from an incoming lane to an outgoing lane with a name to specify direction.
     *
     * @param from  The incoming lane
     * @param to    The outgoing lane
     * @param turnDirection The direction/name of the turn
     * @return The turn created
     */
    public Turn addTurn(Road.Lane from, Road.Lane to, String turnDirection) {
        if (!this.turns.containsKey(from)){
            this.turns.put(from, new ArrayList<>());
        }
        var turn = new Intersection.Turn(turnDirection, to);
        this.turns.get(from).add(turn);
        return turn;
    }

    /** Updates the intersections position also updating all the connecting road positions
     *
     * @param map   The map this intersection is apart of
     * @param x     The new X coord
     * @param y     The nre Y coord
     */
    public void updatePosition(RoadMap map, float x, float y){
        this.x = x;
        this.y = y;
        for(var road : map.outgoing(this)){
            road.updatePosition(map);
        }
        for(var road : map.incoming(this)){
            road.updatePosition(map);
        }
    }

    /** Gets a list of all valid turns from an incoming lane
     *
     * @param lane  The lane we wish to turn from
     * @return      The list of valid turns we can take
     */
    public ArrayList<Turn> getTurns(Road.Lane lane) {
        return this.turns.get(lane);
    }

    /** Gets a collection of all the valid turns every incoming lane in this intersection can take
     *
     * @return  The collection of turns
     */
    public HashMap<Road.Lane, ArrayList<Turn>> getAllTurns() {
        return this.turns;
    }

    /** Draws this intersection to the display
     *
     * @param sim   The simulation this intersection is apart of
     */
    public void draw(Simulation sim) {
        sim.getView().setLayer(Display.Layer.Intersections);
        sim.getView().setColor(Color.RED);
        sim.getView().fillOval(this.getX(), this.getY(), 2, 2);
        sim.getView().setColor(Color.WHITE);
        sim.getView().drawString(this.getName(), this.getX(), this.getY());
        if(sim.getDebug())
            sim.getView().drawString(sim.getMap().getIntersectionId(this), this.getX(), this.getY());
    }

    /*UML_RAW_OUTER Turn "n" o-- "1" Lane: A turn contains a single Lane it can turn onto\n and a Lane can have many turns onto it*/
    public static class Turn{
        protected String name;
        protected Road.Lane lane;
        protected boolean enabled = true;

        public Turn(String name, Road.Lane road){
            this.name = name;
            this.lane = road;
        }

        /**
         * @return  The name of this turn
         */
        public String getName() {
            return this.name;
        }

        /**
         * @return  The lane this turn will turn onto
         */
        public Road.Lane getLane() { return this.lane; };

        /**
         * @param vehicle   The vehicle that wants to turn
         * @return          If the vehicle can turn into this lane
         */
        public boolean canTurn(Vehicle vehicle) {
            if (enabled){
                return this.lane.canFit(vehicle);
            }else{
                return false;
            }
        }

        /**
         * @return  If this turn was enabled or not
         */
        public boolean enabled() {
            return enabled;
        }
    }
}
