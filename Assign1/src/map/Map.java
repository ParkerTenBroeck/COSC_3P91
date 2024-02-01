package map;

import io.Display;
import vehicle.Player;
import vehicle.Vehicle;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Map {
    ArrayList<Road> roads = new ArrayList<>();

    HashMap<Road, Intersection> roadEnds = new HashMap<>();
    HashMap<Road, Intersection> roadStarts = new HashMap<>();
    HashMap<String, Intersection> intersectionNames = new HashMap<>();
    HashMap<String, Road> connections = new HashMap<>();

    public Intersection addIntersection(String name, double x, double y){
        var intersection = new Intersection(name, x, y);
        var old = intersectionNames.put(name, intersection);
        assert old == null;
        return intersection;
    }

    public Road linkIntersection(Intersection from, Intersection to, int lanes){
        var road = new Road(lanes, from.distance(to));
        roads.add(road);
        roadEnds.put(road, to);
        roadStarts.put(road, from);
        var old = connections.put(from + "@" + to, road);
        assert old == null;
        return road;
    }

    public void addTurn(Road from, Road to, String turnDirection){
        var middle = this.roadEnds.get(from);
        var middle_check = this.roadStarts.get(to);
        if (middle != middle_check){
            throw new RuntimeException("Roads aren't connected at intersection");
        }

        if (!middle.turns.containsKey(from)){
            middle.turns.put(from, new ArrayList<>());
        }
        middle.turns.get(from).add(new Intersection.Turn(turnDirection, to));
    }

    public void addTurn(String from, String through, String to, String turnDirection){
        var middle = intersectionNames.get(through);
        var from_road = connections.get(from + "@" + through);
        var to_road = connections.get(through + "@" + to);

        if (!middle.turns.containsKey(from_road)){
            middle.turns.put(from_road, new ArrayList<>());
        }
        middle.turns.get(from_road).add(new Intersection.Turn(turnDirection, to_road));
    }

    public void init(Player player){
        // testing
        this.roads.get(0).lanes[0].add(player);
        this.roads.get(0).lanes[0].add(new Vehicle());
        this.roads.get(0).lanes[0].add(new Vehicle());
        this.roads.get(0).lanes[0].add(new Vehicle());
        this.roads.get(0).lanes[0].add(new Vehicle());
        this.roads.get(0).lanes[0].add(new Vehicle());
        this.roads.get(0).lanes[0].add(new Vehicle());
        this.roads.get(0).lanes[0].add(new Vehicle());
        this.roads.get(0).lanes[0].add(new Vehicle());
        this.roads.get(0).lanes[0].add(new Vehicle());
        this.roads.get(0).lanes[0].add(new Vehicle());
    }

    public void tick(double delta){
        for(var road : roads){
            road.tick(this, delta);
        }
    }

    public void draw(Display display, double x, double y, double zoom){
        Graphics g = display.getGraphics();

        g.setColor(Color.RED);
        for(var intersection : this.intersectionNames.values()){
            g.fillOval((int)((x+intersection.x - 0.5)*zoom), (int)((y+intersection.y - 0.5)*zoom), (int)(1*zoom), (int)(1*zoom));
        }

        for(var road : roads){
            var start = this.roadStarts.get(road);
            var end = this.roadEnds.get(road);
            g.setColor(Color.GREEN);
            g.drawLine((int)((start.x+x)*zoom), (int)((start.y+y)*zoom), (int)((end.x+x)*zoom), (int)((end.y+y)*zoom));

            for(var vehicle : road.lanes[0]){
                var position = carPosition(start, end, road, vehicle);
                vehicle.draw(g, position[0], position[1], x, y, zoom);
            }
        }
    }

    double[] carPosition(Intersection from, Intersection to, Road road, Vehicle vehicle){
        double percent = vehicle.getPosition() / road.length;
        return new double[] {from.x * (1-percent) + to.x*(percent), from.y * (1-percent) + to.y*(percent)};
    }
}
