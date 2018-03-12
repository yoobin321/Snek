import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Game extends JFrame implements ActionListener {
	private static String[] diff = {"Easy", "Normal", "Hard", "Animisa", "Two Peeps"};
	private static ImageIcon icon = new ImageIcon("snek.png");

	static final int WIDTH = 640;
	static final int HEIGHT = 480;
	static final int SSIZE = 10;

	private Snake p = new Snake();
	private Timer t = new Timer(60, this);

	private Game(String difficulty) {
		getContentPane().setBackground(Color.WHITE);
		t.setActionCommand("Not");
		t.setDelay(difficulty(difficulty));

		play();
	}

	private void play() {
		setTitle("Snake");
		setResizable(false);
		setSize(WIDTH, HEIGHT);
		try {
			setIconImage(ImageIO.read(new File("snek.png")));
		} catch (Exception e) {
			System.out.println("Invalid Path");
		}

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		add(p);

		t.start();

		setVisible(true);
	}

	private int difficulty(String d) {
		if (d.equals(diff[0])) {
			addKeyListener(new KeyAdapter());
			return 60;
		} else if (d.equals(diff[1])) {
			addKeyListener(new KeyAdapter());
			return 30;
		} else if (d.equals(diff[2])) {
			addKeyListener(new RKeyAdapter());
			return 60;
		} else if (d.equals(diff[3])) {
			addKeyListener(new KeyAdapter());
			p = new Snake(true);
			return 60;
		} else {
			addKeyListener(new KeyAdapter());
			p = new Snake(0);
			t.setActionCommand("Two Player");
			return 60;
		}
	}

	static class Snake extends JComponent {
		final int GRAV = 10;

		boolean gameOver = false, on = false, too = false;
		int size = 0, size2 = 0;
		String winner = "";
		boolean pressedE = false;
		boolean pressed1 = false;
		BufferedImage fire;

		ArrayList<Rectangle2D.Double> snek = new ArrayList<>();
		ArrayList<Rectangle2D.Double> snek2 = new ArrayList<>();

		Module piece = new Module();
		ArrayList<Module> obstacles = new ArrayList<>(100);
		Direction dir = Direction.DOWN;
		Direction dir2 = Direction.UP;

		public Snake() {
			setPreferredSize(new Dimension(SSIZE, SSIZE));
			snek.add(new Rectangle2D.Double(100, 100, SSIZE, SSIZE));
			try {
				fire = ImageIO.read(new URL("https://upload.wikimedia.org/wikipedia/en/9/93/Tanooki_Mario.jpg"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public Snake(boolean turn) {
			this();
			on = turn;
			obstacles.add(new Module());
		}

		public Snake(int two) {
			this();
			if (two == 0) snek2.add(new Rectangle2D.Double(150, 100, SSIZE, SSIZE));
			too = true;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (gameOver) {
				if (too) {
					g.setColor(Color.PINK);
					g.setFont(new Font("Calibri", Font.BOLD, 20));
					g.drawString(winner, (Game.WIDTH / 2) - (winner.length()) * 10, Game.HEIGHT / 2);
					return;
				}
				File file = new File("high.txt");
				int highscore;
				StringBuilder sb = new StringBuilder();

				try {
					BufferedReader buff = new BufferedReader(new FileReader(file));
					String line = buff.readLine();

					while (line != null) {
						sb.append(line);
						line = buff.readLine();
					}
					buff.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				highscore = Integer.parseInt(sb.toString());

				if (size > highscore) {
					highscore = size;

					try (BufferedWriter write = new BufferedWriter(new FileWriter(file))) {
						write.write("" + size + "\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				String message = "GAME OVER LOSER", score = "SCORE: " + size, high = "HIGHSCORE: " + highscore;

				g.setColor(Color.PINK);
				g.setFont(new Font("Calibri", Font.BOLD, 20));
				g.drawString(message, (Game.WIDTH / 2) - (message.length() * 2), Game.HEIGHT / 2);
				g.drawString(score, (Game.WIDTH / 2), Game.HEIGHT / 2 + 15);
				g.drawString(high, (Game.WIDTH / 2), Game.HEIGHT / 2 + 30);
				return;
			}
			Graphics2D g2 = (Graphics2D) g;

			g2.setColor(Color.black);
			if (pressedE)
				g2.drawImage(fire, new AffineTransformOp(new AffineTransform(1/8, 0, 1/8, 0, 0, 0),
						AffineTransformOp.TYPE_NEAREST_NEIGHBOR), 100, 100);

			if (pressed1) {
				g2.drawString("Christos is a poop", ThreadLocalRandom.current().nextInt(50, Game.WIDTH - 50),
						ThreadLocalRandom.current().nextInt(50, Game.HEIGHT - 50));
			}

			for (int i = 0; i < snek.size(); i++) {
				Rectangle2D.Double r = snek.get(i);
				g2.setColor((i == 0) ? Color.cyan : Color.MAGENTA);
				//g2.setColor(new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
				g2.fill(r);
			}
			if (too) {
				for (Rectangle2D.Double r : snek2) {
					g2.setColor(Color.cyan);
					g2.fill(r);
				}
			}

			piece.draw(g2, Color.red);
			for (Module m : obstacles)
				m.draw(g2, Color.blue);
		}

		private void move() {
			Rectangle2D.Double prev = snek.get(0);

			for (int index = 0; index <= size; index++) {
				if (index == 0) {
					switch (dir) {
						case DOWN:
							snek.set(index, new Rectangle2D.Double((prev.getX()) % Game.WIDTH, (prev.getY() + GRAV) % Game.HEIGHT, SSIZE, SSIZE));
							break;

						case LEFT:
							int x = (int) (prev.getX() - GRAV);
							if (x < 0)
								x = Game.WIDTH;

							snek.set(index, new Rectangle2D.Double(x, (prev.getY()) % Game.HEIGHT, SSIZE, SSIZE));
							break;

						case RIGHT:
							snek.set(index, new Rectangle2D.Double((prev.getX() + GRAV) % Game.WIDTH, (prev.getY()) % Game.HEIGHT, SSIZE, SSIZE));
							break;

						case UP:
							int y = (int) (prev.getY() - GRAV);
							if (y < 0)
								y = Game.HEIGHT;
							snek.set(index, new Rectangle2D.Double((prev.getX()) % Game.WIDTH, y, SSIZE, SSIZE));
							break;
					}
					checkModule();
					checkCollision();
				} else {
					Rectangle2D.Double good = prev;
					prev = snek.get(index);
					snek.set(index, good);
				}
			}
		}

		private void move2() {
			Rectangle2D.Double prev = snek2.get(0);

			for (int index = 0; index <= size2; index++) {
				if (index == 0) {
					switch (dir2) {
						case DOWN:
							snek2.set(index, new Rectangle2D.Double((prev.getX()) % Game.WIDTH, (prev.getY() + GRAV) % Game.HEIGHT, SSIZE, SSIZE));
							break;

						case LEFT:
							int x = (int) (prev.getX() - GRAV);
							if (x < 0)
								x = Game.WIDTH;

							snek2.set(index, new Rectangle2D.Double(x, (prev.getY()) % Game.HEIGHT, SSIZE, SSIZE));
							break;

						case RIGHT:
							snek2.set(index, new Rectangle2D.Double((prev.getX() + GRAV) % Game.WIDTH, (prev.getY()) % Game.HEIGHT, SSIZE, SSIZE));
							break;

						case UP:
							int y = (int) (prev.getY() - GRAV);
							if (y < 0)
								y = Game.HEIGHT;
							snek2.set(index, new Rectangle2D.Double((prev.getX()) % Game.WIDTH, y, SSIZE, SSIZE));
							break;
					}
					checkModule2();
					checkCollision2();
				} else {
					Rectangle2D.Double good = prev;
					prev = snek2.get(index);
					snek2.set(index, good);
				}
			}
		}

		private void checkModule2() {
			if (snek2.get(0).intersects(piece)) {
				addModule2();
				piece = new Module();
			}
		}

		private void addModule2() {
			Rectangle2D.Double last = snek2.get(size2);
			snek2.add(last);
			size2++;
			if (on) obstacles.add(new Module());
		}

		private void checkCollision2() {
			Rectangle2D.Double head = snek2.get(0);
			for (int i = 1; i < size2; i++) {
				if (head.intersects(snek2.get(i))) {
					gameOver = true;
					winner = "PLAYER 1 WINS";
					break;
				}
			}

			gameOver = gameOver || checkObstacles2();
		}

		private void checkSnekCollide() {
			if (size == size2) return;
			for (int i = 0; i <= size; i++) {
				for (int j = 0; j <= size2; j++) {
					boolean inter = snek.get(i).intersects(snek2.get(j));
					if (inter && size > size2) {
						gameOver = true;
						winner = "PLAYER 1 WINS!!!!!!!!!!";
						return;
					} else if (inter && size2 > size) {
						gameOver = true;
						winner = "PLAYER 2 WINS!!!!!!!!!!";
						return;
					}
				}
			}
		}

		private boolean checkObstacles2() {
			if (!on) return false;
			Rectangle2D.Double head = snek2.get(0);

			for (Module m : obstacles)
				if (head.intersects(m)) return true;

			return false;
		}

		private void checkModule() {
			if (snek.get(0).intersects(piece)) {
				addModule();
				piece = new Module();
			}
		}

		private void checkCollision() {
			Rectangle2D.Double head = snek.get(0);
			for (int i = 1; i < size; i++) {
				if (head.intersects(snek.get(i))) {
					gameOver = true;
					winner = "PLAYER 2 WINS";
					break;
				}
			}

			gameOver = gameOver || checkObstacles();
		}

		private boolean checkObstacles() {
			if (!on) return false;
			Rectangle2D.Double head = snek.get(0);

			for (Module m : obstacles)
				if (head.intersects(m)) return true;

			return false;
		}

		private void addModule() {
			Rectangle2D.Double last = snek.get(size);
			snek.add(last);
			size++;
			if (on) obstacles.add(new Module());
		}

		private void changeDir(Direction d) {
			if (size > 0 && d.isOpposite(dir)) {
				return;
			}
			if (d == dir)
				return;
			dir = d;
		}

		private void changeDir2(Direction d) {
			if (size2 > 0 && d.isOpposite(dir2)) {
				return;
			}
			if (d == dir2)
				return;
			dir2 = d;
		}

		public void send(int key) {
			if (key == KeyEvent.VK_1) {
				pressed1 = !pressed1;
			}
			if (key == KeyEvent.VK_ENTER) {
				pressedE = !pressedE;
			}
		}
	}

	static class Module extends Rectangle {
		Module() {
			this.x = ThreadLocalRandom.current().nextInt(50, Game.WIDTH - 50);
			this.y = ThreadLocalRandom.current().nextInt(50, Game.HEIGHT - 50);
			this.width = SSIZE;
			this.height = SSIZE;
		}

		void draw(Graphics2D g, Color color) {
			g.setColor(color);
			g.fill(this);
		}
	}

	public static void main(String... args) {
		Game snek = new Game((String) JOptionPane.showInputDialog(null, "What difficulty?", "Difficulty",
				JOptionPane.PLAIN_MESSAGE, icon, diff, diff[1]));
	}

	public class KeyAdapter implements KeyListener {
		@Override
		public void keyPressed(KeyEvent k) {
			int key = k.getKeyCode();

			if (key == KeyEvent.VK_UP)
				p.changeDir(Direction.UP);

			else if (key == KeyEvent.VK_LEFT)
				p.changeDir(Direction.LEFT);

			else if (key == KeyEvent.VK_RIGHT)
				p.changeDir(Direction.RIGHT);

			else if (key == KeyEvent.VK_DOWN)
				p.changeDir(Direction.DOWN);

			else if (key == KeyEvent.VK_SPACE)
				if (t.isRunning()) t.stop();
				else t.start();

			else if (key == KeyEvent.VK_W)
				p.changeDir2(Direction.UP);

			else if (key == KeyEvent.VK_A)
				p.changeDir2(Direction.LEFT);

			else if (key == KeyEvent.VK_D)
				p.changeDir2(Direction.RIGHT);

			else if (key == KeyEvent.VK_S)
				p.changeDir2(Direction.DOWN);
			//p.fuckEverything();
		}

		@Override
		public void keyReleased(KeyEvent e) {

		}

		@Override
		public void keyTyped(KeyEvent e) {

		}
	}

	public class RKeyAdapter implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {

		}

		@Override
		public void keyPressed(KeyEvent k) {
			int key = k.getKeyCode();

			if (key == KeyEvent.VK_UP)
				p.changeDir(Direction.DOWN);

			else if (key == KeyEvent.VK_LEFT)
				p.changeDir(Direction.RIGHT);

			else if (key == KeyEvent.VK_RIGHT)
				p.changeDir(Direction.LEFT);

			else if (key == KeyEvent.VK_DOWN)
				p.changeDir(Direction.UP);

			else if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_1)
				p.send(key);
		}

		@Override
		public void keyReleased(KeyEvent e) {

		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		p.move();
		if (e.getActionCommand().equals("Two Player")) {
			p.move2();
			p.checkSnekCollide();
		}
		p.repaint();
	}
}
