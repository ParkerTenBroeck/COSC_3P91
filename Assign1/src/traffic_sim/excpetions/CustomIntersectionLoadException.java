package traffic_sim.excpetions;

public class CustomIntersectionLoadException extends Exception{
    /**
     *
     * @param cause     The original cause
     * @param message   The description of how / why the error occured
     */
    public CustomIntersectionLoadException(Throwable cause, String message){
        super(message, cause);
    }
}
