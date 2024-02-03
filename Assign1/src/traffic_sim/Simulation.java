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
    private float simulationMultiplier = 1.0f;
    private final float maxDeltaTick = 0.1f/5f;
    private long targetFrameTime = 16666667;
    private int simTick = 0;
    private long simNanos = 0;

    private final ArrayList<SimSystem> systems = new ArrayList<>();
    private float systemsTime;

    public Simulation(RoadMap map){
        this.map = map;
        this.input = new Input();
        this.display = new Display(input);

        this.view = new View(display, input);

        this.addSystem((sim, delta) -> {
            if (sim.getPaused()){
                map.tick(sim, 0.0f);
            }else{
                delta *= simulationMultiplier;
                var num = (int)Math.ceil(delta/maxDeltaTick);
                var r_tick = delta/num;
                for(int i = 0; i < num; i ++){
                    map.tick(sim, r_tick);
                    simTick++;
                    simNanos += (long) (r_tick*1e9);
                }
            }
        });

        this.addSystem((sim, _delta) -> {
            this.view.setStroke(0.08f);
            view.setColor(Color.BLACK);
            view.clearScreen();
            map.draw(sim);

            if (this.view.getDebug()){
                view.setColor(Color.WHITE);

                view.drawStringHud("X: " + view.panX, 10,10);
                view.drawStringHud("Y: " + view.panY, 10,20);
                view.drawStringHud("Zoom: " + view.zoom, 10,30);
                view.drawStringHud("SimMultiplier: " + this.simulationMultiplier, 10,40);
                view.drawStringHud("Paused: " + this.pause, 10,50);
                view.drawStringHud("Tick: " + this.simTick, 10,60);
                view.drawStringHud("SimTime: " + this.simNanos*1e-9 + "s", 10,70);
                view.drawStringHud("FrameTime: " + this.trueDelta + "s", 10,80);
                view.drawStringHud("SystemsTime: " + this.systemsTime + "s", 10,90);
                view.drawStringHud("TicksPerFrame: " + (int)Math.ceil(_delta*simulationMultiplier/maxDeltaTick)*1f/this.trueDelta, 10,100);
            }

            view.update();
        });
    }


    public float getSimulationMultiplier() {
        return simulationMultiplier;
    }

    public void setSimulationMultiplier(float simulationMultiplier) {
        this.simulationMultiplier = Math.max(simulationMultiplier, 0.0f);
    }

    public int getSimTick() {
        return simTick;
    }

    public long getSimNanos() {
        return simNanos;
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
            var delta = 0.1f;
            for(int i = 0; i < this.systems.size(); i ++){
                this.systems.get(i).run(this, delta);
            }

            var now = System.nanoTime();
            this.systemsTime = (now - start) / 1_000_000_000f;
            try{
                Thread.sleep((targetFrameTime - (now - start))/1_000_000, (int) ((targetFrameTime - (now - start))%1_000_000));
            }catch (Exception ignore){
            }
            now = System.nanoTime();
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
