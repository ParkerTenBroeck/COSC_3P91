package traffic_sim.vehicle;

import traffic_sim.io.View;
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
        super(controller, (float) (Math.random() * 0.4 + 1.0));
        super.speedMultiplier = (float) (Math.random() * 0.4 + 1.0-0.2);
        this.color = color;
    }

    @Override
    public void draw(View g, float x, float y, float nx, float ny) {
        g.setColor(this.color);
        g.fillOval(x-nx*0.3f,y-ny*0.3f, 0.6f, 0.6f);
        g.drawOval(x-nx*getSize()+nx*0.25f,y-ny*getSize()+ny*0.25f, 0.5f, 0.5f);
        g.setColor(Color.WHITE);
        g.drawString(super.m_count+"", x,y);
        super.draw(g,x,y,nx,ny);
    }
}
