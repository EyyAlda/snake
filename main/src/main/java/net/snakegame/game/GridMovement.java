package net.snakegame.game;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.pathfinding.CellMoveComponent;

import javafx.geometry.Point2D;

public class GridMovement extends Component {
    private final int GRID_SIZE;

    private int[][] grid;
    private Point2D targetPosition;
    private Direction currentDirection = Direction.NONE;
    private Controller controller;
    private final int MOVEMENT_SPEED = 5;
    private int currentGridX, currentGridY, nextGridX, nextGridY;
    //private SnakeEntity.Elements element;
    private boolean isMoving;
    private Direction direction;
    private Direction next_direction;

/*
    public GridMovement(int grid_size, Controller controller, SnakeEntity.Elements pElement, int isHead){
        GRID_SIZE = grid_size;
        this.controller = controller;
        element = pElement;
    }

    @Override
    public void onUpdate(double tpf){
        if (!controller.gameOver && isMoving){
            grid = controller.getGrid();
            moveTowardsTarget();
        } else if (!controller.gameOver && !isMoving) {
            move();
        }
    }

    private Direction getNextDirection(){
        for (Direction dir : Direction.values()) {
            if (dir.dx == element.prev.vector.getX() && dir.dy == element.prev.vector.getY()){
                return dir;
            }
        }
        return Direction.NONE;
    }

    private Direction getNextHeadDirection(){
        for (Direction dir : Direction.values()) {
            if (dir.dx == controller.getHeadDirection().getX() && dir.dy == controller.getHeadDirection().getY()) {
                return dir;
            }
        }
        return Direction.NONE;
    }

    public void move() {
        isMoving = true;
        direction = next_direction;
        if (element.prev != null) {
            next_direction = getNextDirection();
        } else {
            next_direction = getNextHeadDirection();
        }
        currentDirection = direction;

        currentGridX = (int) (entity.getX() / GRID_SIZE);
        currentGridY = (int) (entity.getY() / GRID_SIZE);

        nextGridX = currentGridX + direction.dx;
        nextGridY = currentGridY + direction.dy;

        if (isValidMove(nextGridX, nextGridY)){
            targetPosition = new Point2D(
                    nextGridX * GRID_SIZE,
                    nextGridY * GRID_SIZE
                    );
        } else {
            controller.gameOver = true;
        }

    }

    public boolean isValidMove(int gridX, int gridY){
        if (gridX < 0 || gridY < 0 || gridX >= grid[0].length || gridY >= grid.length) {
            return false;
        }

        return grid[gridY][gridX] != 2;
    }

    private void moveTowardsTarget(){
        Point2D currentPosition = new Point2D(entity.getX(), entity.getY());
        Point2D direction = targetPosition.subtract(currentPosition).normalize();
        double dx = direction.getX() * MOVEMENT_SPEED;
        double dy = direction.getY() * MOVEMENT_SPEED;
        entity.translate(dx, dy);

        if (Math.abs(entity.getX() - targetPosition.getX()) < MOVEMENT_SPEED &&
            Math.abs(entity.getY() - targetPosition.getY()) < MOVEMENT_SPEED) {
            // Snap to grid
            entity.setPosition(targetPosition);
            updatePositions();
            isMoving = false;
            }
    }

    private void updatePositions(){
        controller.updateGridAtPosition(currentGridX, currentGridY, 0);
        controller.updateGridAtPosition(nextGridX, nextGridY, 2);
        currentGridX = nextGridX;
        currentGridY = nextGridY;
        element.pos_x = nextGridX;
        element.pos_y = nextGridY;
    }
*/

    public GridMovement(Controller controller, int grid_size){
        GRID_SIZE = grid_size;
    }

    @Override
    public void onUpdate(double tpf){

    }
}
