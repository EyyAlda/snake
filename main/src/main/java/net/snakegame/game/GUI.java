package net.snakegame.game;

import static com.almasb.fxgl.dsl.FXGL.addText;
import static com.almasb.fxgl.dsl.FXGL.addVarText;
import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.getAppHeight;
import static com.almasb.fxgl.dsl.FXGL.getAppWidth;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import static com.almasb.fxgl.dsl.FXGL.getGameTimer;

/** Klasse mit den Daten für die Schlange auf dem Spielfeld
 * @author Nick Gegenheimer
 *
 */
import java.io.File;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Painter;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.MenuItem;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.LoadingScene;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.ui.ProgressBar;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class GUI extends GameApplication {
    private Entity player;
    private Button endGameButton;
    private Button skipTrackButton;
    private Text scoreText;
    private int score = 0;
    private KeyCode upKey = KeyCode.W;
    private KeyCode downKey = KeyCode.S;
    private KeyCode leftKey = KeyCode.A;
    private KeyCode rightKey = KeyCode.D;
    private static boolean isMusicOn = true;
    private static boolean isSoundOn = true;
    private static String selectedSize = "Medium";
    private static String slectedSpeed = "Medium";
    private static MediaPlayer backgroundMusicPlayer;
    private static int currentTrackIndex = 0;
    private static final String[] MUSIC_TRACKS = {
            Files.getUserDir(Files.DirectoryType.DOCUMENTS) + "/myGames/Snake/Sounds/8bitGameMusic.wav",
            Files.getUserDir(Files.DirectoryType.DOCUMENTS) + "/myGames/Snake/Sounds/GameMusic1.wav",
            Files.getUserDir(Files.DirectoryType.DOCUMENTS) + "/myGames/Snake/Sounds/GameMusic2.wav"
    };
    private static MediaPlayer menuMusicPlayer;
    private static final String MENU_MUSIC_TRACK = Files.getUserDir(Files.DirectoryType.DOCUMENTS) + "/myGames/Snake/Sounds/MenuMusic.wav";
    private static int GRID_SIZE_X;
    private static int GRID_SIZE_Y;
    private static int CELL_SIZE;
    private Game snake;
    private boolean hasGameStarted = false;



    public GUI(){}

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(800);
        settings.setHeight(700);
        settings.setTitle("Snake Game");
        settings.setMainMenuEnabled(true);
        settings.setEnabledMenuItems(EnumSet.of(MenuItem.EXTRA));
        settings.setManualResizeEnabled(true);
        settings.setPreserveResizeRatio(true);
        settings.setSceneFactory(new CustomSceneFactory());
    }

    private static class CustomSceneFactory extends SceneFactory {
        @Override
        public FXGLMenu newMainMenu() {
            return new SnakeMainMenu();
        }
    }


    private void initMenuMusic() {
        try {
            if (menuMusicPlayer != null) {
                menuMusicPlayer.stop();
                menuMusicPlayer.dispose();
            }
            Media menuMusic = new Media(new File(MENU_MUSIC_TRACK).toURI().toString());
            menuMusicPlayer = new MediaPlayer(menuMusic);
            menuMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Endlosschleife

            if (isMusicOn) {
                menuMusicPlayer.play();
            }
        } catch (Exception e) {
            System.out.println("Error loading menu music: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Methode zum Stoppen der Menümusik:
    private void stopMenuMusic() {
        if (menuMusicPlayer != null) {
            menuMusicPlayer.stop();
            menuMusicPlayer.dispose();
            menuMusicPlayer = null;
        }
    }


    private void initBackgroundMusic() {

        try {
            loadAndPlayTrack(currentTrackIndex);

            // Set up the end of media handler
            backgroundMusicPlayer.setOnEndOfMedia(() -> {
                currentTrackIndex = (currentTrackIndex + 1) % MUSIC_TRACKS.length;
                loadAndPlayTrack(currentTrackIndex);
            });

            // Get the primary stage and add focus listener
            FXGL.getGameScene().getRoot().sceneProperty().addListener((observable, oldScene, newScene) -> {
                if (newScene != null) {
                    // Add direct focus listener to the scene's window
                    newScene.getWindow().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (backgroundMusicPlayer != null) {
                            if (isNowFocused && isMusicOn) {
                                backgroundMusicPlayer.play();
                            } else {
                                backgroundMusicPlayer.pause();
                            }
                        }
                    });
                }
            });

            // Initial play if music is enabled
            if (isMusicOn) {
                backgroundMusicPlayer.play();
            }
        } catch (Exception e) {
            System.out.println("Error loading background music: " + e.getMessage());
            e.printStackTrace(); // Added for better error tracking
        }
    }

    private void loadAndPlayTrack(int trackIndex) {
        try {
            // Stoppe den aktuellen Player, falls vorhanden
            if (backgroundMusicPlayer != null) {
                backgroundMusicPlayer.stop();
                backgroundMusicPlayer.dispose();
            }

            // Lade und starte den neuen Track
            Media backgroundMusic = new Media(new File(MUSIC_TRACKS[trackIndex]).toURI().toString());
            backgroundMusicPlayer = new MediaPlayer(backgroundMusic);
            backgroundMusicPlayer.setCycleCount(1); // Spiele jeden Track einmal

            // Setze den OnEndOfMedia Handler für den neuen Player
            backgroundMusicPlayer.setOnEndOfMedia(() -> {
                currentTrackIndex = (currentTrackIndex + 1) % MUSIC_TRACKS.length;
                loadAndPlayTrack(currentTrackIndex);
            });

            if (isMusicOn) {
                backgroundMusicPlayer.play();
            }
        } catch (Exception e) {
            System.out.println("Error loading track " + trackIndex + ": " + e.getMessage());
        }
    }
    public void skipToNextTrack() {
        if (backgroundMusicPlayer != null) {
            currentTrackIndex = (currentTrackIndex + 1) % MUSIC_TRACKS.length;
            loadAndPlayTrack(currentTrackIndex);
        }
    }

    private void stopAndDisposeMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
            backgroundMusicPlayer.dispose();
            backgroundMusicPlayer = null;
        }
    }

    private static class SnakeMainMenu extends FXGLMenu {
        private VBox menuBox;
        private VBox optionsBox;
        private Node waitingForKey = null;
        private Text waitingText = null;

        public SnakeMainMenu() {
            super(MenuType.MAIN_MENU);
            createSnakeBackground();

            // Separate boxes for main menu and options
            menuBox = new VBox(15);
            menuBox.setAlignment(Pos.CENTER);

            optionsBox = new VBox(15);
            optionsBox.setAlignment(Pos.CENTER);

            showMainMenu();
            getContentRoot().setOnKeyPressed(this::handleKeyPress);
        }


        public static void play_sound(int soundId){
            if(isSoundOn){
                switch (soundId){
                    case 0:
                        String soundEating = Files.getUserDir(Files.DirectoryType.DOCUMENTS) + "/myGames/Snake/Sounds/eating.wav";
                        Media sound0 = new Media(new File (soundEating).toURI().toString());
                        MediaPlayer mediaPlayer0 = new MediaPlayer(sound0);
                        mediaPlayer0.play();
                        break;

                    case 1:
                        String soundButton = Files.getUserDir(Files.DirectoryType.DOCUMENTS) + "/myGames/Snake/Sounds/KlickSound.wav";
                        Media sound1 = new Media(new File(soundButton).toURI().toString());
                        MediaPlayer mediaPlayer1 = new MediaPlayer(sound1);
                        mediaPlayer1.play();
                        mediaPlayer1.setVolume(1.0);
                        break;

                    case 2:
                        String soundGameOver = Files.getUserDir(Files.DirectoryType.DOCUMENTS) + "/myGames/Snake/Sounds/SoundGameOver.wav";
                        Media sound2 = new Media(new File(soundGameOver).toURI().toString());
                        MediaPlayer mediaPlayer2 = new MediaPlayer(sound2);
                        mediaPlayer2.play();
                        break;
                }
            }
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

            // Entferne optionsBox falls vorhanden
            getContentRoot().getChildren().remove(optionsBox);

            // Starte Menümusik wenn das Hauptmenü angezeigt wird
            GUI mainInstance = (GUI) FXGL.getApp();
            mainInstance.initMenuMusic();

            Text title = FXGL.getUIFactoryService().newText("SNAKE", Color.LIGHTGREEN, 72);
            title.setEffect(new DropShadow(10, Color.BLACK));

            Button btnPlay = createSnakeButton("Start Game");
            btnPlay.setOnAction(e -> {
                play_sound(1);
                GUI mainInstance2 = (GUI) FXGL.getApp();
                mainInstance2.stopMenuMusic(); // Stoppe Menümusik vor Spielstart
                fireNewGame();
            });

            Button btnOptions = createSnakeButton("Options");
            btnOptions.setOnAction(e -> {
                play_sound(1);
                showOptionsMenu();
            });

            Button btnEndGame = createSnakeButton("End Game");
            btnEndGame.setOnAction(e -> {
                play_sound(1);
                FXGL.getGameController().exit();
            });

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

            GUI mainInstance = (GUI) FXGL.getApp();
            if (isMusicOn) {
                if (menuMusicPlayer != null) {
                    menuMusicPlayer.play();
                }
                if (backgroundMusicPlayer != null) {
                    backgroundMusicPlayer.play();
                }
            } else {
                if (menuMusicPlayer != null) {
                    menuMusicPlayer.pause();
                }
                if (backgroundMusicPlayer != null) {
                    backgroundMusicPlayer.pause();
                }
            }
        }


        private void soundControl(Button btnSound) {
            isSoundOn = !isSoundOn;
            btnSound.setText("Sound: " + (isSoundOn ? "ON" : "OFF"));
        }

        private void showOptionsMenu() {
            // Entferne das Hauptmenü
            getContentRoot().getChildren().remove(menuBox);

            optionsBox.getChildren().clear();

            Text optionsTitle = FXGL.getUIFactoryService().newText("Options", Color.LIGHTGREEN, 32);
            Text gameSizeTitel = FXGL.getUIFactoryService().newText("Size", Color.LIGHTGREEN, 26);
            Text speedGameTitel = FXGL.getUIFactoryService().newText("Speed", Color.LIGHTGREEN, 26);

            Button btnSound = createSnakeButton("Sound: " + (isSoundOn ? "ON" : "OFF"));
            btnSound.setOnAction(e -> {
                play_sound(1);
                soundControl(btnSound);
            });

            Button btnMusic = createSnakeButton("Music: " + (isMusicOn ? "ON" : "OFF"));
            btnMusic.setOnAction(e -> {
                play_sound(1);
                musicControl(btnMusic);
            });

            ComboBox<String> sizeSelector = new ComboBox<>();
            sizeSelector.getItems().addAll("Small", "Medium", "Large");
            sizeSelector.setValue(selectedSize);
            sizeSelector.setStyle(
                    "-fx-background-color: #004d00;" +
                            "-fx-color: white;" +
                            "-fx-font-size: 14px;"
            );
            sizeSelector.setOnAction(e -> {
                play_sound(1);
                selectedSize = sizeSelector.getValue();
            });

            ComboBox<String> speedSelector = new ComboBox<>();
            speedSelector.getItems().addAll("Fast", "Medium", "Slow");
            speedSelector.setValue(slectedSpeed);
            speedSelector.setStyle(
                    "-fx-background-color: #004d00;" +
                            "-fx-color: white;" +
                            "-fx-font-size: 14px;"
            );
            speedSelector.setOnAction(e -> {
                play_sound(1);
                slectedSpeed = speedSelector.getValue();
            });

            Button btnControls = createSnakeButton("Controls");
            btnControls.setOnAction(e -> {
                play_sound(1);
                showControlsMenu();
            });

            Button btnBack = createSnakeButton("Back");
            btnBack.setOnAction(e -> {
                play_sound(1);
                showMainMenu();
            });

            optionsBox.getChildren().addAll(
                    optionsTitle,
                    createSeparator(),
                    btnSound,
                    btnMusic,
                    btnControls,
                    createSeparator(),
                    gameSizeTitel,
                    sizeSelector,
                    speedGameTitel,
                    speedSelector,
                    createSeparator(),
                    btnBack
            );

            // Position the options box higher up than the main menu
            optionsBox.setTranslateX(getAppWidth() / 2.0 - 150);
            optionsBox.setTranslateY(getAppHeight() / 2.0 - 300); // Höher positioniert als das Hauptmenü

            if (!getContentRoot().getChildren().contains(optionsBox)) {
                getContentRoot().getChildren().add(optionsBox);
            }
        }

        private void showControlsMenu() {
            // Remove the options box first
            getContentRoot().getChildren().remove(optionsBox);

            // Clear and reuse the options box for controls
            optionsBox.getChildren().clear();

            Text controlsTitle = FXGL.getUIFactoryService().newText("Controls", Color.LIGHTGREEN, 32);

            GUI mainInstance = (GUI) FXGL.getApp();

            VBox controlsBox = new VBox(20);
            controlsBox.setAlignment(Pos.CENTER);

            HBox upControl = createControlButton("Up", mainInstance.upKey, "up");
            HBox downControl = createControlButton("Down", mainInstance.downKey, "down");
            HBox leftControl = createControlButton("Left", mainInstance.leftKey, "left");
            HBox rightControl = createControlButton("Right", mainInstance.rightKey, "right");

            Button btnBack = createSnakeButton("Back");
            btnBack.setOnAction(e -> {
                play_sound(1);
                showOptionsMenu();
            });

            controlsBox.getChildren().addAll(
                    upControl,
                    downControl,
                    leftControl,
                    rightControl
            );

            optionsBox.getChildren().addAll(
                    controlsTitle,
                    createSeparator(),
                    controlsBox,
                    createSeparator(),
                    btnBack
            );

            // Position the controls box
            optionsBox.setTranslateX(getAppWidth() / 2.0 - 150);
            optionsBox.setTranslateY(getAppHeight() / 2.0 - 300);

            // Add the controls box to the scene
            if (!getContentRoot().getChildren().contains(optionsBox)) {
                getContentRoot().getChildren().add(optionsBox);
            }
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
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("score", 0);
    }

    @Override
    protected void initUI() {
        addText("Score: ", getAppWidth() - 120, getAppHeight() - 20);
        addVarText("score", getAppWidth() - 50, getAppHeight() - 20);
    }

    @Override
    protected void initGame() {
        initBackgroundMusic();
        int screenWidth = 900;
        int screenHeight = 700;
        int gridWidth, gridHeight;
        gridHeight = switch (selectedSize) {
            case "Small" -> {
                gridWidth = 15;
                yield 12;
            }
            case "Large" -> {
                gridWidth = 25;
                yield 20;
            }
            default -> {
                gridWidth = 20;
                yield 16; 
            }
        };
        GRID_SIZE_X = gridWidth;
        GRID_SIZE_Y = gridHeight;
        CELL_SIZE = (int) 800 / gridWidth;


        getGameScene().setBackgroundColor(Color.GREEN);

        Canvas canvas = new Canvas(GRID_SIZE_X * CELL_SIZE, GRID_SIZE_Y * CELL_SIZE); 
        GraphicsContext gc = canvas.getGraphicsContext2D();

        
        for (int y = 0; y < GRID_SIZE_Y; y++) {
            for (int x = 0; x < GRID_SIZE_X; x++){
                if ((y + x) % 2 == 0){
                    gc.setFill(Color.rgb(171, 214, 81));
                } else {
                    gc.setFill(Color.rgb(162, 208, 72));
                }

                gc.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }

        Entity background = entityBuilder()
            .at(0, 0)
            .view(canvas)
            .zIndex(-1)
            .buildAndAttach();


        snake = new Game((int) (gridWidth/3) * CELL_SIZE, (int) (gridHeight/2) * CELL_SIZE, CELL_SIZE, GRID_SIZE_Y, GRID_SIZE_X);

        if (hasGameStarted) getGameTimer().runAtInterval(snake::move, Duration.seconds(0.2));

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
                "-fx-background-color: #004d00;" +
                        "-fx-text-fill: #90ee90;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 8px 16px;" +
                        "-fx-border-color: #2e8b57;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 5px;" +
                        "-fx-background-radius: 5px;"
        );
        endGameButton.setOnAction(e -> {
            stopAndDisposeMusic();
            initMenuMusic();
            FXGL.getGameController().gotoMainMenu();
        });

        skipTrackButton = new Button("Skip Track");
        skipTrackButton.setStyle(
                "-fx-background-color: #004d00;" + // Dark green
                        "-fx-text-fill: #90ee90;" + // Light green
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 8px 16px;" +
                        "-fx-border-color: #2e8b57;" + // Sea green
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 5px;" +
                        "-fx-background-radius: 5px;"
        );
        skipTrackButton.setOnAction(e -> skipToNextTrack());

        BorderPane controls = new BorderPane();

        HBox controlsContainer = new HBox(20);
        controlsContainer.getChildren().addAll(skipTrackButton, endGameButton);
        controlsContainer.setMaxHeight(50);
        controlsContainer.setMinHeight(50);
        controlsContainer.setPadding(new Insets(0, 0, 0, 20));
        controls.setPrefHeight(getAppHeight());
        controls.setPrefWidth(getAppWidth());
        controls.setBottom(controlsContainer);
        getGameScene().addUINode(controls);
    }

    @Override
    protected void initInput() {
        FXGL.onKey(upKey, () -> {
            if (snake.getDirection() != Direction.DOWN) snake.updateDirection(Direction.UP);
        });
        FXGL.onKey(downKey, () -> {
            if (snake.getDirection() != Direction.UP) snake.updateDirection(Direction.DOWN);
        });
        FXGL.onKey(leftKey, () -> {
            if (snake.getDirection() != Direction.RIGHT) snake.updateDirection(Direction.LEFT);
        });
        FXGL.onKey(rightKey, () -> {
            if (snake.getDirection() != Direction.LEFT) snake.updateDirection(Direction.RIGHT);
        });
        /*FXGL.onKeyDown(KeyCode.ESCAPE, () -> {
            snake.updateDirection(Direction.NONE);
            FXGL.getGameController().gotoMainMenu();
        });*/
    }

    public static void start_gui(String[] args) {
        Controller controller = new Controller();
        controller.FilesDownloader();
        launch(args);
    }

    public int[] get_grid_size(){
        return new int[] {GRID_SIZE_X, GRID_SIZE_Y};
    }

    public int get_cell_size(){
        return CELL_SIZE;
    }


}
