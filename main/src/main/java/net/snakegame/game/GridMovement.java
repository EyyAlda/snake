package net.snakegame.game;

import com.almasb.fxgl.entity.component.Component;

import javafx.geometry.Point2D;

public class GridMovement extends Component {
    private final int GRID_SIZE;

    private int[][] grid;
    private Point2D targetPosition;
    private Direction currentDirection = Direction.NONE;
    private Controller controller;
    private final int MOVEMENT_SPEED = 5;


    public GridMovement(int[][] grid, int grid_size, Controller controller){
        this.grid = grid;
        GRID_SIZE = grid_size;
        this.controller = controller;
    }

    @Override
    public void onUpdate(double tpf){

    }

    public void move(Direction direction) {
        currentDirection = direction;

        int currentGridX = (int) (entity.getX() / GRID_SIZE);
        int currentGridY = (int) (entity.getY() / GRID_SIZE);

        int nextGridX = currentGridX + direction.dx;
        int nextGridY = currentGridY + direction.dy;

        if (isValidMove(nextGridX, nextGridY)){
            targetPosition = new Point2D(
                    nextGridX * GRID_SIZE,
                    nextGridY * GRID_SIZE
                    );
        }

    }

    public boolean isValidMove(int gridX, int gridY){
        if (gridX < 0 || gridY < 0 || gridX >= grid[0].length || gridY >= grid.length) {
            return false;
        }

        return grid[gridY][gridX] != 1;
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

            currentDirection = Direction.NONE;
            }
    }

}
