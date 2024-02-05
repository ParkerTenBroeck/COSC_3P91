package traffic_sim;

import traffic_sim.io.Display;
import traffic_sim.io.Input;
import traffic_sim.io.View;
import traffic_sim.map.RoadMap;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

/*UML_RAW_OUTER hide Runnable*/
public class Simulation implements Runnable{
    /*UML_RAW_OUTER Simulation "1" *-- "1" View: simulation contains a view into the world*/
    private final View view;
    /*UML_RAW_OUTER Simulation "1" *-- "1" RoadMap: simulation contains a map*/
    private RoadMap map;

    private float trueDelta = 0.1f;

    private boolean pause = false;
    private boolean debug = true;
    private float simulationMultiplier = 1.0f;
    private final float maxDeltaTick = 0.1f/5f;
    private long targetFrameTime = 33333333;//16666667;
    private int simTick = 0;
    private long simNanos = 0;

    /*UML_RAW_OUTER Simulation "1" *-- "n" SimSystem: simulation contains many systems*/
    private final ArrayList<SimSystem> systems = new ArrayList<>();
    private float systemsTime;

    public Simulation(RoadMap map){
        this.map = map;

        this.view = new View();

        this.addSystem(new SimSystem(1) {
            @Override
            public void init(Simulation sim) {}

            @Override
            public void run(Simulation sim, float delta) {
                sim.view.getInput().update();
                sim.view.setDefaultStroke(0.08f);
                view.clear();
            }
        });

        this.addSystem(new SimSystem(50) {
            @Override
            public void init(Simulation sim) {}

            @Override
            public void run(Simulation sim, float delta) {
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
            }
        });

        this.addSystem(new SimSystem(1000) {
            @Override
            public void init(Simulation sim) {}

            @Override
            public void run(Simulation sim, float delta) {
                map.draw(sim);

                if (sim.getDebug()){
                    view.setColor(Color.WHITE);

                    view.drawStringHud("X: " + view.panX, 10,10);
                    view.drawStringHud("Y: " + view.panY, 10,20);
                    view.drawStringHud("Zoom: " + view.zoom, 10,30);
                    view.drawStringHud("SimMultiplier: " + sim.simulationMultiplier, 10,40);
                    view.drawStringHud("Paused: " + sim.pause, 10,50);
                    view.drawStringHud("Tick: " + sim.simTick, 10,60);
                    view.drawStringHud("SimTime: " + sim.simNanos*1e-9 + "s", 10,70);
                    view.drawStringHud("FrameTime: " + sim.trueDelta + "s", 10,80);
                    view.drawStringHud("SystemsTime: " + sim.systemsTime + "s", 10,90);
                    view.drawStringHud("TicksPerFrame: " + (int)Math.ceil(delta*simulationMultiplier/maxDeltaTick)*1f/sim.trueDelta, 10,100);
                }
            }
        });

        this.addSystem(new SimSystem(2000) {
            @Override
            public void init(Simulation sim) {}

            @Override
            public void run(Simulation sim, float delta) {
                view.update();
            }
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
        return this.view.getInput();
    }

    public View getView(){
        return this.view;
    }

    public boolean getPaused(){
        return this.pause;
    }
    public void setPaused(boolean paused) {
        this.pause = paused;
    }

    public boolean getDebug() { return this.debug; }

    public void setDebug(boolean debug) { this.debug = debug; }

    public void addSystem(SimSystem runnable){
        this.systems.add(runnable);
        this.systems.sort(Comparator.comparingInt(simSystem -> simSystem.priority)
        );
    }

    public float getTrueDelta(){
        return trueDelta;
    }

    public RoadMap getMap() { return this.map; }

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



    public abstract static class SimSystem {
        public final int priority;
        public SimSystem(int priority){
            this.priority = priority;
        }

        public abstract void init(Simulation sim);
        public abstract void run(Simulation sim, float delta);
    }
}
