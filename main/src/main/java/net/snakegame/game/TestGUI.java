package net.snakegame.game;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;

import javafx.scene.input.KeyCode;

public class TestGUI extends GameApplication {

    Controller controller = null;
    int[][] grid = new int[15][12];
    Game snake = null;

    @Override
    protected void initSettings(GameSettings settings){
        settings.setWidth(900);
        settings.setHeight(700);
        settings.setTitle("Snake Game");
    }

    @Override
    protected void initGame(){
        controller = new Controller(this);
        //controller.FilesDownloader();
        snake = new Game(5, 3, 20, 15, 15);        
        //snake.create_snake();
    }

    @Override
    protected void initInput() {
        FXGL.onKey(KeyCode.A, () -> {
            snake.updateDirection(Direction.LEFT);
        });
        FXGL.onKey(KeyCode.D, () -> {
            snake.updateDirection(Direction.RIGHT);
        });
        FXGL.onKey(KeyCode.W, () -> {
            snake.updateDirection(Direction.UP);
        });
        FXGL.onKey(KeyCode.S, () -> {
            snake.updateDirection(Direction.DOWN);
        });
    }




    public static void main(String[] args) {
        launch(args);
    }

}
