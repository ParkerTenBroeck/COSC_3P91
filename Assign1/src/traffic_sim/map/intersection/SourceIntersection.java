package traffic_sim.map.intersection;

import traffic_sim.Simulation;
import traffic_sim.vehicle.Car;
import traffic_sim.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * An intersection that spawns new Vehicles at random and allows for inserting specified Vehicles to place
 */
public class SourceIntersection extends Intersection{

    private Supplier<Vehicle>[] vehicleSuppliers = new Supplier[]{Car::new};
    private final ArrayList<Vehicle> toAdd = new ArrayList<>();

    public SourceIntersection(String name, float x, float y) {
        super(name, x, y);
    }

    @Override
    public void tick(Simulation sim, double delta) {
        var outgoing = sim.getMap().outgoing(this);
        if (outgoing == null) return;
        if (!outgoing.isEmpty()){
            var random = outgoing.get((int) (outgoing.size()*Math.random()));
            if (random.getNumLanes() > 0){
                var lane = (int)(random.getNumLanes()*Math.random());
                if (random.getRemainingSpace(lane) > 2.0){
                    if (toAdd.isEmpty()){
                        var supplier = (int)(vehicleSuppliers.length*Math.random());
                        random.getLane(lane).addVehicle(vehicleSuppliers[supplier].get());
                    }else{
                        random.getLane(lane).addVehicle(toAdd.remove(0));
                    }
                }
            }
        }
    }

    /**
     * @param vehicle   A Vehicle that should eventually be added to the RoadMap once space is available
     */
    public void toAdd(Vehicle vehicle) {
        vehicle.setDistanceAlongRoad(0);
        toAdd.add(vehicle);
    }

    public void suppliers(Supplier<Vehicle>... suppliers){
        this.vehicleSuppliers = suppliers;
    }
}
