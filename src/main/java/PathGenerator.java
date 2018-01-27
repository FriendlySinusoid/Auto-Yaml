import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PathGenerator extends JFrame {

    private BufferedImage background = ImageIO.read(new File("Field.png"));

    private ArrayList<double[]> path = new ArrayList<>();

    public static void main(String args[]){
        try {
            PathGenerator pg = new PathGenerator();



        }catch(IOException e){
            e.printStackTrace();
            System.exit(1);
        }

    }

    public PathGenerator() throws IOException{
        this.setSize(540,1080);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);


    }

    @Override
    public void paint(Graphics g){
        g.drawImage(background,0,0,this.getWidth(),this.getHeight(),null);
    }
}
