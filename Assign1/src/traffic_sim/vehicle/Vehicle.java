package traffic_sim.vehicle;

import traffic_sim.io.View;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.map.RoadMap;
import traffic_sim.map.Road;
import traffic_sim.vehicle.controller.Controller;

import java.awt.*;
import java.util.ArrayList;

public abstract class Vehicle {
    private static int count = 0;
    protected final int m_count = count++;

    private float health = 1.0f;
    protected float reputation = 1.0f;
    private float distanceAlongRoad = 0.0f;
    private float lastX;
    private float lastY;
    private boolean onRoad;
    private Controller controller;

    protected float speedMultiplier;
    private final float size;

    public Vehicle(Controller controller, float size){
        this.size = size;
        this.controller = controller;
    }

    public void setController(Controller controller){
        this.controller = controller;
    }
    public float getReputation() { return this.reputation; }

    public float getHealth() { return this.health; }
    public boolean isAlive() { return this.health > 0; }

    public float getSpeedMultiplier() { return this.speedMultiplier; }
    public void setSpeedMultiplier(float speedMultiplier) { this.speedMultiplier = speedMultiplier; }

    public float getSize() {
        return size;
    }

    public float getDistanceAlongRoad() {
        return distanceAlongRoad;
    }

    public void setDistanceAlongRoad(float distanceAlongRoad){
        this.distanceAlongRoad = distanceAlongRoad;
    }

    public void tick(RoadMap map, Road road, float delta) {
        if(controller != null) controller.tick(this, map, road, delta);
    }

    public Intersection.Turn chooseTurn(ArrayList<Intersection.Turn> turns){
        if(controller != null) return controller.chooseTurn(this, turns);
        return null;
    }

    public int changeLane(RoadMap map, Road.Lane lane){
        if(controller != null) return controller.laneChange(this, map, lane);
        return 0;
    }

    public void putInLane(Road.Lane rode){
        this.onRoad = true;
    }
    public void removeFromRoad(){
        this.onRoad = false;
    }

    public boolean isOnRoad(){
        return this.onRoad;
    }

    public void draw(View g, float x, float y, float nx, float ny){
        if (g.getDebug()){
            g.setColor(Color.WHITE);
            g.drawString(m_count+"", x,y);
        }
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
