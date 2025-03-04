package net.snakegame.game;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SnakeElements {

    private int pos_x, pos_y;
    private Direction vector;
    private Entity snake;

    public SnakeElements(int posX, int posY, Direction vector, Controller controller) {
        this.pos_x = posX;
        this.pos_y = posY;
        this.vector = vector;
        snake = FXGL.entityBuilder()
            .at(pos_x, pos_y)
            .viewWithBBox(new Rectangle(38, 38, Color.BLUE))
            .with(new GridMovement(controller, 38))
            .buildAndAttach();
    }

    public void updatePositions(int posX, int posY){
        this.pos_x = posX;
        this.pos_y = posY;
    }

    public void updateDirection(Direction direction){
        this.vector = direction;
    }
    
}
