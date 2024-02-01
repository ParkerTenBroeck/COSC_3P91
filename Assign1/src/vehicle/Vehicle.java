package vehicle;

import map.Intersection;
import map.Map;
import map.Road;

import java.awt.*;
import java.util.ArrayList;

public class Vehicle {

    private static int count = 0;
    private final int m_count = count++;
    private double health = 1.0;
    protected double reputation = 1.0;
    private double position = 0.0;
    protected double speedPercentage = Math.random() * 0.2 + 1.0;
    private final double size = Math.random() * 0.2 + 1.0;


    public double getReputation() { return this.reputation; }

    public double getHealth() { return this.health; }

    public double getSpeedPercentage() { return this.speedPercentage; }

    public double getSize() {
        return size;
    }

    public double getPosition() {
        return position;
    }

    public void setPosition(double position){
        this.position = position;
    }

    public void tick(Map map, Road road, double delta) {}

        public Intersection.Turn chooseTurn(ArrayList<Intersection.Turn> turns){
        if (turns == null || turns.isEmpty()) {
            return null;
        }else{
            var random = (int)(Math.random() * turns.size());
            if (turns.get(random).canFit(this)){
                return turns.get(random);
            }else{
                return null;
            }
        }
    }

    public void destroy(){}

    public void draw(Graphics g, double x, double y, double nx, double ny, double zoom) {
        g.setColor(Color.BLUE);
        g.fillOval((int)(x+(-0.3-nx*0.3)*zoom),(int)(y+(-0.3-ny*0.3)*zoom), (int)(0.6*zoom), (int)(0.6*zoom));
        g.drawOval((int)(x+(-0.25-nx*getSize()+nx*0.25)*zoom),(int)(y+(-0.25-ny*getSize()+ny*0.25)*zoom), (int)(0.5*zoom), (int)(0.5*zoom));
        g.setColor(Color.WHITE);
        g.drawString(m_count+"", (int)(x),(int)(y));
    }
}
