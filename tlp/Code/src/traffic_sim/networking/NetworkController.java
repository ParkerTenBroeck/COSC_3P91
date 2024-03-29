package traffic_sim.networking;

import traffic_sim.Simulation;
import traffic_sim.map.Road;
import traffic_sim.map.intersection.Intersection;
import traffic_sim.vehicle.Vehicle;
import traffic_sim.vehicle.controller.Controller;

import java.util.ArrayList;

public class NetworkController implements Controller {
    public NetworkController(NetworkServerSystem.Client client) {
    }

    @Override
    public void tick(Vehicle v, Simulation sim, Road.Lane lane, int laneIndex, boolean changedLanes, float delta) {

    }

    @Override
    public Intersection.Turn chooseTurn(Vehicle v, Simulation sim, Intersection intersection, ArrayList<Intersection.Turn> turns) {
        return turns.get(0);
    }

    @Override
    public Road.LaneChangeDecision laneChange(Vehicle v, Simulation sim, Road.Lane lane, int current_index, int left_vehicle_back_index, int right_vehicle_back_index) {
        return null;
    }

    @Override
    public void draw(Simulation sim, Vehicle v, float x, float y, float dx, float dy) {
    }

    @Override
    public void putInLane(Vehicle vehicle, Road.Lane lane) {
    }
}