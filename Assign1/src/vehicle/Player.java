package vehicle;

import io.View;
import io.Input;
import map.Intersection;
import map.Map;
import map.Road;

import java.awt.*;
import java.util.ArrayList;

public class Player extends Vehicle{

    private final Input input;

    public Player(Input input){
        super();
        this.speedPercentage = 1.0f;
        this.input = input;
    }



    @Override
    public void tick(Map map, Road road, float delta) {
        if (this.input.keyHeld('i')){
            this.speedPercentage += delta * 0.1;
        }
        if (this.input.keyHeld('k')){
            this.speedPercentage -= delta * 0.1;
        }
        this.speedPercentage = Float.min(this.speedPercentage, 1.5f);
        this.speedPercentage = Float.max(this.speedPercentage, 0);
    }

    @Override
    public void draw(View g, float x, float y, float nx, float ny) {
        g.setColor(Color.MAGENTA);
        g.fillOval(x-nx*0.3f,y-ny*0.3f, 0.6f, 0.6f);
        g.drawOval(x-nx*getSize()+nx*0.25f,y-ny*getSize()+ny*0.25f, 0.5f, 0.5f);
        g.setColor(Color.WHITE);
        g.drawString("Player", x,y);
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
