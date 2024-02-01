package traffic_sim;

import traffic_sim.io.Display;
import traffic_sim.io.Input;
import traffic_sim.io.View;
import traffic_sim.map.RoadMap;

import java.awt.*;
import java.util.ArrayList;

public class Simulation implements Runnable{
    private Display display;
    private Input input;
    private View view;
    private RoadMap map;

    private float trueDelta = 0.1f;

    private boolean pause = false;

    private ArrayList<SimSystem> systems = new ArrayList<>();

    public Simulation(RoadMap map){
        this.map = map;
        this.input = new Input();
        this.display = new Display(input);

        this.view = new View(display, input);

        this.addSystem((sim, delta) -> {
            if (delta == 0.0f){
                map.tick(delta);
            }else{
                var num = 5;
                var r_tick = delta/num;
                for(int i = 0; i < num; i ++){
                    map.tick(r_tick);
                }
            }
        });

        this.addSystem((sim, _delta) -> {
            view.setColor(Color.BLACK);
            view.clearScreen();
            map.draw(view);

            view.setColor(Color.WHITE);
            view.test();

            view.drawStringHud("X: " + view.panX, 10,10);
            view.drawStringHud("Y: " + view.panY, 10,20);
            view.drawStringHud("Zoom: " + view.zoom, 10,30);

            view.update();
        });
    }

    public Input getInput() {
        return this.input;
    }

    public Display getDisplay(){
        return this.display;
    }

    public View getView(){
        return this.view;
    }

    public boolean getPaused(){
        return this.pause;
    }

    public void addSystem(SimSystem runnable){
        this.systems.add(runnable);
    }

    public float getTrueDelta(){
        return trueDelta;
    }

    @Override
    public void run(){
        long start = System.nanoTime();
        while(true){
            var delta = this.getPaused() ? 0.0f : 0.1f;
            for(int i = 0; i < this.systems.size(); i ++){
                this.systems.get(i).run(this, delta);
            }

            try{
                Thread.sleep(16);
            }catch (Exception ignore){
            }
            var now = System.nanoTime();
            this.trueDelta = (now - start) / 1_000_000_000f;
            start = now;
        }
    }

    public void setPaused(boolean paused) {
        this.pause = paused;
    }

    public interface SimSystem {
        void run(Simulation sim, float delta);
    }
}
