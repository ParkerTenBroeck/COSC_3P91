import io.View;
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

        var sr = map.linkIntersection(is, i1, 3);
        map.linkIntersection(i1, is, 3);
        var dr = map.linkIntersection(i3, id, 1);

        var r1 = map.linkIntersection(i1, i2, 1);
        var r2 = map.linkIntersection(i2, i3, 2);
        var r3 = map.linkIntersection(i3, i4, 1);
        var r4 = map.linkIntersection(i4, i1, 1);

        var r5 = map.linkIntersection(i4, i5, 1);

        var r6 = map.linkIntersection(i5, i6, 1);
        var r7 = map.linkIntersection(i6, i1, 1);

        var r8 = map.linkIntersection(i1, i6, 1);
        var r9 = map.linkIntersection(i6, i4, 1);

        map.addTurn(sr.getLane(0), r1.getLane(0), "Left");
        map.addTurn(sr.getLane(0), r8.getLane(0), "Right");
        map.addTurn(sr.getLane(1), r8.getLane(0), "Right");
        map.addTurn(sr.getLane(2), r8.getLane(0), "Right");

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

        r1.setSpeedLimit(2.0f);

        var input = new Input();
        var display = new Display(input);

        var graphics = new View(display, input);

        var player = new Player(input);
        map.init(player);

        graphics.panX = 0;
        graphics.panY = 0;
        graphics.zoom = 21;

        while(true){
            float tick = 0.1f;
            {
                var num = 5;
                var r_tick = tick/num;
                for(int i = 0; i < num; i ++){
                    map.tick(r_tick);
                }
            }

            graphics.setColor(Color.BLACK);
            graphics.clearScreen();

            map.draw(graphics);

            if (input.keyHeld('d')){
                graphics.panX -= tick*2;
            }
            if (input.keyHeld('a')){
                graphics.panX += tick*2;
            }
            if (input.keyHeld('w')){
                graphics.panY += tick*2;
            }
            if (input.keyHeld('s')){
                graphics.panY -= tick*2;
            }
            if (input.keyHeld('q')){
                graphics.zoom *= tick*10.5;
            }
            if (input.keyHeld('e')){
                graphics.zoom /= tick*10.5;
            }
            if (input.keyPressed('f')){
                if (graphics.getFollowing() == null){
                    graphics.setFollowing(player);
                }else{
                    graphics.setFollowing(null);
                }
            }
            if (input.keyPressed('r')){
                if (!player.isOnRoad()){
                    is.toAdd(player);
                }
            }

            graphics.setColor(Color.WHITE);

            graphics.drawOval(graphics.getScreenX(), graphics.getScreenY(), 2,2);

            graphics.drawStringHud("X: " + graphics.panX, 10,10);
            graphics.drawStringHud("Y: " + graphics.panY, 10,20);
            graphics.drawStringHud("Zoom: " + graphics.zoom, 10,30);

            graphics.update();
            try{
                Thread.sleep(16);
            }catch (Exception ignore){
            }
        }
    }
}