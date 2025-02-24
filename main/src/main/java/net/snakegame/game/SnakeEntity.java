package net.snakegame.game;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SnakeEntity {

    Controller controller;

    private int[][] playing_area;
    private int starting_pos_x, starting_pos_y;

    private Entity head;
    private Elements elements;

    

    public SnakeEntity(int starting_x, int starting_y, int[][] playing_grid, Controller pController) {
        this.starting_pos_x = starting_x;
        this.starting_pos_y = starting_y;
        this.playing_area = playing_grid;
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
            entity = FXGL.entityBuilder().at(starting_x, starting_y).viewWithBBox(new Rectangle(38, 38, Color.BLUE)).with(new GridMovement(38, controller, this)).buildAndAttach();
            Elements temp = prev;
            Elements temp_prev = temp.prev;
            while (temp_prev != null){
                temp = temp.prev;
                temp_prev = temp.prev;
            }
            temp.length++;
        }

        public void add_element(){
            next = new Elements(pos_x, pos_y, vector, this);
        }

        public int length(){
            return length;
        }


    }

}
