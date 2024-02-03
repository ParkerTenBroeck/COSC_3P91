package traffic_sim.io;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

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

    private BufferedImage[] layers = new BufferedImage[Layer.Hud.index+1];
    private Graphics[] graphics = new Graphics[Layer.Hud.index+1];
    private final JFrame frame;
    private final JPanel panel;

    public Display(Input input) {

        int WINDOW_X = 1920;
        int WINDOW_Y = 1080;

        frame = new JFrame("Traffic Simulator");
        for(int i = 0; i < layers.length; i ++)
            layers[i] = new BufferedImage(WINDOW_X, WINDOW_Y, 2);

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
                for(int i = 0; i < layers.length; i ++)
                    g.drawImage(layers[i], 0, 0, null);
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

    public void clear() {
        for(int i = 0; i < layers.length; i ++){
            if (i == 0)
                ((Graphics2D)this.graphics[i]).setBackground(new Color(0,0,0,255));
            else
                ((Graphics2D)this.graphics[i]).setBackground(new Color(0,0,0,0));
            this.graphics[i].clearRect(0,0,getWidth(), getHeight());
        }
    }

    public void setDefaultStroke(Stroke stroke) {
        for(int i = 0; i < layers.length; i ++){
            ((Graphics2D)this.graphics[i]).setStroke(stroke);
        }
    }

    public void update(){
        this.frame.repaint();
        for(int i = 0; i < layers.length; i ++)
            this.graphics[i] = this.layers[i].getGraphics();
    }

    public Graphics2D getGraphics(Layer layer){
        return (Graphics2D) this.graphics[layer.index];
    }

    public int getHeight(){
        return this.layers[0].getHeight();
    }

    public int getWidth(){
        return this.layers[0].getWidth();
    }


}