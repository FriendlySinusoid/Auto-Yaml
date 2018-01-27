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
    private BufferedImage bufferImage = new BufferedImage(1080,540,BufferedImage.TYPE_INT_RGB);
    private ArrayList<double[]> path = new ArrayList<>();
    private Graphics buffer = bufferImage.getGraphics();

    private enum Mode{
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

        if(mode == Mode.heading){
            buffer.setColor(Color.blue);
            buffer.fillOval( (int) path.get(headingIndex)[0] - 4, (int) path.get(headingIndex)[1] - 4,8,8);
            buffer.drawLine((int) path.get(headingIndex)[0],
                    (int) path.get(headingIndex)[1],(int) path.get(headingIndex)[0] + (int) (30 * Math.cos(path.get(headingIndex)[2])), (int) path.get(headingIndex)[1] + (int) (30 * Math.sin(path.get(headingIndex)[2])));

        }

        g.drawImage(bufferImage,0,0,null);

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_U && mode == Mode.path) {
            path.remove(path.size() - 1);
        }

        if(e.getKeyCode() == KeyEvent.VK_S && mode == Mode.path){
            int answer = JOptionPane.showConfirmDialog(this,"Move onto headings?");
            if(answer == 0)
                mode = Mode.heading;
        }


        if(e.getKeyCode() == KeyEvent.VK_S && mode == Mode.heading){
            int answer = JOptionPane.showConfirmDialog(this,"Save Final Map?");
            if(answer == 0) {
                try {
                    sameMaptoYAML();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(this,"Error | IOException in saving map");
                }
            }
        }

        if(e.getKeyCode() == KeyEvent.VK_ENTER && mode == Mode.heading && headingIndex < path.size()-1){
            headingIndex++;
        }

        if(e.getKeyCode() == KeyEvent.VK_SHIFT && mode == Mode.heading && headingIndex > 0){
            headingIndex--;
        }

    }

    private void sameMaptoYAML() throws IOException {
        Scanner fileScanner = new Scanner(new File("BaseFileHeading.txt"));

        String userDirectory = System.getProperty("user.dir");
        String fileName = JOptionPane.showInputDialog("Enter File Name");

        String firstSegment = new String();
        int countOne = 0;
        while(fileScanner.hasNext() && countOne != 15){
            firstSegment += fileScanner.nextLine();
            countOne += 1;
        }
        


        while(fileName != null && userDirectory != null){
            File PathYAML = new File(userDirectory + "/" + fileName);
            BufferedWriter typewritter = new BufferedWriter(new FileWriter(PathYAML));
//            typewritter.write();
//
//            typewritter.close();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(mode == Mode.path)
            path.add(new double[]{e.getX(), e.getY(), 0});
        if(mode == Mode.heading){
            double angle = 0;
            angle = Math.atan((path.get(headingIndex)[1] - e.getY()) / (path.get(headingIndex)[0] - e.getX()));
            if(path.get(headingIndex)[0] - e.getX() > 0)
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
