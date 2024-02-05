package traffic_sim.map;

import traffic_sim.Simulation;
import traffic_sim.map.intersection.DrainIntersection;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.map.intersection.SourceIntersection;
import traffic_sim.map.intersection.TimedIntersection;
import traffic_sim.vehicle.Vehicle;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * A map of connected Intersection and Roads
 */
public class RoadMap {
    /*UML_RAW_OUTER RoadMap "1" *-- "n" Road: map contains many roads*/
    private final ArrayList<Road> roads = new ArrayList<>();
    /*UML_RAW_OUTER RoadMap "1" *-- "n" Intersection: map contains many intersections*/
    private final ArrayList<Intersection> intersections = new ArrayList<>();


    /*UML_RAW_OUTER Intersection "2" *-- "1" Road: road connecting two intersections (one way)*/
    private final HashMap<Road, Intersection> roadEnds = new HashMap<>();
    private final HashMap<Road, Intersection> roadStarts = new HashMap<>();
    /*UML_RAW_OUTER Intersection "1" o-- "n" Road: outgoing/incoming roads*/
    private final HashMap<Intersection, ArrayList<Road>> outgoing = new HashMap<>();
    private final HashMap<Intersection, ArrayList<Road>> incoming = new HashMap<>();


    /** Adds a default intersection at the provided coords with the provided name
     *
     * @param name  The name of the intersection
     * @param x     The X component of the coord
     * @param y     the Y component of the coord
     * @return      The created intersection
     */
    public Intersection addIntersection(String name, float x, float y){
        var intersection = new Intersection(name, x, y);
        return addIntersection(intersection);
    }

    /** Adds the provided intersection to the RoadMap
     *
     * @param intersection The intersection to add
     * @return      The intersection provided
     */
    public Intersection addIntersection(Intersection intersection){
        intersections.add(intersection);
        outgoing.put(intersection, new ArrayList<>());
        incoming.put(intersection, new ArrayList<>());
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
    public Road linkIntersection(Intersection from, Intersection to, String name, int lanes){

        if(this.getLinked(from, to) != null)
            throw  new RuntimeException("Roads already linked");

        var road = new Road(name, lanes, from, to);
        roads.add(road);
        roadEnds.put(road, to);
        roadStarts.put(road, from);

        outgoing.get(from).add(road);
        incoming.get(to).add(road);

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
    public void addTurn(Road.Lane from, Road.Lane to, String turnDirection){
        var middle = this.roadEnds.get(from.road());
        var middle_check = this.roadStarts.get(to.road());
        if (middle == null || middle != middle_check){
            throw new RuntimeException("Roads aren't connected at intersection");
        }

        middle.addTurn(from, to, turnDirection);
    }

    /** A simulation tick for the entire map
     *
     * @param sim   The simulation this map is apart of
     * @param delta The simulation delta in seconds
     */
    public void tick(Simulation sim, float delta){
        for(var intersection : intersections){
            intersection.tick(sim, delta);
        }
        for(var road : roads){
            road.tick(sim, delta);
        }
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

    /** Writes the this map (not including vehicles) to the output stream
     *
     * @param out   The place we want to output to
     * @throws IOException
     */
    public void write(OutputStreamWriter out) throws IOException {
        out.write("# type \\t  id  \\t   name  \t   x  \\t   \\y   \\t  kind \\t ...\n");
        var encountered = new HashMap<String, Integer>();
        var intersection_id_map = new HashMap<Intersection, String>();
        for(var intersection : intersections){
            out.write("intersection\t");
            int num;
            if (encountered.containsKey(intersection.getName())){
                num = encountered.get(intersection.getName())+1;
                encountered.put(intersection.getName(), num);
            }else{
                num = 0;
                encountered.put(intersection.getName(), 0);
            }
            out.write(intersection.getName()+num);
            intersection_id_map.put(intersection, intersection.getName()+num);
            out.write("\t");
            out.write(intersection.getName());
            out.write("\t");
            out.write(intersection.getX()+"");
            out.write("\t");
            out.write(intersection.getY()+"");
            out.write("\t");
            if (intersection instanceof DrainIntersection){
                out.write("Drain");
            }else if (intersection instanceof SourceIntersection){
                out.write("Source");
            }else if(intersection instanceof TimedIntersection){
                out.write("Timed");
            }else{
                out.write("Default");
            }
            out.write('\n');
        }
        out.write('\n');

        encountered.clear();
        var road_id_map = new HashMap<Road, String>();
        out.write("# type \\t  id  \\t  name \\t lanes  \\t  inter_id_from \\t inter_id_to\n");
        for(var road : roads){
            out.write("road\t");
            int num;
            if (encountered.containsKey(road.getName())){
                num = encountered.get(road.getName())+1;
                encountered.put(road.getName(), num);
            }else{
                num = 0;
                encountered.put(road.getName(), 0);
            }
            out.write(road.getName()+num);
            road_id_map.put(road, road.getName()+num);
            out.write("\t");
            out.write(road.getName());
            out.write("\t");
            out.write(road.getNumLanes()+"");
            out.write("\t");
            out.write(intersection_id_map.get(this.roadStarts.get(road)));
            out.write("\t");
            out.write(intersection_id_map.get(this.roadEnds.get(road)));
            out.write('\n');
        }
        out.write('\n');


        out.write("# type \\t  from_road_id \\t from_road_lane \\t name \\t to_road_id \\t to_road_lane\n");

        for(var intersection : intersections){
            for(var turns : intersection.getAllTurns().entrySet()){
                for(var turn : turns.getValue()){
                    out.write("turn\t");
                    out.write(road_id_map.get(turns.getKey().road()));
                    out.write("\t"+turns.getKey().getLane()+"\t");
                    out.write(turn.getName()+"\t");
                    out.write(road_id_map.get(turn.getLane().road()));
                    out.write("\t"+turn.getLane().getLane());
                    out.write('\n');
                }
            }
        }

        out.flush();
    }

    /** Reads the map from the input provided filling out this map with its contents
     *
     * @param in    The place we want to read from
     */
    public void read(Scanner in) {
        var intersection_id_map = new HashMap<String, Intersection>();
        var road_id_map = new HashMap<String, Road>();
        while(in.hasNext()){
            var line = in.nextLine().trim();

            if(line.startsWith("#") || line.isEmpty()) continue;
            var split = line.split("\t", 2);
            var kind = split[0];
            switch (kind) {
                case "intersection" -> {
                    split = split[1].split("\t", 6);
                    var name = split[1];
                    float x = Float.parseFloat(split[2]);
                    float y = Float.parseFloat(split[3]);
                    Intersection intersection = null;
                    switch (split[4]) {
                        case "Default" -> intersection = new Intersection(name, x, y);
                        case "Source" -> intersection = new SourceIntersection(name, x, y);
                        case "Drain" -> intersection = new DrainIntersection(name, x, y);
                        case "Timed" -> intersection = new TimedIntersection(name, x, y);
                    }
                    intersection_id_map.put(split[0], intersection);
                    this.addIntersection(intersection);
                }
                case "road" -> {
                    split = split[1].split("\t", 5);
                    var id = split[0];
                    var name = split[1];
                    int lanes = Integer.parseInt(split[2]);
                    var from = intersection_id_map.get(split[3]);
                    var to = intersection_id_map.get(split[4]);
                    var road = this.linkIntersection(from, to, name, lanes);
                    road_id_map.put(id, road);
                }
                case "turn" -> {
                    split = split[1].split("\t", 5);
                    var from = road_id_map.get(split[0]);
                    var from_lane = Integer.parseInt(split[1]);
                    var name = split[2];
                    var to = road_id_map.get(split[3]);
                    var to_lane = Integer.parseInt(split[4]);
                    this.addTurn(from.getLane(from_lane), to.getLane(to_lane), name);
                }
            }
        }
    }
}
