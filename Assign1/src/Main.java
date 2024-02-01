import io.Display;
import io.Input;
import map.DrainIntersection;
import map.Map;
import map.SourceIntersection;
import vehicle.Player;

import java.awt.*;

public class Main {
    public static void main(String[] args) {
        var map = new Map();
        var i1 = map.addIntersection("1", 0,0);
        var i2 = map.addIntersection("2", 10,0);
        var i3 = map.addIntersection("3", 10,10);
        var i4 = map.addIntersection("4", 0,10);
        var i5 = map.addIntersection("5", 0,20);
        var i6 = map.addIntersection("6", -10,10);
        var is = new SourceIntersection("Source", 0, -10);
        var id = new DrainIntersection("Drain", 10, 20);
        map.addIntersection(is);
        map.addIntersection(id);

        var sr = map.linkIntersection(is, i1, 1);
        var dr = map.linkIntersection(i3, id, 1);

        var r1 = map.linkIntersection(i1, i2, 1);
        var r2 = map.linkIntersection(i2, i3, 1);
        var r3 = map.linkIntersection(i3, i4, 1);
        var r4 = map.linkIntersection(i4, i1, 1);

        var r5 = map.linkIntersection(i4, i5, 1);

        var r6 = map.linkIntersection(i5, i6, 1);
        var r7 = map.linkIntersection(i6, i1, 1);

        var r8 = map.linkIntersection(i1, i6, 1);
        var r9 = map.linkIntersection(i6, i4, 1);

        map.addTurn(sr.getLane(0), r1.getLane(0), "Left");
        map.addTurn(sr.getLane(0), r8.getLane(0), "Right");

        map.addTurn(r2.getLane(0), dr.getLane(0), "Forward");

        map.addTurn(r4.getLane(0), r8.getLane(0), "Left");
        map.addTurn(r8.getLane(0), r9.getLane(0), "Left");
        map.addTurn(r9.getLane(0), r4.getLane(0), "Left");
        map.addTurn(r9.getLane(0), r5.getLane(0), "Right");
        map.addTurn(r6.getLane(0), r9.getLane(0), "Right");


        map.addTurn(r1.getLane(0), r2.getLane(0), "Right");
        map.addTurn(r2.getLane(0), r3.getLane(0), "Right");
        map.addTurn(r3.getLane(0), r4.getLane(0), "Right");
        map.addTurn(r4.getLane(0), r1.getLane(0), "Right");

        map.addTurn(r3.getLane(0), r5.getLane(0), "Left");

        map.addTurn(r5.getLane(0), r6.getLane(0), "Right");
        map.addTurn(r6.getLane(0), r7.getLane(0), "Right");
        map.addTurn(r7.getLane(0), r1.getLane(0), "Right");

        r1.setSpeedLimit(2.0);

        var input = new Input();
        var display = new Display(input);


        var player = new Player(input);
        map.init(player);

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
            if (input.keyPressed('r')){
                if (!player.isAlive()){
                    player.revive();
                    is.toAdd(player);
                }
            }

            if (player.isAlive()){
                x = -player.getX()+display.getWidth()/2.0/zoom;
                y = -player.getY()+display.getHeight()/2.0/zoom;
            }

            display.getGraphics().setColor(Color.WHITE);
            display.getGraphics().drawString("X: " + x, 10,10);
            display.getGraphics().drawString("Y: " + y, 10,20);
            display.getGraphics().drawString("Zoom: " + zoom, 10,30);

            display.update();
            input.update();
            try{
                Thread.sleep(16);
            }catch (Exception ignore){
            }
        }
    }
}