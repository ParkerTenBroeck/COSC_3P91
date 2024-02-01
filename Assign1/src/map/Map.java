package map;

import io.Display;
import vehicle.Player;
import vehicle.Vehicle;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Map {
    private final ArrayList<Road> roads = new ArrayList<>();
    private final ArrayList<Intersection> intersectionNames = new ArrayList<>();


    private final HashMap<Road, Intersection> roadEnds = new HashMap<>();
    private final HashMap<Road, Intersection> roadStarts = new HashMap<>();
    private final HashMap<Intersection, ArrayList<Road>> outgoing = new HashMap<>();
    private final HashMap<Intersection, ArrayList<Road>> incoming = new HashMap<>();



    public Intersection addIntersection(String name, double x, double y){
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

    public void init(Player player){
        this.roads.get(0).getLane(0).vehicles.add(player);
    }

    public void tick(double delta){
        for(var intersection : intersectionNames){
            intersection.tick(this, delta);
        }
        for(var road : roads){
            road.tick(this, delta);
        }
    }

    public void draw(Display display, double x, double y, double zoom){
        Graphics g = display.getGraphics();

        g.setColor(Color.RED);
        for(var intersection : this.intersectionNames){
            g.fillOval((int)((x+intersection.getX() - 1)*zoom), (int)((y+intersection.getY() - 1)*zoom), (int)(2*zoom), (int)(2*zoom));
        }
        g.setColor(Color.WHITE);
        for(var intersection : this.intersectionNames){
            g.drawString(intersection.getName(), (int)((x+intersection.getX() - 1)*zoom), (int)((y+intersection.getY() - 1)*zoom));
        }

        for(var road : roads){
            road.draw(g, this, x, y, zoom);
        }

    }

    public double[] carPosition(Road road, Vehicle vehicle) {
        return this.carPosition(this.roadStarts.get(road), this.roadEnds.get(road), road, vehicle);
    }
    public double[] carPosition(Intersection from, Intersection to, Road road, Vehicle vehicle){
        double percent = vehicle.getPosition() / road.getLength();
        return new double[] {from.getX() * (1-percent) + to.getX()*(percent), from.getY() * (1-percent) + to.getY()*(percent)};
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
