package traffic_sim.map.intersection;

import traffic_sim.Simulation;
import traffic_sim.io.Display;
import traffic_sim.map.RoadMap;
import traffic_sim.map.Road;
import traffic_sim.vehicle.Vehicle;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Intersection {

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

    public void tick(Simulation sim, RoadMap map, double delta){}

    public float distance(Intersection other){
        var xdiff = this.x - other.x;
        var ydiff = this.y - other.y;
        return (float) Math.sqrt(xdiff*xdiff + ydiff*ydiff);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public String getName() {
        return name;
    }

    public void addTurn(Road.Lane from, Road.Lane to, String turnDirection) {
        if (!this.turns.containsKey(from)){
            this.turns.put(from, new ArrayList<>());
        }
        this.turns.get(from).add(new Intersection.Turn(turnDirection, to));
    }

    public void updatePosition(RoadMap map, float x, float y){
        this.x = x;
        this.y = y;
        for(var road : map.outgoing(this)){
            road.changedPosition(map);
        }
        for(var road : map.incoming(this)){
            road.changedPosition(map);
        }
    }

    public ArrayList<Turn> getTurns(Road.Lane lane) {
        return this.turns.get(lane);
    }

    public HashMap<Road.Lane, ArrayList<Turn>> getAllTurns() {
        return this.turns;
    }

    public void draw(Simulation sim, RoadMap map) {
        sim.getView().setLayer(Display.Layer.Intersections);
        sim.getView().setColor(Color.RED);
        sim.getView().fillOval(this.getX(), this.getY(), 2, 2);
        sim.getView().setColor(Color.WHITE);
        sim.getView().drawString(this.getName(), this.getX(), this.getY());
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

        public String getName() {
            return this.name;
        }

        public Road.Lane getLane() { return this.lane; };

        public boolean canTurn(Vehicle vehicle) {
            if (enabled){
                return this.lane.canFit(vehicle);
            }else{
                return false;
            }
        }
    }
}
