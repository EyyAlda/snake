package net.snakegame.game;

/**Beinhaltet die Spiellogik
 * @author Lennard Rütten
 * zuletzt Bearbeitet: 10.03.25
 *
 */

import static com.almasb.fxgl.dsl.FXGL.animationBuilder;
import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.inc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;

import com.sun.scenario.Settings;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class Game {


    Controller controller;
    //private List<SnakeElements> snakeElements = new ArrayList<SnakeElements>();
    private List<Entity> snakeBody = new ArrayList<>();
    private int length;
    private final int CELL_SIZE;
    private Direction currentDirection = Direction.RIGHT;
    private Direction nextDirection = Direction.RIGHT;
    private boolean isMoving = false;
    private final int GRID_HEIGHT;
    private final int GRID_WIDTH;
    private final double MOVE_SPEED;
    private Entity food;
    private Random random = new Random();
    private GUI gui;
    private boolean isPaused = false;

    public Game(int starting_x, int starting_y, int cell_size, int grid_height, int grid_width, GUI gui){
        this.gui = gui;
        this.GRID_HEIGHT = grid_height;
        this.GRID_WIDTH = grid_width;
        this.CELL_SIZE = cell_size;
        this.MOVE_SPEED = gui.getMovementSpeed();
        createBackground();
        Entity head = createSnakeSegment(starting_x / CELL_SIZE, starting_y / CELL_SIZE, Color.BLUEVIOLET);
        snakeBody.add(head);
        addSnakeSegment();
        addSnakeSegment();
        addSnakeSegment();
        spawnFood();
    }

    private Entity createSnakeSegment(int gridX, int gridY, Color color) {
        return entityBuilder()
                .at(gridX * CELL_SIZE, gridY * CELL_SIZE)
                .viewWithBBox(new Rectangle(CELL_SIZE, CELL_SIZE, color))
                .buildAndAttach();
    }

    private void addSnakeSegment() {
        // Get last segment position
        Entity lastSegment = snakeBody.get(snakeBody.size() - 1);
        double x = lastSegment.getX() / CELL_SIZE;
        double y = lastSegment.getY() / CELL_SIZE;

        // Create new segment at the same position (it will follow during movement)
        Entity newSegment = createSnakeSegment((int)x, (int)y, Color.BLUE);
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
                int segX = (int)(segment.getX() / CELL_SIZE);
                int segY = (int)(segment.getY() / CELL_SIZE);

                if (segX == x && segY == y) {
                    validPosition = false;
                    break;
                }
            }
        } while (!validPosition);

        food = entityBuilder()
                .at(x * CELL_SIZE, y * CELL_SIZE)
                .viewWithBBox(new Rectangle(CELL_SIZE, CELL_SIZE, Color.RED))
                .buildAndAttach();
    }

    public void move() {
        // Wenn das Spiel pausiert ist, keine Bewegung ausführen
        if (isPaused) {
            return;
        }

        // Update direction
        currentDirection = nextDirection;
        isMoving = true;

        Entity head = snakeBody.get(0);
        double currentX = head.getX();
        double currentY = head.getY();

        // Calculate new head position
        int newGridX = (int)(currentX / CELL_SIZE) + currentDirection.dx;
        int newGridY = (int)(currentY / CELL_SIZE) + currentDirection.dy;

        // Check boundaries - game over if hit wall
        if (newGridX < 0 || newGridX >= GRID_WIDTH || newGridY < 0 || newGridY >= GRID_HEIGHT) {
            gameOver();
            return;
        }

        // Check self-collision - game over if hit self
        for (int i = 1; i < snakeBody.size(); i++) {
            Entity segment = snakeBody.get(i);
            int segX = (int)(segment.getX() / CELL_SIZE);
            int segY = (int)(segment.getY() / CELL_SIZE);

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
        double targetX = newGridX * CELL_SIZE;
        double targetY = newGridY * CELL_SIZE;

        animationBuilder()
                .duration(Duration.seconds(MOVE_SPEED * 0.95)) // Slightly less than move interval
                .onFinished(() -> {
                    isMoving = false;

                    // Check food collision after animation completes
                    if (food != null) {
                        int foodX = (int)(food.getX() / CELL_SIZE);
                        int foodY = (int)(food.getY() / CELL_SIZE);

                        if (newGridX == foodX && newGridY == foodY) {
                            gui.playSound(0);
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

    public void createBackground() {
        Canvas canvas = new Canvas(GRID_WIDTH * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();


        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++){
                if ((y + x) % 2 == 0){
                    gc.setFill(Color.rgb(171, 214, 81));
                } else {
                    gc.setFill(Color.rgb(162, 208, 72));
                }

                gc.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }

        Entity background = entityBuilder()
                .at(0, 0)
                .view(canvas)
                .zIndex(-1)
                .buildAndAttach();
    }

    public void updateDirection(Direction dir){
        // Ignoriere Richtungsänderungen, wenn das Spiel pausiert ist
        if (!isPaused) {
            nextDirection = dir;
        }
    }

    public Direction getDirection() {
        return nextDirection;
    }

    private void gameOver() {
        gui.playSound(2);
        FXGL.getGameController().gotoMainMenu();
    }

    // Neue Methode zum Pausieren des Spiels
    public void pauseGame() {
        isPaused = true;
    }

    // Neue Methode zum Fortsetzen des Spiels
    public void resumeGame() {
        isPaused = false;
    }

    // Methode zum Prüfen, ob das Spiel pausiert ist
    public boolean isPaused() {
        return isPaused;
    }
}