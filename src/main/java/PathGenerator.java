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

	private BufferedImage background = ImageIO.read(new File("Field.png"));
	private BufferedImage bufferImage = new BufferedImage(1080, 540, BufferedImage.TYPE_INT_RGB);
	private ArrayList<double[]> path = new ArrayList<>();
	private Graphics buffer = bufferImage.getGraphics();
	private Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

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
					saveMaptoYAML();
				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(this, "Error | IOException in Saving Map");
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

	private void saveMaptoYAML() throws IOException {
		Scanner fileScanner = new Scanner(new File("BaseFileHeading.txt"));

		String userDirectory = System.getProperty("user.dir");
		String fileNameBase = JOptionPane.showInputDialog("Enter File Name");

		if(fileNameBase != "" || fileNameBase != null) {
			String fileName = fileNameBase + ".yml";

			if (fileName != null && userDirectory != null) {
				File PathYAML = new File(userDirectory + "/" + fileName);
				BufferedWriter typewritter = new BufferedWriter(new FileWriter(PathYAML));
				String cleanStringFINAL = new String();

				String sectionHeader = new String();
				int countBase = 0;
				while (fileScanner.hasNext() && countBase != 2) {
					if (countBase < 1) {
						sectionHeader += fileScanner.nextLine() + "\n";
					} else if (countBase == 1) {
						sectionHeader += fileScanner.nextLine();
					}
					countBase++;
				}
				System.out.println(sectionHeader);
				System.out.println();

				String commandIdOne = JOptionPane.showInputDialog("Enter Custom @id");
				String commandIdTwo = JOptionPane.showInputDialog("Enter Another Custom @id");

				if(commandIdOne != "" || commandIdOne != null) {
					String wholeFile = new String();
					while (fileScanner.hasNext()) {
						wholeFile += fileScanner.nextLine();
					}
//			System.out.println(wholeFile);
//			System.out.println();

				int beginSub = wholeFile.indexOf("- org"), endSub = wholeFile.indexOf("ion:") + 4;
					System.out.println();

					System.out.println("Begin Substring: " + beginSub + " End Substring: " + endSub);
					System.out.println();
					String firstSegment = wholeFile.substring(beginSub, endSub);

					String dirtyStringAddition = sectionHeader + "\n" + "\t" + "\t" + "\'@id\': " + commandIdOne + "\n" + "\t" + "\t" + "commandList:" + "\n" + "\t" + "\t" + "\t" + firstSegment + "" +
							"\n" + "\t" + "\t" + "\t" + "\'@id\': " + commandIdTwo;
					System.out.println(dirtyStringAddition);

					String secondSegment = new String();
					for (int i = 0; i < path.size(); i++) {
						double angle = path.get(i)[2];
						angle = ((angle * (180 / Math.PI)) + 180) % 360;
						if (angle > 180) {
							angle = angle - 360;
						}
						secondSegment += firstSegment + "\n                x: 1080" + "\n                y: 540"
								+ "\n                theta: " + angle + "\n                deltaTime: 0.05" + "\n" + "\t" + "\t" + "\t";
					}

					cleanStringFINAL = dirtyStringAddition + secondSegment;

					System.out.println();
					System.out.println("Final YAML File: \n");
					System.out.println("\n" + cleanStringFINAL);

					typewritter.write(cleanStringFINAL);

					typewritter.close();
				} else {
					JOptionPane.showMessageDialog(this, "Error | Unacceptable Id");    /** COME BACK TO THIS COME BACK TO THIS COME BACK TO THIS COME BACK TO THIS COME BACK TO THIS */
				}
			} else {
				JOptionPane.showMessageDialog(this, "Error | FileNotFoundException (Base FileHeading.txt)");    /** COME BACK TO THIS COME BACK TO THIS COME BACK TO THIS COME BACK TO THIS */
			}
		} else {
			JOptionPane.showMessageDialog(this, "Error | Unacceptable File Name");   /** COME BACK TO THIS COME BACK TO THIS COME BACK TO THIS COME BACK TO THIS COME BACK TO THIS COME BACK TO THIS */
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
