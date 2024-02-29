package traffic_sim.vehicle;

import traffic_sim.Simulation;
import traffic_sim.vehicle.controller.Controller;
import traffic_sim.vehicle.controller.RandomController;

import java.awt.*;

/**
 * A Car Vehicle.
 */
public class Car extends Vehicle{
    private final Color color;

    public Car() {
        this(new RandomController(), Color.BLUE);
    }

    public Car(Controller controller) {
        this(controller, Color.BLUE);
    }

    public Car(Controller controller, Color color) {
        super(controller, (float) (Math.random() * 0.4 + 1.0));
        super.speedMultiplier = (float) (Math.random() * 0.4 + 1.0-0.2);
        this.color = color;
    }

    @Override
    public void draw(Simulation sim, float x, float y, float dx, float dy) {
        var g = sim.getView();
        g.setColor(this.color);
        g.fillOval(x- dx *0.3f,y- dy *0.3f, 0.6f, 0.6f);
        g.drawOval(x- dx *getSize()+ dx *0.25f,y- dy *getSize()+ dy *0.25f, 0.5f, 0.5f);
        super.draw(sim,x,y, dx, dy);
    }
}
