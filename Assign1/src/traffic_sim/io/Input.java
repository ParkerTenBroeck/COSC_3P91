package traffic_sim.io;

import java.awt.event.*;
import java.util.HashSet;

/*UML_RAW_OUTER interface KeyListener*/
/*UML_RAW_OUTER interface MouseListener*/
/*UML_RAW_OUTER interface MouseMotionListener*/
/*UML_RAW_OUTER interface MouseWheelListener*/
/*UML_RAW_OUTER hide Cloneable*/
public class Input implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

    /*UML_RAW_OUTER Storage "2" --* "1" Input */
    private static class Storage implements Cloneable{
        private HashSet<Integer> keyPressed = new HashSet<>();
        private HashSet<Integer> keyHeld = new HashSet<>();
        private HashSet<Integer> keyReleased = new HashSet<>();
        private boolean[] mousePressed = new boolean[4];
        private boolean[] mouseHeld = new boolean[4];
        private boolean[] mouseReleased = new boolean[4];
        private float mouseX = 0.0f;
        private float mouseY = 0.0f;
        private StringBuilder typed = new StringBuilder();


        /*UML_HIDE*/
        @Override
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
            return other;
        }
    }

    final Storage s1 = new Storage();
    Storage s2 = new Storage();




    public boolean keyPressed(int key){
        return s2.keyPressed.contains(key);
    }
    public boolean keyHeld(int key){
        return s2.keyHeld.contains(key);
    }
    public boolean keyReleased(int key){
        return s2.keyReleased.contains(key);
    }

    public float getMouseX() {
        return s2.mouseX;
    }

    public float getMouseY() {
        return s2.mouseY;
    }

    public boolean mouseHeld(MouseKey key) {
        return s2.mouseHeld[key.key];
    }
    public boolean mousePressed(MouseKey key) {
        return s2.mousePressed[key.key];
    }
    public boolean mouseReleased(MouseKey key) {
        return s2.mouseReleased[key.key];
    }

    /*UML_INNER_CLASS_LINE_NOTE Inner class / uses */
    public enum MouseKey {
        Left(1),
        Middle(2),
        Right(3);

        private int key;
        MouseKey(int key) {
            this.key = key;
        }

    }

    public void update() {
        synchronized (s1){
            s2 = s1.clone();
            for(int i = 0; i < 4; i ++) {
                s1.mousePressed[i] = false;
                s1.mouseReleased[i] = false;
            }
            s1.keyReleased.clear();
            s1.keyPressed.clear();
            s1.typed = new StringBuilder();
        }

    }


    /*UML_HIDE*/
    @Override
    public void mouseDragged(MouseEvent e) {
        s1.mouseX = e.getX();
        s1.mouseY = e.getY();
    }

    /*UML_HIDE*/
    @Override
    public void mouseMoved(MouseEvent e) {
        s1.mouseX = e.getX();
        s1.mouseY = e.getY();
    }


    /*UML_HIDE*/
    @Override
    public void keyTyped(KeyEvent e) {
        synchronized (s1) {
            s1.typed.append(e.getKeyCode());
        }
    }

    /*UML_HIDE*/
    @Override
    public void keyPressed(KeyEvent e) {
        synchronized (s1) {
            s1.keyPressed.add(e.getKeyCode());
            s1.keyHeld.add(e.getKeyCode());
        }
    }

    /*UML_HIDE*/
    @Override
    public void keyReleased(KeyEvent e) {
        synchronized (s1){
            s1.keyHeld.remove(e.getKeyCode());
            s1.keyReleased.add(e.getKeyCode());
        }
    }

    /*UML_HIDE*/
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    /*UML_HIDE*/
    @Override
    public void mousePressed(MouseEvent e) {
        s1.mousePressed[e.getButton()] = true;
        s1.mouseHeld[e.getButton()] = true;
    }

    /*UML_HIDE*/
    @Override
    public void mouseReleased(MouseEvent e) {
        s1.mouseHeld[e.getButton()] = false;
        s1.mouseReleased[e.getButton()] = true;
    }

    /*UML_HIDE*/
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
//        e.
    }

    /*UML_HIDE*/
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    /*UML_HIDE*/
    @Override
    public void mouseExited(MouseEvent e) {

    }
}
