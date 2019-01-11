import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Game extends JFrame implements ActionListener {
    private static String[] diff = {"Easy", "Normal", "Hard", "Harder", "Two Player VS"};
    private static ImageIcon icon = new ImageIcon("snek.png");
    private static boolean reverse = false;

    static final int WIDTH = 640;
    static final int HEIGHT = 480;
    static final int SSIZE = 10;
    static final int GRAV = 10;

    private Snake p = new Snake();
    private Timer t = new Timer(60, this);

    public Game(String difficulty) {
        getContentPane().setBackground(Color.WHITE);
        t.setActionCommand("Not");
        t.setDelay(difficulty(difficulty));
    }

    public void play() {
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
        int r;
        if (d == null) System.exit(0);

        if (d.equals(diff[0]))
            r = 60;
        else if (d.equals(diff[1]))
            r = 60;
        else if (d.equals(diff[2])) {
            reverse = true;
            r = 30;
        } else if (d.equals(diff[3])) {
            p = new Snake(true);
            r = 60;
        } else {
            p = new Snake(0);
            t.setActionCommand("Two Player");
            r = 60;
        }
        addKeyListener(new KeyAdapter());
        return r;
    }

    static class Snake extends JComponent {

        boolean gameOver, on, too;
        String winner = "";

        SnakeRect snek = new SnakeRect(), snek2 = new SnakeRect();
        Module piece = new Module();
        ArrayList<Module> obstacles = new ArrayList<>(100);
        Module shrink = new Module();

        public Snake() {
            setPreferredSize(new Dimension(SSIZE, SSIZE));
            snek.add(new Rectangle2D.Double(100, 100, SSIZE, SSIZE));
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

        public void checkSnekCollide() {
            if (snek.size == snek2.size) return;
            for (int i = 0; i <= snek.size; i++) {
                for (int j = 0; j <= snek2.size; j++) {
                    boolean inter = snek.get(i).intersects(snek2.get(j));
                    if (inter) {
                        winner = "PLAYER " + (snek.size > snek2.size ? "1" : "2") + "WINS!!!!!!!!!!";
                        if (i == 0 && j == 0) {
                            gameOver = true;
                            return;
                        }
                        if (snek.size > snek2.size) {
                            for (int x = snek2.size() - 1; x >= j && x >= 1; x--) snek2.remove(x);
                            snek2.size = snek2.size() - 1;
                            if (snek2.size == 0) gameOver = true;
                            return;
                        } else if (snek2.size > snek.size) {
                            for (int x = snek.size() - 1; x >= i && x >= 1; x--) snek.remove(x);
                            snek.size = snek.size() - 1;
                            if (snek.size == 0) gameOver = true;
                            return;
                        }
                    }
                }
            }
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

                if (snek.size > highscore) {
                    highscore = snek.size;

                    try (BufferedWriter write = new BufferedWriter(new FileWriter(file))) {
                        write.write("" + snek.size + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                String message = "GAME OVER", score = "SCORE: " + snek.size, high = "HIGHSCORE: " + highscore;

                g.setColor(Color.PINK);
                g.setFont(new Font("Calibri", Font.BOLD, 20));
                g.drawString(message, (Game.WIDTH / 2) - (message.length() * 2), Game.HEIGHT / 2);
                g.drawString(score, (Game.WIDTH / 2), Game.HEIGHT / 2 + 15);
                g.drawString(high, (Game.WIDTH / 2), Game.HEIGHT / 2 + 30);
                return;
            }
            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(Color.black);

            for (int i = 0; i < snek.size(); i++) {
                Rectangle2D.Double r = snek.get(i);
                g2.setColor((i == 0) ? Color.cyan : Color.MAGENTA);
                //g2.setColor(new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
                g2.fill(r);
            }
            if (snek2 != null) {
                for (Rectangle2D.Double r : snek2) {
                    g2.setColor(Color.cyan);
                    g2.fill(r);
                }
            }

            piece.draw(g2, Color.red);
            for (Module m : obstacles)
                m.draw(g2, Color.blue);
            shrink.draw(g2, Color.green);
        }

        class SnakeRect extends ArrayList<Rectangle2D.Double> {
            Direction dir = Direction.DOWN;
            int size;

            private void move() {
                Rectangle2D.Double prev = get(0);
                switch (dir) {
                    case DOWN:
                        set(0, new Rectangle2D.Double((prev.getX()) % Game.WIDTH, (prev.getY() + GRAV) % Game.HEIGHT, SSIZE, SSIZE));
                        break;

                    case LEFT:
                        int x = (int) (prev.getX() - GRAV);
                        if (x < 0)
                            x = Game.WIDTH;

                        set(0, new Rectangle2D.Double(x, (prev.getY()) % Game.HEIGHT, SSIZE, SSIZE));
                        break;

                    case RIGHT:
                        set(0, new Rectangle2D.Double((prev.getX() + GRAV) % Game.WIDTH, (prev.getY()) % Game.HEIGHT, SSIZE, SSIZE));
                        break;

                    case UP:
                        int y = (int) (prev.getY() - GRAV);
                        if (y < 0)
                            y = Game.HEIGHT;
                        set(0, new Rectangle2D.Double((prev.getX()) % Game.WIDTH, y, SSIZE, SSIZE));
                        break;
                }
                checkModule();
                checkCollision();

                for (int index = 1; index <= size; index++) {
                    Rectangle2D.Double good = prev;
                    prev = get(index);
                    set(index, good);
                }
            }

            private void checkModule() {
                if (get(0).intersects(piece)) {
                    addModule();
                    piece = new Module();
                }
                if (get(0).intersects(shrink)) {
                    int x = snek.size() - 1;
                    if (x==0) {
                        gameOver = true;
                        return;
                    }
                    snek.remove(x);
                    size--;
                    shrink = new Module();
                }
            }

            private void addModule() {
                Rectangle2D.Double last = get(size);
                add(last);
                size++;
                if (on) obstacles.add(new Module());
            }

            private void changeDir(Direction d) {
                if (size > 0 && Direction.getOpposite(d) == dir)
                    return;

                if (d == dir)
                    return;
                dir = d;
            }

            private void checkCollision() {
                Rectangle2D.Double head = get(0);
                for (int i = 1; i < size; i++) {
                    if (head.intersects(get(i))) {
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
        snek.play();
    }

    public class KeyAdapter implements KeyListener {
        @Override
        public void keyPressed(KeyEvent k) {
            int key = k.getKeyCode();

            Direction dir = Direction.DEFAULT;
            Snake.SnakeRect ps = k.isActionKey() ? p.snek : p.snek2;
            switch (key) {
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    dir = Direction.UP;
                    break;
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    dir = Direction.LEFT;
                    break;
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    dir = Direction.RIGHT;
                    break;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                    dir = Direction.DOWN;
                    break;
                case KeyEvent.VK_SPACE:
                    if (t.isRunning()) t.stop();
                    else t.start();
            }
            if (reverse) dir = Direction.getOpposite(dir);

            ps.changeDir(dir);
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        p.snek.move();
        if (e.getActionCommand().equals("Two Player")) {
            p.snek2.move();
            p.checkSnekCollide();
        }
        p.repaint();
    }
}
