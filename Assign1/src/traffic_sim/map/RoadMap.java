package traffic_sim.map;

import traffic_sim.io.View;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.vehicle.Vehicle;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class RoadMap {
    private final ArrayList<Road> roads = new ArrayList<>();
    private final ArrayList<Intersection> intersectionNames = new ArrayList<>();


    private final HashMap<Road, Intersection> roadEnds = new HashMap<>();
    private final HashMap<Road, Intersection> roadStarts = new HashMap<>();
    private final HashMap<Intersection, ArrayList<Road>> outgoing = new HashMap<>();
    private final HashMap<Intersection, ArrayList<Road>> incoming = new HashMap<>();



    public Intersection addIntersection(String name, float x, float y){
        var intersection = new Intersection(name, x, y);
        return addIntersection(intersection);
    }

    public Intersection addIntersection(Intersection intersection){
        intersectionNames.add(intersection);
        return intersection;
    }

    public Road linkIntersection(Intersection from, Intersection to, int lanes){
        var road = new Road(lanes, from.distance(to));
        roads.add(road);
        roadEnds.put(road, to);
        roadStarts.put(road, from);

        if (!outgoing.containsKey(from)){
            outgoing.put(from, new ArrayList<>());
        }
        outgoing.get(from).add(road);

        if (!incoming.containsKey(to)){
            incoming.put(to, new ArrayList<>());
        }
        incoming.get(to).add(road);

        return road;
    }

    public void addTurn(Road.Lane from, Road.Lane to, String turnDirection){
        var middle = this.roadEnds.get(from.road());
        var middle_check = this.roadStarts.get(to.road());
        if (middle == null || middle != middle_check){
            throw new RuntimeException("Roads aren't connected at intersection");
        }

        middle.addTurn(from, to, turnDirection);
    }

    public void init(Vehicle player){
        this.roads.get(0).getLane(0).addVehicle(player);
    }

    public void tick(float delta){
        for(var intersection : intersectionNames){
            intersection.tick(this, delta);
        }
        for(var road : roads){
            road.tick(this, delta);
        }
    }

    public void draw(View g){
        for(var intersection : this.intersectionNames){
            intersection.draw(g, this);
        }
        for(var road : roads){
            road.draw(g, this);
        }
    }

    public float[] carPosition(Road road, Vehicle vehicle) {
        return this.carPosition(this.roadStarts.get(road), this.roadEnds.get(road), road, vehicle);
    }
    public float[] carPosition(Intersection from, Intersection to, Road road, Vehicle vehicle){
        float percent = vehicle.getDistanceAlongRoad() / road.getLength();
        return new float[] {from.getX() * (1-percent) + to.getX()*(percent), from.getY() * (1-percent) + to.getY()*(percent)};
    }

    public ArrayList<Road> incoming(Intersection intersection) {
        return this.incoming.get(intersection);
    }

    public ArrayList<Road> outgoing(Intersection intersection) {
        return this.outgoing.get(intersection);
    }

    public Intersection roadEnds(Road road) {
        return this.roadEnds.get(road);
    }

    public Intersection roadStarts(Road road) {
        return this.roadStarts.get(road);
    }
}
