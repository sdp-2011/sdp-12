package main.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.event.MouseInputAdapter;

@SuppressWarnings("serial")
public class Simulator extends JComponent implements ActionListener {
	// Objects for the simulator
	private Ball ball;
	private Pitch pitch;
	private Robot robot1;
	private Robot robot2;
	
	CollisionDetector collisionDetector;
	
	// Variable to switch collisions on and off
	private boolean collisionsEnabled;
	
	// List of drawables
	private ArrayList<Drawable> drawables = new ArrayList<Drawable>();
	
	// Walls of the pitch
	private ArrayList<Wall> walls;
	
	// Buffer used to paint on to avoid flickering
	private Graphics2D g2d;
	private Image backBufferImage;
	
	// Timer
	private Timer timer;
	
	/**
	 * Constructor for simulator
	 * 
	 * @param pitch
	 * @param robot1
	 * @param robot2
	 * @param ball
	 */
	public Simulator(Pitch pitch, Robot robot1, Robot robot2, Ball ball) {
		this.pitch = pitch;
		this.robot1 = robot1;
		this.robot2 = robot2;
		this.ball = ball;
		
		collisionDetector = new CollisionDetector();
		collisionDetector.addListener(robot1);
		collisionDetector.addListener(robot1.getKicker());
		collisionDetector.addListener(robot2);
		collisionDetector.addListener(ball);
		
		collisionsEnabled = true;
		initializeWalls();
		setFocusable(true);
		addListeners();
		timer = new Timer(15, this);
	}
	
	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		// Remove drawables with old positions
		//drawables.clear();
		
		// Update the simulator and advance objects
		simulatorUpdate();
		// Draw the objects with their new locations
		simulatorRender();
		
		// Request a repaint with the new locations
		repaint();
	}
	
	private void simulatorUpdate() {
		robot1.updateLocation();
		robot2.updateLocation();
		
		if (collisionsEnabled) {
			collisionDetector.checkCollisions();
		}
		
//		ArrayList<Line2D> r1lines = ball.getShapeSides();
//		for(Line2D line : r1lines)
//		drawables.add(new Drawable(Drawable.LINE,
//				(int) line.getX1(), (int) line.getY1(), (int) line.getX2(), (int) line.getY2(), Color.RED));
	}
	
	/**
	 * Draw object on back buffer
	 * 
	 * @param g2d
	 */
	private void simulatorRender() {
		g2d = getImageGraphics();
		setGraphicsSettings(g2d);
		
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.fillRect(0, 0, pitch.getWidth() + 350, pitch.getHeight());
		
		g2d.drawImage(pitch.getImage(), 0, 0, null);
		ball.draw(g2d);
		robot2.draw(g2d);
		robot1.draw(g2d);
		g2d.draw(ball.getShape());
		g2d.draw(robot2.getShape());
		g2d.draw(robot1.getShape());
//        for(Point2D point : robot1.getCorners()) {
//        	drawPoint(point.getX(), point.getY());
//        	System.out.println(robot1.getShape().contains(point));
//        }
		drawDrawables(g2d);
	}
	
	/**
	 * Paint the buffer image to the screen
	 */
	protected void paintComponent(Graphics graphics) {
		graphics.drawImage(backBufferImage, 0, 0, null);
	}
	
	/**
	 * Get graphics from image to paint on
	 * 	Used for double buffering
	 * 
	 * @return
	 */
	private Graphics2D getImageGraphics() {
		if(backBufferImage == null) {
			backBufferImage = createImage(pitch.getWidth() + 350, pitch.getHeight());
			if(backBufferImage == null) {
				System.out.println("backBufferImage is null");
				return null;
			} else {
				g2d = (Graphics2D) backBufferImage.getGraphics();
			}
		}
		
		return g2d;
	}
	
	/**
	 * Draw all the drawables add to the ArrayList
	 * 
	 * @param g2d
	 */
	public synchronized void drawDrawables(Graphics2D g2d) {
		if(drawables != null) {
			for(Drawable drawable : drawables) {
				drawable.draw(g2d);
			}
		}
	}
	
	/**
	 * Enable antialiasing
	 * 
	 * @param g2d
	 */
	private void setGraphicsSettings(Graphics2D g2d) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
	}
	
	protected void drawPoint(AbstractSimulatedObject ampo, String label) {
		drawables.add(new Drawable(Drawable.CIRCLE,
						(int) ampo.getXPos(), (int) ampo.getYPos(), Color.WHITE));
		if (label != null) {
			drawables.add(new Drawable(Drawable.LABEL,
					label,
					(int) ampo.getXPos() + 5, (int) ampo.getYPos(), Color.WHITE));
		}
	}
	
	protected void drawPoint(double xPos, double yPos) {
		drawables.add(new Drawable(Drawable.CIRCLE,
						(int) xPos, (int) yPos, Color.WHITE));
	}
	
	/**
	 * Define the limits of the pitch 
	 */
	private void initializeWalls() {
		Wall top = new Wall(new Rectangle(30, 0, 705, 107), Wall.TOP_WALL);
		Wall bottom = new Wall(new Rectangle(30, 477, 705, 100), Wall.BOTTOM_WALL);
		Wall leftTop = new Wall(new Rectangle(0, 107, 30, 97), Wall.UPPER_LEFT_WALL);
		Wall rightTop = new Wall(new Rectangle(735, 107, 30, 92), Wall.UPPER_RIGHT_WALL);
		Wall leftBottom = new Wall(new Rectangle(0, 380, 30, 97), Wall.LOWER_LEFT_WALL);
		Wall rightBottom = new Wall(new Rectangle(735, 380, 30, 97), Wall.LOWER_RIGHT_WALL);
		
		walls = new ArrayList<Wall>();
		
		walls.add(top);
		walls.add(bottom);
		walls.add(leftTop);
		walls.add(rightTop);
		walls.add(leftBottom);
		walls.add(rightBottom);
	}
	
	/**
	 * Flip the objects robots that each
	 * 	of the variables is pointing to
	 */
	public void flipRobots() {
		Robot robotTemp = robot1;
		robot1 = robot2;
		robot2 = robotTemp;
		robotTemp = null;
	}
	
	/**
	 * Add listeners for keyboard and mouse shortcuts
	 */
	private void addListeners() {
		addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent keyEvent) {
				if(keyEvent.getKeyCode() == KeyEvent.VK_R) {
					getRobot1().toggleCommandReceiving();
				} else if (keyEvent.getKeyCode() == KeyEvent.VK_B) {
					getBall().stop();
				} else if (keyEvent.getKeyCode() == KeyEvent.VK_SPACE) {
					getRobot1().kick();
				}
			}
		
			@Override
			public void keyReleased(KeyEvent arg0) {}

			@Override
			public void keyTyped(KeyEvent arg0) {}
		});
				
		addMouseListener(new MouseInputAdapter() {
			public void mousePressed(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				
				if (e.getButton() == MouseEvent.BUTTON1) {
					getBall().setXPos(x);
					getBall().setYPos(y);
				} else if(e.getButton() == MouseEvent.BUTTON3) {
					getRobot1().setXPos(x);
					getRobot1().setYPos(y);
					getRobot1().getKicker().updateLocation(
							getRobot1().getXPos(), getRobot1().getYPos(), getRobot1().getTheta());
				}
			}
		});
	}
	
	/**
	 * The simulator starts executing when the timer starts
	 */
	public void start() {
		timer.start();
	}
	
	public synchronized void addDrawables(ArrayList<Drawable> drawables) {
		for(Drawable drawable : drawables) {
			this.drawables.add(drawable);
		}
	}
	
	/*
	 * GETTERS AND SETTERS
	 */
	
	public Robot getRobot1() { return robot1; }
	public Robot getRobot2() { return robot2; }
	public Ball getBall() { return ball; }
	public Pitch getPitch() { return pitch; }
	
	// TODO Make it add to the existing drawables or 
	// 		make a separate method to do this
	public void setDrawables(ArrayList<Drawable> drawables) { this.drawables = drawables; }
}
