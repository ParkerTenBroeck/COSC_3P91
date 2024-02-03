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

public class RoadMap {
    private final ArrayList<Road> roads = new ArrayList<>();
    private final ArrayList<Intersection> intersections = new ArrayList<>();


    private final HashMap<Road, Intersection> roadEnds = new HashMap<>();
    private final HashMap<Road, Intersection> roadStarts = new HashMap<>();
    private final HashMap<Intersection, ArrayList<Road>> outgoing = new HashMap<>();
    private final HashMap<Intersection, ArrayList<Road>> incoming = new HashMap<>();



    public Intersection addIntersection(String name, float x, float y){
        var intersection = new Intersection(name, x, y);
        return addIntersection(intersection);
    }

    public Intersection addIntersection(Intersection intersection){
        intersections.add(intersection);
        outgoing.put(intersection, new ArrayList<>());
        incoming.put(intersection, new ArrayList<>());
        return intersection;
    }

    public Road getLinked(Intersection from, Intersection to){
        for(var incoming : incoming.get(to)){
            for(var outgoing : outgoing.get(from)){
                if (incoming == outgoing)
                    return incoming;
            }
        }
        return null;
    }

    public Road linkIntersection(Intersection from, Intersection to, String name, int lanes){

        if(this.getLinked(from, to) != null)
            throw  new RuntimeException("Roads already linked");

        var road = new Road(name, lanes, from, to);
        roads.add(road);
        roadEnds.put(road, to);
        roadStarts.put(road, from);

        outgoing.get(from).add(road);
        incoming.get(to).add(road);

        road.changedPosition(this);
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

    public void tick(Simulation sim, float delta){
        for(var intersection : intersections){
            intersection.tick(sim, this, delta);
        }
        for(var road : roads){
            road.tick(sim, delta);
        }
    }

    public void draw(Simulation sim){
        for(var intersection : this.intersections){
            intersection.draw(sim, this);
        }
        for(var road : roads){
            road.draw(sim, this);
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

    public ArrayList<Intersection> getIntersections() {
        return this.intersections;
    }
}
