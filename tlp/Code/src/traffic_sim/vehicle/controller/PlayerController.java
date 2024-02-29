package traffic_sim.vehicle.controller;

import traffic_sim.Simulation;
import traffic_sim.io.Display;
import traffic_sim.io.Input;
import traffic_sim.map.Road;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.vehicle.Vehicle;

import java.awt.*;
import java.util.ArrayList;

/**
 * A controller that allows for a player to control a vehicle with user input
 */
public class PlayerController implements Controller{
    private final Input input;
    private int menu_index = 0;
    private Intersection.Turn choice = null;

    public PlayerController(Input input){
        this.input = input;
    }
    @Override
    public void tick(Vehicle v, Simulation sim, Road.Lane lane, int laneIndex, boolean changedLanes, float delta) {
        if (this.input.keyHeld('K')){
            v.setSpeedMultiplier(v.getSpeedMultiplier() - delta * 1f);
        }
        if (this.input.keyHeld('I')){
            v.setSpeedMultiplier(v.getSpeedMultiplier() + delta * 1f);
        }else{
            v.setSpeedMultiplier(v.getSpeedMultiplier() - delta * 0.2f);
        }


        v.setSpeedMultiplier(Float.min(v.getSpeedMultiplier(), 2.0f));
        v.setSpeedMultiplier(Float.max(v.getSpeedMultiplier(), 0));
    }

    @Override
    public Intersection.Turn chooseTurn(Vehicle v, Simulation sim, Intersection intersection, ArrayList<Intersection.Turn> turns) {
        if (turns == null){
            return null;
        }
        if (this.input.keyPressed('K')){
            this.menu_index += 1;
        }
        if (this.input.keyPressed('I')){
            this.menu_index -= 1;
        }
        this.menu_index = Math.min(Math.max(0, this.menu_index), turns.size() -1);

        if (this.input.keyPressed(10)){
            if (menu_index >= 0 && menu_index < turns.size()){
                choice = turns.get(menu_index);
                menu_index = 0;
                sim.setPaused(false);
            }
        }

        if (choice != null){
            if(choice.canTurn(v)){
                var tmp = choice;
                choice = null;
                menu_index = 0;
                return tmp;
            }else{
                return null;
            }
        }else{
            sim.setPaused(true);
        }



        var offset = 0;
        sim.getView().setLayer(Display.Layer.Hud);

        for(int i = 0; i < turns.size(); i ++){
            if (this.menu_index == i) {
                if (turns.get(i).canTurn(v)) {
                    sim.getView().setColor(Color.GREEN);
                }else {
                    sim.getView().setColor(Color.YELLOW);
                }
            }else {
                if (turns.get(i).canTurn(v)) {
                    sim.getView().setColor(Color.WHITE);
                }else {
                    sim.getView().setColor(Color.RED);
                }
            }
//            sim.getView().drawBox();
//            sim.getView().drawString(turns.get(i).getName(), v.getLastX()-40/sim.getView().zoom, v.getLastY() + offset/sim.getView().zoom);
            offset += 15;
        }

       return null;
    }

    @Override
    public  Road.LaneChangeDecision laneChange(Vehicle v, Simulation sim, Road.Lane lane, int current_index, int left_vehicle_back_index, int right_vehicle_back_index) {
        if (input.keyHeld('J')){
            if (input.keyHeld(16)){
                return  Road.LaneChangeDecision.NudgeLeft;
            }
            if (input.keyHeld(17)){
                return  Road.LaneChangeDecision.ForceLeft;
            }
            return  Road.LaneChangeDecision.WaitLeft;
        }

        if (input.keyHeld('L')){
            if (input.keyHeld(16)){
                return  Road.LaneChangeDecision.NudgeRight;
            }
            if (input.keyHeld(17)){
                return  Road.LaneChangeDecision.ForceRight;
            }
            return  Road.LaneChangeDecision.WaitRight;
        }

        return  Road.LaneChangeDecision.Nothing;

    }
}
