package net.snakegame.game;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


public class Game implements EntityFactory{

    public enum EntityType {
        HEAD, BODY, FOOD, WALL
    }

    private GUI gui;
    private static int GRID_SIZE_X;
    private static int GRID_SIZE_Y;
    private static int TILE_SIZE;
    private static final double SPEED = 5;

    private Entity head;
    private List<Entity> body = new ArrayList<>();
    private Deque<Point2D> movement_history = new LinkedList<>();
    private Point2D direction = new Point2D(1, 0);


    public Game(GUI pGui){
        this.gui = pGui;
        int[] sizes = gui.get_grid_size();
        GRID_SIZE_X = sizes[0];
        GRID_SIZE_Y = sizes[1];
        TILE_SIZE = gui.get_cell_size();
    }

    @Spawns("Head")
    public Entity newHead(SpawnData data) {
        return FXGL.entityBuilder(data)
        .type(EntityType.HEAD)
        .viewWithBBox(new Rectangle(TILE_SIZE, TILE_SIZE, Color.BLUE))
        .with(new CollidableComponent(true))
        .build();
    }

    @Spawns("Body")
    public Entity newBodySegment(SpawnData data) {
        return FXGL.entityBuilder(data)
        .type(EntityType.BODY)
        .viewWithBBox(new Rectangle(TILE_SIZE, TILE_SIZE, Color.GREEN))
        .with(new CollidableComponent(true))
        .build();
    }

    public void set_direction(Point2D new_direction) {
        direction = new_direction;
    }
    
}
