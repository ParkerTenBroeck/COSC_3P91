package traffic_sim.gamble;

import traffic_sim.map.Road;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.vehicle.Vehicle;

public class DefaultGambleHandler implements GambleHandler{
    @Override
    public void laneChange(Vehicle v, Road.Lane lane, int index, boolean wasSafe, Road.LaneChangeDecision laneChange) {
        v.setReputation(v.getReputation() + (wasSafe?0.01f:-0.01f));
        if (laneChange.force){
            v.setReputation(v.getReputation() - 0.01f);
        }
        if(!wasSafe){
            var value = Math.max(0f, Math.min(1f, (float)Math.random() /v.getReputation())) * (v.getSpeedMultiplier()+1) * -0.03f;
            v.setHealth(v.getHealth() + value);
        }
    }

    @Override
    public void chooseTurn(Vehicle v, Road.Lane from, Intersection.Turn turn){
        var to = turn.getLane();
        // blow a stop
        if(!turn.enabled()){
            v.setReputation(v.getReputation()-0.01f);
        }
        if (!to.canFit(v)){
            v.setReputation(v.getReputation()-0.01f);

            var crashedWith = to.getFirstVehicle();
            var speed_dif = (v.getSpeedMultiplier() - crashedWith.getSpeedMultiplier()) * to.road().getSpeedLimit();
            var dif = v.getReputation()/crashedWith.getReputation();
            var value = Math.max(0f, Math.min(1f, (float)Math.random() * dif)) * speed_dif * -0.03f;
            v.setHealth(v.getHealth() + value);
        }else{
            v.setReputation(v.getReputation()+0.0001f);
        }
    }

    @Override
    public void turnedIntoNonExistedLane(Vehicle v, Road.Lane current, float delta) {
        v.setReputation(v.getReputation() - 0.001f*delta);
        var value = Math.max(0f, Math.min(1f, (float)Math.random() /v.getReputation())) * (v.getSpeedMultiplier()+1) * -0.03f * delta;
        v.setHealth(v.getHealth() + value);
    }


}
