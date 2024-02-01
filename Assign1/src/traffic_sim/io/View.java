package traffic_sim.io;

import traffic_sim.vehicle.Vehicle;

import java.awt.*;

public class View {
    private final Display display;
    private final Input input;

    public float panX;
    public float panY;
    private int height;
    private int width;
    public float zoom;
    private Vehicle follow;

    private boolean debug = true;

    public View(Display display, Input input){
        this.display = display;
        this.input = input;
        update();
    }

    public void drawOval(float x, float y, float width, float height){
        width *= zoom;
        height *= zoom;
        display.getGraphics().drawOval((int)((x+panX)*zoom-width/2)+this.width/2,(int)((y+panY)*zoom-height/2)+this.height/2, (int)(width), (int)(height));
    }

    public void fillOval(float x, float y, float width, float height){
        width *= zoom;
        height *= zoom;
        display.getGraphics().fillOval((int)((x+panX)*zoom-width/2+this.width/2),(int)((y+panY)*zoom-height/2)+this.height/2, (int)(width), (int)(height));
    }

    public void drawLine(float x1, float y1, float x2, float y2){
        this.display.getGraphics().drawLine((int)((x1+ panX)*zoom)+this.width/2, (int)((y1+ panY)*zoom)+this.height/2, (int)((x2+ panX)*zoom)+this.width/2, (int)((y2+ panY)*zoom)+this.height/2);
    }

    public void drawString(String message, float x, float y){
        this.display.getGraphics().drawString(message, (int)((x+panX)*zoom)+this.width/2, (int)((y+panY)*zoom)+this.height/2);
    }

    public void drawStringHud(String message, float x, float y){
        this.display.getGraphics().drawString(message, (int)(x), (int)(y));
    }

    public void setColor(Color color){
        this.display.getGraphics().setColor(color);
    }

    public void clearScreen() {
        this.display.getGraphics().fillRect(0,0, width, height);
    }

    public void update(){
        this.display.update();
        this.input.update();

        this.height = this.display.getHeight();
        this.width = this.display.getWidth();

        if (follow != null && follow.isOnRoad()){
            this.panX = -follow.getLastX();
            this.panY = -follow.getLastY();
        }
    }

    public void setDebug(boolean debug){
        this.debug = debug;
    }

    public boolean getDebug(){
        return this.debug;
    }

    public void setFollowing(Vehicle vehicle) {
        this.follow = vehicle;
    }

    public Vehicle getFollowing(){
        return this.follow;
    }

    public float getScreenX(){
        return (this.input.getMouseX() - this.width/2)/this.zoom-this.panX;
    }

    public float getScreenY(){
        return (this.input.getMouseY() - this.height/2)/this.zoom-this.panY;
    }
}
