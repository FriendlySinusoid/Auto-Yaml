import com.sun.org.apache.xpath.internal.SourceTree;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class PathGenerator extends JFrame implements KeyListener, MouseListener {

    private BufferedImage background = ImageIO.read(new File("field.png"));
    private BufferedImage bufferImage = new BufferedImage(1080, 540, BufferedImage.TYPE_INT_RGB);
    private ArrayList<double[]> path = new ArrayList<>();
    private Graphics buffer = bufferImage.getGraphics();

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

        g.drawImage(bufferImage, 0, 0, null);

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_U && mode == Mode.path) {
            path.remove(path.size() - 1);
        }

        if (e.getKeyCode() == KeyEvent.VK_S && mode == Mode.path) {
            int answer = JOptionPane.showConfirmDialog(this, "Move onto headings?");
            if (answer == 0)
                mode = Mode.heading;
        }


        if (e.getKeyCode() == KeyEvent.VK_S && mode == Mode.heading) {
            int answer = JOptionPane.showConfirmDialog(this, "Save Final Map?");
            if (answer == 0) {
                try {
                    sameMaptoYAML();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error | IOException in saving map");
                }
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER && mode == Mode.heading && headingIndex < path.size() - 1) {
            headingIndex++;
        }

        if (e.getKeyCode() == KeyEvent.VK_SHIFT && mode == Mode.heading && headingIndex > 0) {
            headingIndex--;
        }

    }

    private void sameMaptoYAML() throws IOException {
        Scanner fileScanner = new Scanner(new File("BaseFileHeading.txt"));

        String userDirectory = System.getProperty("user.dir");
        String fileName = JOptionPane.showInputDialog("Enter File Name") + ".yml";

        while (fileName != null && userDirectory != null) {
            File PathYAML = new File(userDirectory + "/" + fileName);
            BufferedWriter typewritter = new BufferedWriter(new FileWriter(PathYAML));
            String finalFile = new String();

            String sectionHeader = new String();
            int countBase = 0;
            while (fileScanner.hasNext() && countBase != 5) {
                if(countBase < 4){
                    sectionHeader += fileScanner.nextLine() + "\n";
                } else if(countBase == 4){
                    sectionHeader += fileScanner.nextLine();
                }
                countBase++;
            }
            System.out.println(sectionHeader);

            String wholeFile = new String();
            while (fileScanner.hasNext()) {
                wholeFile += fileScanner.next();
            }
            System.out.println(wholeFile);
            System.out.println();
//            int beginSub = wholeFile.indexOf('-'), endSub = wholeFile.indexOf('\"');
//            System.out.println();
//            System.out.println("Begin Substring: " + beginSub + " End Substring: " + endSub);
//            String firstSegment = wholeFile.substring(beginSub, endSub);


//            String secondSegment = new String();
//            for (int i = 0; i < path.size(); i++) {
//                double angle = path.get(i)[2];
//                angle = (angle * (180 / Math.PI)) + 180 % 360;
//                if (angle > 180) {
//                    angle = angle - 360;
//                }
//
//                secondSegment += firstSegment + "\n                x: 1080" + "\n                y: 540"
//                        + "\n                theta: " + angle + "\n                deltaTime: 0.05";
//            }
//
//            finalFile += secondSegment;
//            typewritter.write(finalFile);
//
//            typewritter.close();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (mode == Mode.path)
            path.add(new double[]{e.getX(), e.getY(), 0});
        if (mode == Mode.heading) {
            double angle = 0;
            angle = Math.atan((path.get(headingIndex)[1] - e.getY()) / (path.get(headingIndex)[0] - e.getX()));
            if (path.get(headingIndex)[0] - e.getX() > 0)
                angle -= Math.PI;
            path.get(headingIndex)[2] = angle;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

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
