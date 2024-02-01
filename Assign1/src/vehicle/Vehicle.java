package vehicle;

import map.Intersection;

import java.awt.*;
import java.util.ArrayList;

public class Vehicle {

    double health = 1.0;
    double reputation = 1.0;
    double position = 0.0;
    double speedPercentage = Math.random() * 0.2 + 1.0;

    public double getReputation() { return this.reputation; }

    public double getHealth() { return this.health; }

    public double getSpeedPercentage() { return this.speedPercentage; }

    public void tick(double delta){}
    public Intersection.Turn chooseTurn(ArrayList<Intersection.Turn> turns){
        if (turns == null || turns.size() == 0) {
            return null;
        }else{
            var random = (int)(Math.random() * turns.size());
            return turns.get(random);
        }
    }

    public void draw(Graphics g, double x, double y, double panX, double panY, double zoom) {
        g.setColor(Color.BLUE);
        g.drawOval((int)((x+panX - 0.5)*zoom), (int)((y+panY - 0.5)*zoom), (int)(1*zoom), (int)(1*zoom));
    }

    public double getSize() {
        return 1.0;
    }

    public double getPosition() {
        return position;
    }

    public void setPosition(double position){
        this.position = position;
    }
}
