package traffic_sim.map;

import traffic_sim.Simulation;
import traffic_sim.vehicle.Vehicle;

import java.awt.*;
import java.util.ArrayList;

public class Road {

    private float speedLimit = 1.0f;
    private final float length;
    private final Lane[] lanes;

    private float normalX;
    private float normalY;


    public Road(int lanes, float length) {
        assert lanes > 1;
        this.length = length;
        this.lanes = new Lane[lanes];
        for(int i = 0; i < this.lanes.length; i ++){
            this.lanes[i] = new Lane();
        }
    }

    public void tick(Simulation _sim, RoadMap map, float delta){
        for(var lane : lanes){
            var end = length + (lane.vehicles.isEmpty() ? 0.0f : lane.vehicles.get(lane.vehicles.size() - 1).getSize()/2);
            for(int i = lane.vehicles.size() - 1; i >= 0; i --){
                var vehicle = lane.vehicles.get(i);
                vehicle.tick(map, this, delta);
                var new_pos = vehicle.getDistanceAlongRoad() + speedLimit * delta * vehicle.getSpeedMultiplier();
                new_pos = Float.min(end, new_pos);
                if (new_pos + 0.0001 > length){
                    var turns = map.roadEnds(this).getTurns(lane);
                    var turn = vehicle.chooseTurn(turns);
                    if (turn != null){
                        lane.vehicles.remove(lane.vehicles.size() - 1);
                        turn.getLane().vehicles.add(0, vehicle);
                        vehicle.setDistanceAlongRoad(vehicle.getSize() / 2);
                        end = length;
                        continue;
                    }
                }
                vehicle.setDistanceAlongRoad(new_pos);
                end = new_pos - vehicle.getSize() - 0.05f;
                end = Float.max(end, 0);
            }
            lane.remainingSpace = end;
        }
    }

    public void updateNormal(RoadMap map){
        var start = map.roadStarts(this);
        var end = map.roadEnds(this);

        var nx = end.getX() - start.getX();
        var ny = end.getY() - start.getY();
        var xs = nx * nx;
        var ys = ny * ny;
        var len = (float)Math.sqrt(xs + ys);
        normalX = nx / len;
        normalY = ny / len;
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
        g.setColor(Color.LIGHT_GRAY);
        g.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
        for (var lane : lanes){


            if (g.getDebug()){
                float p = lane.remainingSpace / this.length;
                g.setColor(Color.RED);
                g.drawLine(end.getX()*p+start.getX()*(1-p)+rx, end.getY()*p+start.getY()*(1-p)+ry, end.getX()+rx, end.getY()+ry);
                g.setColor(Color.GREEN);
                g.drawLine(start.getX()+rx, start.getY()+ry, end.getX()*(p)+start.getX()*(1-p)+rx, end.getY()*(p)+start.getY()*(1-p)+ry);
            }else{
                g.setColor(Color.YELLOW);
                g.drawLine(start.getX()+rx, start.getY()+ry, end.getX()+rx, end.getY()+ry);
            }

            for(var vehicle : lane.vehicles){
                var position = map.carPosition(start, end, this, vehicle);
                vehicle.updatePosition(position[0]+rx, position[1]+ry);
                vehicle.draw(sim, (position[0]+rx), (position[1]+ry), normalX, normalY);
            }

            rx -= normalY/1.7f;
            ry += normalX/1.7f;
        }
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

    public class Lane{
        float remainingSpace;
        ArrayList<Vehicle> vehicles = new ArrayList<>();

        private Lane(){
            this.remainingSpace = length;
        }

        public Road road() {
            return Road.this;
        }

        public Vehicle removeEnd() {
            if(this.vehicles.isEmpty()) return null;
            var last = this.vehicles.remove(this.vehicles.size() - 1);
            if (last.getDistanceAlongRoad() + 0.0001 > length){
                return last;
            }else{
                this.vehicles.add(last);
            }
            return null;
        }

        public boolean canFit(Vehicle vehicle) {
            return this.remainingSpace > vehicle.getSize() / 2.0f;
        }

        public void addVehicle(Vehicle vehicle) {
            vehicle.putInLane(this);
            this.vehicles.add(0, vehicle);
            this.remainingSpace = 0;
        }
    }
}
