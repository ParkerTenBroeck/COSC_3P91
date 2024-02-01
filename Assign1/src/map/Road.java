package map;

import vehicle.Vehicle;

import java.util.ArrayList;

public class Road {

    double speedLimit = 1.0;
    double length;
    double[] end;
    ArrayList<Vehicle>[] lanes;


    public Road(int lanes, double length) {

        assert lanes > 1;
        this.length = length;
        this.end = new double[lanes];
        this.lanes = new ArrayList[lanes];
        for(int i = 0; i < this.lanes.length; i ++){
            this.lanes[i] = new ArrayList<>();
            this.end[i] = length;
        }
    }

    public void tick(Map map, double delta){
        for(int l = 0; l < lanes.length; l ++){
            var lane = lanes[l];
            var end = length;
            for(int i = lane.size() - 1; i >= 0; i --){
                var vehicle = lane.get(i);
                vehicle.tick(delta);
                var new_pos = vehicle.getPosition() + speedLimit * delta * vehicle.getSpeedPercentage();
                new_pos = Double.min(end, new_pos);
                if (new_pos + 0.0001 > length){
                    var turns = map.roadEnds.get(this).turns.get(this);
                    var turn = vehicle.chooseTurn(turns);
                    if (turn != null){
                        lane.remove(lane.size() - 1);
                        turn.road.lanes[0].add(0, vehicle);
                        vehicle.setPosition(0);
                        end = length;
                        continue;
                    }
                }
                vehicle.setPosition(new_pos);
                end = new_pos - vehicle.getSize() - 0.05;
                end = Double.max(end, 0);
            }
            this.end[l] = end;
        }
    }


}
