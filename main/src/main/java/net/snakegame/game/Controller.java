package net.snakegame.game;

public class Controller {
PlayingArea area = null;
Snake snake = null;

    public void create_playing_area(PlayingArea.Size size){
        area = new PlayingArea(size, this);
    }

    public void create_snake(){
        int[] size = area.get_playing_area_size();
        int starting_y = size[1] / 2;
        int starting_x = (int) Math.round(size[0] * 0.3);
        snake = new Snake(starting_x, starting_y);
    }

    public boolean check_for_snake_position(int x, int y){
        return snake.is_snake_at_position(x, y);
    }

    public void move_snake(){
        snake.move_snake();
    }
}