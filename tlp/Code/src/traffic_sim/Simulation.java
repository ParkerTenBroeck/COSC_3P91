package traffic_sim;

import traffic_sim.gamble.DefaultGambleHandler;
import traffic_sim.gamble.GambleHandler;
import traffic_sim.io.Input;
import traffic_sim.io.TextView;
import traffic_sim.io.View;
import traffic_sim.map.RoadMap;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

/*UML_RAW_OUTER interface Runnable*/
public class Simulation implements Runnable{
    /*UML_RAW_OUTER Simulation "1" *-- "1" View: simulation contains a view into the world*/
    private View view;
    /*UML_RAW_OUTER Simulation "1" *-- "1" RoadMap: simulation contains a map*/
    private RoadMap map;

    private float frameDelta = 0.1f;

    private boolean pause = false;
    private boolean debug = true;
    private float simulationMultiplier = 1.0f;
    private final float maxDeltaTick = 0.1f/1f;
    private long targetFrameTime = 41666666;//33333333;//16666667;
    private int simTick = 0;
    private long simNanos = 0;

    /*UML_RAW_OUTER Simulation "1" *-- "n" SimSystem: simulation contains many systems*/
    private final ArrayList<SimSystem> systems = new ArrayList<>();
    private float systemsTime;


    private GambleHandler gambleHandler = new DefaultGambleHandler();

    public Simulation(RoadMap map){
        this.map = map;

        this.addSystem(SimSystem.simple(50, (sim, delta) -> {
            if (sim.getPaused()){
                map.tick(sim, 0.0f);
            }else{
                delta *= sim.simulationMultiplier;
                var num = (int)Math.ceil(delta/sim.maxDeltaTick);
                var r_tick = delta/num;
                for(int i = 0; i < num; i ++){
                    sim.tick(r_tick);
                }
            }
        }));
    }

    public Simulation(View view){
        this.view = view;
        this.addSystem(SimSystem.simple(1, (sim, delta) -> {
            sim.view.getInput().update();
            sim.view.setDefaultStroke(0.08f);
            sim.view.clearAll();
        }));

        this.addSystem(SimSystem.simple(1000, (sim, delta) -> {
            sim.map.draw(sim);

            sim.view.setColor(Color.WHITE);

            sim.view.drawStringHud("X: " + sim.view.panX, 10,10);
            sim.view.drawStringHud("Y: " + sim.view.panY, 10,20);
            sim.view.drawStringHud("Zoom: " + sim.view.zoom, 10,30);
            sim.view.drawStringHud("SimMultiplier: " + sim.simulationMultiplier, 10,40);
            sim.view.drawStringHud("Paused: " + sim.pause, 10,50);
            sim.view.drawStringHud("Tick: " + sim.simTick, 10,60);
            sim.view.drawStringHud("SimTime: " + sim.simNanos*1e-9 + "s", 10,70);
            sim.view.drawStringHud("FrameTime: " + sim.frameDelta + "s", 10,80);
            sim.view.drawStringHud("SystemsTime: " + sim.systemsTime + "s", 10,90);
            sim.view.drawStringHud("TicksPerFrame: " + (int)Math.ceil(delta*sim.simulationMultiplier/sim.maxDeltaTick)*1f/sim.frameDelta, 10,100);
        }));

        this.addSystem(SimSystem.simple(2000, (sim, delta) -> sim.view.update()));
    }

    /**
     * @param map  the map the simulation should use
     */
    public Simulation(RoadMap map, View view){
        this(map);
        this.view = view;

        this.addSystem(SimSystem.simple(1, (sim, delta) -> {
            sim.view.getInput().update();
            sim.view.setDefaultStroke(0.08f);
            sim.view.clearAll();
        }));

        this.addSystem(SimSystem.simple(1000, (sim, delta) -> {
            sim.map.draw(sim);

//            if (sim.getDebug()){
                sim.view.setColor(Color.WHITE);

                sim.view.drawStringHud("X: " + sim.view.panX, 10,10);
                sim.view.drawStringHud("Y: " + sim.view.panY, 10,20);
                sim.view.drawStringHud("Zoom: " + sim.view.zoom, 10,30);
                sim.view.drawStringHud("SimMultiplier: " + sim.simulationMultiplier, 10,40);
                sim.view.drawStringHud("Paused: " + sim.pause, 10,50);
                sim.view.drawStringHud("Tick: " + sim.simTick, 10,60);
                sim.view.drawStringHud("SimTime: " + sim.simNanos*1e-9 + "s", 10,70);
                sim.view.drawStringHud("FrameTime: " + sim.frameDelta + "s", 10,80);
                sim.view.drawStringHud("SystemsTime: " + sim.systemsTime + "s", 10,90);
                sim.view.drawStringHud("TicksPerFrame: " + (int)Math.ceil(delta*sim.simulationMultiplier/sim.maxDeltaTick)*1f/sim.frameDelta, 10,100);
//            }
        }));

        this.addSystem(SimSystem.simple(2000, (sim, delta) -> sim.view.update()));
    }

    public Simulation(RoadMap map, TextView display, boolean show_gui){
        this(map, show_gui?new View():null);
        this.systems.removeIf((s) -> s.priority == 50);

        if(!show_gui){
            this.systems.removeIf((s) -> s.priority == 50 || s.priority == 2000 || s.priority == 1 || s.priority == 1000);
        }

        display.attach(this);
    }
    
    public void tick(float delta){
        this.map.tick(this, delta);
        this.simTick++;
        this.simNanos += (long) (delta*1e9);
    }


    /**
     * @return  gets the current simulation multiplier
     */
    public float getSimulationMultiplier() {
        return simulationMultiplier;
    }

    /** Sets the multiplier of the simulation
     *
     * The value is clamped to 0 if the provided value is less than 0
     *
     * @param simulationMultiplier  The multiplier (1.0 being normal time)
     */
    public void setSimulationMultiplier(float simulationMultiplier) {
        this.simulationMultiplier = Math.max(simulationMultiplier, 0.0f);
    }

    /**
     * @return  The current simulation tick
     */
    public int getSimTick() {
        return simTick;
    }

    /**
     * @return  The time of the simulation in nanoseconds
     */
    public long getSimNanos() {
        return simNanos;
    }

    /**
     * @return  return the Input of the simulation
     */
    public Input getInput() {
        return this.view.getInput();
    }

    /**
     * @return  return the View of the simulation
     */
    public View getView(){
        return this.view;
    }

    /**
     * @return  if the simulation is paused
     */
    public boolean getPaused(){
        return this.pause;
    }

    /**
     * @param paused    if the simulation should pause or not
     */
    public void setPaused(boolean paused) {
        this.pause = paused;
    }

    /**
     * @return  if debug is enabled or not
     */
    public boolean getDebug() { return this.debug; }

    /**
     * @param debug if debug should be enabled or not
     */
    public void setDebug(boolean debug) { this.debug = debug; }

    /** Adds a new System to the Simulation that will be ran each frame
     *
     * @param runnable the system to add
     */
    public void addSystem(SimSystem runnable){
        this.systems.add(runnable);
        this.systems.sort(Comparator.comparingInt(simSystem -> simSystem.priority)
        );
    }

    /**
     * @return  gets the delta time of the frame in second
     */
    public float getFrameDelta(){
        return frameDelta;
    }

    /**
     * @return  The RoadMap this simulation is using
     */
    public RoadMap getMap() { return this.map; }

    /**
     * Actually run the simulation. This is blocking
     */
    @Override
    public void run(){
        long start = System.nanoTime();

        for(int i = 0; i < this.systems.size(); i ++){
            this.systems.get(i).init(this);
        }

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
            this.frameDelta = (now - start) / 1_000_000_000f;
            start = now;
        }
    }

    public GambleHandler getGambleHandler() {
        return this.gambleHandler;
    }
    
    public void setGambleHandler(GambleHandler handler){
        this.gambleHandler = handler;
    }

    public void setMap(RoadMap map) {
        this.map = map;
    }

    public void setTick(int i) {
        this.simTick = i;
    }

    public void setSimNanos(long n){
        this.simNanos = n;
    }


    public interface RunSystem{
        void run(Simulation sim, float delta);
    }

    public abstract static class SimSystem implements RunSystem {
        /**
         * The lower priority Systems get ran before the higher priority systems
         */
        public final int priority;
        public SimSystem(int priority){
            this.priority = priority;
        }

        public static SimSystem simple(int priority, RunSystem runSystem){
            return new SimSystem(priority) {
                @Override
                public void init(Simulation sim) {}

                @Override
                public void run(Simulation sim, float delta) {
                    runSystem.run(sim, delta);
                }
            };
        }

        public abstract void init(Simulation sim);
        @Override
        public abstract void run(Simulation sim, float delta);
    }
}
