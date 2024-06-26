package traffic_sim.map;

import traffic_sim.Simulation;
import traffic_sim.ThreadPool;
import traffic_sim.excpetions.MapBuildingException;
import traffic_sim.map.intersection.*;
import traffic_sim.vehicle.Vehicle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Future;

/**
 * A map of connected Intersection and Roads
 */
public class RoadMap implements Serializable {
    /*UML_RAW_OUTER RoadMap "1" *-- "n" Road: map contains many roads*/
    private final ArrayList<Road> roads = new ArrayList<>();
    /*UML_RAW_OUTER RoadMap "1" *-- "n" Intersection: map contains many intersections*/
    private final ArrayList<Intersection> intersections = new ArrayList<>();


    private final HashMap<String, Intersection> idToIntersection = new HashMap<>();
    private final HashMap<Intersection, String> intersectionToId = new HashMap<>();
    private final HashMap<String, Road> idToRoad = new HashMap<>();
    private final HashMap<Road, String> roadToId = new HashMap<>();


    /*UML_RAW_OUTER Intersection "2" *-- "1" Road: road connecting two intersections (one way)*/
    private final HashMap<Road, Intersection> roadEnds = new HashMap<>();
    private final HashMap<Road, Intersection> roadStarts = new HashMap<>();
    /*UML_RAW_OUTER Intersection "1" o-- "n" Road: outgoing/incoming roads*/
    private final HashMap<Intersection, ArrayList<Road>> outgoing = new HashMap<>();
    private final HashMap<Intersection, ArrayList<Road>> incoming = new HashMap<>();


    /**
     * Gets the intersection associated with the ID, null if none exists
     *
     * @param id    The id of the desired road
     * @return  The found intersection or null
     */
    public Intersection getIntersectionById(String id) {
        return this.idToIntersection.get(id);
    }


    /**
     * @param intersection  The intersection whos ID we want to find
     * @return          The ID of the provided intersection
     */
    public String getIntersectionId(Intersection intersection) {
        return this.intersectionToId.get(intersection);
    }

    /**
     * Gets the road associated with the ID, null if none exists
     *
     * @param id    The id of the desired road
     * @return  The found road or null
     */
    public Road getRoadById(String id){
        return this.idToRoad.get(id);
    }

    /**
     * @param road  The road whos ID we want to find
     * @return      The ID of the provided road
     */
    public String getRoadId(Road road){
        return this.roadToId.get(road);
    }

    /** Adds a default intersection at the provided coords with the provided name
     *
     * @param name  The name of the intersection
     * @param x     The X component of the coord
     * @param y     the Y component of the coord
     * @return      The created intersection
     */
    public Intersection addIntersection(String id, String name, float x, float y) throws MapBuildingException{
        var intersection = new DefaultIntersection(name, x, y);
        return addIntersection(id, intersection);
    }

    /** Adds the provided intersection to the RoadMap
     *
     * @param intersection The intersection to add
     * @return      The intersection provided
     */
    public Intersection addIntersection(String id, Intersection intersection) throws MapBuildingException{
        if(id == null) id = "I"+this.idToIntersection.size();
        if(this.idToIntersection.containsKey(id))
            throw new MapBuildingException.IntersectionIDClash("Intersection ID already exists");

        intersections.add(intersection);
        outgoing.put(intersection, new ArrayList<>());
        incoming.put(intersection, new ArrayList<>());

        idToIntersection.put(id, intersection);
        intersectionToId.put(intersection, id);

        return intersection;
    }

    /** Gets the Road linking the intersections from and to (in that direction) returns null if there is none
     *
     * @param from  The starting intersection
     * @param to    The ending intersection
     * @return      The Road connecting them (if anu)
     */
    public Road getLinked(Intersection from, Intersection to){
        for(var incoming : incoming.get(to)){
            for(var outgoing : outgoing.get(from)){
                if (incoming == outgoing)
                    return incoming;
            }
        }
        return null;
    }

    /** Links two intersections (directionally) with a road, The two intersections must not already have a Road connecting
     * them
     *
     * @param from  The starting intersection of the road
     * @param to    The ending intersection of the road
     * @param name  The name of the road
     * @param lanes How many lanes should the road have to start out with
     * @return The Road linking the two provided intersections
     */
    public Road linkIntersection(Intersection from, Intersection to, String id, String name, int lanes) throws MapBuildingException{

        if(id == null) id = "R"+this.idToRoad.size();
        if(this.idToRoad.containsKey(id))
            throw new MapBuildingException.RoadIDClash("Road ID already exists: " + id);

        if(this.getLinked(from, to) != null)
            throw new MapBuildingException.InvalidRoadLink("Intersections already linked with Road segment, existing: " + roadToId.get(this.getLinked(from, to)) + " new: " + id);

        var road = new Road(name, lanes, from, to);
        roads.add(road);
        roadEnds.put(road, to);
        roadStarts.put(road, from);

        outgoing.get(from).add(road);
        incoming.get(to).add(road);

        idToRoad.put(id, road);
        roadToId.put(road, id);

        road.updatePosition(from, to);
        return road;
    }

    /** Adds a turn between the two provided lanes, The lanes must be an incoming and outgoing lane respectively
     * both apart of the same intersection
     *
     * @param from  The lane to turn off of
     * @param to    The lane to turn on to
     * @param turnDirection The name of the turn
     */
    public void addTurn(Road.Lane from, Road.Lane to, String turnDirection) throws MapBuildingException{
        var middle = this.roadEnds.get(from.road());
        var middle_check = this.roadStarts.get(to.road());
        if (middle == null || middle != middle_check){
            throw new MapBuildingException.InvalidTurn("Roads aren't connected at intersection");
        }

        middle.addTurn(from, to, turnDirection);
    }


    static ThreadPool threadPool = new ThreadPool();

    /** A simulation tick for the entire map
     *
     * @param sim   The simulation this map is apart of
     * @param delta The simulation delta in seconds
     */
    public void tick(Simulation sim, float delta){

        for (Intersection intersection : intersections) {
            intersection.tick(sim, delta);
        }

        for (Road value : roads) {
            value.tickTurn(sim, delta);
        }

        for(var road : roads){
            threadPool.add(() -> road.tick(sim, delta));
        }
        threadPool.join();
    }

    /** Draws this map to the display
     *
     * @param sim   The simulation this map is apart of
     */
    public void draw(Simulation sim){
        for(var intersection : this.intersections){
            intersection.draw(sim);
        }
        for(var road : roads){
            road.draw(sim);
        }
    }

    /** Calculates the provided vehicles position in map space and return its x,y coords
     *
     * @param road      The road the vehicle is on
     * @param vehicle   The vehicle itself
     * @return          The X,Y coords respectivly
     */
    public float[] vehiclePosition(Road road, Vehicle vehicle) {
        return this.vehiclePosition(this.roadStarts.get(road), this.roadEnds.get(road), road, vehicle);
    }

    /** Calculates the provided vehicles position in map space and return its x,y coords
     *
     * @param from  The starting Intersection of the provided road
     * @param to    The ending Intersection of the provided road
     * @param road      The road the vehicle is on
     * @param vehicle   The vehicle itself
     * @return          The X,Y coords respectivly
     */
    public float[] vehiclePosition(Intersection from, Intersection to, Road road, Vehicle vehicle){
        float percent = vehicle.getDistanceAlongRoad() / road.getRoadLength();
        return new float[] {from.getX() * (1-percent) + to.getX()*(percent), from.getY() * (1-percent) + to.getY()*(percent)};
    }

    /** Gets the incoming roads for the provided intersection
     *
     * @param intersection  The intersection we want to find incoming roads for
     * @return   A list containing all incoming roads for the provided intersection
     */
    public ArrayList<Road> incoming(Intersection intersection) {
        return this.incoming.get(intersection);
    }

    /** Gets the outgoing roads for the provided intersection
     *
     * @param intersection  The intersection we want to find outgoing roads for
     * @return   A list containing all outgoing roads for the provided intersection
     */
    public ArrayList<Road> outgoing(Intersection intersection) {
        return this.outgoing.get(intersection);
    }

    /** Gets the ending intersection for the provided road
     *
     * @param road  The road we want to find the ending Intersection for
     * @return  The intersection the provided road ends at
     */
    public Intersection roadEnds(Road road) {
        return this.roadEnds.get(road);
    }

    /** Gets the starting intersection for the provided road
     *
     * @param road  The road we want to find the starting Intersection for
     * @return  The intersection the provided road starts at
     */
    public Intersection roadStarts(Road road) {
        return this.roadStarts.get(road);
    }

    /**
     * @return A list of all intersections in this map
     */
    public ArrayList<Intersection> getIntersections() {
        return this.intersections;
    }

    /**
     *
     * @return  a list of all the roads in this map
     */
    public ArrayList<Road> getRoads() {
        return this.roads;
    }


    /**
     * Automatically generate turns for roads with the most appropriate name. Clears all existing turns.
     */
    public void autoLinkTurns() throws MapBuildingException {
        for(var intersection : this.getIntersections()){
            intersection.getAllTurns().clear();
            for(var incoming_road : this.incoming(intersection))
                for(var outgoing_road : this.outgoing(intersection))
                    if(outgoing_road != incoming_road && this.roadStarts(incoming_road) != this.roadEnds(outgoing_road))
                        for(var incoming_lane : incoming_road.getLanes())
                            for(var outgoing_lane : outgoing_road.getLanes()){
                                var angle = (Math.atan2(
                                        outgoing_road.getDirectionX(), outgoing_road.getDirectionY())
                                        -  Math.atan2(-incoming_road.getDirectionX(), -incoming_road.getDirectionY()))*180/Math.PI;
                                angle += 360;
                                angle %= 360;
                                angle -= 180;
                                var primary = "uturn";
                                if (-45 <= angle && angle <= 45){
                                    primary = "forward";
                                }else if (45 <= angle){
                                    primary = "left";
                                }else if (angle <= -45){
                                    primary = "right";
                                }
                                if (outgoing_road.getLanes().length > 1){
                                    primary += " merge to lane " + outgoing_lane.getLane();
                                }
                                this.addTurn(incoming_lane, outgoing_lane, primary);
                            }
        }
    }
}
