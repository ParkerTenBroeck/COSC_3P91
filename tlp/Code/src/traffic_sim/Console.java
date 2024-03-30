package traffic_sim;

public class Console {

    public static void enterRawMode() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            String[] cmd = {"/bin/sh", "-c", "stty -raw </dev/tty"};
            try {
                Runtime.getRuntime().exec(cmd).waitFor();
            } catch (Exception ignore) {}
        }));
        String[] cmd = {"/bin/sh", "-c", "stty -echo raw </dev/tty"};
        try{
            Runtime.getRuntime().exec(cmd).waitFor();
        }catch (Exception e){throw new RuntimeException(e);}
    }
}
