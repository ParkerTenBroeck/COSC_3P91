package traffic_sim.vehicle;

import traffic_sim.Simulation;

import traffic_sim.map.Road;
import traffic_sim.vehicle.controller.Controller;
import traffic_sim.vehicle.controller.RandomController;

import java.awt.*;

/**
 * A Truck Vehicle, normally larger and slower than other vehicles
 */
public class Truck extends Vehicle{
    private final Color color;
    private boolean slow = false;

    public Truck() {
        this(new RandomController(), Color.CYAN);
    }

    public Truck(Controller controller) {
        this(controller, Color.CYAN);
    }

    public Truck(Controller controller, Color color) {
        super(controller, (float) (Math.random() * 1 + 3.0));
        super.speedMultiplier = (float) (Math.random() * 0.4 + 0.4-0.2);
        this.color = color;
    }

    @Override
    public void tick(Simulation sim, Road.Lane lane, int laneIndex, boolean changedLanes, float delta) {
        super.tick(sim, lane, laneIndex, changedLanes, delta);
    }

    @Override
    public float getSpeedMultiplier() {
        return this.slow ? 0.01f : super.getSpeedMultiplier();
    }


    @Override
    public Road.LaneChangeDecision changeLane(Simulation sim, Road.Lane lane, int current_index, int left_vehicle_back_index, int right_vehicle_back_index) {
        if (!lane.leftmost){
            return Road.LaneChangeDecision.WaitLeft;
        }else{
            return Road.LaneChangeDecision.Nothing;
        }
    }

    @Override
    public void draw(Simulation sim, float x, float y, float dx, float dy) {
        var g = sim.getView();
        g.setColor(this.color);
        float width = 0.6f/2;
        var x0 = x+ dy *width;
        var y0 = y- dx *width;
        var x1 = x- dy *width;
        var y1 = y+ dx *width;
        var x2 = x0- dx *getSize();
        var y2 = y0- dy *getSize();
        var x3 = x1- dx *getSize();
        var y3 = y1- dy *getSize();
        g.drawLine(x0,y0,x1,y1);
        g.drawLine(x2,y2,x3,y3);
        g.drawLine(x1,y1,x3,y3);
        g.drawLine(x2,y2,x0,y0);
        super.draw(sim,x,y, dx, dy);
    }
}
