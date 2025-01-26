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
        settings.setWidth(900);
        settings.setHeight(700);
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
            Text gameSizeTitel = FXGL.getUIFactoryService().newText("Size", Color.LIGHTGREEN, 26);

            Button btnSound = createSnakeButton("Sound: " + (isSoundOn ? "ON" : "OFF"));
            btnSound.setOnAction(e -> soundControl(btnSound));

            Button btnMusic = createSnakeButton("Music: " + (isMusicOn ? "ON" : "OFF"));
            btnMusic.setOnAction(e -> musicControl(btnMusic));

            ComboBox<String> sizeSelector = new ComboBox<>();
            sizeSelector.getItems().addAll("Small", "Medium", "Large");
            sizeSelector.setValue(selectedSize);
            sizeSelector.setStyle(
                    "-fx-background-color: #004d00;" +
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
                    btnControls,
                    createSeparator(),
                    gameSizeTitel,
                    sizeSelector,
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
            return "-fx-background-color: #004d00;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 14px;" +
                    "-fx-padding: 5px 10px;" +
                    "-fx-border-color: #2e8b57;" +
                    "-fx-border-width: 1px;";
        }

        private String createWaitingControlStyle() {
            return "-fx-background-color: #008000;" +
                    "-fx-text-fill: yellow;" +
                    "-fx-font-size: 14px;" +
                    "-fx-padding: 5px 10px;" +
                    "-fx-border-color: yellow;" +
                    "-fx-border-width: 2px;";
        }

        private Button createSnakeButton(String text) {
            Button button = new Button(text);
            button.setPrefWidth(300);

            String normalStyle = "-fx-background-color: #004d00;" + // Darker green
                    "-fx-text-fill: #90ee90;" + // Light green text
                    "-fx-font-size: 18px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 15px;" +
                    "-fx-border-color: #2e8b57;" + // Sea green border
                    "-fx-border-width: 2px;" +
                    "-fx-border-radius: 10px;" + // More rounded corners
                    "-fx-background-radius: 10px;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 5, 0.0, 0, 1);"; // Shadow effect

            String hoverStyle = "-fx-background-color: #008000;" + // Brighter green on hover
                    "-fx-text-fill: #e0ffff;" + // Light cyan text
                    "-fx-font-size: 18px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 15px;" +
                    "-fx-border-color: #3cb371;" + // Medium sea green border
                    "-fx-border-width: 2px;" +
                    "-fx-border-radius: 10px;" +
                    "-fx-background-radius: 10px;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0.0, 0, 1);"; // Stronger shadow

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
            Rectangle bg = new Rectangle(getAppWidth(), getAppHeight(), Color.rgb(0, 30, 0));
            getContentRoot().getChildren().add(0, bg);

            for (int i = 0; i < 12; i++) {
                Circle snakeScale = new Circle(3 + Math.random() * 5,
                        Color.rgb(50 + (int)(Math.random() * 50), 100 + (int)(Math.random() * 100), 50, 0.7));
                snakeScale.setTranslateX(Math.random() * getAppWidth());
                snakeScale.setTranslateY(Math.random() * getAppHeight());
                getContentRoot().getChildren().add(snakeScale);

                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.seconds(2 + Math.random() * 3), e -> {
                            snakeScale.setTranslateX(Math.random() * getAppWidth());
                            snakeScale.setTranslateY(Math.random() * getAppHeight());
                            snakeScale.setRadius(3 + Math.random() * 5);
                            snakeScale.setFill(Color.rgb(
                                    50 + (int)(Math.random() * 50),
                                    100 + (int)(Math.random() * 100),
                                    50,
                                    0.7
                            ));
                        })
                );
                timeline.setCycleCount(Timeline.INDEFINITE);
                timeline.play();
            }
        }
    }

    @Override
    protected void initGame() {
        int screenWidth = 900;
        int screenHeight = 700;
        int cellSize = 25;
        int gridWidth, gridHeight;
        switch (selectedSize) {
            case "Small":
                gridWidth = 15;
                gridHeight = 12;
                break;
            case "Large":
                gridWidth = 25;
                gridHeight = 20;
                break;
            default: // Medium
                gridWidth = 20;
                gridHeight = 16;
                break;
        }

        int totalGridWidth = cellSize * gridWidth;
        int totalGridHeight = cellSize * gridHeight;

        // Snake-themed animated background
        Rectangle bg = new Rectangle(screenWidth, screenHeight, Color.rgb(0, 30, 0));
        FXGL.getGameScene().addUINode(bg);

        // Add animated circles similar to the main menu
        for (int i = 0; i < 12; i++) {
            Circle snakeScale = new Circle(3 + Math.random() * 5,
                    Color.rgb(50 + (int)(Math.random() * 50), 100 + (int)(Math.random() * 100), 50, 0.7));
            snakeScale.setTranslateX(Math.random() * screenWidth);
            snakeScale.setTranslateY(Math.random() * screenHeight);
            FXGL.getGameScene().addUINode(snakeScale);

            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(2 + Math.random() * 3), e -> {
                        snakeScale.setTranslateX(Math.random() * screenWidth);
                        snakeScale.setTranslateY(Math.random() * screenHeight);
                        snakeScale.setRadius(3 + Math.random() * 5);
                        snakeScale.setFill(Color.rgb(
                                50 + (int)(Math.random() * 50),
                                100 + (int)(Math.random() * 100),
                                50,
                                0.7
                        ));
                    })
            );
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        }

        HBox gameContainer = new HBox(20);
        gameContainer.setAlignment(Pos.CENTER);
        gameContainer.setPadding(new Insets(20));

        // Create snake-scale grid
        VBox grid = new VBox(0);
        grid.setAlignment(Pos.CENTER);

        for (int i = 0; i < gridHeight; i++) {
            HBox row = new HBox(0);
            row.setAlignment(Pos.CENTER);
            for (int j = 0; j < gridWidth; j++) {
                Rectangle cell = new Rectangle(cellSize, cellSize);
                if ((i + j) % 2 == 0) {
                    cell.setFill(Color.rgb(144, 238, 144)); // Medium sea green
                } else {
                    cell.setFill(Color.rgb(50, 205, 50)); // Sea green
                }
                cell.setStroke(Color.rgb(34, 139, 34, 0.4)); // Forest green light border
                row.getChildren().add(cell);
            }
            grid.getChildren().add(row);
        }

        // Sidebar with snake theme
        VBox sidebar = new VBox(20);
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle(
                "-fx-background-color: rgba(0, 100, 0, 0.8);" + // Dark green with transparency
                        "-fx-border-color: #3cb371;" + // Medium sea green border
                        "-fx-border-width: 2px;"
        );
        sidebar.setPrefWidth(200);

        // Score text with snake theme
        scoreText = new Text("Score: 0");
        scoreText.setStyle(
                "-fx-font-size: 24px;" +
                        "-fx-fill: #90ee90;" + // Light green
                        "-fx-font-weight: bold;"
        );

        // End game button with snake theme
        endGameButton = new Button("End Game");
        endGameButton.setStyle(
                "-fx-background-color: #004d00;" + // Dark green
                        "-fx-text-fill: #90ee90;" + // Light green
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 8px 16px;" +
                        "-fx-border-color: #2e8b57;" + // Sea green
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 5px;" +
                        "-fx-background-radius: 5px;"
        );
        endGameButton.setOnAction(e -> FXGL.getGameController().gotoMainMenu());

        sidebar.getChildren().addAll(scoreText, endGameButton);
        gameContainer.getChildren().addAll(grid, sidebar);

        gameContainer.setTranslateX((screenWidth - (totalGridWidth + 240)) / 2);
        gameContainer.setTranslateY((screenHeight - totalGridHeight) / 2);

        FXGL.getGameScene().addUINode(gameContainer);

        // Player as a snake segment
        player = FXGL.entityBuilder()
                .at(gameContainer.getTranslateX() + (totalGridWidth / 2),
                        gameContainer.getTranslateY() + (totalGridHeight / 2))
                .view(new Rectangle(cellSize, cellSize, Color.rgb(34, 139, 34, 0.9))) // Forest green
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