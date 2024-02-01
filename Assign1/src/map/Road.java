package map;

import vehicle.Vehicle;

import java.awt.*;
import java.util.ArrayList;

public class Road {

    private double speedLimit = 1.0;
    private final double length;
    private final Lane[] lanes;


    public Road(int lanes, double length) {
        assert lanes > 1;
        this.length = length;
        this.lanes = new Lane[lanes];
        for(int i = 0; i < this.lanes.length; i ++){
            this.lanes[i] = new Lane();
        }
    }

    public void tick(Map map, double delta){
        for(var lane : lanes){
            var end = length;
            for(int i = lane.vehicles.size() - 1; i >= 0; i --){
                var vehicle = lane.vehicles.get(i);
                vehicle.tick(map, this, delta);
                var new_pos = vehicle.getPosition() + speedLimit * delta * vehicle.getSpeedPercentage();
                new_pos = Double.min(end, new_pos);
                if (new_pos + 0.0001 > length){
                    var turns = map.roadEnds(this).getTurns(lane);
                    var turn = vehicle.chooseTurn(turns);
                    if (turn != null){
                        lane.vehicles.remove(lane.vehicles.size() - 1);
                        turn.lane.vehicles.add(0, vehicle);
                        vehicle.setPosition(0);
                        end = length;
                        continue;
                    }
                }
                vehicle.setPosition(new_pos);
                end = new_pos - vehicle.getSize() - 0.05;
                end = Double.max(end, 0);
            }
            lane.remainingSpace = end;
        }
    }

    public void draw(Graphics g, Map map, double px, double py, double zoom){
        var start = map.roadStarts(this);
        var end = map.roadEnds(this);

        var nx = end.getX() - start.getX();
        var xs = nx * nx;
        var ny = end.getY() - start.getY();
        var ys = ny * ny;
        var len = Math.sqrt(xs + ys);


        var rx = px - ny/2/21;
        var ry = py + nx/2/21;
        for (var lane : lanes){
            double p = lane.remainingSpace / this.length;
            g.setColor(Color.RED);
            g.drawLine((int)((end.getX()*(p)+start.getX()*(1-p)+rx)*zoom), (int)((end.getY()*(p)+start.getY()*(1-p)+ry)*zoom), (int)((end.getX()+rx)*zoom), (int)((end.getY()+ry)*zoom));
            g.setColor(Color.GREEN);
            g.drawLine((int)((start.getX()+rx)*zoom), (int)((start.getY()+ry)*zoom), (int)((end.getX()*(p)+start.getX()*(1-p)+rx)*zoom), (int)((end.getY()*(p)+start.getY()*(1-p)+ry)*zoom));

            for(var vehicle : lane.vehicles){
                var position = map.carPosition(start, end, this, vehicle);

                vehicle.draw(g, (position[0]+rx)*zoom, (position[1]+ry)*zoom, nx / len, ny / len, zoom);
            }

            rx -= ny/1.2/21;
            ry += nx/1.2/21;
        }
    }

    public double getSpeedLimit(){
        return this.speedLimit;
    }

    public void setSpeedLimit(double speedLimit){
        this.speedLimit = speedLimit;
    }

    public double getSpace(int lane) {
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

    public double getLength() {
        return this.length;
    }

    public class Lane{
        double remainingSpace;
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
            if (last.getPosition() + 0.0001 > length){
                return last;
            }else{
                this.vehicles.add(last);
            }
            return null;
        }

        public boolean canFit(Vehicle vehicle) {
            return this.remainingSpace > 0.01;
        }

        public void addVehicle(Vehicle vehicle) {
            this.vehicles.add(0, vehicle);
        }
    }
}
