package snake_game;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.LinkedList;
import java.util.Random;
import java.io.*;
import javax.sound.sampled.*;

/**
 * Main game panel for Snake. Handles game logic, rendering, and input.
 * Future: Add sound effects, obstacles, and levels.
 */
public class GamePanel extends JPanel implements ActionListener {
    // Game constants
    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int DELAY = 60; // Reduced delay for smoother movement
    private static final String highScoreFile = "highscore.txt";

    // Game state
    LinkedList<Point> snake = new LinkedList<>();
    int bodyParts = 6;
    int applesEaten;
    int highScore = 0;
    int appleX;
    int appleY;
    char direction = 'R';
    boolean running = false;
    boolean gameStarted = false;
    Timer timer;
    Random random;

    // Obstacles
    int obstacleCount = 8;
    LinkedList<Point> obstacles = new LinkedList<>();

    // Colors
    Color snakeHeadColor = new Color(0, 255, 0);
    Color snakeBodyColor = new Color(45, 180, 0);
    Color gridColor = new Color(30, 30, 30);
    Color backgroundColor = new Color(10, 10, 10);

    // Future: Sound, levels, obstacles

    public GamePanel() {
        random = new Random();
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(backgroundColor);
        setFocusable(true);
        addKeyListener(new MyKeyAdapter());
        loadHighScore();
        showStartScreen();
    }
    private void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(highScoreFile))) {
            String line = reader.readLine();
            if (line != null) {
                highScore = Integer.parseInt(line.trim());
            }
        } catch (NumberFormatException | IOException e) {
            highScore = 0; // Reset high score if file is invalid or doesn't exist
            System.err.println("Error loading high score or no high score file: " + e.getMessage());
        }
    }

    public void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(highScoreFile))) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            System.err.println("Error saving high score: " + e.getMessage());
        }
    }
    public void showStartScreen() {
        gameStarted = false;
        running = false;
        repaint();
    }

    public void startGame() {
        bodyParts = 6;
        applesEaten = 0;
        direction = 'R';
        snake.clear();
        obstacles.clear();
        int startX = SCREEN_WIDTH / 2;
        int startY = SCREEN_HEIGHT / 2;
        for (int i = 0; i < bodyParts; i++) {
            snake.add(new Point(startX - i * UNIT_SIZE, startY));
        }

        placeObstacles();
        gameStarted = true;
        running = true;
        newApple();
        timer = new Timer(DELAY, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!gameStarted) {
            drawStartScreen(g2d);
        } else if (running) {
            drawGame(g2d);
        } else {
            drawGameOver(g2d);
        }
    }

    public void drawStartScreen(Graphics2D g) {
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 75));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("SNAKE GAME", (SCREEN_WIDTH - metrics.stringWidth("SNAKE GAME")) / 2, SCREEN_HEIGHT / 3);

        g.setFont(new Font("Arial", Font.PLAIN, 30));
        g.drawString("Press SPACE to Start", (SCREEN_WIDTH - metrics.stringWidth("Press SPACE to Start")) / 2, SCREEN_HEIGHT / 2);

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("High Score: " + highScore, (SCREEN_WIDTH - metrics.stringWidth("High Score: " + highScore)) / 2, 2 * SCREEN_HEIGHT / 3);
    }

    public void drawGame(Graphics2D g) {
        // Draw grid
        g.setColor(gridColor);
        for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
            g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT);
            g.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE);
        }

        // Draw obstacles
        g.setColor(Color.GRAY);
        for (Point obs : obstacles) {
            g.fillRect(obs.x, obs.y, UNIT_SIZE, UNIT_SIZE);
        }

        // Draw apple
        GradientPaint appleGradient = new GradientPaint(
                appleX, appleY, Color.YELLOW,
                appleX + UNIT_SIZE, appleY + UNIT_SIZE, new Color(220, 220, 0)
        );
        g.setPaint(appleGradient);
        g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

        // Draw snake
        for (int i = 0; i < snake.size(); i++) {
            Point segment = snake.get(i);
            if (i == 0) {
                g.setColor(snakeHeadColor);
                g.fillRoundRect(segment.x, segment.y, UNIT_SIZE, UNIT_SIZE, 10, 10);
                // Eyes
                g.setColor(Color.BLACK);
                int eyeSize = UNIT_SIZE / 5;
                switch (direction) {
                    case 'R':
                        g.fillOval(segment.x + UNIT_SIZE - eyeSize * 2, segment.y + eyeSize, eyeSize, eyeSize);
                        g.fillOval(segment.x + UNIT_SIZE - eyeSize * 2, segment.y + UNIT_SIZE - eyeSize * 2, eyeSize, eyeSize);
                        break;
                    case 'L':
                        g.fillOval(segment.x + eyeSize, segment.y + eyeSize, eyeSize, eyeSize);
                        g.fillOval(segment.x + eyeSize, segment.y + UNIT_SIZE - eyeSize * 2, eyeSize, eyeSize);
                        break;
                    case 'U':
                        g.fillOval(segment.x + eyeSize, segment.y + eyeSize, eyeSize, eyeSize);
                        g.fillOval(segment.x + UNIT_SIZE - eyeSize * 2, segment.y + eyeSize, eyeSize, eyeSize);
                        break;
                    case 'D':
                        g.fillOval(segment.x + eyeSize, segment.y + UNIT_SIZE - eyeSize * 2, eyeSize, eyeSize);
                        g.fillOval(segment.x + UNIT_SIZE - eyeSize * 2, segment.y + UNIT_SIZE - eyeSize * 2, eyeSize, eyeSize);
                        break;
                }
            } else {
                float intensity = 1.0f - ((float) i / snake.size() * 0.5f);
                Color segmentColor = new Color(
                        (int) (snakeBodyColor.getRed() * intensity),
                        (int) (snakeBodyColor.getGreen() * intensity),
                        (int) (snakeBodyColor.getBlue() * intensity)
                );
                g.setColor(segmentColor);
                g.fillRoundRect(segment.x, segment.y, UNIT_SIZE, UNIT_SIZE, 10, 10);
            }
        }

        // Draw score
        g.setColor(Color.darkGray);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics metrics = getFontMetrics(g.getFont());
        String scoreText = "Score: " + applesEaten;
        g.drawString(scoreText, (SCREEN_WIDTH - metrics.stringWidth(scoreText)) / 2 + 2, g.getFont().getSize() + 2);
        g.setColor(Color.white);
        g.drawString(scoreText, (SCREEN_WIDTH - metrics.stringWidth(scoreText)) / 2, g.getFont().getSize());
    }

    public void drawGameOver(Graphics2D g) {
        // Update high score and save
        if (applesEaten > highScore) {
            highScore = applesEaten;
            saveHighScore();
        }

        // Draw score
        g.setColor(Color.red);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        String scoreText = "Score: " + applesEaten;
        g.drawString(scoreText, (SCREEN_WIDTH - metrics1.stringWidth(scoreText)) / 2, g.getFont().getSize());

        // Draw high score
        String highScoreText = "High Score: " + highScore;
        g.drawString(highScoreText, (SCREEN_WIDTH - metrics1.stringWidth(highScoreText)) / 2, g.getFont().getSize() * 2);

        // Game Over text with shadow
        g.setColor(Color.darkGray);
        g.setFont(new Font("Arial", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics2.stringWidth("Game Over")) / 2 + 2, SCREEN_HEIGHT / 2 + 2);
        g.setColor(Color.red);
        g.drawString("Game Over", (SCREEN_WIDTH - metrics2.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);

        // Draw restart instruction
        g.setFont(new Font("Arial", Font.PLAIN, 30));
        String restartText = "Press SPACE to Restart";
        g.drawString(restartText, (SCREEN_WIDTH - metrics2.stringWidth(restartText)) / 2, 2 * SCREEN_HEIGHT / 3);
    }

    public void newApple() {
        boolean validPosition;
        do {
            validPosition = true;
            appleX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
            appleY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;

            // Check if apple spawns on snake body
            for (Point segment : snake) {
                if ((appleX == segment.x) && (appleY == segment.y)) {
                    validPosition = false;
                    break;
                }
            }
        } while (!validPosition);
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') direction = 'L';
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') direction = 'R';
                    break;
                case KeyEvent.VK_UP:
                    if (direction != 'D') direction = 'U';
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') direction = 'D';
                    break;
                case KeyEvent.VK_SPACE:
                    if (!gameStarted || !running) startGame();
                    break;
            }
        }
    }

    public void move() {
        Point head = snake.getFirst();
        Point newHead;

        switch (direction) {
            case 'U': newHead = new Point(head.x, head.y - UNIT_SIZE); break;
            case 'D': newHead = new Point(head.x, head.y + UNIT_SIZE); break;
            case 'L': newHead = new Point(head.x - UNIT_SIZE, head.y); break;
            case 'R': newHead = new Point(head.x + UNIT_SIZE, head.y); break;
            default: return;
        }

        snake.addFirst(newHead);
        if (snake.size() > bodyParts) snake.removeLast();
    }

    public void checkApple() {
        Point head = snake.getFirst();
        if ((head.x == appleX) && (head.y == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
            playSound();
        }
    }

    public void checkCollisions() {
        Point head = snake.getFirst();
        // Check collision with body
        if (snake.subList(1, snake.size()).contains(head)) running = false;
        // Check collision with walls
        if (head.x < 0 || head.x >= SCREEN_WIDTH || head.y < 0 || head.y >= SCREEN_HEIGHT) running = false;
        // Check collision with obstacles
        for (Point obs : obstacles) {
            if (head.equals(obs)) {
                running = false;
                break;
            }
        }
        if (!running) timer.stop();
    }

    // Place obstacles randomly, avoiding snake and apple
    private void placeObstacles() {
        obstacles.clear();
        int tries = 0;
        while (obstacles.size() < obstacleCount && tries < 1000) {
            int x = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
            int y = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
            Point p = new Point(x, y);
            boolean valid = true;
            for (Point s : snake) {
                if (p.equals(s)) { valid = false; break; }
            }
            if (p.x == appleX && p.y == appleY) valid = false;
            for (Point o : obstacles) {
                if (p.equals(o)) { valid = false; break; }
            }
            if (valid) obstacles.add(p);
            tries++;
        }
    }

    // Play sound effect when apple is eaten
    private void playSound() {
        try {
            // Simple beep sound
            java.awt.Toolkit.getDefaultToolkit().beep();
            // For custom sound, use:
            // AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File("eat.wav"));
            // Clip clip = AudioSystem.getClip();
            // clip.open(audioIn);
            // clip.start();
        } catch (Exception e) {
            System.err.println("Sound error: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }
}
