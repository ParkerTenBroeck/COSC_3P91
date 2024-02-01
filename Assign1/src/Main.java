import io.Display;
import io.Input;
import map.Map;
import vehicle.Player;

import java.awt.*;

public class Main {
    public static void main(String[] args) {
        var map = new Map();
        var i1 = map.addIntersection("First", 0,0);
        var i2 = map.addIntersection("Second", 10,0);
        var i3 = map.addIntersection("Third", 10,10);
        var i4 = map.addIntersection("Fourth", 0,10);
        var i5 = map.addIntersection("Fifth", 0,20);
        var i6 = map.addIntersection("Sixth", -10,10);

        var r1 = map.linkIntersection(i1, i2, 1);
        var r2 = map.linkIntersection(i2, i3, 1);
        var r3 = map.linkIntersection(i3, i4, 1);
        var r4 = map.linkIntersection(i4, i1, 1);

        var r5 = map.linkIntersection(i4, i5, 1);

        var r6 = map.linkIntersection(i5, i6, 1);
        var r7 = map.linkIntersection(i6, i1, 1);

        map.addTurn(r1, r2, "Right");
        map.addTurn(r2, r3, "Right");
        map.addTurn(r3, r4, "Right");
        map.addTurn(r4, r1, "Right");

        map.addTurn(r3, r5, "Left");

        map.addTurn(r5, r6, "Right");
        map.addTurn(r6, r7, "Right");
        map.addTurn(r7, r1, "Right");


        var input = new Input();
        var display = new Display(input);


        map.init(new Player(input));

        double x = 20;
        double y = 4;
        double zoom = 21;

        while(true){
            double tick = 0.1;
            map.tick(tick);

            display.getGraphics().setColor(Color.BLACK);
            display.getGraphics().fillRect(0,0, display.getWidth(), display.getHeight());

            map.draw(display, x, y, zoom);

            if (input.keyHeld('d')){
                x -= tick*1;
            }
            if (input.keyHeld('a')){
                x += tick*1;
            }
            if (input.keyHeld('w')){
                y += tick*1;
            }
            if (input.keyHeld('s')){
                y -= tick*1;
            }
            if (input.keyHeld('q')){
                zoom += tick*1;
            }
            if (input.keyHeld('e')){
                zoom -= tick*1;
            }

            display.getGraphics().setColor(Color.WHITE);
            display.getGraphics().drawString("X: " + x, 10,10);
            display.getGraphics().drawString("Y: " + y, 10,20);
            display.getGraphics().drawString("Zoom: " + zoom, 10,30);

            display.update();
            input.update();
            try{
                Thread.sleep(16);
            }catch (Exception e){
            }
        }
    }
}