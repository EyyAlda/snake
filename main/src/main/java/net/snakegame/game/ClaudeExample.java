package net.snakegame.game;
import static com.almasb.fxgl.dsl.FXGL.addVarText;
import static com.almasb.fxgl.dsl.FXGL.animationBuilder;
import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.getGameController;
import static com.almasb.fxgl.dsl.FXGL.getGameTimer;
import static com.almasb.fxgl.dsl.FXGL.geti;
import static com.almasb.fxgl.dsl.FXGL.inc;
import static com.almasb.fxgl.dsl.FXGL.onKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;

import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ClaudeExample extends GameApplication {
    
    private static final int GRID_SIZE = 20; // Size of grid cells
    private static final int GRID_WIDTH = 30; // Grid width in cells
    private static final int GRID_HEIGHT = 20; // Grid height in cells
    private static final double MOVE_SPEED = 0.2; // Seconds per cell (lower = faster)

    private Direction currentDirection = Direction.RIGHT;
    private Direction nextDirection = Direction.RIGHT;
    private List<Entity> snakeBody = new ArrayList<>();
    private Entity food;
    private boolean isMoving = false;
    private Random random = new Random();

    // Enum for directions
    private enum Direction {
        UP(0, -1),
        DOWN(0, 1),
        LEFT(-1, 0),
        RIGHT(1, 0);

        final int x;
        final int y;

        Direction(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(GRID_WIDTH * GRID_SIZE);
        settings.setHeight(GRID_HEIGHT * GRID_SIZE + 10);
        settings.setTitle("Snake Game");
        settings.setVersion("1.0");
        settings.setMainMenuEnabled(true);
        settings.setManualResizeEnabled(true);
        settings.setPreserveResizeRatio(true);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        // Initialize game variables
        vars.put("score", 0);
    }

    @Override
    protected void initGame() {
        // Create snake head
        Entity head = createSnakeSegment(5, 5, Color.GREEN);
        snakeBody.add(head);

        // Add some initial body segments
        addSnakeSegment();
        addSnakeSegment();

        // Spawn first food
        spawnFood();

        // Set up game loop for movement
        getGameTimer().runAtInterval(this::moveSnake, Duration.seconds(MOVE_SPEED));
    }

    @Override
    protected void initInput() {
        onKey(KeyCode.UP, () -> {
            if (currentDirection != Direction.DOWN) {
                nextDirection = Direction.UP;
            }
        });

        onKey(KeyCode.DOWN, () -> {
            if (currentDirection != Direction.UP) {
                nextDirection = Direction.DOWN;
            }
        });

        onKey(KeyCode.LEFT, () -> {
            if (currentDirection != Direction.RIGHT) {
                nextDirection = Direction.LEFT;
            }
        });

        onKey(KeyCode.RIGHT, () -> {
            if (currentDirection != Direction.LEFT) {
                nextDirection = Direction.RIGHT;
            }
        });
    }

    private Entity createSnakeSegment(int gridX, int gridY, Color color) {
        return entityBuilder()
                .at(gridX * GRID_SIZE, gridY * GRID_SIZE)
                .viewWithBBox(new Rectangle(GRID_SIZE - 1, GRID_SIZE - 1, color))
                .buildAndAttach();
    }

    private void addSnakeSegment() {
        // Get last segment position
        Entity lastSegment = snakeBody.get(snakeBody.size() - 1);
        double x = lastSegment.getX() / GRID_SIZE;
        double y = lastSegment.getY() / GRID_SIZE;

        // Create new segment at the same position (it will follow during movement)
        Entity newSegment = createSnakeSegment((int)x, (int)y, Color.LIGHTGREEN);
        snakeBody.add(newSegment);
    }

    private void spawnFood() {
        if (food != null) {
            food.removeFromWorld();
        }

        int x, y;
        boolean validPosition;

        // Find position not occupied by snake
        do {
            x = random.nextInt(GRID_WIDTH);
            y = random.nextInt(GRID_HEIGHT);
            validPosition = true;

            for (Entity segment : snakeBody) {
                int segX = (int)(segment.getX() / GRID_SIZE);
                int segY = (int)(segment.getY() / GRID_SIZE);

                if (segX == x && segY == y) {
                    validPosition = false;
                    break;
                }
            }
        } while (!validPosition);

        food = entityBuilder()
                .at(x * GRID_SIZE, y * GRID_SIZE)
                .viewWithBBox(new Rectangle(GRID_SIZE - 1, GRID_SIZE - 1, Color.RED))
                .buildAndAttach();
    }

    private void moveSnake() {
        // Update direction
        currentDirection = nextDirection;
        isMoving = true;

        Entity head = snakeBody.get(0);
        double currentX = head.getX();
        double currentY = head.getY();

        // Calculate new head position
        int newGridX = (int)(currentX / GRID_SIZE) + currentDirection.x;
        int newGridY = (int)(currentY / GRID_SIZE) + currentDirection.y;

        // Check boundaries - game over if hit wall
        if (newGridX < 0 || newGridX >= GRID_WIDTH || newGridY < 0 || newGridY >= GRID_HEIGHT) {
            gameOver();
            return;
        }

        // Check self-collision - game over if hit self
        for (int i = 1; i < snakeBody.size(); i++) {
            Entity segment = snakeBody.get(i);
            int segX = (int)(segment.getX() / GRID_SIZE);
            int segY = (int)(segment.getY() / GRID_SIZE);

            if (segX == newGridX && segY == newGridY) {
                gameOver();
                return;
            }
        }

        // Store previous positions for smooth following
        List<Point2D> prevPositions = new ArrayList<>();
        for (Entity segment : snakeBody) {
            prevPositions.add(new Point2D(segment.getX(), segment.getY()));
        }

        // Move head with animation
        double targetX = newGridX * GRID_SIZE;
        double targetY = newGridY * GRID_SIZE;

        animationBuilder()
            .duration(Duration.seconds(MOVE_SPEED * 0.95)) // Slightly less than move interval
            .onFinished(() -> {
                isMoving = false;
                
                // Check food collision after animation completes
                if (food != null) {
                    int foodX = (int)(food.getX() / GRID_SIZE);
                    int foodY = (int)(food.getY() / GRID_SIZE);
                    
                    if (newGridX == foodX && newGridY == foodY) {
                        addSnakeSegment();
                        spawnFood();
                        inc("score", +1);
                    }
                }
            })
            .translate(head)
            .from(new Point2D(currentX, currentY))
            .to(new Point2D(targetX, targetY))
            .buildAndPlay();

        // Move body segments with animation (follow the leader)
        for (int i = 1; i < snakeBody.size(); i++) {
            Entity segment = snakeBody.get(i);
            Point2D prevPos = prevPositions.get(i - 1);
            
            animationBuilder()
                .duration(Duration.seconds(MOVE_SPEED * 0.95))
                .translate(segment)
                .from(new Point2D(segment.getX(), segment.getY()))
                .to(prevPos)
                .buildAndPlay();
        }
    }

    private void gameOver() {
        // Simple game over - could be enhanced with proper UI
        System.out.println("Game Over! Score: " + geti("score"));
        getGameController().gotoMainMenu();
    }

    @Override
    protected void initUI() {
        addVarText("score", 20, 20);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
