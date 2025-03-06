package net.snakegame.game;

import static com.almasb.fxgl.dsl.FXGL.animationBuilder;
import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

import java.util.ArrayList;
import java.util.List;

import com.almasb.fxgl.entity.Entity;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class Game {
/* 
    Controller controller;

    private int starting_pos_x, starting_pos_y;

    private Entity head;
    private Elements elements;
    private Point2D head_direction;

    public void setDirection(Point2D direction){
        head_direction = direction;
    }

    public Point2D getDirection(){
        return head_direction;
    }

    public SnakeEntity(int starting_x, int starting_y, Controller pController) {
        this.starting_pos_x = starting_x;
        this.starting_pos_y = starting_y;
        this.controller = pController;
    }

    public void create_snake() {
        elements = new Elements(starting_pos_y, starting_pos_x, new Point2D(1, 0), null);
    }

    public void add_snake_element() {
        Elements temp = elements.next;
        while (temp.next != null) {
            temp = temp.next;
        }
        temp.next = new Elements(temp.pos_x, temp.pos_y, temp.vector, temp);

    }

    public void setDirection(Direction dir) {
        elements.vector = new Point2D(dir.dx, dir.dy);
    }
    
    public class Elements {
        int length;
        int pos_x, pos_y;
        Entity entity;
        Point2D vector;

        Elements next;
        Elements prev = null;

        public Elements(int starting_x, int starting_y, Point2D pVector, Elements prev) {
            this.prev = prev;
            pos_x = starting_x;
            pos_y = starting_y;
            vector = pVector;
            next = null;
            entity = FXGL.entityBuilder().at(starting_x, starting_y).viewWithBBox(new Rectangle(38, 38, Color.BLUE)).with(new GridMovement(38, controller, this, 1)).buildAndAttach();
            Elements temp = prev;
            if (temp != null){
            Elements temp_prev = temp.prev;
            while (temp_prev != null){
                temp = temp.prev;
                temp_prev = temp.prev;
            }
            
            temp.length++;
        }
        }

        public void add_element(){
            next = new Elements(pos_x, pos_y, vector, this);
        }

        public int length(){
            return length;
        }


    }
*/

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
    private final double MOVE_SPEED = 0.2;

    public Game(int starting_x, int starting_y, int cell_size, int grid_height, int grid_width){
        this.GRID_HEIGHT = grid_height;
        this.GRID_WIDTH = grid_width;
        this.CELL_SIZE = cell_size;
        Entity head = createSnakeSegment(starting_x / CELL_SIZE, starting_y / CELL_SIZE, Color.GREEN);
        snakeBody.add(head);
        addSnakeSegment();
        addSnakeSegment();
        addSnakeSegment();

 //       snakeElements.add(new SnakeElements(starting_x, starting_y, Direction.RIGHT, Color.GREEN, length, CELL_SIZE, this));
 //       length++;
 //       addSnakeElement(starting_x - CELL_SIZE, starting_y);
 //       addSnakeElement(starting_x - (CELL_SIZE * 2), starting_y);
 //       addSnakeElement(starting_x - (CELL_SIZE * 3), starting_y);

    }
    private Entity createSnakeSegment(int gridX, int gridY, Color color) {
        return entityBuilder()
                .at(gridX * CELL_SIZE, gridY * CELL_SIZE)
                .viewWithBBox(new Rectangle(CELL_SIZE - 1, CELL_SIZE - 1, color))
                .buildAndAttach();
    }

    private void addSnakeSegment() {
        // Get last segment position
        Entity lastSegment = snakeBody.get(snakeBody.size() - 1);
        double x = lastSegment.getX() / CELL_SIZE;
        double y = lastSegment.getY() / CELL_SIZE;

        // Create new segment at the same position (it will follow during movement)
        Entity newSegment = createSnakeSegment((int)x, (int)y, Color.LIGHTGREEN);
        snakeBody.add(newSegment);
    }
//    public void addSnakeElement(/*Color color*/) {
/*        int starting_x;
        int starting_y;
        Direction dir;
        starting_x = snakeElements.get(length - 1).getPosX();
        starting_y = snakeElements.get(length - 1).getPosY();
        dir = snakeElements.get(length - 1).getDirection();
        snakeElements.add(new SnakeElements(starting_x, starting_y, dir, Color.LIGHTGREEN, length, CELL_SIZE, this));
        length++;
        

    }

    public void addSnakeElement(int pos_x, int pos_y) {
        int starting_x;
        int starting_y;
        Direction dir;
        starting_x = snakeElements.get(length - 1).getPosX();
        starting_y = snakeElements.get(length - 1).getPosY();
        dir = snakeElements.get(length - 1).getDirection();
        snakeElements.add(new SnakeElements(starting_x, starting_y, dir, Color.LIGHTGREEN, length, CELL_SIZE, this));
        length++;
        

    }
*/

    public void move() {
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
            //gameOver();
            return;
        }

        // Check self-collision - game over if hit self
        for (int i = 1; i < snakeBody.size(); i++) {
            Entity segment = snakeBody.get(i);
            int segX = (int)(segment.getX() / CELL_SIZE);
            int segY = (int)(segment.getY() / CELL_SIZE);

            if (segX == newGridX && segY == newGridY) {
                //gameOver();
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
                /*if (food != null) {
                    int foodX = (int)(food.getX() / GRID_SIZE);
                    int foodY = (int)(food.getY() / GRID_SIZE);
                    
                    if (newGridX == foodX && newGridY == foodY) {
                        addSnakeSegment();
                        spawnFood();
                        inc("score", +1);
                    }
                }*/
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
    public void updateDirection(Direction dir){
        nextDirection = dir;
    }

    public Direction getDirection() {
        return nextDirection;
    }
}
