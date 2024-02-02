package traffic_sim.vehicle;

import traffic_sim.Simulation;
import traffic_sim.vehicle.controller.Controller;
import traffic_sim.vehicle.controller.RandomTurnController;

import java.awt.*;

public class Truck extends Vehicle{
    private final Color color;

    public Truck() {
        this(new RandomTurnController(), Color.CYAN);
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
    public void draw(Simulation sim, float x, float y, float nx, float ny) {
        var g = sim.getView();
        g.setColor(this.color);
        float width = 0.6f/2;
        var x0 = x+ny*width;
        var y0 = y-nx*width;
        var x1 = x-ny*width;
        var y1 = y+nx*width;
        var x2 = x0-nx*getSize();
        var y2 = y0-ny*getSize();
        var x3 = x1-nx*getSize();
        var y3 = y1-ny*getSize();
        g.drawLine(x0,y0,x1,y1);
        g.drawLine(x2,y2,x3,y3);
        g.drawLine(x1,y1,x3,y3);
        g.drawLine(x2,y2,x0,y0);
        super.draw(sim,x,y,nx,ny);
    }
}
