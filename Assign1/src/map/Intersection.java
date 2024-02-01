package map;

import vehicle.Vehicle;

import java.util.ArrayList;
import java.util.HashMap;

public class Intersection {

    protected final double x;
    protected final double y;
    protected final String name;

    protected final HashMap<Road.Lane, ArrayList<Turn>> turns = new HashMap<>();


    public Intersection(String name, double x, double y){
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public void tick(Map map, double delta){}

    public double distance(Intersection other){
        var xdiff = this.x - other.x;
        var ydiff = this.y - other.y;
        return Math.sqrt(xdiff*xdiff + ydiff*ydiff);
    }

    public double getX() {
        return x;
    }

    public double getY() {
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

    public ArrayList<Turn> getTurns(Road.Lane lane) {
        return this.turns.get(lane);
    }

    public static class Turn{
        String name;
        Road.Lane lane;

        public Turn(String name, Road.Lane road){
            this.name = name;
            this.lane = road;
        }

        public String getName() {
            return this.name;
        }

        public boolean canFit(Vehicle vehicle) {
            return this.lane.canFit(vehicle);
        }
    }
}
