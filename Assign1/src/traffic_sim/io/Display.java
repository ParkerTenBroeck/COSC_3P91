package traffic_sim.io;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Display {


    private BufferedImage image;
    private Graphics graphics;
    private final JFrame frame;
    private final JPanel panel;

    public Display(Input input) {

        int WINDOW_X = 1000;
        int WINDOW_Y = 600;

        frame = new JFrame("Traffic Simulator");
        image = new BufferedImage(WINDOW_X, WINDOW_Y, 1);

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
                g.drawImage(image, 0, 0, null);
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

    public void update(){
        this.frame.repaint();
        this.graphics = this.image.getGraphics();
    }

    public Graphics2D getGraphics(){
        return (Graphics2D) this.graphics;
    }

    public int getHeight(){
        return this.image.getHeight();
    }

    public int getWidth(){
        return this.image.getWidth();
    }


}