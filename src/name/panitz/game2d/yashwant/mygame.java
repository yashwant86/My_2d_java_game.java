package name.panitz.game2d.yashwant;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import name.panitz.game2d.*;

// RedBox class to represent falling red boxes
class RedBox extends AbstractGameObj {
	public RedBox(Vertex corner, Vertex movement) {
		super(corner, movement, 50, 50);
	}

	@Override
	public void paintTo(Graphics g) {
		g.setColor(Color.RED);
		g.fillRect((int) pos.x, (int) pos.y, (int) width, (int) height);
	}
}

// BlueBox class to represent the player-controlled blue box
class BlueBox extends AbstractGameObj {
	public BlueBox(Vertex corner, Vertex movement) {
		super(corner, movement, 50, 50);
	}

	@Override
	public void paintTo(Graphics g) {
		g.setColor(Color.BLUE);
		g.fillRect((int) pos.x, (int) pos.y, (int) width, (int) height);
	}

	@Override
	public void move() {
		pos.add(velocity);

		if (pos.x < 0) {
			pos.x = 0;
		} else if (pos.x + width > GameImpl.getWidth()) {
			pos.x = GameImpl.getWidth() - width;
		}
	}
}

class BlackBox extends AbstractGameObj {
	public BlackBox(Vertex corner, Vertex movement) {
		super(corner, movement, 50, 50); // Size can be adjusted
	}

	@Override
	public void paintTo(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect((int) pos.x, (int) pos.y, (int) width, (int) height);
	}

	public Vertex getVelocity() {   // Add this method to access the velocity
		return velocity;
	}
}

// Game class that implements the game logic
class GameImpl implements Game {
	private static final int WIDTH = 800; // Adjust the game window width
	private static final int HEIGHT = 600; // Adjust the game window height
	private static final int INITIAL_LIVES = 3;

	private BlueBox player;
	private List<RedBox> redBoxes;
	private List<BlackBox> blackBoxes;
	private int score = 0;
	private int lives = INITIAL_LIVES;
	private boolean gameOver = false;
	private int highScore = 0;
	private static final String HIGH_SCORE_FILE = "highscore.txt"; // File to store the high score

	private long gameStartTime;
	private Timer spawnTimer;
	private Timer difficultyTimer;
	private boolean confirmationDialogShown = false; // Flag to track whether the confirmation dialog is shown

	// Add this method to load the high score from a file
	private void loadHighScore() {
		try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
			String line = reader.readLine();
			if (line != null && !line.isEmpty()) {
				highScore = Integer.parseInt(line);
			}
		} catch (IOException | NumberFormatException e) {
			e.printStackTrace();
		}
	}

	// Add this method to save the high score to a file
	private void saveHighScore() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
			writer.write(Integer.toString(highScore));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public GameImpl() {
		redBoxes = new ArrayList<>();
		player = new BlueBox(new Vertex(WIDTH / 2, HEIGHT - 50), new Vertex(0, 0));
		score = 0;
		lives = INITIAL_LIVES;
		blackBoxes = new ArrayList<>();
		gameOver = false;
		loadHighScore();
		init();
		initGame();
	}

	public static int getWidth() {
		return WIDTH;
	}

	public static int getHeight() {
		return HEIGHT;
	}

	@Override
	public int width() {
		return WIDTH;
	}

	@Override
	public int height() {
		return HEIGHT;
	}

	@Override
	public GameObj player() {
		return player;
	}

	@Override
	public int getCurrentLevelIndex() {
		return 0;
	}

	@Override
	public int getmyScore() {
		return score;
	}

	@Override
	public int getmyLives() {
		return lives;
	}

	private void initGame() {
		player = new BlueBox(new Vertex(WIDTH / 2, HEIGHT - 50), new Vertex(0, 0));
		redBoxes.clear();
		blackBoxes.clear();
		score = 0;
		lives = INITIAL_LIVES;
		gameOver = false;
		gameStartTime = System.currentTimeMillis();
		spawnRedBoxes();
		spawnBlackBoxes();
		increaseDifficultyOverTime();
	}

	private void spawnRedBoxes() {
		spawnTimer = new Timer(2000, e -> {
			Random rand = new Random();
			int x = rand.nextInt(WIDTH - 50);
			RedBox redBox = new RedBox(new Vertex(x, -50), new Vertex(0, 2));
			redBoxes.add(redBox);
		});
		spawnTimer.start();
	}

	private void spawnBlackBoxes() {
		spawnTimer = new Timer(5000, e -> {
			Random rand = new Random();
			int x = rand.nextInt(WIDTH - 50);
			BlackBox blackBox = new BlackBox(new Vertex(x, -50), new Vertex(0, 3));
			blackBoxes.add(blackBox);
		});
		spawnTimer.start();
	}

	private void increaseDifficultyOverTime() {
		difficultyTimer = new Timer(30000, e -> {
			spawnTimer.setDelay(Math.max(1000, spawnTimer.getDelay() - 100)); // Reduce delay to spawn boxes faster
			for (BlackBox blackBox : blackBoxes) {
				blackBox.getVelocity().y += 1; // Increase falling speed of black boxes
			}
		});
		difficultyTimer.start();
	}

	@Override
	public List<List<? extends GameObj>> goss() {
		List<List<? extends GameObj>> allObjects = new ArrayList<>();
		allObjects.add(redBoxes); // Add red boxes for rendering
		allObjects.add(blackBoxes); // Add black boxes for rendering, assuming you've added this list to the class
		allObjects.add(List.of(player)); // Add the player
		return allObjects;
	}

	@Override
	public void init() {
		// Initialize the game (e.g., set up the game window)
		JFrame frame = new JFrame("2D Game");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new SwingScreen(this));
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public void doChecks() {
		// Check for collisions between blue box and red boxes
		for (RedBox redBox : redBoxes) {
			if (player.touches(redBox)) {
				score++;
				redBoxes.remove(redBox);
				spawnRedBox();
				return; // Only one collision per frame
			}
		}
		for (int i = 0; i < blackBoxes.size(); i++) {
			BlackBox blackBox = blackBoxes.get(i);
			if (player.touches(blackBox)) {
				lives--; // Decrease life
				blackBoxes.remove(i); // Remove the black box after collision

				// Check if the game should end due to losing all lives
				if (lives <= 0) {
					gameOver = true;
					showGameOverDialog();
					return; // End the game immediately
				}

				// Optionally, respawn the black box to continue the game
				spawnBlackBoxes();
				return; // Exit early since the list has been modified
			}

			// Remove the black box if it moves out of the screen
			Vertex objectPos = blackBox.getPos();
			if (player.getPos().equals(objectPos)) {
				// Your logic here
			}

		}
	}

	@Override
	public void keyPressedReaction(KeyEvent keyEvent) {
		// Handle user input to move the blue box (player)
		int keyCode = keyEvent.getKeyCode();
		if (keyCode == KeyEvent.VK_LEFT) {
			player.velocity().x = -5; // Adjust the movement speed
		} else if (keyCode == KeyEvent.VK_RIGHT) {
			player.velocity().x = 5; // Adjust the movement speed
		}
	}

	@Override
	public boolean won() {
		// Game win condition (not used in this example)
		return false;
	}

	@Override
	public boolean lost() {
		// Game over condition
		return gameOver;
	}

	@Override
	public void Play() {
		// Unused method
	}

	private void spawnRedBox() {
		Random rand = new Random();
		int x = rand.nextInt(WIDTH - 50); // Adjust the range as needed
		int y = -rand.nextInt(70); // Random Y-offset for the starting position
		RedBox redBox = new RedBox(new Vertex(x, y), new Vertex(0, 2)); // Create a RedBox without an image
		redBoxes.add(redBox);
	}

	private void showGameOverDialog() {
		String title = "Game Over";
		String message;

		if (getmyScore() > highScore) {
			highScore = getmyScore(); // Update the high score
			saveHighScore(); // Save the high score to a file
			message = "Congratulations! You've achieved a new high score: " + highScore;
		} else {
			message = "Your Score: " + getmyScore() + "\nHigh Score: " + highScore;
		}

		JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);

		// Prompt the player to continue the game
		int choice = JOptionPane.showConfirmDialog(null, "Do you want to continue playing?", "Continue", JOptionPane.YES_NO_OPTION);
		if (choice == JOptionPane.YES_OPTION) {
			initGame(); // Restart the game
		} else {
			System.exit(0); // Exit the game
		}
	}
}

// SwingScreen class for rendering the game
class SwingScreen extends JPanel {
	private Game logic;
	private Timer timer;

	public SwingScreen(Game logic) {
		this.logic = logic;
		timer = new Timer(16, (e) -> {
			logic.move();
			logic.doChecks();
			repaint();
		});
		timer.start();

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				logic.keyPressedReaction(e);
			}
		});
		setFocusable(true);
		requestFocus();
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(logic.width(), logic.height());
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		logic.paintTo(g); // Assuming this method is meant to draw game objects

		// Draw the score
		g.setColor(Color.BLACK);
		g.setFont(new Font("Arial", Font.BOLD, 16));
		g.drawString("Score: " + logic.getmyScore(), 10, 20); // Use the corrected method name
		g.drawString("Lives: " + ((GameImpl)logic).getmyLives(), 10, 40); // You need to implement getLives in GameImpl
	}
}

// Entry point of the game
public class mygame {
	public static void main(String[] args) {
		Game game = new GameImpl();
		game.play();
	}
}
