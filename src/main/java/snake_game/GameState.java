package snake_game;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

public class GameState {
    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;

    private static final String highScoreFile = "highscore.txt";

    public LinkedList<Point> snake = new LinkedList<>();
    public int bodyParts = 6;
    public int applesEaten;
    public int highScore = 0;
    public int appleX;
    public int appleY;
    public char direction = 'R';
    public boolean running = false;
    public boolean gameStarted = false;

    public int obstacleCount = 8;
    public LinkedList<Point> obstacles = new LinkedList<>();

    public Random random = new Random();

    public int level = 1;
    public int baseDelay = 60;
    public int currentDelay = 60;

    public GameState() {
        loadHighScore();
    }

    public void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(highScoreFile))) {
            String line = reader.readLine();
            if (line != null) {
                highScore = Integer.parseInt(line.trim());
            }
        } catch (NumberFormatException | IOException e) {
            highScore = 0;
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

    public void resetGame() {
        bodyParts = 6;
        applesEaten = 0;
        direction = 'R';
        snake.clear();
        obstacles.clear();
        level = 1;
        currentDelay = baseDelay;
        obstacleCount = 8;

        int startX = SCREEN_WIDTH / 2;
        int startY = SCREEN_HEIGHT / 2;
        for (int i = 0; i < bodyParts; i++) {
            snake.add(new Point(startX - i * UNIT_SIZE, startY));
        }
    }

    public void newApple() {
        boolean validPosition;
        do {
            validPosition = true;
            appleX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
            appleY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;

            for (Point segment : snake) {
                if ((appleX == segment.x) && (appleY == segment.y)) {
                    validPosition = false;
                    break;
                }
            }
            if (validPosition) {
                for (Point obs : obstacles) {
                    if (appleX == obs.x && appleY == obs.y) {
                        validPosition = false;
                        break;
                    }
                }
            }
        } while (!validPosition);
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

        if (!running) {
            if (applesEaten > highScore) {
                highScore = applesEaten;
                saveHighScore();
            }
        }
    }

    public void checkLevelUp() {
        int newLevel = (applesEaten / 5) + 1;
        if (newLevel > level) {
            level = newLevel;
            currentDelay = Math.max(20, baseDelay - (level - 1) * 5); // Speed up
            placeObstacles(); // Re-place/add more structured obstacles
        }
    }

    public void placeObstacles() {
        obstacles.clear();

        // As level goes up, create more structured obstacles
        if (level > 1) {
            int pattern = level % 3;
            if (pattern == 0) {
                // Horizontal walls
                createWall(10, 5, 10, true);
                createWall(4, 15, 15, true);
            } else if (pattern == 1) {
                // Vertical walls
                createWall(5, 5, 10, false);
                createWall(18, 5, 10, false);
            } else {
                // Box / Random combination
                createWall(5, 5, 5, true);
                createWall(5, 5, 5, false);
            }
        }

        // Random obstacles
        obstacleCount = 3 + (level * 2);

        int tries = 0;
        while (obstacles.size() < obstacleCount + (level > 1 ? 10 : 0) && tries < 1000) {
            int x = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
            int y = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
            Point p = new Point(x, y);
            boolean valid = true;

            // Avoid snake
            for (Point s : snake) {
                if (p.equals(s)) { valid = false; break; }
            }
            // Avoid snake head immediate path to be safe
            if (!snake.isEmpty()) {
                Point head = snake.getFirst();
                if (Math.abs(p.x - head.x) < UNIT_SIZE * 3 && Math.abs(p.y - head.y) < UNIT_SIZE * 3) {
                    valid = false;
                }
            }

            // Avoid apple
            if (p.x == appleX && p.y == appleY) valid = false;

            for (Point o : obstacles) {
                if (p.equals(o)) { valid = false; break; }
            }

            if (valid) obstacles.add(p);
            tries++;
        }
    }

    private void createWall(int startGridX, int startGridY, int length, boolean horizontal) {
        for (int i = 0; i < length; i++) {
            int x = (startGridX + (horizontal ? i : 0)) * UNIT_SIZE;
            int y = (startGridY + (horizontal ? 0 : i)) * UNIT_SIZE;

            // Check bounds
            if (x >= 0 && x < SCREEN_WIDTH && y >= 0 && y < SCREEN_HEIGHT) {
                Point p = new Point(x, y);
                // Simple validation to ensure we don't spawn on snake head starting position
                boolean valid = true;
                if (!snake.isEmpty()) {
                    Point head = snake.getFirst();
                    if (Math.abs(p.x - head.x) < UNIT_SIZE * 3 && Math.abs(p.y - head.y) < UNIT_SIZE * 3) {
                        valid = false;
                    }
                }
                if (valid) {
                    obstacles.add(p);
                }
            }
        }
    }
}
