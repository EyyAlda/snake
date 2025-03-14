package net.snakegame.game;

import static com.almasb.fxgl.dsl.FXGL.addText;
import static com.almasb.fxgl.dsl.FXGL.addVarText;
import static com.almasb.fxgl.dsl.FXGL.getAppHeight;
import static com.almasb.fxgl.dsl.FXGL.getAppWidth;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import static com.almasb.fxgl.dsl.FXGL.getGameTimer;

import java.io.File;
import java.util.EnumSet;
import java.util.Map;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.MenuItem;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
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

    private static enum MovementSpeed {
        DEFAULT(0.2),
        SLOW(0.3),
        QUICK(0.1);

        double speed;

        MovementSpeed(double speed) {
            this.speed = speed;
        }

        public double getSpeed() {
            return speed;
        }
    }

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
    private static MovementSpeed speed = MovementSpeed.DEFAULT;
    public boolean isPaused = false;
    private BorderPane pauseMenuOverlay;
    private BorderPane pauseMenuOverlayOptions;
    private BorderPane pauseMenuOverlayControls;


    public GUI() {
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setGameMenuEnabled(false);
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



    public void initMenuMusic() {
        try {
            // Stoppe zuerst die Hintergrundmusik, wenn sie läuft
            stopAndDisposeMusic();

            if (menuMusicPlayer != null) {
                menuMusicPlayer.stop();
                menuMusicPlayer.dispose();
                menuMusicPlayer = null;
            }

            Media menuMusic = new Media(new File(MENU_MUSIC_TRACK).toURI().toString());
            menuMusicPlayer = new MediaPlayer(menuMusic);
            menuMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);

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


    public void initBackgroundMusic() {
        try {
            // Stelle sicher, dass vorherige Musik gestoppt wird
            stopAndDisposeMusic();

            // Lade den ersten Track
            loadAndPlayTrack(currentTrackIndex);

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
        } catch (Exception e) {
            System.out.println("Error loading background music: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAndPlayTrack(int trackIndex) {
        try {
            // Stoppe den aktuellen Player, falls vorhanden
            if (backgroundMusicPlayer != null) {
                backgroundMusicPlayer.stop();
                backgroundMusicPlayer.dispose();
                backgroundMusicPlayer = null;  // Wichtig: Referenz auf null setzen
            }

            // Lade und starte den neuen Track nur, wenn Musik eingeschaltet ist
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
            e.printStackTrace();
        }
    }

    public void skipToNextTrack() {
        if (backgroundMusicPlayer != null) {
            currentTrackIndex = (currentTrackIndex + 1) % MUSIC_TRACKS.length;
            loadAndPlayTrack(currentTrackIndex);
        }
    }

    public void stopAndDisposeMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
            backgroundMusicPlayer.dispose();
            backgroundMusicPlayer = null;
        }
    }

    public void createPauseMenu() {
        BorderPane pauseOverlay = new BorderPane();
        pauseOverlay.setPrefWidth(getAppWidth());
        pauseOverlay.setPrefHeight(getAppHeight());
        pauseOverlay.setBackground(new Background(new BackgroundFill(Color.rgb(0, 30, 0, 0.5), new CornerRadii(0.0), new Insets(0))));

        VBox pauseBox = new VBox(15);
        pauseBox.setAlignment(Pos.CENTER);
        pauseBox.setMaxWidth(400);
        pauseBox.setPadding(new Insets(30));
        

        Text title = FXGL.getUIFactoryService().newText("PAUSE", Color.LIGHTGREEN, 48);
        title.setEffect(new DropShadow(10, Color.BLACK));

        Button btnResume = createPauseButton("Resume Game");
        btnResume.setOnAction(e -> {
            SnakeMainMenu.play_sound(1);
            hidePauseMenu();
            snake.resumeGame();
        });

        Button btnOptions = createPauseButton("Options");
        btnOptions.setOnAction(e -> {
            SnakeMainMenu.play_sound(1);
            showPauseOptions();
        });

        Button btnMainMenu = createPauseButton("Main Menu");
        btnMainMenu.setOnAction(e -> {
            SnakeMainMenu.play_sound(1);
            hidePauseMenu();
            stopAndDisposeMusic();
            initMenuMusic();
            FXGL.getGameController().gotoMainMenu();
        });

        Rectangle separator1 = createMenuSeparator();
        Rectangle separator2 = createMenuSeparator();

        pauseBox.getChildren().addAll(
                title,
                separator1,
                btnResume,
                btnOptions,
                separator2,
                btnMainMenu
        );

        pauseOverlay.setCenter(pauseBox);

        // Speichere die Referenz für späteren Zugriff
        this.pauseMenuOverlay = pauseOverlay;
    }

    private void createPauseOptions() {
        SnakeMainMenu soundControl = new SnakeMainMenu();
        BorderPane pauseOverlayOptions = new BorderPane();
        pauseOverlayOptions.setPrefWidth(getAppWidth());
        pauseOverlayOptions.setPrefHeight(getAppHeight());
        pauseOverlayOptions.setBackground(new Background(new BackgroundFill(Color.rgb(0, 30, 0, 0.5), new CornerRadii(0.0), new Insets(0))));


        VBox pauseBoxoptions = new VBox(15);
        pauseBoxoptions.setAlignment(Pos.CENTER);
        pauseBoxoptions.setMaxWidth(400);
        pauseBoxoptions.setPadding(new Insets(30));

        Text title = FXGL.getUIFactoryService().newText("Options", Color.LIGHTGREEN, 48);
        title.setEffect(new DropShadow(10, Color.BLACK));

        Button btnSound = createPauseButton("Sound: " + (isSoundOn ? "ON" : "OFF"));
        btnSound.setOnAction(e -> {
            SnakeMainMenu.play_sound(1);
            soundControl.soundControl(btnSound);
        });

        Button btnMusic = createPauseButton("Music: " + (isMusicOn ? "ON" : "OFF"));
        btnMusic.setOnAction(e -> {
            SnakeMainMenu.play_sound(1);
            soundControl.musicControl(btnMusic);
        });

        Button btnControls = createPauseButton("Controls");
        btnControls.setOnAction(e -> {
            SnakeMainMenu.play_sound(1);
            showPauseControls();
        });

        Button btnBack = createPauseButton("Back");
        btnBack.setOnAction(e -> {
            SnakeMainMenu.play_sound(1);
            showPauseMenu();
        });

        Rectangle separator1 = createMenuSeparator();
        Rectangle separator2 = createMenuSeparator();
        Rectangle separator3 = createMenuSeparator();


        pauseBoxoptions.getChildren().addAll(
                title,
                separator1,
                btnMusic,
                btnSound,
                separator2,
                btnControls,
                separator3,
                btnBack
        );

        pauseOverlayOptions.setCenter(pauseBoxoptions);

        // Speichere die Referenz für späteren Zugriff
        this.pauseMenuOverlayOptions = pauseOverlayOptions;
    }

    private void createPauseControls() {
        BorderPane pauseOverlayControls = new BorderPane();
        pauseOverlayControls.setPrefWidth(getAppWidth());
        pauseOverlayControls.setPrefHeight(getAppHeight());
        pauseOverlayControls.setBackground(new Background(new BackgroundFill(Color.rgb(0, 30, 0, 0.5), new CornerRadii(0.0), new Insets(0))));


        VBox pauseBoxControls = new VBox(15);
        pauseBoxControls.setAlignment(Pos.CENTER);
        pauseBoxControls.setMaxWidth(400);
        pauseBoxControls.setPadding(new Insets(30));

        Text title = FXGL.getUIFactoryService().newText("Controls", Color.LIGHTGREEN, 48);
        title.setEffect(new DropShadow(10, Color.BLACK));

        // GUI-Instanz abrufen
        GUI mainInstance = this;

        VBox controlsBox = new VBox(15);
        controlsBox.setAlignment(Pos.CENTER);

        // Controls erstellen
        HBox upControl = createControlButton("Up", mainInstance.upKey, "up");
        HBox downControl = createControlButton("Down", mainInstance.downKey, "down");
        HBox leftControl = createControlButton("Left", mainInstance.leftKey, "left");
        HBox rightControl = createControlButton("Right", mainInstance.rightKey, "right");

        Button btnBack = createPauseButton("Back");
        btnBack.setOnAction(e -> {
            SnakeMainMenu.play_sound(1);
            showPauseOptions();
        });

        controlsBox.getChildren().addAll(
                upControl,
                downControl,
                leftControl,
                rightControl
        );

        Rectangle separator1 = createMenuSeparator();
        Rectangle separator2 = createMenuSeparator();

        pauseBoxControls.getChildren().addAll(
                title,
                separator1,
                controlsBox,
                separator2,
                btnBack
        );

        pauseOverlayControls.setCenter(pauseBoxControls);

        // Speichere die Referenz für späteren Zugriff
        this.pauseMenuOverlayControls = pauseOverlayControls;
    }

    // Hilfsmethode zum Erstellen der Control-Buttons
    private HBox createControlButton(String label, KeyCode currentKey, String controlType) {
        HBox control = new HBox(10);
        control.setAlignment(Pos.CENTER);

        Text keyText = FXGL.getUIFactoryService().newText(label + ": " + currentKey.getName(), Color.WHITE, 18);
        Button btnChange = new Button("Change");
        btnChange.setStyle(
                "-fx-background-color: #004d00;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 5px 10px;" +
                        "-fx-border-color: #2e8b57;" +
                        "-fx-border-width: 1px;"
        );
        btnChange.setUserData(controlType);

        btnChange.setOnAction(e -> {
            SnakeMainMenu.play_sound(1);
            setWaitingForKeyInput(btnChange, keyText, label);
        });

        control.getChildren().addAll(keyText, btnChange);
        return control;
    }

    // Hilfsvariablen für das Warten auf Tastendruck
    private Node waitingForKey = null;
    private Text waitingText = null;

    // Methode zum Setzen des Wartezustands
    private void setWaitingForKeyInput(Button button, Text text, String label) {
        // Zurücksetzen des vorherigen Wartezustands
        if (waitingForKey != null) {
            waitingForKey.setStyle(
                    "-fx-background-color: #004d00;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 14px;" +
                            "-fx-padding: 5px 10px;" +
                            "-fx-border-color: #2e8b57;" +
                            "-fx-border-width: 1px;"
            );
        }

        waitingForKey = button;
        waitingText = text;
        button.setStyle(
                "-fx-background-color: #008000;" +
                        "-fx-text-fill: yellow;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 5px 10px;" +
                        "-fx-border-color: yellow;" +
                        "-fx-border-width: 2px;"
        );
        text.setText(label + ": Press any key...");

        // Remove any existing handlers first
        removeKeyPressHandlers();

        // Then add a fresh handler
        getGameScene().getRoot().addEventHandler(KeyEvent.KEY_PRESSED, this::handleControlsKeyPress);

        // Stelle sicher, dass die Szene den Fokus hat, um Tastatureingaben zu erhalten
        getGameScene().getRoot().requestFocus();
    }

    // Hilfsmethode für die Separatoren
    private Rectangle createMenuSeparator() {
        Rectangle separator = new Rectangle(250, 2);
        separator.setFill(Color.LIGHTGREEN);
        separator.setOpacity(0.5);
        return separator;
    }

    // Hilfsmethode für die Erstellung der Buttons mit Snake-Thema
    private Button createPauseButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(300);

        String normalStyle = "-fx-background-color: #004d00;" +
                "-fx-text-fill: #90ee90;" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 15px;" +
                "-fx-border-color: #2e8b57;" +
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 10px;" +
                "-fx-background-radius: 10px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 5, 0.0, 0, 1);";

        String hoverStyle = "-fx-background-color: #008000;" +
                "-fx-text-fill: #e0ffff;" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 15px;" +
                "-fx-border-color: #3cb371;" +
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 10px;" +
                "-fx-background-radius: 10px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0.0, 0, 1);";

        button.setStyle(normalStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(normalStyle));

        return button;
    }


    public void showPauseMenu() {
        isPaused = true;
        if (pauseMenuOverlay == null) {
            createPauseMenu();
        }

        // Optionsmenü ausblenden
        if (pauseMenuOverlayOptions != null) {
            getGameScene().removeUINode(pauseMenuOverlayOptions);
        }

        // Pausiere das Spiel
        snake.pauseGame();

        // Füge das Pausemenü zur Szene hinzu
        getGameScene().addUINode(pauseMenuOverlay);
    }

    // Update the showPauseOptions() method:
    public void showPauseOptions() {
        if (pauseMenuOverlayOptions == null) {
            createPauseOptions();
        }

        // Remove controls menu if visible
        if (pauseMenuOverlayControls != null) {
            getGameScene().removeUINode(pauseMenuOverlayControls);
        }

        // Hide pause menu without resuming the game
        if (pauseMenuOverlay != null) {
            getGameScene().removeUINode(pauseMenuOverlay);
        }

        // Show options menu
        getGameScene().addUINode(pauseMenuOverlayOptions);
    }

    public void showPauseControls() {
        if (pauseMenuOverlayControls == null) {
            createPauseControls();
        }

        // Optionsmenü ausblenden
        if (pauseMenuOverlayOptions != null) {
            getGameScene().removeUINode(pauseMenuOverlayOptions);
        }

        // Pausemenü ausblenden
        if (pauseMenuOverlay != null) {
            getGameScene().removeUINode(pauseMenuOverlay);
        }

        // Controlsmenü anzeigen
        getGameScene().addUINode(pauseMenuOverlayControls);
    }

    private void removeKeyPressHandlers() {
        // Remove any existing key handlers from the scene
        getGameScene().getRoot().removeEventHandler(KeyEvent.KEY_PRESSED, this::handleControlsKeyPress);

        // Also ensure any other handlers are properly removed
        if (pauseMenuOverlayControls != null) {
            Node node = pauseMenuOverlayControls.getCenter();
            if (node instanceof VBox) {
                VBox pauseBox = (VBox) node;
                for (Node child : pauseBox.getChildren()) {
                    if (child instanceof HBox) {
                        HBox hbox = (HBox) child;
                        for (Node item : hbox.getChildren()) {
                            if (item instanceof Button) {
                                Button button = (Button) item;
                                button.setOnAction(null);
                            }
                        }
                    }
                }
            }
        }
    }

    // In der hidePauseMenu()-Methode
    public void hidePauseMenu() {
        isPaused = false;

        if (pauseMenuOverlay != null) {
            getGameScene().removeUINode(pauseMenuOverlay);
        }
        if (pauseMenuOverlayOptions != null) {
            getGameScene().removeUINode(pauseMenuOverlayOptions);
        }
        if (pauseMenuOverlayControls != null) {
            getGameScene().removeUINode(pauseMenuOverlayControls);
        }

        // Reset waiting state
        waitingForKey = null;
        waitingText = null;

    }

    // Add this method to the GUI class
    private void refreshInputHandlers() {

        // Re-initialize the input handlers with the current key bindings
        FXGL.onKey(upKey, () -> {
            if (!isPaused && snake.getDirection() != Direction.DOWN) snake.updateDirection(Direction.UP);
        });
        FXGL.onKey(downKey, () -> {
            if (!isPaused && snake.getDirection() != Direction.UP) snake.updateDirection(Direction.DOWN);
        });
        FXGL.onKey(leftKey, () -> {
            if (!isPaused && snake.getDirection() != Direction.RIGHT) snake.updateDirection(Direction.LEFT);
        });
        FXGL.onKey(rightKey, () -> {
            if (!isPaused && snake.getDirection() != Direction.LEFT) snake.updateDirection(Direction.RIGHT);
        });
        FXGL.onKeyDown(KeyCode.ESCAPE, () -> {
            if (!isPaused) {
                showPauseMenu();
            } else {
                hidePauseMenu();
                snake.resumeGame();
            }
        });
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

            // Clean up any existing handlers
            getContentRoot().removeEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);

            showMainMenu();
            getContentRoot().addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);
        }

        private void cleanupKeyHandlers() {
            // Remove key event handlers to prevent duplicates
            getContentRoot().removeEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);

            // Reset waiting state
            waitingForKey = null;
            waitingText = null;
        }

        public static void play_sound(int soundId) {
            if (isSoundOn) {
                switch (soundId) {
                    case 0:
                        String soundEating = Files.getUserDir(Files.DirectoryType.DOCUMENTS) + "/myGames/Snake/Sounds/eating.wav";
                        Media sound0 = new Media(new File(soundEating).toURI().toString());
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

                // Wichtig: Informiere die Hauptklasse über die Änderungen
                mainInstance.refreshInputHandlers();

                event.consume();
            }
        }

        private void showMainMenu() {
            cleanupKeyHandlers();

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
                mainInstance.stopMenuMusic(); // Stoppe Menümusik vor Spielstart
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

            getContentRoot().addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);

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

        public void showOptionsMenu() {
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
                switch(speedSelector.getValue()) {
                    case "Fast":
                        speed = MovementSpeed.QUICK;
                        break;
                    case "Medium":
                        speed = MovementSpeed.DEFAULT;
                        break;
                    case "Slow":
                        speed = MovementSpeed.SLOW;
                        break;
                }
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
                // Clean up any previous handlers first
                cleanupKeyHandlers();

                if (waitingForKey != null) {
                    waitingForKey.setStyle(createNormalControlStyle());
                }
                waitingForKey = btnChange;
                waitingText = keyText;
                btnChange.setStyle(createWaitingControlStyle());
                keyText.setText(label + ": Press any key...");

                // Re-add the handler
                getContentRoot().addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);
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
                        Color.rgb(50 + (int) (Math.random() * 50), 100 + (int) (Math.random() * 100), 50, 0.7));
                snakeScale.setTranslateX(Math.random() * getAppWidth());
                snakeScale.setTranslateY(Math.random() * getAppHeight());
                getContentRoot().getChildren().add(snakeScale);

                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.seconds(2 + Math.random() * 3), e -> {
                            snakeScale.setTranslateX(Math.random() * getAppWidth());
                            snakeScale.setTranslateY(Math.random() * getAppHeight());
                            snakeScale.setRadius(3 + Math.random() * 5);
                            snakeScale.setFill(Color.rgb(
                                    50 + (int) (Math.random() * 50),
                                    100 + (int) (Math.random() * 100),
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

    public void playSound(int sound) {
        SnakeMainMenu.play_sound(sound);
    }

    @Override
    protected void initGame() {
        initBackgroundMusic();
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



        snake = new Game((int) (gridWidth/3) * CELL_SIZE, (int) (gridHeight/2) * CELL_SIZE, CELL_SIZE, GRID_SIZE_Y, GRID_SIZE_X, this);

        getGameTimer().runAtInterval(snake::move, Duration.seconds(speed.getSpeed()));

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
        getGameScene().getRoot().addEventHandler(KeyEvent.KEY_PRESSED, this::handleControlsKeyPress);

    }

    private void handleControlsKeyPress(KeyEvent event) {
        if (waitingForKey != null && waitingText != null && isPaused) {
            KeyCode pressedKey = event.getCode();

            if (waitingForKey.getUserData().equals("up")) {
                upKey = pressedKey;
                waitingText.setText("Up: " + pressedKey.getName());
            } else if (waitingForKey.getUserData().equals("down")) {
                downKey = pressedKey;
                waitingText.setText("Down: " + pressedKey.getName());
            } else if (waitingForKey.getUserData().equals("left")) {
                leftKey = pressedKey;
                waitingText.setText("Left: " + pressedKey.getName());
            } else if (waitingForKey.getUserData().equals("right")) {
                rightKey = pressedKey;
                waitingText.setText("Right: " + pressedKey.getName());
            }

            waitingForKey.setStyle(
                    "-fx-background-color: #004d00;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 14px;" +
                            "-fx-padding: 5px 10px;" +
                            "-fx-border-color: #2e8b57;" +
                            "-fx-border-width: 1px;"
            );
            waitingForKey = null;
            waitingText = null;

            // Refresh the input handlers with the new key bindings
            refreshInputHandlers();

            event.consume();
        }
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
        FXGL.onKeyDown(KeyCode.ESCAPE, () -> {
            if (!isPaused) {
                showPauseMenu();
            }
        });
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

    public double getMovementSpeed() {
        return speed.getSpeed();
    }

}
