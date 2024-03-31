package traffic_sim.vehicle.controller;

import traffic_sim.ConsoleUtils;
import traffic_sim.Simulation;
import traffic_sim.map.Road;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

/**
 * A controller for Vehicles and also a way to display information about the currently controlled vehicle
 */
public class TextPlayerController implements Controller {

    private int index = 0;
    private boolean select = false;
    private final static int START_TURN_Y = 8;

    @Override
    public void tick(Vehicle v, Simulation sim, Road.Lane lane, int laneIndex, boolean changedLanes, float delta) {}

    @Override
    public Intersection.Turn chooseTurn(Vehicle v, Simulation sim, Road.Lane current_lane, Intersection intersection, ArrayList<Intersection.Turn> turns) {
        if(turns == null) return null;

        if(select){
            select = false;
            try{
                return turns.get(index);
            }catch (Exception ignore){}
        }

        ConsoleUtils.moveCursor(13, START_TURN_Y);
        ConsoleUtils.print("At Intersection: '" + intersection.getName() + "'");

        ConsoleUtils.moveCursor(13, START_TURN_Y+1);
        ConsoleUtils.print("Select With Up/Down arrow keys, Enter/Space to select");


        for(int i = 0; i < turns.size(); i ++){
            var turn = turns.get(i);

            ConsoleUtils.moveCursor(13, START_TURN_Y+3+i);
            if(index == i){
                ConsoleUtils.applyStyle(ConsoleUtils.Style.Underline);
                ConsoleUtils.print("*");
            }else{
                ConsoleUtils.print(" ");
            }
            if(!turn.enabled()){
                ConsoleUtils.applyStyle(ConsoleUtils.BasicBackground.Yellow);
            }
            if(!turn.getLane().canFit(v)){
                ConsoleUtils.applyStyle(ConsoleUtils.BasicBackground.Red);
            }
            ConsoleUtils.print("'"+turn.getName() + "' onto '" + turn.getLane().road().getName() + "': " + (turn.enabled()?"Enabled":"Disabled") + " " + (turn.getLane().canFit(v)?"Can Fit":"Can't Fit"));
            ConsoleUtils.resetStyle();
        }
        if(index < 0 || index >= turns.size()){
            index = 0;
        }


        return null;
    }

    @Override
    public Road.LaneChangeDecision laneChange(Vehicle v, Simulation sim, Road.Lane lane, int current_index, int left_vehicle_back_index, int right_vehicle_back_index) {

        try{
            var middle =( v.getDistanceAlongRoadBack() + v.getDistanceAlongRoad()) / 2;
            var left = lane.leftLane() != null ? crateLane(lane.leftLane(), left_vehicle_back_index, middle,lane.road().getRoadLength()):null;
            var curr = crateLane(lane, current_index, middle, lane.road().getRoadLength());
            var right = lane.rightLane() != null ? crateLane(lane.rightLane(), right_vehicle_back_index, middle,lane.road().getRoadLength()):null;

            ConsoleUtils.moveCursor(1, 1);
            for(int i = curr.length-1; i >= 0; i --){
                printVehicleLine(left == null?null:left[i], '#');
                ConsoleUtils.print(" ");
                printVehicleLine(curr[i], '@');
                ConsoleUtils.print(" ");
                printVehicleLine(right == null?null:right[i], '#');
                ConsoleUtils.println();
            }
        }catch (Exception ignore){}


        ConsoleUtils.moveCursor(13, 1);
        ConsoleUtils.println("Speed: " + v.getSpeedMultiplier()*lane.road().getSpeedLimit());
        ConsoleUtils.moveCursor(13, 2);
        ConsoleUtils.println("Current Speed Limit: " + lane.road().getSpeedLimit());
        ConsoleUtils.moveCursor(13, 3);
        ConsoleUtils.println("Speed Multiplier: " + v.getSpeedMultiplier());
        ConsoleUtils.moveCursor(13, 4);
        ConsoleUtils.println("Health: " + v.getHealth());
        ConsoleUtils.moveCursor(13, 5);
        ConsoleUtils.println("Reputation: " + v.getReputation());
        ConsoleUtils.moveCursor(13, 6);
        ConsoleUtils.println("Road: '" + lane.road().getName()+"' lane " + lane.getLane());

//        ConsoleUtils.println("Lane change? (nothing for no change) ForceLeft, NudgeLeft, WaitLeft, WaitRight, NudgeRight, ForceRight, SpeedUp, SlowDown");
        var laneChange  = Road.LaneChangeDecision.Nothing;
        try{
            ConsoleUtils.moveCursor(13, 20);
            while(ConsoleUtils.hasNext()) {
                var read = ConsoleUtils.read();
                switch(read){
                    case 'A' -> index -= 1;
                    case 'B' -> index += 1;

                    case 'D' -> laneChange = Road.LaneChangeDecision.WaitLeft;
                    case 'C' -> laneChange = Road.LaneChangeDecision.WaitRight;
                    case ' ', 13 -> select = true;
                }
                System.out.println(read);
            }
        }catch (Exception ignore){}
        ConsoleUtils.show();
        ConsoleUtils.fullClear();
        ConsoleUtils.moveCursor(0, 0);

        return laneChange;
    }

    /** Print a singular line of e vehicle/road piece
     *
     * @param got       The character used to represent the line
     * @param highlight What character the highlighted character should be replaced with
     */
    private static void printVehicleLine(Character got, char highlight){
        if(got != null){
            if (got == '|'){
                ConsoleUtils.print(" | ");
                return;
            };
            if (got == '^'){
                got = highlight;
                ConsoleUtils.print(got + "^" + got);
                return;
            }
            if (got == '@'){
                got = highlight;
            }
            ConsoleUtils.print("" + got + got + got);
        }else{
            ConsoleUtils.print("   ");
        }
    }

    /** Make a character representation of a slice of a lane.
     *
     * @param lane      The lane we want to draw
     * @param middleV    The index of the middle vehicle
     * @param max   The max length of the visible lane
     * @return  The lane slice represented as characters
     */
    private static char[] crateLane(Road.Lane lane, int middleV, float middle, float max){
        int height = ConsoleUtils.getHeight();
//        System.out.println(height);
        float bruh = 4;
        var full = new char[height];
        var from = middle - height / bruh / 2;
        var to =  middle + height / bruh / 2;
        var max_l = Math.min(to, max);
        var min_l = Math.max(0, from);
        Function<Float, Integer> map_l = (v) -> (int)((Math.max(Math.min(v, max_l), min_l)-from)*bruh);
        Function<Float, Integer> map_v = (v) -> (int)((Math.max(Math.min(v, to), from)-from)*bruh);
        var from_i = map_l.apply(from);
        var to_i = map_l.apply(to);

        Arrays.fill(full, ' ');
        for(int i = from_i; i < to_i; i ++) full[i] = '|';

        var v = lane.vehicleAt(middleV);
        if (v != null){
            var start = map_v.apply(v.getDistanceAlongRoadBack());
            var end = map_v.apply(v.getDistanceAlongRoad());
            for(int i = start; i < end; i ++){
                if(i == end-1) full[i] = '^';
                else if(i == start) full[i] = '*';
                else full[i] = '@';
            }
        }

        var index = middleV - 1;
        while((v = lane.vehicleAt(index--)) != null){
            var start = v.getDistanceAlongRoadBack();
            var end = v.getDistanceAlongRoad();
            for(int i = map_v.apply(start); i < map_v.apply(end); i ++){

                if(i == end-1) full[i] = '^';
                else if(i == start) full[i] = '*';
                else full[i] = '#';
            }
        }

        index = middleV + 1;
        while((v = lane.vehicleAt(index++)) != null){
            var start = v.getDistanceAlongRoadBack();
            var end = v.getDistanceAlongRoad();
            for(int i = map_v.apply(start); i < map_v.apply(end); i ++){

                if(i == end-1) full[i] = '^';
                else if(i == start) full[i] = '*';
                else full[i] = '#';
            }
        }


        return full;
    }
}
