package traffic_sim.vehicle.controller;

import traffic_sim.Simulation;
import traffic_sim.map.Road;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.function.Function;

/**
 * A controller for Vehicles and also a way to display information about the currently controlled vehicle
 */
public class TextPlayerController implements Controller {

    public void attach(Simulation simulation) {
        simulation.addSystem(new Simulation.SimSystem(50) {
            @Override
            public void init(Simulation sim) {}

            @Override
            public void run(Simulation sim, float delta) { sim.tick(0.5f); }
        });
    }

    @Override
    public void tick(Vehicle v, Simulation sim, Road.Lane lane, int laneIndex, boolean changedLanes, float delta) {}

    @Override
    public Intersection.Turn chooseTurn(Vehicle v, Simulation sim, Intersection intersection, ArrayList<Intersection.Turn> turns) {
        if(turns == null) return null;

        System.out.println("At Intersection: '" + intersection.getName() + "'");
        System.out.println("Select Turn, Enter name of turn or index to select or enter nothing to wait");

        boolean first = true;
        while(true){
            if(!first)
                System.out.println("Invalid turn, please select a valid turn");
            first = false;

            for(int i = 0; i < turns.size(); i ++){
                var turn = turns.get(i);
                System.out.println("turn ("+i+") '"+turn.getName() + "' onto '" + turn.getLane().road().getName() + "': " + (turn.enabled()?"Enabled":"Disabled") + " " + (turn.getLane().canFit(v)?"Can Fit":"Can't Fit"));
            }

            var input = new Scanner(System.in).nextLine().trim();
            if (input.isEmpty()) return null;

            try{
                return turns.get(Integer.parseInt(input));
            }catch (Exception ignore){}

            for(var turn : turns){
                if (turn.getName().equalsIgnoreCase(input))return turn;
            }
        }
    }

    @Override
    public Road.LaneChangeDecision laneChange(Vehicle v, Simulation sim, Road.Lane lane, int current_index, int left_vehicle_back_index, int right_vehicle_back_index) {

        var start = v.getDistanceAlongRoadBack()-1f;
        var end = v.getDistanceAlongRoad()+1f;
        var left = lane.leftLane() != null ? crateLane(lane.leftLane(), left_vehicle_back_index, start,end,lane.road().getRoadLength()):null;
        var curr = crateLane(lane, current_index, start, end, lane.road().getRoadLength());
        var right = lane.rightLane() != null ? crateLane(lane.rightLane(), right_vehicle_back_index, start,end,lane.road().getRoadLength()):null;

        for(int i = curr.length-1; i >= 0; i --){
            printVehicleLine(left == null?null:left[i], '#');
            System.out.print(" ");
            printVehicleLine(curr[i], '@');
            System.out.print(" ");
            printVehicleLine(right == null?null:right[i], '#');
            System.out.println();
        }

        System.out.println("Speed: " + v.getSpeedMultiplier()*lane.road().getSpeedLimit());
        System.out.println("Current Speed Limit: " + lane.road().getSpeedLimit());
        System.out.println("Speed Multiplier: " + v.getSpeedMultiplier());
        System.out.println("Health: " + v.getHealth());
        System.out.println("Reputation: " + v.getReputation());
        System.out.println("Road: '" + lane.road().getName()+"' lane " + lane.getLane());

        System.out.println("Lane change? (nothing for no change) ForceLeft, NudgeLeft, WaitLeft, WaitRight, NudgeRight, ForceRight, SpeedUp, SlowDown");
        var input = new Scanner(System.in).nextLine().trim();
        if(input.isEmpty()) return  Road.LaneChangeDecision.Nothing;
        switch (input.toLowerCase()){
            case "forceleft" -> {
                return Road.LaneChangeDecision.ForceLeft;
            }
            case "nudgeleft" -> {
                return Road.LaneChangeDecision.NudgeLeft;
            }
            case "waitleft" -> {
                return Road.LaneChangeDecision.WaitLeft;
            }
            case "waitright" -> {
                return Road.LaneChangeDecision.WaitRight;
            }
            case "nudgeright" -> {
                return Road.LaneChangeDecision.NudgeRight;
            }
            case "forceright" -> {
                return Road.LaneChangeDecision.ForceRight;
            }
            case "speedup" -> {
                v.setSpeedMultiplier((float)Math.min(1.8, v.getSpeedMultiplier() + 0.1f));
                return Road.LaneChangeDecision.Nothing;
            }
            case "slowdown" -> {
                v.setSpeedMultiplier((float)Math.max(0.3, v.getSpeedMultiplier() - 0.1f));
                return Road.LaneChangeDecision.Nothing;
            }
            default -> {
                return Road.LaneChangeDecision.Nothing;
            }
        }
    }

    /** Print a singular line of e vehicle/road piece
     *
     * @param got       The character used to represent the line
     * @param highlight What character the highlighted character should be replaced with
     */
    private static void printVehicleLine(Character got, char highlight){
        if(got != null){
            if (got == '|'){
                System.out.print(" | ");
                return;
            };
            if (got == '^'){
                got = highlight;
                System.out.print(got + "^" + got);
                return;
            }
            if (got == '@'){
                got = highlight;
            }
            System.out.print("" + got + got + got);
        }else{
            System.out.print("   ");
        }
    }

    /** Make a character representation of a slice of a lane.
     *
     * @param lane      The lane we want to draw
     * @param middleV    The index of the middle vehicle
     * @param from  the start of the slice of lane to display
     * @param to    The end of the slice of lane to display
     * @param max   The max length of the visible lane
     * @return  The lane slice represented as characters
     */
    private static char[] crateLane(Road.Lane lane, int middleV, float from, float to, float max){
        float bruh = 4;
        var full = new char[(int) (bruh * (to - from))];
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
