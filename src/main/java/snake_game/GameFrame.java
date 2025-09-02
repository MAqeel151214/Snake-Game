package snake_game;
import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Game window setup and event handling.
 */
public class GameFrame extends JFrame {
    private GamePanel gamePanel; // Store a reference to the GamePanel

    public GameFrame() {
        gamePanel = new GamePanel(); // Initialize GamePanel
        this.add(gamePanel);
        this.setTitle("Snake");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);

        // register the window closing event
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (gamePanel != null) {
                    gamePanel.saveHighScore(); // Correct call to saveHighScore
                }
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });
    }
}
// ...existing code...
