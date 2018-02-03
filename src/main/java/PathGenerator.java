import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class PathGenerator extends JFrame implements KeyListener, MouseListener {

    private BufferedImage background = ImageIO.read(new File("Field.png"));
    private BufferedImage bufferImage = new BufferedImage(1080, 540, BufferedImage.TYPE_INT_RGB);
    private ArrayList<double[]> path = new ArrayList<>();
    private Graphics buffer = bufferImage.getGraphics();
    private Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

    private static final String address = "[PLACEHOLDER]";

    private enum Mode {
        path,
        heading
    }

    private int headingIndex = 0;

    private Mode mode = Mode.path;

    public static void main(String args[]) {
        try {
            PathGenerator pg = new PathGenerator();
            pg.paint(pg.getGraphics());
            while (true) {
                pg.repaint();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    public PathGenerator() throws IOException {
        this.setSize(1080, 540);
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.addMouseListener(this);
        this.addKeyListener(this);
        this.setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        buffer.drawImage(background, 0, 0, 1080, 540, null);
        if (path.size() >= 2) {
            buffer.setColor(Color.RED);
            double[] last = path.get(0);
            for (int i = 1; i < path.size(); i++) {
                buffer.drawLine((int) last[0], (int) last[1], (int) path.get(i)[0], (int) path.get(i)[1]);
                last = path.get(i);
            }
        }

        if (mode == Mode.heading) {
            buffer.setColor(Color.blue);
            buffer.fillOval((int) path.get(headingIndex)[0] - 4, (int) path.get(headingIndex)[1] - 4, 8, 8);
            buffer.drawLine((int) path.get(headingIndex)[0],
                    (int) path.get(headingIndex)[1], (int) path.get(headingIndex)[0] + (int) (30 * Math.cos(path.get(headingIndex)[2])), (int) path.get(headingIndex)[1] + (int) (30 * Math.sin(path.get(headingIndex)[2])));

        }

        buffer.setFont(new Font("TimesRoman", 1,20));
        switch(mode){
            case heading:
                buffer.setColor(Color.BLUE);
                buffer.drawString("Shift: Last Point",10,50);
                buffer.drawString("Enter: Next Point",10,70);
                buffer.drawString("S: Save",10,90);
                break;
            case path:
                buffer.setColor(Color.BLUE);
                buffer.drawString("Left Click: New Point",10,50);
                buffer.drawString("U: Undo Last Placement",10,70);
                buffer.drawString("S: Move on to Heading Assignment",10,90);
                break;
            default:
                break;
        }


        g.drawImage(bufferImage, 0, 0, null);

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_U && mode == Mode.path && path.size() > 0) {
            path.remove(path.size() - 1);
        }else
        if (e.getKeyCode() == KeyEvent.VK_S) {
            if(mode == Mode.path) {
                int answer = JOptionPane.showConfirmDialog(this, "Move onto headings?");
                if (answer == 0)
                    mode = Mode.heading;
            }
            else if(mode == Mode.heading){
                int answer = JOptionPane.showConfirmDialog(this, "Save Final Map?");
                if (answer == 0) {
                    try {
                        saveMaptoYAML();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error | IOException in Saving Map");
                    }
                }
            }
        }else
        if (e.getKeyCode() == KeyEvent.VK_ENTER && mode == Mode.heading && headingIndex < path.size() - 1) {
            headingIndex++;
        }else
        if (e.getKeyCode() == KeyEvent.VK_SHIFT && mode == Mode.heading && headingIndex > 0) {
            headingIndex--;
        }

    }

    private void saveMaptoYAML() throws IOException {
        String file;
        try {
            file = JOptionPane.showInputDialog("File name") + ".yml";
        }catch(IndexOutOfBoundsException e){
            e.printStackTrace();
            file = "AutoPath.yml";
            System.out.println("Continuing save, default file " + file);
        }
        PrintWriter output = new PrintWriter(new File(file));
        String fileHeading =
                "autoStartupCommand:\n" +
                "    org.usfirst.frc.team449.robot.commands.general.CommandSequence:\n" +
                "        '@id': autoStartupCommandRightSide\n" +
                "        commandList:";
        String goToCommandHeading =
                "           - org.usfirst.frc.team449.robot.subsystem.interfaces.motionProfile.TwoSideMPSubsystem.commands.GoToPosition:\n" +
                "                '@id': ";
        String firstGoToCommandBody =
                "                deltaTime: .05\n" +
                        "                poseEstimator:\n" +
                "                    org.usfirst.frc.team449.robot.other.UnidirectionalPoseEstimator:\n" +
                "                        poseEstimator\n" +
                "                pathRequester:\n" +
                "                    org.usfirst.frc.team449.robot.components.PathRequester:\n" +
                "                        '@id': autoPath\n" +
                "                        # This is Unchecked, just a place holder\n" +
                "                        address: " + address;
        String goToCommandBody =
                "                deltaTime: .05\n" +
                        "                poseEstimator:\n" +
                        "                    org.usfirst.frc.team449.robot.other.UnidirectionalPoseEstimator:\n" +
                        "                        poseEstimator\n" +
                        "                pathRequester:\n" +
                        "                    org.usfirst.frc.team449.robot.components.PathRequester:\n" +
                        "                        autoPath";
        output.println(fileHeading);
        output.println(goToCommandHeading + System.nanoTime());
        output.println(firstGoToCommandBody);
        double[] head = path.get(0);
        head[2] = -head[2] * 180 / Math.PI ;
        head[2] = head[2] + ((head[2] <= -180) ? 360 : 0);
        head[0] = head[0] / this.getWidth() * 54;
        head[1] = head[1] / this.getHeight() * 27;

        output.println(goToCommandHeading + System.nanoTime());
        output.println(goToCommandBody);
        output.println("                x: " + head[0]);
        output.println("                y: " + head[1]);
        output.println("                theta: " + head[2]);
        path.remove(0);
        for(double[] node : path){
            //Normalize values to fit with the field
            node[2] = -node[2] * 180 / Math.PI ;
            node[2] = node[2] + ((node[2] <= -180) ? 360 : 0);
            node[0] = node[0] / this.getWidth() * 54;
            node[1] = node[1] / this.getHeight() * 27;

            output.println(goToCommandHeading + System.nanoTime());
            output.println(goToCommandBody);
            output.println("                x: " + node[0]);
            output.println("                y: " + node[1]);
            output.println("                theta: " + node[2]);

        }
        output.close();
//        JOptionPane.showMessageDialog(this,"Finished");
        System.out.println("Finished save");
        System.exit(0);
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (mode == Mode.path)
            path.add(new double[]{e.getX(), e.getY(), 0});
        else if (mode == Mode.heading) {
            double angle = 0;
            angle = Math.atan((path.get(headingIndex)[1] - e.getY()) / (path.get(headingIndex)[0] - e.getX()));
            if (path.get(headingIndex)[0] - e.getX() > 0)
                angle -= Math.PI;
            path.get(headingIndex)[2] = angle;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
