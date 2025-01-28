package net.snakegame.game;

public class PlayingArea {
    public static enum Size {
        LARGE,
        MEDIUM,
        SMALL
    }

    private int grid[][];

    public PlayingArea(Size area_size) {
        int h_size;
        int v_size;
        switch (area_size) {
            case LARGE:
                h_size = 25;
                v_size = 20;
                break;
            case SMALL:
                h_size = 15;
                v_size = 12;
                break;
            default:
                h_size = 20;
                v_size = 16;
                break;
        }
        grid = new int[v_size][h_size];
    }

    public void place_fruit(){

    }
}
