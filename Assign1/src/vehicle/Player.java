package vehicle;

import io.Input;
import map.Intersection;

import java.awt.*;
import java.util.ArrayList;

public class Player extends Vehicle{

    Input input;

    public Player(Input input){
        super();
        this.speedPercentage = 0.0;
        this.input = input;
    }

    @Override
    public void tick(double delta) {
        if (this.input.keyHeld('i')){
            this.speedPercentage += delta * 0.1;
        }
        if (this.input.keyHeld('k')){
            this.speedPercentage -= delta * 0.1;
        }
        this.speedPercentage = Double.min(this.speedPercentage, 1.5);
        this.speedPercentage = Double.max(this.speedPercentage, 0);
        super.tick(delta);
    }

    @Override
    public void draw(Graphics g, double x, double y, double panX, double panY, double zoom) {
        g.setColor(Color.MAGENTA);
        g.fillOval((int)((x+panX - 0.5)*zoom), (int)((y+panY - 0.5)*zoom), (int)(1*zoom), (int)(1*zoom));
    }

    @Override
    public Intersection.Turn chooseTurn(ArrayList<Intersection.Turn> turns){
        if (turns == null || turns.size() == 0) {
            return null;
        }else{
            var random = (int)(Math.random() * turns.size());
            return turns.get(random);
        }
    }
}
