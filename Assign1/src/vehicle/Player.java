package vehicle;

import io.Input;
import map.Intersection;
import map.Map;
import map.Road;

import java.awt.*;
import java.util.ArrayList;

public class Player extends Vehicle{

    private final Input input;
    private double lastX;
    private double lastY;
    private boolean alive = true;

    public Player(Input input){
        super();
        this.speedPercentage = 1.0;
        this.input = input;
    }

    public boolean isAlive(){
        return this.alive;
    }

    public double getX(){
        return this.lastX;
    }

    public double getY(){
        return this.lastY;
    }

    @Override
    public void tick(Map map, Road road, double delta) {
        var position = map.carPosition(road, this);
        this.lastX = position[0];
        this.lastY = position[1];
        if (this.input.keyHeld('i')){
            this.speedPercentage += delta * 0.1;
        }
        if (this.input.keyHeld('k')){
            this.speedPercentage -= delta * 0.1;
        }
        this.speedPercentage = Double.min(this.speedPercentage, 1.5);
        this.speedPercentage = Double.max(this.speedPercentage, 0);
    }

    @Override
    public void destroy() {
        this.alive = false;
    }

    public void revive() {
        this.alive = true;
    }

    @Override
    public void draw(Graphics g, double x, double y, double nx, double ny, double zoom) {
        g.setColor(Color.MAGENTA);
        g.fillOval((int)(x+(-0.3-nx*0.3)*zoom),(int)(y+(-0.3-ny*0.3)*zoom), (int)(0.6*zoom), (int)(0.6*zoom));
        g.drawOval((int)(x+(-0.25-nx*getSize()+nx*0.25)*zoom),(int)(y+(-0.25-ny*getSize()+ny*0.25)*zoom), (int)(0.5*zoom), (int)(0.5*zoom));
    }

    @Override
    public Intersection.Turn chooseTurn(ArrayList<Intersection.Turn> turns){
        if (turns == null || turns.isEmpty()) {
            return null;
        }else{
            var random = (int)(Math.random() * turns.size());
            return turns.get(random);
        }
    }
}
