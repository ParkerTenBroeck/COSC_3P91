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

    private Display.Layer layer = Display.Layer.Hud;

    public View(Display display, Input input){
        this.display = display;
        this.input = input;
        update();
    }

    public void setLayer(Display.Layer layer){
        this.layer = layer;
    }

    public void clear(){
        display.clear();
    }

    public void drawOval(float x, float y, float width, float height){
        width *= zoom;
        height *= zoom;
        display.getGraphics(layer).drawOval((int)((x+panX)*zoom-width/2)+this.width/2,(int)((y+panY)*zoom-height/2)+this.height/2, (int)(width), (int)(height));
    }

    public void fillOval(float x, float y, float width, float height){
        width *= zoom;
        height *= zoom;
        display.getGraphics(layer).fillOval((int)((x+panX)*zoom-width/2+this.width/2),(int)((y+panY)*zoom-height/2)+this.height/2, (int)(width), (int)(height));
    }

    public void drawLine(float x1, float y1, float x2, float y2){
        this.display.getGraphics(layer).drawLine((int)((x1+ panX)*zoom)+this.width/2, (int)((y1+ panY)*zoom)+this.height/2, (int)((x2+ panX)*zoom)+this.width/2, (int)((y2+ panY)*zoom)+this.height/2);
    }

    public void drawString(String message, float x, float y){
        this.display.getGraphics(layer).drawString(message, (int)((x+panX)*zoom)+this.width/2, (int)((y+panY)*zoom)+this.height/2);
    }

    public void drawStringHud(String message, float x, float y){
        this.display.getGraphics(layer).drawString(message, (int)(x), (int)(y));
    }

    public void setColor(Color color){
        this.display.getGraphics(layer).setColor(color);
    }

    public Stroke getStroke() {
        return this.display.getGraphics(layer).getStroke();
    }

    public void setStroke(float width){
        this.display.getGraphics(layer).setStroke(new BasicStroke(width*zoom));
    }

    public void setStroke(Stroke stroke){
        this.display.getGraphics(layer).setStroke(stroke);
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


    public void setFollowing(Vehicle vehicle) {
        this.follow = vehicle;
    }

    public Vehicle getFollowing(){
        return this.follow;
    }

    public float getScreenX(){
        return (this.input.getMouseX() - (float) this.width /2)/this.zoom-this.panX;
    }

    public float getScreenY(){
        return (this.input.getMouseY() - (float) this.height /2)/this.zoom-this.panY;
    }

    public void setDefaultStroke(float width) {
        this.display.setDefaultStroke(new BasicStroke(width*zoom));
    }
}
