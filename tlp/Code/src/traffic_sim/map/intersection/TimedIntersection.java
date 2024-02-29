package traffic_sim.map.intersection;

import traffic_sim.Simulation;
import traffic_sim.io.Display;

import java.awt.*;

/**
 * An Intersection that will enable/disable certain turns depending on a timer. Like traffic lights
 */
public class TimedIntersection extends Intersection {
    public TimedIntersection(String name, float x, float y) {
        super(name, x, y);
    }

    @Override
    public void tick(Simulation sim, double delta) {
        for (var from : this.turns.values()){
            for(var turn : from){
                turn.enabled = false;
            }
        }

        float xn = (float) Math.sin(sim.getSimNanos() / 10000000000.0);
        float yn = (float) Math.cos(sim.getSimNanos() / 10000000000.0);

        for (var in : sim.getMap().incoming(this)){

            float closeness = 0.4f;
            if (
                    (Math.abs(in.getDirectionX()-xn) < closeness && Math.abs(in.getDirectionY()-yn) < closeness)
                | (Math.abs(in.getDirectionX()+xn) < closeness && Math.abs(in.getDirectionY()+yn) < closeness)
            )
                for(var lane : in.getLanes())
                    if(this.turns.containsKey(lane))
                        for(var turn : this.turns.get(lane))
                            turn.enabled = true;
        }

        super.tick(sim, delta);
    }

    @Override
    public void draw(Simulation sim) {
        super.draw(sim);
        sim.getView().setLayer(Display.Layer.TopLevel);
        sim.getView().setColor(Color.GREEN);
        float xn = (float) Math.sin(sim.getSimNanos() / 10000000000.0);
        float yn = (float) Math.cos(sim.getSimNanos() / 10000000000.0);
        sim.getView().drawLine(x+xn, y+yn, x-xn, y-yn);

        for (var in : sim.getMap().incoming(this)){


            float closeness = 0.4f;
            if (
                    (Math.abs(in.getDirectionX()-xn) < closeness && Math.abs(in.getDirectionY()-yn) < closeness)
                            | (Math.abs(in.getDirectionX()+xn) < closeness && Math.abs(in.getDirectionY()+yn) < closeness)
            ){
                sim.getView().setColor(Color.GREEN);
            }else{
                sim.getView().setColor(Color.RED);
            }
            sim.getView().fillOval(this.x-in.getDirectionX(), this.y-in.getDirectionY(), 0.4f, 0.4f);

            var old = sim.getView().getStroke();
            sim.getView().setStroke(0.04f);
            sim.getView().setColor(Color.WHITE);
            sim.getView().drawOval(this.x-in.getDirectionX(), this.y-in.getDirectionY(), 0.4f, 0.4f);
            sim.getView().setStroke(old);
        }
    }
}
