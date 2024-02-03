package traffic_sim.io;

import java.awt.event.*;
import java.util.HashSet;

public class Input implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {




    private static class Storage implements Cloneable{
        private HashSet<Character> keyPressed = new HashSet<>();
        private HashSet<Character> keyHeld = new HashSet<>();
        private HashSet<Character> keyReleased = new HashSet<>();
        private boolean[] mousePressed = new boolean[4];
        private boolean[] mouseHeld = new boolean[4];
        private boolean[] mouseReleased = new boolean[4];
        private float mouseX = 0.0f;
        private float mouseY = 0.0f;


        @Override
        public Storage clone(){
            var other = new Storage();
            other.keyPressed = (HashSet<Character>) this.keyPressed.clone();
            other.keyHeld = (HashSet<Character>) this.keyHeld.clone();
            other.keyReleased = (HashSet<Character>) this.keyReleased.clone();
            other.mousePressed = this.mousePressed.clone();
            other.mouseHeld = this.mouseHeld.clone();
            other.mouseReleased = this.mouseReleased.clone();
            other.mouseX = this.mouseX;
            other.mouseY = this.mouseY;
            return other;
        }
    }

    final Storage s1 = new Storage();
    Storage s2 = new Storage();




    public boolean keyPressed(char key){
        return s2.keyPressed.contains(key);
    }
    public boolean keyHeld(char key){
        return s2.keyHeld.contains(key);
    }
    public boolean keyReleased(char key){
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
        }

    }


    @Override
    public void mouseDragged(MouseEvent e) {
        s1.mouseX = e.getX();
        s1.mouseY = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        s1.mouseX = e.getX();
        s1.mouseY = e.getY();
    }


    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        synchronized (s1) {
            s1.keyPressed.add(e.getKeyChar());
            s1.keyHeld.add(e.getKeyChar());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        synchronized (s1){
            s1.keyHeld.remove(e.getKeyChar());
            s1.keyReleased.add(e.getKeyChar());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        s1.mousePressed[e.getButton()] = true;
        s1.mouseHeld[e.getButton()] = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        s1.mouseHeld[e.getButton()] = false;
        s1.mouseReleased[e.getButton()] = true;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
//        e.
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
