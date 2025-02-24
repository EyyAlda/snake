package net.snakegame.game;

public class Main {

    public static void main(String[] args){
        Controller files = new Controller();
        files.FilesDownloader();
        GUI spiel = new GUI();
        spiel.start_gui(args);
    }
}
