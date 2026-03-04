package snake_game;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GamePanel extends JPanel implements ActionListener {

    GameState state;
    Timer timer;

    // UI Panel for mobile/touch
    JPanel controlsPanel;

    // Colors
    Color snakeHeadColor = new Color(0, 255, 0);
    Color snakeBodyColor = new Color(45, 180, 0);
    Color gridColor = new Color(30, 30, 30);
    Color backgroundColor = new Color(10, 10, 10);

    public GamePanel() {
        state = new GameState();

        setLayout(new BorderLayout());

        // Game drawing area
        JPanel renderPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (!state.gameStarted) {
                    drawStartScreen(g2d);
                } else if (state.running) {
                    drawGame(g2d);
                } else {
                    drawGameOver(g2d);
                }
            }
        };
        renderPanel.setPreferredSize(new Dimension(GameState.SCREEN_WIDTH, GameState.SCREEN_HEIGHT));
        renderPanel.setBackground(backgroundColor);

        add(renderPanel, BorderLayout.CENTER);

        // Controls panel
        setupControlsPanel();
        add(controlsPanel, BorderLayout.SOUTH);

        setFocusable(true);
        addKeyListener(new MyKeyAdapter());

        showStartScreen();
    }

    private void setupControlsPanel() {
        controlsPanel = new JPanel();
        controlsPanel.setLayout(new GridLayout(2, 3));
        controlsPanel.setBackground(Color.DARK_GRAY);

        JButton upButton = createControlButton("UP", 'U');
        JButton leftButton = createControlButton("LEFT", 'L');
        JButton rightButton = createControlButton("RIGHT", 'R');
        JButton downButton = createControlButton("DOWN", 'D');
        JButton startRestartBtn = new JButton("START/RESTART");
        startRestartBtn.setFocusable(false);
        startRestartBtn.setBackground(Color.LIGHT_GRAY);
        startRestartBtn.addActionListener(e -> {
            if (!state.gameStarted || !state.running) {
                startGame();
            }
        });

        controlsPanel.add(new JLabel()); // Empty
        controlsPanel.add(upButton);
        controlsPanel.add(startRestartBtn);

        controlsPanel.add(leftButton);
        controlsPanel.add(downButton);
        controlsPanel.add(rightButton);
    }

    private JButton createControlButton(String label, char dir) {
        JButton btn = new JButton(label);
        btn.setFocusable(false);
        btn.setBackground(Color.LIGHT_GRAY);
        btn.addActionListener(e -> {
            switch(dir) {
                case 'U': if (state.direction != 'D') state.direction = 'U'; break;
                case 'D': if (state.direction != 'U') state.direction = 'D'; break;
                case 'L': if (state.direction != 'R') state.direction = 'L'; break;
                case 'R': if (state.direction != 'L') state.direction = 'R'; break;
            }
        });
        return btn;
    }

    public void saveHighScore() {
        state.saveHighScore();
    }

    public void showStartScreen() {
        state.gameStarted = false;
        state.running = false;
        repaint();
    }

    public void startGame() {
        state.resetGame();
        state.placeObstacles();
        state.newApple();

        state.gameStarted = true;
        state.running = true;

        if (timer != null) timer.stop();
        timer = new Timer(state.currentDelay, this);
        timer.start();
        repaint();
    }

    public void drawStartScreen(Graphics2D g) {
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 75));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("SNAKE GAME", (GameState.SCREEN_WIDTH - metrics.stringWidth("SNAKE GAME")) / 2, GameState.SCREEN_HEIGHT / 3);

        g.setFont(new Font("Arial", Font.PLAIN, 30));
        g.drawString("Press SPACE or START", (GameState.SCREEN_WIDTH - metrics.stringWidth("Press SPACE or START")) / 2, GameState.SCREEN_HEIGHT / 2);

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("High Score: " + state.highScore, (GameState.SCREEN_WIDTH - metrics.stringWidth("High Score: " + state.highScore)) / 2, 2 * GameState.SCREEN_HEIGHT / 3);
    }

    public void drawGame(Graphics2D g) {
        // Draw grid
        g.setColor(gridColor);
        for (int i = 0; i < GameState.SCREEN_HEIGHT / GameState.UNIT_SIZE; i++) {
            g.drawLine(i * GameState.UNIT_SIZE, 0, i * GameState.UNIT_SIZE, GameState.SCREEN_HEIGHT);
            g.drawLine(0, i * GameState.UNIT_SIZE, GameState.SCREEN_WIDTH, i * GameState.UNIT_SIZE);
        }

        // Draw obstacles
        g.setColor(Color.GRAY);
        for (Point obs : state.obstacles) {
            g.fillRect(obs.x, obs.y, GameState.UNIT_SIZE, GameState.UNIT_SIZE);
        }

        // Draw apple
        GradientPaint appleGradient = new GradientPaint(
                state.appleX, state.appleY, Color.YELLOW,
                state.appleX + GameState.UNIT_SIZE, state.appleY + GameState.UNIT_SIZE, new Color(220, 220, 0)
        );
        g.setPaint(appleGradient);
        g.fillOval(state.appleX, state.appleY, GameState.UNIT_SIZE, GameState.UNIT_SIZE);

        // Draw snake
        for (int i = 0; i < state.snake.size(); i++) {
            Point segment = state.snake.get(i);
            if (i == 0) {
                g.setColor(snakeHeadColor);
                g.fillRoundRect(segment.x, segment.y, GameState.UNIT_SIZE, GameState.UNIT_SIZE, 10, 10);
                // Eyes
                g.setColor(Color.BLACK);
                int eyeSize = GameState.UNIT_SIZE / 5;
                switch (state.direction) {
                    case 'R':
                        g.fillOval(segment.x + GameState.UNIT_SIZE - eyeSize * 2, segment.y + eyeSize, eyeSize, eyeSize);
                        g.fillOval(segment.x + GameState.UNIT_SIZE - eyeSize * 2, segment.y + GameState.UNIT_SIZE - eyeSize * 2, eyeSize, eyeSize);
                        break;
                    case 'L':
                        g.fillOval(segment.x + eyeSize, segment.y + eyeSize, eyeSize, eyeSize);
                        g.fillOval(segment.x + eyeSize, segment.y + GameState.UNIT_SIZE - eyeSize * 2, eyeSize, eyeSize);
                        break;
                    case 'U':
                        g.fillOval(segment.x + eyeSize, segment.y + eyeSize, eyeSize, eyeSize);
                        g.fillOval(segment.x + GameState.UNIT_SIZE - eyeSize * 2, segment.y + eyeSize, eyeSize, eyeSize);
                        break;
                    case 'D':
                        g.fillOval(segment.x + eyeSize, segment.y + GameState.UNIT_SIZE - eyeSize * 2, eyeSize, eyeSize);
                        g.fillOval(segment.x + GameState.UNIT_SIZE - eyeSize * 2, segment.y + GameState.UNIT_SIZE - eyeSize * 2, eyeSize, eyeSize);
                        break;
                }
            } else {
                float intensity = 1.0f - ((float) i / state.snake.size() * 0.5f);
                Color segmentColor = new Color(
                        (int) (snakeBodyColor.getRed() * intensity),
                        (int) (snakeBodyColor.getGreen() * intensity),
                        (int) (snakeBodyColor.getBlue() * intensity)
                );
                g.setColor(segmentColor);
                g.fillRoundRect(segment.x, segment.y, GameState.UNIT_SIZE, GameState.UNIT_SIZE, 10, 10);
            }
        }

        // Draw score & Level
        g.setColor(Color.darkGray);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics metrics = getFontMetrics(g.getFont());
        String scoreText = "Score: " + state.applesEaten;
        g.drawString(scoreText, (GameState.SCREEN_WIDTH - metrics.stringWidth(scoreText)) / 2 + 2, g.getFont().getSize() + 2);

        g.setColor(Color.white);
        g.drawString(scoreText, (GameState.SCREEN_WIDTH - metrics.stringWidth(scoreText)) / 2, g.getFont().getSize());

        g.setFont(new Font("Arial", Font.BOLD, 20));
        String levelText = "Level: " + state.level;
        g.drawString(levelText, 10, 25);
    }

    public void drawGameOver(Graphics2D g) {
        // Draw score
        g.setColor(Color.red);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        String scoreText = "Score: " + state.applesEaten;
        g.drawString(scoreText, (GameState.SCREEN_WIDTH - metrics1.stringWidth(scoreText)) / 2, g.getFont().getSize());

        // Draw high score
        String highScoreText = "High Score: " + state.highScore;
        g.drawString(highScoreText, (GameState.SCREEN_WIDTH - metrics1.stringWidth(highScoreText)) / 2, g.getFont().getSize() * 2);

        // Game Over text with shadow
        g.setColor(Color.darkGray);
        g.setFont(new Font("Arial", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (GameState.SCREEN_WIDTH - metrics2.stringWidth("Game Over")) / 2 + 2, GameState.SCREEN_HEIGHT / 2 + 2);
        g.setColor(Color.red);
        g.drawString("Game Over", (GameState.SCREEN_WIDTH - metrics2.stringWidth("Game Over")) / 2, GameState.SCREEN_HEIGHT / 2);

        // Draw restart instruction
        g.setFont(new Font("Arial", Font.PLAIN, 30));
        String restartText = "Press SPACE or START to Restart";
        g.drawString(restartText, (GameState.SCREEN_WIDTH - metrics2.stringWidth(restartText)) / 2, 2 * GameState.SCREEN_HEIGHT / 3);
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (state.direction != 'R') state.direction = 'L';
                    break;
                case KeyEvent.VK_RIGHT:
                    if (state.direction != 'L') state.direction = 'R';
                    break;
                case KeyEvent.VK_UP:
                    if (state.direction != 'D') state.direction = 'U';
                    break;
                case KeyEvent.VK_DOWN:
                    if (state.direction != 'U') state.direction = 'D';
                    break;
                case KeyEvent.VK_SPACE:
                    if (!state.gameStarted || !state.running) startGame();
                    break;
            }
        }
    }

    public void checkApple() {
        Point head = state.snake.getFirst();
        if ((head.x == state.appleX) && (head.y == state.appleY)) {
            state.bodyParts++;
            state.applesEaten++;

            SoundManager.playEatSound();

            int oldLevel = state.level;
            state.checkLevelUp();
            if (state.level > oldLevel) {
                // Update timer delay
                timer.setDelay(state.currentDelay);
            }

            state.newApple();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (state.running) {
            state.move();
            checkApple();
            state.checkCollisions();

            if (!state.running) {
                timer.stop();
                SoundManager.playGameOverSound();
            }
        }
        repaint();
    }
}
