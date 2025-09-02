# Snake Game (Java)

A classic Snake game implemented in Java using Swing. Features:
- Smooth movement
- High score saving
- Start/restart screens
- Colorful graphics

## Structure
- `GamePanel.java`: Main game logic and rendering
- `GameFrame.java`: Window setup and event handling
- `Snake.java`: Entry point
- `highscore.txt`: High score persistence (created automatically)
- `.gitignore`: Ignore compiled files and IDE configs

## How to Run
1. Compile all Java files:
   ```bash
   javac snake_game/*.java
   ```
2. Run the game:
   ```bash
   java snake_game.Snake
   ```

## Suggestions & Improvements
- Add sound effects (see comments in code for future extension)
- Add levels or obstacles
- Refactor for even better separation of concerns
- Improve mobile compatibility
