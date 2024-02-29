package traffic_sim.excpetions;

/**
 * Used to indicate when something goes wrong or an invalid state would be made while building the RoadMap
 */
public abstract class MapBuildingException extends Exception{
    public MapBuildingException(String message){
        super(message);
    }

    /**
     * Used when a link is made between two intersections who already have a link made in that direction
     */
    public static class InvalidRoadLink extends MapBuildingException{
        public InvalidRoadLink(String message){
            super(message);
        }
    }

    /**
     * Used when a road ID already exists in the map
     */
    public static class RoadIDClash extends  MapBuildingException{
        public RoadIDClash(String message){
            super(message);
        }
    }

    /**
     * Used when an intersection ID already exists in the map
     */
    public static class IntersectionIDClash extends MapBuildingException{
        public IntersectionIDClash(String message){
            super(message);
        }
    }

    /**
     * Used when a invalid turn is attempted to be made. A turn must only happen between an incoming lane to an outgoing road from the same Intersection
     */
    public static class InvalidTurn extends MapBuildingException{
        public InvalidTurn(String message){
            super(message);
        }
    }
}
