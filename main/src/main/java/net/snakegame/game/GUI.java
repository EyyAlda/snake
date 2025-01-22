package net.snakegame.game;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.MenuItem;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.effect.DropShadow;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.shape.Circle;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import java.util.EnumSet;

public class GUI extends GameApplication {
    private Entity player;
    private Button endGameButton;
    private Text scoreText;
    private int score = 0;
    private KeyCode upKey = KeyCode.W;
    private KeyCode downKey = KeyCode.S;
    private KeyCode leftKey = KeyCode.A;
    private KeyCode rightKey = KeyCode.D;
    private static boolean isMusicOn = true;
    private static boolean isSoundOn = true;
    private static String selectedSize = "Medium";

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1200);
        settings.setHeight(800);
        settings.setTitle("Snake Game");
        settings.setMainMenuEnabled(true);
        settings.setEnabledMenuItems(EnumSet.of(MenuItem.EXTRA));
        settings.setSceneFactory(new CustomSceneFactory());
    }

    private static class CustomSceneFactory extends SceneFactory {
        @Override
        public FXGLMenu newMainMenu() {
            return new SnakeMainMenu();
        }
    }

    private static class SnakeMainMenu extends FXGLMenu {
        private VBox menuBox;
        private Node waitingForKey = null;
        private Text waitingText = null;

        public SnakeMainMenu() {
            super(MenuType.MAIN_MENU);
            createSnakeBackground();
            menuBox = new VBox(15);
            menuBox.setAlignment(Pos.CENTER);
            showMainMenu();
            getContentRoot().setOnKeyPressed(this::handleKeyPress);
        }

        private void handleKeyPress(KeyEvent event) {
            if (waitingForKey != null && waitingText != null) {
                KeyCode pressedKey = event.getCode();
                GUI mainInstance = (GUI) FXGL.getApp();

                if (waitingForKey.getUserData().equals("up")) {
                    mainInstance.upKey = pressedKey;
                    waitingText.setText("Up: " + pressedKey.getName());
                } else if (waitingForKey.getUserData().equals("down")) {
                    mainInstance.downKey = pressedKey;
                    waitingText.setText("Down: " + pressedKey.getName());
                } else if (waitingForKey.getUserData().equals("left")) {
                    mainInstance.leftKey = pressedKey;
                    waitingText.setText("Left: " + pressedKey.getName());
                } else if (waitingForKey.getUserData().equals("right")) {
                    mainInstance.rightKey = pressedKey;
                    waitingText.setText("Right: " + pressedKey.getName());
                }

                waitingForKey.setStyle(createNormalControlStyle());
                waitingForKey = null;
                waitingText = null;
                event.consume();
            }
        }

        private void showMainMenu() {
            menuBox.getChildren().clear();

            Text title = FXGL.getUIFactoryService().newText("SNAKE", Color.LIGHTGREEN, 72);
            title.setEffect(new DropShadow(10, Color.BLACK));

            Button btnPlay = createSnakeButton("Start Game");
            btnPlay.setOnAction(e -> fireNewGame());

            Button btnOptions = createSnakeButton("Options");
            btnOptions.setOnAction(e -> showOptionsMenu());

            Button btnEndGame = createSnakeButton("End Game");
            btnEndGame.setOnAction(e -> FXGL.getGameController().exit());

            menuBox.getChildren().addAll(
                    title,
                    createSeparator(),
                    btnPlay,
                    btnOptions,
                    btnEndGame
            );

            menuBox.setTranslateX(getAppWidth() / 2.0 - 150);
            menuBox.setTranslateY(getAppHeight() / 2.0 - 200);

            if (!getContentRoot().getChildren().contains(menuBox)) {
                getContentRoot().getChildren().add(menuBox);
            }
        }

        private void musicControl(Button btnMusic) {
            isMusicOn = !isMusicOn;
            btnMusic.setText("Music: " + (isMusicOn ? "ON" : "OFF"));
        }

        private void soundControl(Button btnSound) {
            isSoundOn = !isSoundOn;
            btnSound.setText("Sound: " + (isSoundOn ? "ON" : "OFF"));
        }

        private void showOptionsMenu() {
            menuBox.getChildren().clear();

            Text optionsTitle = FXGL.getUIFactoryService().newText("Options", Color.LIGHTGREEN, 32);

            Button btnSound = createSnakeButton("Sound: " + (isSoundOn ? "ON" : "OFF"));
            btnSound.setOnAction(e -> soundControl(btnSound));

            Button btnMusic = createSnakeButton("Music: " + (isMusicOn ? "ON" : "OFF"));
            btnMusic.setOnAction(e -> musicControl(btnMusic));

            ComboBox<String> sizeSelector = new ComboBox<>();
            sizeSelector.getItems().addAll("Small", "Medium", "Large");
            sizeSelector.setValue(selectedSize);
            sizeSelector.setStyle(
                    "-fx-background-color: #006400;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 14px;"
            );
            sizeSelector.setOnAction(e -> selectedSize = sizeSelector.getValue());

            Button btnControls = createSnakeButton("Controls");
            btnControls.setOnAction(e -> showControlsMenu());

            Button btnBack = createSnakeButton("Back");
            btnBack.setOnAction(e -> showMainMenu());

            menuBox.getChildren().addAll(
                    optionsTitle,
                    createSeparator(),
                    btnSound,
                    btnMusic,
                    sizeSelector,
                    btnControls,
                    createSeparator(),
                    btnBack
            );
        }

        private void showControlsMenu() {
            menuBox.getChildren().clear();

            Text controlsTitle = FXGL.getUIFactoryService().newText("Controls", Color.LIGHTGREEN, 32);

            GUI mainInstance = (GUI) FXGL.getApp();

            VBox controlsBox = new VBox(20);
            controlsBox.setAlignment(Pos.CENTER);

            HBox upControl = createControlButton("Up", mainInstance.upKey, "up");
            HBox downControl = createControlButton("Down", mainInstance.downKey, "down");
            HBox leftControl = createControlButton("Left", mainInstance.leftKey, "left");
            HBox rightControl = createControlButton("Right", mainInstance.rightKey, "right");

            Button btnBack = createSnakeButton("Back");
            btnBack.setOnAction(e -> showOptionsMenu());

            controlsBox.getChildren().addAll(
                    upControl,
                    downControl,
                    leftControl,
                    rightControl
            );

            menuBox.getChildren().addAll(
                    controlsTitle,
                    createSeparator(),
                    controlsBox,
                    createSeparator(),
                    btnBack
            );
        }

        private HBox createControlButton(String label, KeyCode currentKey, String controlType) {
            HBox control = new HBox(10);
            control.setAlignment(Pos.CENTER);

            Text keyText = FXGL.getUIFactoryService().newText(label + ": " + currentKey.getName(), Color.WHITE, 18);
            Button btnChange = new Button("Change");
            btnChange.setStyle(createNormalControlStyle());
            btnChange.setUserData(controlType);

            btnChange.setOnAction(e -> {
                if (waitingForKey != null) {
                    waitingForKey.setStyle(createNormalControlStyle());
                }
                waitingForKey = btnChange;
                waitingText = keyText;
                btnChange.setStyle(createWaitingControlStyle());
                keyText.setText(label + ": Press any key...");
            });

            control.getChildren().addAll(keyText, btnChange);
            return control;
        }

        private String createNormalControlStyle() {
            return "-fx-background-color: #006400;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 14px;" +
                    "-fx-padding: 5px 10px;" +
                    "-fx-border-color: #32CD32;" +
                    "-fx-border-width: 1px;";
        }

        private String createWaitingControlStyle() {
            return "-fx-background-color: #008800;" +
                    "-fx-text-fill: yellow;" +
                    "-fx-font-size: 14px;" +
                    "-fx-padding: 5px 10px;" +
                    "-fx-border-color: yellow;" +
                    "-fx-border-width: 2px;";
        }

        private Button createSnakeButton(String text) {
            Button button = new Button(text);
            button.setPrefWidth(300);

            String normalStyle = "-fx-background-color: #006400;" +
                    "-fx-text-fill: #98FB98;" +
                    "-fx-font-size: 18px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 15px;" +
                    "-fx-border-color: #32CD32;" +
                    "-fx-border-width: 2px;" +
                    "-fx-border-radius: 5px;" +
                    "-fx-background-radius: 5px;";

            String hoverStyle = "-fx-background-color: #008000;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 18px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 15px;" +
                    "-fx-border-color: #98FB98;" +
                    "-fx-border-width: 2px;" +
                    "-fx-border-radius: 5px;" +
                    "-fx-background-radius: 5px;";

            button.setStyle(normalStyle);
            button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
            button.setOnMouseExited(e -> button.setStyle(normalStyle));

            return button;
        }

        private Rectangle createSeparator() {
            Rectangle separator = new Rectangle(250, 2);
            separator.setFill(Color.LIGHTGREEN);
            separator.setOpacity(0.5);
            return separator;
        }

        private void createSnakeBackground() {
            Rectangle bg = new Rectangle(getAppWidth(), getAppHeight(), Color.rgb(0, 20, 0));
            getContentRoot().getChildren().add(0, bg);

            for (int i = 0; i < 8; i++) {
                Circle food = new Circle(5, Color.RED);
                food.setTranslateX(Math.random() * getAppWidth());
                food.setTranslateY(Math.random() * getAppHeight());
                getContentRoot().getChildren().add(food);

                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.seconds(2), e -> {
                            food.setTranslateX(Math.random() * getAppWidth());
                            food.setTranslateY(Math.random() * getAppHeight());
                        })
                );
                timeline.setCycleCount(Timeline.INDEFINITE);
                timeline.play();
            }
        }
    }

    @Override
    protected void initGame() {
        HBox gameContainer = new HBox(20);
        gameContainer.setAlignment(Pos.CENTER_LEFT);

        int cellSize = 20;
        int gridWidth, gridHeight;
        switch (selectedSize) {
            case "Small":
                gridWidth = 30;
                gridHeight = 30;
                break;
            case "Large":
                gridWidth = 50;
                gridHeight = 40;
                break;
            default: // Medium
                gridWidth = 40;
                gridHeight = 35;
                break;
        }

        VBox grid = new VBox(0);
        grid.setAlignment(Pos.CENTER);

        for (int i = 0; i < gridHeight; i++) {
            HBox row = new HBox(0);
            row.setAlignment(Pos.CENTER);
            for (int j = 0; j < gridWidth; j++) {
                Rectangle cell = new Rectangle(cellSize, cellSize);
                if ((i + j) % 2 == 0) {
                    cell.setFill(Color.rgb(50, 150, 50));
                } else {
                    cell.setFill(Color.rgb(100, 200, 100));
                }
                row.getChildren().add(cell);
            }
            grid.getChildren().add(row);
        }

        VBox sidebar = new VBox(20);
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: white;");
        sidebar.setPrefWidth(200);

        scoreText = new Text("Score: 0");
        scoreText.setStyle("-fx-font-size: 24px; -fx-fill: #006400;");

        endGameButton = new Button("End Game");
        endGameButton.setStyle(
                "-fx-background-color: #006400;" +
                        "-fx-text-fill: #98FB98;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 8px 16px;" +
                        "-fx-border-color: #32CD32;" +
                        "-fx-border-width: 1px;"
        );
        endGameButton.setOnAction(e -> FXGL.getGameController().gotoMainMenu());

        sidebar.getChildren().addAll(scoreText, endGameButton);
        gameContainer.getChildren().addAll(grid, sidebar);
        FXGL.getGameScene().addUINode(gameContainer);

        player = FXGL.entityBuilder()
                .at(400, 300)
                .view(new Rectangle(20, 20, Color.MEDIUMVIOLETRED))
                .buildAndAttach();
    }

    @Override
    protected void initInput() {
        FXGL.onKey(upKey, () -> player.translateY(-5));
        FXGL.onKey(downKey, () -> player.translateY(5));
        FXGL.onKey(leftKey, () -> player.translateX(-5));
        FXGL.onKey(rightKey, () -> player.translateX(5));

        FXGL.onKeyDown(KeyCode.ESCAPE, () -> {
            FXGL.getGameController().gotoMainMenu();
        });
    }

    public void start_gui(String[] args) {

        launch(args);
    }
}