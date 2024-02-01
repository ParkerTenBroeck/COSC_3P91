package traffic_sim.vehicle;

import traffic_sim.io.View;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.map.Map;
import traffic_sim.map.Road;

import java.awt.*;
import java.util.ArrayList;

public class Vehicle {

    private static int count = 0;
    private final int m_count = count++;
    private float health = 1.0f;
    protected float reputation = 1.0f;
    private float distanceAlongRoad = 0.0f;
    private float lastX;
    private float lastY;
    private boolean onRoad;

    protected float speedPercentage = (float) (Math.random() * 0.2 + 1.0);
    private final float size = (float) (Math.random() * 0.2 + 1.0);


    public float getReputation() { return this.reputation; }

    public float getHealth() { return this.health; }

    public float getSpeedPercentage() { return this.speedPercentage; }

    public float getSize() {
        return size;
    }

    public float getDistanceAlongRoad() {
        return distanceAlongRoad;
    }

    public void setDistanceAlongRoad(float distanceAlongRoad){
        this.distanceAlongRoad = distanceAlongRoad;
    }

    public void tick(Map map, Road road, float delta) {}

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

    public void putOnRoad(Road rode){
        this.onRoad = true;
    }
    public void removeFromRoad(){
        this.onRoad = false;
    }

    public boolean isOnRoad(){
        return this.onRoad;
    }

    public void draw(View g, float x, float y, float nx, float ny) {
        g.setColor(Color.BLUE);
        g.fillOval(x-nx*0.3f,y-ny*0.3f, 0.6f, 0.6f);
        g.drawOval(x-nx*getSize()+nx*0.25f,y-ny*getSize()+ny*0.25f, 0.5f, 0.5f);
        g.setColor(Color.WHITE);
        g.drawString(m_count+"", x,y);
    }

    public void updatePosition(float x, float y){
        this.lastX = x;
        this.lastY = y;
    }

    public float getLastX(){
        return this.lastX;
    }

    public float getLastY(){
        return this.lastY;
    }
}
