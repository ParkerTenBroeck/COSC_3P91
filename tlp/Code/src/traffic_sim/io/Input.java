package traffic_sim.io;

import java.awt.event.*;
import java.util.HashSet;

/*UML_RAW_OUTER interface KeyListener*/
/*UML_RAW_OUTER interface MouseListener*/
/*UML_RAW_OUTER interface MouseMotionListener*/
/*UML_RAW_OUTER interface MouseWheelListener*/

/**
 * The input events per frame.
 */
public class Input implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

    /*UML_RAW_OUTER Storage "2" --* "1" Input */
    private static class Storage {
        private HashSet<Integer> keyPressed = new HashSet<>();
        private HashSet<Integer> keyHeld = new HashSet<>();
        private HashSet<Integer> keyReleased = new HashSet<>();
        private boolean[] mousePressed = new boolean[4];
        private boolean[] mouseHeld = new boolean[4];
        private boolean[] mouseReleased = new boolean[4];
        private float mouseX = 0.0f;
        private float mouseY = 0.0f;
        private int scrollDelta = 0;
        private StringBuilder typed = new StringBuilder();


        public Storage clone(){
            var other = new Storage();
            other.keyPressed = (HashSet<Integer>) this.keyPressed.clone();
            other.keyHeld = (HashSet<Integer>) this.keyHeld.clone();
            other.keyReleased = (HashSet<Integer>) this.keyReleased.clone();
            other.mousePressed = this.mousePressed.clone();
            other.mouseHeld = this.mouseHeld.clone();
            other.mouseReleased = this.mouseReleased.clone();
            other.typed = new StringBuilder(this.typed);
            other.mouseX = this.mouseX;
            other.mouseY = this.mouseY;
            other.scrollDelta = this.scrollDelta;
            return other;
        }
    }

    final Storage working = new Storage();
    Storage current = new Storage();


    /**
     * @param key   The key code to query
     * @return  if the provided key was pressed or not
     */
    public boolean keyPressed(int key){
        return current.keyPressed.contains(key);
    }

    /**
     * @param key   The key code to query
     * @return  if the provided key is held or not
     */
    public boolean keyHeld(int key){
        return current.keyHeld.contains(key);
    }

    /**
     * @param key   The key code to query
     * @return  if the provided key was released or not
     */
    public boolean keyReleased(int key){
        return current.keyReleased.contains(key);
    }

    /**
     * @return  the screen space X coord of the mouse
     */
    public float getMouseX() {
        return current.mouseX;
    }

    /**
     * @return  the screen space Y coord of the mouse
     */
    public float getMouseY() {
        return current.mouseY;
    }

    /**
     * @param button The mouse button to query
     * @return  If the mouse button provided was held
     */
    public boolean mouseHeld(MouseKey button) {
        return current.mouseHeld[button.button];
    }

    /**
     * @param button The mouse button to query
     * @return  If the mouse button provided was pressed
     */
    public boolean mousePressed(MouseKey button) {
        return current.mousePressed[button.button];
    }

    /**
     * @param button The mouse button to query
     * @return  If the mouse button provided was released
     */
    public boolean mouseReleased(MouseKey button) {
        return current.mouseReleased[button.button];
    }

    /**
     * @return  The scroll delta of the frame
     */
    public int getScrollDelta(){
        return current.scrollDelta;
    }

    /**
     * @return  The current String of characters typed in the frame
     */
    public String getFrameTyped(){
        synchronized (current){
            return current.typed.toString();
        }
    }

    /*UML_INNER_CLASS_LINE_NOTE Inner class / uses */
    public enum MouseKey {
        Left(1),
        Middle(2),
        Right(3);

        private int button;
        MouseKey(int button) {
            this.button = button;
        }

    }

    /**
     * Sets the current buffer to the working buffer and clears the working buffer
     */
    public void update() {
        synchronized (working){
            current = working.clone();
            for(int i = 0; i < 4; i ++) {
                working.mousePressed[i] = false;
                working.mouseReleased[i] = false;
            }
            working.keyReleased.clear();
            working.keyPressed.clear();
            working.typed = new StringBuilder();
        }

    }


    @Override
    public void mouseDragged(MouseEvent e) {
        working.mouseX = e.getX();
        working.mouseY = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        working.mouseX = e.getX();
        working.mouseY = e.getY();
    }


    @Override
    public void keyTyped(KeyEvent e) {
        synchronized (working) {
            working.typed.append(e.getKeyCode());
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        synchronized (working) {
            working.keyPressed.add(e.getKeyCode());
            working.keyHeld.add(e.getKeyCode());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        synchronized (working){
            working.keyHeld.remove(e.getKeyCode());
            working.keyReleased.add(e.getKeyCode());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        try{
            working.mousePressed[e.getButton()] = true;
            working.mouseHeld[e.getButton()] = true;
        }catch (Exception ignore){}
    }


    @Override
    public void mouseReleased(MouseEvent e) {
        try{
            working.mouseHeld[e.getButton()] = false;
            working.mouseReleased[e.getButton()] = true;
        }catch (Exception ignore){}
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        synchronized (working){
            working.scrollDelta += e.getScrollAmount();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}
