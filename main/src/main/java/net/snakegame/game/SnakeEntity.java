package net.snakegame.game;

import java.util.ArrayList;
import java.util.List;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SnakeEntity {
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

    private List<SnakeElements> snakeElements = new ArrayList<SnakeElements>();

    public SnakeEntity(int starting_x, int starting_y, Controller controller){
        snakeElements.add(new SnakeElements(starting_x, starting_y, Direction.RIGHT, controller));

    }

    public void updateDirection(Direction dir){
        snakeElements.get(0).updateDirection(dir);
    }
}
