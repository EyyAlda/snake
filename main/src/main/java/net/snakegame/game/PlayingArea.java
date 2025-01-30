package net.snakegame.game;

public class PlayingArea {

    /**
     * Größenpreset identfier für die 3 standard Spielfeldgrößen
     */
    public static enum Size {
        LARGE,
        MEDIUM,
        SMALL
    }

    /**
     * Spielfelddaten
     */
    private int grid[][];

    Controller controller;

    public PlayingArea(Size area_size, Controller controller) {
        this.controller = controller;
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
        for (int y = 0; y < grid.length; y++){
            for (int x = 0; x < grid[y].length; x++){
                grid[y][x] = 0;
            }
        }
    }

    /** Gibt true zurück, wenn ein Platz im Grid vergeben ist
     * @author Lennard Rütten
     * @param x
     * @param y
     * @return
     */
    public boolean is_spot_taken(int x, int y){
        if (grid[y][x] != 0) {
        return true;
        }
        return false;
    }

    /** Plaziert eine Frucht zufällig auf dem Spielfeld
     * @author Lennard Rütten
     */
    public void place_fruit(){
        int x;
        int y;
        do {
            x = (int) Math.random() * grid[0].length;
            y = (int) Math.random() * grid.length;
        } while (is_spot_taken(x, y));
        grid[y][x] = 4;

    }

    public void refresh_playing_area(){
        for (int y = 0; y < grid.length; y++){
            for (int x = 0; x < grid[y].length; x++){
                if (grid[y][x] != 4){
                    grid[y][x] = 0;
                    if (controller.check_for_snake_position(x, y)){
                        grid[y][x] = 1;
                    }
                }
            }
        }
    }

    public int[] get_playing_area_size(){
        return new int[] {grid[0].length, grid.length};
    }
}
