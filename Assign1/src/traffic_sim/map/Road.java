package traffic_sim.map;

import traffic_sim.Simulation;
import traffic_sim.io.Display;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.vehicle.Vehicle;

import java.awt.*;
import java.util.ArrayList;

/**
 * A Edge on a graph that connects two Intersections in a single direction
 */
public final class Road {

    private final String name;
    private float speedLimit = 1.0f;
    private float length;
    /*UML_RAW_OUTER Road "1" *-- "1..n" Lane: A Road will consist of one or more lanes*/
    private Lane[] lanes;

    private float directionX;
    private float directionY;

    private long tick;


    protected Road(String name, int lanes, Intersection from, Intersection to) {
        this.name = name;
        this.lanes = new Lane[lanes];
        for(int i = 0; i < this.lanes.length; i ++){
            this.lanes[i] = new Lane(i, i==0, i==this.lanes.length-1);
        }
        this.updatePosition(from, to);
    }

    /** Adds some number of additional lanes. numNew must be non negative
     *
     * @param numNew    The number of new lanes to add
     */
    public void addLanes(int numNew) {
        var newLanes = new Lane[numNew + lanes.length];
        for(int i = 0; i < newLanes.length; i ++){
            newLanes[i] = new Lane(i, i==0, i==this.lanes.length-1);
            if (i < lanes.length){
                newLanes[i].vehicles.addAll(lanes[i].vehicles);
            }
        }
        this.lanes = newLanes;
    }


    public enum LaneChangeDecision{
        ForceLeft(false, true, -1),
        NudgeLeft(true, false, -1),
        WaitLeft(false, false, -1),
        Nothing(false, false, 0),
        WaitRight(false, false, 1),
        NudgeRight(true, false, 1),
        ForceRight(false, true, 1);

        private boolean nudge;
        private boolean force;
        private int laneOffset;

        LaneChangeDecision(boolean nudge, boolean force, int laneOffset){
            this.force = force;
            this.nudge = nudge;
            this.laneOffset = laneOffset;
        }
    }

    /** This function is ran every simulation tick and updates all Vehicles on it.
     *
     * @param sim   The simulation this road is apart of
     * @param delta The simulation delta time in seconds
     */
    public void tick(Simulation sim, float delta){
        for(int l = 0; l < lanes.length; l ++) {
            var current_lane = lanes[l];
            if(!current_lane.vehicles.isEmpty()){
                var vehicle = current_lane.vehicles.get(0);
                if (vehicle.getDistanceAlongRoad() + 0.0001 + vehicle.getSize()/2 > length){
                    var turns = sim.getMap().roadEnds(this).getTurns(current_lane);
                    var turn = vehicle.chooseTurn(sim, turns);
                    if (turn != null){
                        current_lane.vehicles.remove(0);
                        turn.getLane().vehicles.add(turn.getLane().vehicles.size(), vehicle);
                        vehicle.setDistanceAlongRoad(vehicle.getSize()/2);
                        // road was already updated so we need to run the update on le car
                        if (turn.getLane().road().tick == sim.getSimTick())
                            vehicle.tick(sim, turn.getLane(), delta);
                    }
                }
            }

            if (!current_lane.vehicles.isEmpty()){
                current_lane.remainingSpace = this.length + current_lane.vehicles.get(0).getSize()/2.0f;
            }else{
                current_lane.remainingSpace = this.length;
            }
        }


        this.tick = sim.getSimTick();
        var indexes = new int[this.lanes.length];


        boolean has_next;
        do{
            float furthest = Float.NEGATIVE_INFINITY;
            int furthest_lane = 0;

            has_next = false;
            for(int l = 0; l < lanes.length; l ++){
                if (indexes[l] >= lanes[l].vehicles.size())
                    continue;
                var vehicle = lanes[l].vehicles.get(indexes[l]);
                if (vehicle.getDistanceAlongRoad() > furthest){
                    furthest = vehicle.getDistanceAlongRoad();
                    furthest_lane = l;
                    has_next = true;
                }
            }
            if (has_next){
                var vehicle = lanes[furthest_lane]
                        .vehicles.get(indexes[furthest_lane]);

                boolean update = false;
                var lane_change = vehicle.changeLane(sim, lanes[furthest_lane], furthest_lane>1?indexes[furthest_lane-1]:-1, furthest_lane<indexes.length-1?indexes[furthest_lane+1]:-1);
                var new_lane = furthest_lane+lane_change.laneOffset;
                boolean can_merge = false;
                if(new_lane >= 0 && new_lane < lanes.length && lane_change.laneOffset != 0){
                    if (vehicle.getDistanceAlongRoad() < lanes[new_lane].remainingSpace){
                        if (indexes[new_lane]<lanes[new_lane].vehicles.size()){
                            can_merge = vehicle.getDistanceAlongRoadBack() > lanes[new_lane].vehicles.get(indexes[new_lane]).getDistanceAlongRoad();
                        }else{
                            can_merge = true;
                        }
                    }
                    if(can_merge){
                        lanes[furthest_lane].vehicles.remove(indexes[furthest_lane]);
                        lanes[new_lane].vehicles.add(indexes[new_lane], vehicle);
                        vehicle.putInLane(lanes[new_lane]);
                    }else{
                        if (lane_change.nudge)
                            lanes[new_lane].remainingSpace = vehicle.getDistanceAlongRoadBack();

                        if (lane_change.force){
                            lanes[furthest_lane].vehicles.remove(indexes[furthest_lane]);
                            lanes[new_lane].vehicles.add(indexes[new_lane], vehicle);
                            vehicle.putInLane(lanes[new_lane]);
                        }else{

                        }
                        update = true;
                    }
                }else{
                    update = true;
                }

                if (update){
                    lanes[furthest_lane].remainingSpace = Math.max(lanes[furthest_lane].remainingSpace, vehicle.getDistanceAlongRoad());
                    vehicle.tick(sim, lanes[furthest_lane], delta);
                    lanes[furthest_lane].remainingSpace = Math.max(0.0f, vehicle.getDistanceAlongRoadBack());
                    indexes[furthest_lane]++;
                }
            }

        }while(has_next);

        for(var lane : lanes){
            lane.remainingSpace = Math.min(this.length, lane.remainingSpace);
            lane.remainingSpace = Math.max(0, lane.remainingSpace);
        }
    }


    /** Recalculates the position/angle/direction/length of this road according to its from and to intersection
     *
     * @param map   the map this road is apart of
     */
    public void updatePosition(RoadMap map){
        var start = map.roadStarts(this);
        var end = map.roadEnds(this);
        this.updatePosition(start, end);
    }

    /** Recalculates the position/angle/direction/length of this road according to its from and to intersection
     *
     * @param start The intersection this road starts at
     * @param end   The intersection this road ends at
     */
    public void updatePosition(Intersection start, Intersection end){
        var nx = end.getX() - start.getX();
        var ny = end.getY() - start.getY();
        var xs = nx * nx;
        var ys = ny * ny;
        var len = (float)Math.sqrt(xs + ys);
        directionX = nx / len;
        directionY = ny / len;
        this.length = len;
        for(var lane : lanes){
            for(var vehicle : lane.vehicles){
                vehicle.setDistanceAlongRoad(Math.min(this.length, vehicle.getDistanceAlongRoad()));
            }
        }
    }

    /**
     * @return  The X component of the direction vector for this road
     */
    public float getDirectionX() {
        return directionX;
    }

    /**
     * @return  The Y component of the normal direction for this road
     */
    public float getDirectionY() {
        return directionY;
    }

    /** Draws the road, and the vehicles on it to the display
     *
     * @param sim   The simulation this road is apart of
     */
    public void draw(Simulation sim){
        var g = sim.getView();
        var start = sim.getMap().roadStarts(this);
        var end = sim.getMap().roadEnds(this);

        var rx = -directionY /3.0f;
        var ry = directionX /3.0f;

        g.setLayer(Display.Layer.Roads);
        g.setColor(Color.LIGHT_GRAY);
        g.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
        for (var lane : lanes){


            g.setLayer(Display.Layer.Roads);
            if (sim.getDebug()){
                float p = lane.remainingSpace / this.length;
                g.setColor(Color.RED);
                g.drawLine(end.getX()*p+start.getX()*(1-p)+rx, end.getY()*p+start.getY()*(1-p)+ry, end.getX()+rx, end.getY()+ry);
                g.setColor(Color.GREEN);
                g.drawLine(start.getX()+rx, start.getY()+ry, end.getX()*(p)+start.getX()*(1-p)+rx, end.getY()*(p)+start.getY()*(1-p)+ry);
            }else{
                g.setColor(Color.YELLOW);
                g.drawLine(start.getX()+rx, start.getY()+ry, end.getX()+rx, end.getY()+ry);
            }

            g.setLayer(Display.Layer.Cars);
            for(var vehicle : lane.vehicles){
                var position = sim.getMap().vehiclePosition(start, end, this, vehicle);
                vehicle.updatePosition(position[0]+rx, position[1]+ry);
                vehicle.draw(sim, (position[0]+rx), (position[1]+ry), directionX, directionY);
            }

            rx -= directionY /1.7f;
            ry += directionX /1.7f;
        }
    }

    /**
     * @return  The name of this road
     */
    public String getName(){
        return this.name;
    }


    /**
     * @return  The speed limit of this road
     */
    public float getSpeedLimit(){
        return this.speedLimit;
    }

    /** Updates the speed limit of this road. The provided speed must be a positive value
     *
     * @param speedLimit  The new speed limit
     */

    public void setSpeedLimit(float speedLimit){
        this.speedLimit = speedLimit;
    }


    /** Gets the remaining space at the start of the lane.
     *
     * @param lane  The lane we want to find remaining space in
     * @return      The remaining space left in the specified lane
     */
    public float getRemainingSpace(int lane) {
        return this.lanes[lane].remainingSpace;
    }

    /**
     * @param lane  The lane number 0 is left most
     * @return  The lane associated with the specified lane number
     */
    public Lane getLane(int lane) {
        return this.lanes[lane];
    }

    /**
     * @return  The number of lanes on this road
     */
    public int getNumLanes(){
        return this.lanes.length;
    }

    /**
     * @return  The length of this road
     */
    public float getRoadLength(){
        return this.length;
    }

    /**
     * @return  A list of all the lanes this road has
     */
    public Lane[] getLanes() {
        return this.lanes;
    }


    public final class Lane{
        private final int lane;
        private float remainingSpace;
        /*UML_RAW_OUTER Lane "1" o-- "n" Vehicle: A lane will have cars on it*/
        private ArrayList<Vehicle> vehicles = new ArrayList<>();
        public boolean leftmost;
        public boolean rightmost;

        private Lane(int lane, boolean leftmost, boolean rightmost){
            this.remainingSpace = length;
            this.lane = lane;
            this.rightmost = rightmost;
            this.leftmost = leftmost;
        }

        /**
         * @return  The lane to the left of this one, null if there is none
         */
        public Lane leftLane(){
            if (this.leftmost){
                return null;
            }else{
                return lanes[this.lane - 1];
            }
        }

        /**
         * @return  The lane to the right of this one, null if there is none
         */
        public Lane rightLane(){
            if (this.rightmost){
                return null;
            }else{
                return lanes[this.lane + 1];
            }
        }

        /**
         * @param index The index of the vehicle
         * @return  The Vehicle at the specified index, or null if none exists
         */
        public Vehicle vehicleAt(int index){
            if (this.vehicles.size() < index && index >= 0){
                return this.vehicles.get(index);
            }else{
                return null;
            }
        }

        /**
         * @return  The Road that contains this lane
         */
        public Road road() {
            return Road.this;
        }

        /** Removes the last vehicle on this road (closes to the outgoing end) and returns it if it exists
         *
         * @return  the Vehicle of one was removed, null if no vehicle was removed
         */
        public Vehicle removeLastVehicle() {
            if(this.vehicles.isEmpty()) return null;
            var last = this.vehicles.remove(0);
            if (last.getDistanceAlongRoad() + 0.0001 > length){
                return last;
            }else{
                this.vehicles.add(0,last);
            }
            return null;
        }

        /** Checks if the provided vehicle can fit on the road
         *
         * @param vehicle   The vehicle to check
         * @return          If it can fit or not
         */
        public boolean canFit(Vehicle vehicle) {
            return this.remainingSpace > vehicle.getSize()/2;
        }

        /** Adds the provided Vehicle to the back (closest to incoming end of road) of this road
         *
         * @param vehicle   the Vehicle to add
         */
        public void addVehicle(Vehicle vehicle) {
            vehicle.putInLane(this);
            this.vehicles.add(this.vehicles.size(), vehicle);
            this.remainingSpace = 0;
        }

        /**
         * @return  The ramining space from the last most vehicle to the incoming end of the road
         */
        public float remainingSpace() {
            return this.remainingSpace;
        }

        /**
         * @return  The lane number of this lane
         */
        public int getLane() {
            return this.lane;
        }
    }
}
