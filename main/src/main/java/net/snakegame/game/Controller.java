package net.snakegame.game;

public class Controller {
PlayingArea area = null;

    public void create_playing_area(PlayingArea.Size size){
        area = new PlayingArea(size);
    }

    public void create_snake(){

    }
}