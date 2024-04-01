package traffic_sim.map.intersection;

import traffic_sim.Simulation;
import traffic_sim.vehicle.Car;
import traffic_sim.vehicle.Vehicle;
import traffic_sim.vehicle.VehicleFactory;

import java.util.ArrayList;

/**
 * An intersection that spawns new Vehicles at random and allows for inserting specified Vehicles to place
 */
public class SourceIntersection extends Intersection{

    transient private VehicleFactory[] vehicleFactories = new VehicleFactory[]{Car::new};
    transient private final ArrayList<Vehicle> toAdd = new ArrayList<>();

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

                if(toAdd.isEmpty()){
                    var newCar = getRandom();
                    if(newCar == null) return;
                    toAdd.add(newCar);
                }

                if (random.getRemainingSpace(lane) > toAdd.get(0).getSize()){
                    random.getLane(lane).addVehicle(toAdd.remove(0));
                }
            }
        }
    }

    public Vehicle getRandom() {
        if(vehicleFactories.length > 0){
            var supplier = (int)(vehicleFactories.length*Math.random());
            return vehicleFactories[supplier].create();
        }else{
            return null;
        }
    }

    /**
     * @param vehicle   A Vehicle that should eventually be added to the RoadMap once space is available
     */
    public void toAdd(Vehicle vehicle) {
        vehicle.setDistanceAlongRoad(0);
        toAdd.add(vehicle);
    }

    public void factories(VehicleFactory... suppliers){
        this.vehicleFactories = suppliers;
    }
}
