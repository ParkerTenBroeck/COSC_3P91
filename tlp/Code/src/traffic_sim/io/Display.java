package traffic_sim.io;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A window that displays the items drawn to the screen.
 */
public class Display {

    public enum Layer{
        Hud(4),
        TopLevel(3),
        Cars(2),
        Intersections(1),
        Roads(0);
        private final int index;
        Layer(int index){
            this.index = index;
        }
    }

    private BufferedImage[] layers1 = new BufferedImage[Layer.Hud.index+1];
    private BufferedImage[] layers2 = new BufferedImage[Layer.Hud.index+1];
    private Graphics[] graphics = new Graphics[Layer.Hud.index+1];
    private final JFrame frame;
    private final JPanel panel;

    public Display(Input input) {

        int WINDOW_X = 720;
        int WINDOW_Y = 480;

        frame = new JFrame("Traffic Simulator");
        for(int i = 0; i < layers1.length; i ++){
            layers1[i] = new BufferedImage(WINDOW_X, WINDOW_Y, 2);
            layers2[i] = new BufferedImage(WINDOW_X, WINDOW_Y, 2);
        }

        var insets = frame.getInsets();
        var frameHeight = insets.top + insets.bottom + WINDOW_Y;
        var frameWidth = insets.left + insets.right + WINDOW_X;
        frame.setMaximumSize(new Dimension(frameWidth, frameHeight));
        frame.setPreferredSize(new Dimension(frameWidth, frameHeight));
        frame.setMinimumSize(new Dimension(frameWidth, frameHeight));

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                for(int i = 0; i < layers2.length; i ++)
                    g.drawImage(layers2[i], 0, 0, null);
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right));

        panel.setDoubleBuffered(true);
        frame.addKeyListener(input);
        panel.addMouseListener(input);
        panel.addMouseMotionListener(input);

        panel.setSize(new Dimension(WINDOW_X, WINDOW_Y));
        panel.setMaximumSize(new Dimension(WINDOW_X, WINDOW_Y));
        panel.setPreferredSize(new Dimension(WINDOW_X, WINDOW_Y));
        panel.setMinimumSize(new Dimension(WINDOW_X, WINDOW_Y));

        panel.setSize(WINDOW_X, WINDOW_Y);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
        this.update();
    }

    /**
     * Clears all layers in the Display
     */
    public void clearAll() {
        for(int i = 0; i < layers1.length; i ++){
            if (i == 0)
                ((Graphics2D)this.graphics[i]).setBackground(new Color(0,0,0,255));
            else
                ((Graphics2D)this.graphics[i]).setBackground(new Color(0,0,0,0));
            this.graphics[i].clearRect(0,0,getWidth(), getHeight());
        }
    }

    /**
     * @param stroke    Sets the default stroke for all layers
     */
    public void setDefaultStroke(Stroke stroke) {
        for(int i = 0; i < layers1.length; i ++){
            ((Graphics2D)this.graphics[i]).setStroke(stroke);
        }
    }

    /**
     * Actually draw the layers to the screen
     */
    public void update(){
        var tmp = layers1;
        layers1 = layers2;
        layers2 = tmp;
        this.frame.repaint();
        for(int i = 0; i < layers1.length; i ++)
            this.graphics[i] = this.layers1[i].getGraphics();
    }

    /**
     * @param layer The layer of the graphics we want to get
     * @return      The graphics object for the layer
     */
    public Graphics2D getGraphics(Layer layer){
        return (Graphics2D) this.graphics[layer.index];
    }

    /**
     * @return  The height in pixels of the Display
     */
    public int getHeight(){
        return this.layers1[0].getHeight();
    }

    /**
     * @return  The width in pixels of the Display
     */
    public int getWidth(){
        return this.layers1[0].getWidth();
    }


}