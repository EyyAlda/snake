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

    /**Erstellt ein einzelnes Segment für die Schlange
     * Wird im Konstruktor und in addSnakeElement() aufgerufen
     * @author Lennard Rütten
     */
    private Entity createSnakeSegment(int gridX, int gridY, Color color) {
        return entityBuilder()
                .at(gridX * CELL_SIZE, gridY * CELL_SIZE)
                .viewWithBBox(new Rectangle(CELL_SIZE, CELL_SIZE, color))
                .buildAndAttach();
    }

    /**Managed das Erstellen von Snakeelementen
     * Wird aufgerufen von der move() Funktion, wenn über ein Food-Element gefahren wurde
     * @author Lennard Rütten
     */
    private void addSnakeSegment() {
        // Get last segment position
        Entity lastSegment = snakeBody.get(snakeBody.size() - 1);
        double x = lastSegment.getX() / CELL_SIZE;
        double y = lastSegment.getY() / CELL_SIZE;

        // Create new segment at the same position (it will follow during movement)
        Entity newSegment = createSnakeSegment((int)x, (int)y, Color.BLUE);
        snakeBody.add(newSegment);
    }


    /**Erstellt ein Food-Element und plaziert es zufällig auf dem Spielfeld
     * Wird aufgerufen in move() und im Konstruktor
     *@author Lennard Rütten
     */
    private void spawnFood() {
        if (food != null) {
            food.removeFromWorld();
        }

        int x, y;
        boolean validPosition;

        // Finde eine Position, die nicht von der Schlange belegt wurde
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

        // Erstelle das Element als Entity
        food = entityBuilder()
                .at(x * CELL_SIZE, y * CELL_SIZE)
                .viewWithBBox(new Rectangle(CELL_SIZE, CELL_SIZE, Color.RED))
                .buildAndAttach();
    }

    /**Bewegt die Schlange ein Feld weiter
     * Wird durch den FXGL game timer aufgerufen (definiert in GUI.java -> initGame())
     * @author Lennard Rütten
     */
    public void move() {
        // Wenn das Spiel pausiert ist, keine Bewegung ausführen
        if (isPaused) {
            return;
        }

        // Richtung aktualisieren
        currentDirection = nextDirection;
        isMoving = true;

        Entity head = snakeBody.get(0);
        double currentX = head.getX();
        double currentY = head.getY();

        // Berechnung der neuen Position für den Snake-Head
        int newGridX = (int)(currentX / CELL_SIZE) + currentDirection.dx;
        int newGridY = (int)(currentY / CELL_SIZE) + currentDirection.dy;

        // Überprüfung der Spielfeldgrenzen - Wenn außerhalb, gameOver()
        if (newGridX < 0 || newGridX >= GRID_WIDTH || newGridY < 0 || newGridY >= GRID_HEIGHT) {
            gameOver();
            return;
        }

        // Überprüfe, ob die Schlange sich selbst treffen würde - Wenn ja, gameOver()
        for (int i = 1; i < snakeBody.size(); i++) {
            Entity segment = snakeBody.get(i);
            int segX = (int)(segment.getX() / CELL_SIZE);
            int segY = (int)(segment.getY() / CELL_SIZE);

            if (segX == newGridX && segY == newGridY) {
                gameOver();
                return;
            }
        }

        // Speichere die vorherigen Positionen der Snake-Elemente
        List<Point2D> prevPositions = new ArrayList<>();
        for (Entity segment : snakeBody) {
            prevPositions.add(new Point2D(segment.getX(), segment.getY()));
        }

        // Berechne die neuen Koordinaten für den Kopf
        double targetX = newGridX * CELL_SIZE;
        double targetY = newGridY * CELL_SIZE;

        // Spiele die Bewegung als Animation ab
        animationBuilder()
                .duration(Duration.seconds(MOVE_SPEED * 0.95))
                .onFinished(() -> {
                    isMoving = false;

                    // Überprüfe, ob ein Food-Element getroffen wurde
                    if (food != null) {
                        int foodX = (int)(food.getX() / CELL_SIZE);
                        int foodY = (int)(food.getY() / CELL_SIZE);

                        if (newGridX == foodX && newGridY == foodY) {
                            gui.playSound(0); // spiele Essens sound
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

        // Ziehe die restlichen Elemente hinterher
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

    /**Erstellt den Hintergrund
     * @author Lennard Rütten
     */
    public void createBackground() {
        // Canvas beinhaltet den Hintergrund
        Canvas canvas = new Canvas(GRID_WIDTH * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();


        // Erstelle ein Karomuster auf dem Canvas
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

        // platziere den Hintergrund als Entity unterhalb der Spielelemente
        Entity background = entityBuilder()
                .at(0, 0)
                .view(canvas)
                .zIndex(-1)
                .buildAndAttach();
    }

    /**Aktualisiert die Richtung des Kopfelements
     * Aufgerufen in GUI -> initInput
     * @author Lennard Rütten
     */
    public void updateDirection(Direction dir){
        // Ignoriere Richtungsänderungen, wenn das Spiel pausiert ist
        if (!isPaused) {
            nextDirection = dir;
        }
    }

    /**Gibt die aktuelle Kopfbewegungsrichtung zurück
     * Wird von GUI -> initInput verwendet -> Notwendig, um 180° Drehungen zu verhindern
     * @author Lennard Rütten
     */
    public Direction getDirection() {
        return nextDirection;
    }

    /**Bricht das Spiel ab und zeigt das Hauptmenü
     * @author Lennard Rütten, Nick Gegenheimer
     */
    private void gameOver() {
        gui.playSound(2);
        gui.stopAndDisposeMusic();
        gui.initMenuMusic();
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
