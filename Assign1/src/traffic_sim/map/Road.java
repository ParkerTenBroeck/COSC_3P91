package traffic_sim.map;

import traffic_sim.Simulation;
import traffic_sim.io.Display;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.vehicle.Vehicle;

import java.awt.*;
import java.util.ArrayList;

public final class Road {

    private final String name;
    private float speedLimit = 1.0f;
    private float length;
    /*UML_RAW_OUTER Road "1" *-- "1..n" Lane: A Road will consist of one or more lanes*/
    private Lane[] lanes;

    private float normalX;
    private float normalY;

    private long tick;


    protected Road(String name, int lanes, Intersection from, Intersection to) {
        this.name = name;
        this.lanes = new Lane[lanes];
        for(int i = 0; i < this.lanes.length; i ++){
            this.lanes[i] = new Lane(i, i==0, i==this.lanes.length-1);
        }
        this.changedPosition(from, to);
    }

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


    public static class Surroundings {

        Vehicle leftFront;
        double leftSpaceToFront;
        Vehicle leftBack;
        double leftSpaceToBack;
        Lane left;


        Lane current;
        Vehicle front;
        double spaceToFront;
        Vehicle back;
        double spaceToBack;

        Lane right;
        Vehicle rightFront;
        double rightSpaceToFront;
        Vehicle rightBack;
        double rightSpaceToBack;
    }

    public enum LaneChangeDecision{
        ForceLeft(false, true, -1),
        NudgeLeft(true, false, -1),
        WaitLeft(false, false, -1),
        Nothing(false, false, 0),
        WaitRight(false, false, 1),
        NudgeRight(true, false, 1),
        ForceRight(false, true, 1);

        boolean nudge;
        boolean force;
        int laneOffset;

        LaneChangeDecision(boolean nudge, boolean force, int laneOffset){
            this.force = force;
            this.nudge = nudge;
            this.laneOffset = laneOffset;
        }
    }

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
        var surroundings = new Surroundings();
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
                var lane_change = vehicle.changeLane(sim, lanes[furthest_lane]);
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
                    }else{
                        if (lane_change.nudge)
                            lanes[new_lane].remainingSpace = vehicle.getDistanceAlongRoadBack();

                        if (lane_change.force){
                            lanes[furthest_lane].vehicles.remove(indexes[furthest_lane]);
                            lanes[new_lane].vehicles.add(indexes[new_lane], vehicle);
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

    public void changedPosition(RoadMap map){
        var start = map.roadStarts(this);
        var end = map.roadEnds(this);
        this.changedPosition(start, end);
    }
    public void changedPosition(Intersection start, Intersection end){
        var nx = end.getX() - start.getX();
        var ny = end.getY() - start.getY();
        var xs = nx * nx;
        var ys = ny * ny;
        var len = (float)Math.sqrt(xs + ys);
        normalX = nx / len;
        normalY = ny / len;
        this.length = len;
        for(var lane : lanes){
            for(var vehicle : lane.vehicles){
                vehicle.setDistanceAlongRoad(Math.min(this.length, vehicle.getDistanceAlongRoad()));
            }
        }
    }

    public float getNormalX() {
        return normalX;
    }

    public float getNormalY() {
        return normalY;
    }

    public void draw(Simulation sim, RoadMap map){
        var g = sim.getView();
        var start = map.roadStarts(this);
        var end = map.roadEnds(this);

        var rx = -normalY/3.0f;
        var ry = normalX/3.0f;

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
                var position = map.carPosition(start, end, this, vehicle);
                vehicle.updatePosition(position[0]+rx, position[1]+ry);
                vehicle.draw(sim, (position[0]+rx), (position[1]+ry), normalX, normalY);
            }

            rx -= normalY/1.7f;
            ry += normalX/1.7f;
        }
    }

    public String getName(){
        return this.name;
    }

    public float getSpeedLimit(){
        return this.speedLimit;
    }

    public void setSpeedLimit(float speedLimit){
        this.speedLimit = speedLimit;
    }

    public float getSpace(int lane) {
        return this.lanes[lane].remainingSpace;
    }

    public Lane getLane(int lane) {
        return this.lanes[lane];
    }
    
    public int getNumLanes(){
        return this.lanes.length;
    }
    
    public Lane[] getLanes() {
        return this.lanes;
    }

    public float getLength() {
        return this.length;
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

        public Road road() {
            return Road.this;
        }

        public Vehicle removeEnd() {
            if(this.vehicles.isEmpty()) return null;
            var last = this.vehicles.remove(0);
            if (last.getDistanceAlongRoad() + 0.0001 > length){
                return last;
            }else{
                this.vehicles.add(0,last);
            }
            return null;
        }

        public boolean canFit(Vehicle vehicle) {
            return this.remainingSpace > vehicle.getSize()/2;
        }

        public void addVehicle(Vehicle vehicle) {
            vehicle.putInLane(this);
            this.vehicles.add(this.vehicles.size(), vehicle);
            this.remainingSpace = 0;
        }

        public float remainingSpace() {
            return this.remainingSpace;
        }

        public int getLane() {
            return this.lane;
        }
    }
}
