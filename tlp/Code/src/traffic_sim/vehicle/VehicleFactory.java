package traffic_sim.vehicle;

/**
 * To construct Vehicles
 */
public interface VehicleFactory {
    /**
     * @return  The newly constructed vehicle
     */
    Vehicle create();
}
