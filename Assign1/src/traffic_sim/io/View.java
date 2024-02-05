package traffic_sim.io;

import traffic_sim.vehicle.Vehicle;

import java.awt.*;

/**
 * A View of the Display that allows for Pan and Zoom, as well as following a Vehicle. Coordinates in the draw methods are translated
 * from map space to screen space according to the zoom and pan.
 */
public class View {
    /*UML_RAW_OUTER View "1" *-- "1" Display: View contains a single Display*/
    private final Display display;
    /*UML_RAW_OUTER View "1" *-- "1" Input: View has one source of Input*/
    private final Input input;

    public float panX;
    public float panY;
    private int height;
    private int width;
    public float zoom;

    /*UML_RAW_OUTER View o-- "0..1" Vehicle: View can potentially follow a Vehicle*/
    private Vehicle follow;

    private Display.Layer layer = Display.Layer.Hud;

    public View(){
        this.input = new Input();
        this.display = new Display(input);
        update();
    }

    /**
     * @param layer The layer to use in the following methods
     */
    public void setLayer(Display.Layer layer){
        this.layer = layer;
    }

    /**
     * Clears all layers
     */

    public void clearAll(){
        display.clearAll();
    }

    /**
     * Draws a hollow oval, all coord/values are in map space
     *
     * @param x center x coord
     * @param y center y coord
     * @param width width
     * @param height height
     */
    public void drawOval(float x, float y, float width, float height){
        width *= zoom;
        height *= zoom;
        display.getGraphics(layer).drawOval((int)((x+panX)*zoom-width/2)+this.width/2,(int)((y+panY)*zoom-height/2)+this.height/2, (int)(width), (int)(height));
    }

    /**
     * Draws a filled oval, all coord/values are in map space
     *
     * @param x center x coord
     * @param y center y coord
     * @param width width
     * @param height height
     */
    public void fillOval(float x, float y, float width, float height){
        width *= zoom;
        height *= zoom;
        display.getGraphics(layer).fillOval((int)((x+panX)*zoom-width/2+this.width/2),(int)((y+panY)*zoom-height/2)+this.height/2, (int)(width), (int)(height));
    }

    /** Draws a line, coords are in map space
     *
     * @param x1  First x coord
     * @param y1  First y coord
     * @param x2  Second x coord
     * @param y2  Second y coord
     */
    public void drawLine(float x1, float y1, float x2, float y2){
        this.display.getGraphics(layer).drawLine((int)((x1+ panX)*zoom)+this.width/2, (int)((y1+ panY)*zoom)+this.height/2, (int)((x2+ panX)*zoom)+this.width/2, (int)((y2+ panY)*zoom)+this.height/2);
    }

    /**
     * Draws a string whos leftside starts at coords (x,y). coords are in map space
     *
     * @param message   the text to display
     * @param x         x coord
     * @param y         y coord
     */
    public void drawString(String message, float x, float y){
        this.display.getGraphics(layer).drawString(message, (int)((x+panX)*zoom)+this.width/2, (int)((y+panY)*zoom)+this.height/2);
    }

    /**
     * Draws a string whos leftside starts at coords (x,y). coords are in screen space
     *
     * @param message   the text to display
     * @param x         x coord
     * @param y         y coord
     */
    public void drawStringHud(String message, float x, float y){
        this.display.getGraphics(layer).drawString(message, (int)(x), (int)(y));
    }

    /** Sets the color for the current layer for the following draw calls
     *
     * @param color the color to use
     */
    public void setColor(Color color){
        this.display.getGraphics(layer).setColor(color);
    }

    /**
     * @return  the Stroke used in the current layer
     */
    public Stroke getStroke() {
        return this.display.getGraphics(layer).getStroke();
    }

    /** Sets the Stroke for the current layer for the following draw calls.
     *
     * @param width the width of the stroke
     */
    public void setStroke(float width){
        this.display.getGraphics(layer).setStroke(new BasicStroke(width*zoom));
    }

    /** Sets the Stroke for the current layer for the following draw calls.
     *
     * @param stroke the stroke to use
     */
    public void setStroke(Stroke stroke){
        this.display.getGraphics(layer).setStroke(stroke);
    }

    /**
     * Updates the display and updates the pan position if following a vehicle
     */
    public void update(){
        this.display.update();

        this.height = this.display.getHeight();
        this.width = this.display.getWidth();

        if (follow != null && follow.isOnRoad()){
            this.panX = -follow.getLastX();
            this.panY = -follow.getLastY();
        }
    }


    /** Follows a vehicle
     *
     * @param vehicle   the vehicle to follow
     */
    public void setFollowing(Vehicle vehicle) {
        this.follow = vehicle;
    }

    /**
     * @return  returns the Vehicle that this view is currently following, if there is none it is null
     */
    public Vehicle getFollowing(){
        return this.follow;
    }

    /**
     * @return  The X coord of the mouse in map space coordinates
     */
    public float getMouseMapX(){
        return (this.input.getMouseX() - (float) this.width /2)/this.zoom-this.panX;
    }

    /**
     * @return  The Y coord of the mouse in map space coordinates
     */
    public float getMouseMapY(){
        return (this.input.getMouseY() - (float) this.height /2)/this.zoom-this.panY;
    }

    /**
     * @param width Sets the Stroke for all layers with the current width
     */
    public void setDefaultStroke(float width) {
        this.display.setDefaultStroke(new BasicStroke(width*zoom));
    }

    /**
     * @return  The Input associated with the Display this view is using
     */
    public Input getInput() {
        return this.input;
    }

}
