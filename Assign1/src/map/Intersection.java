package map;

import java.util.ArrayList;
import java.util.HashMap;

public class Intersection {

    double x;
    double y;
    String name;

    HashMap<Road, ArrayList<Turn>> turns = new HashMap<>();


    public Intersection(String name, double x, double y){
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public double distance(Intersection other){
        var xdiff = this.x - other.x;
        var ydiff = this.y - other.y;
        return Math.sqrt(xdiff*xdiff + ydiff*ydiff);
    }


    public static class Turn{
        String name;
        Road road;

        public Turn(String name, Road road){
            this.name = name;
            this.road = road;
        }

        public String getName() {
            return this.name;
        }
    }
}
