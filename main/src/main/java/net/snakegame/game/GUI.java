package net.snakegame.game;

/**
 * Beinhaltet die Nutzeroberfläche
 * @author Nick Gegenhimer
 * zuletzt Bearbeitet: 11.03.25
 *
 */

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

    /**
     * Enum für die Bewegungsgeschwindigkeit des Spiels
     * Definiert verschiedene Geschwindigkeitsstufen für die Schlange
     */
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

    /**
     * Initialisiert die grundlegenden Einstellungen
     * @author Nick Gegenheimer
     */
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

    /**
     * Custom Scene Factory für das Hauptmenü
     * Erstellt ein spezielles Menü für das Snake-Spiel
     */
    private static class CustomSceneFactory extends SceneFactory {
        @Override
        public FXGLMenu newMainMenu() {
            return new SnakeMainMenu();
        }
    }


    /**
     * Initialisiert die Musik für das Hauptmenü
     * Stoppt vorhandene Musik und startet die Menümusik
     */
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

    /**
     * Methode zum Stoppen der Menümusik
     * Beendet die Wiedergabe und entfernt die Ressourcen
     */
    private void stopMenuMusic() {
        if (menuMusicPlayer != null) {
            menuMusicPlayer.stop();
            menuMusicPlayer.dispose();
            menuMusicPlayer = null;
        }
    }

    /**
     * Initialisiert die Hintergrundmusik für das Spiel
     * Lädt die Musik und fügt Listener für den Fensterfokus hinzu
     */
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

    /**
     * Lädt und spielt einen bestimmten Musiktrack
     * @param trackIndex Index des Tracks in der MUSIC_TRACKS-Liste
     */
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

    /**
     * Wechselt zum nächsten Musiktrack
     * Wird vom Skip-Button aufgerufen
     */
    public void skipToNextTrack() {
        if (backgroundMusicPlayer != null) {
            currentTrackIndex = (currentTrackIndex + 1) % MUSIC_TRACKS.length;
            loadAndPlayTrack(currentTrackIndex);
        }
    }

    /**
     * Stoppt und entfernt die aktuelle Hintergrundmusik
     * Wichtig für das Ressourcenmanagement
     */
    public void stopAndDisposeMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
            backgroundMusicPlayer.dispose();
            backgroundMusicPlayer = null;
        }
    }

    /**
     * Erstellt das Pause-Menü für das Spiel
     * Enthält Buttons zum Fortsetzen, für Optionen und zum Hauptmenü
     */
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

    /**
     * Erstellt das Options-Menü während der Spielpause
     * Enthält Einstellungen für Sound, Musik und Steuerung
     */
    private void createPauseOptions() {
        SnakeMainMenu soundControl = new SnakeMainMenu();
        BorderPane pauseOverlayOptions = new BorderPane();
        pauseOverlayOptions.setPrefWidth(getAppWidth());
        pauseOverlayOptions.setPrefHeight(getAppHeight());

        // Halbdurchsichtiger Hintergrund
        Rectangle background = new Rectangle(getAppWidth(), getAppHeight());
        background.setFill(Color.rgb(0, 30, 0, 0.8));

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

    /**
     * Erstellt die Steuerelemente für das Pausemenü zur Anpassung der Steuerungstasten
     */
    private void createPauseControls() {
        BorderPane pauseOverlayControls = new BorderPane();
        pauseOverlayControls.setPrefWidth(getAppWidth());
        pauseOverlayControls.setPrefHeight(getAppHeight());

        // Halbdurchsichtiger Hintergrund für bessere Lesbarkeit
        Rectangle background = new Rectangle(getAppWidth(), getAppHeight());
        background.setFill(Color.rgb(0, 30, 0, 0.8));

        // Container für alle Steuerelemente
        VBox pauseBoxControls = new VBox(15);
        pauseBoxControls.setAlignment(Pos.CENTER);
        pauseBoxControls.setMaxWidth(400);
        pauseBoxControls.setPadding(new Insets(30));

        // Titel des Controlsmenüs
        Text title = FXGL.getUIFactoryService().newText("Controls", Color.LIGHTGREEN, 48);
        title.setEffect(new DropShadow(10, Color.BLACK));

        // GUI-Instanz abrufen für Zugriff auf die aktuellen Tastenbelegungen
        GUI mainInstance = this;

        // Container für die einzelnen Steuerungsoptionen
        VBox controlsBox = new VBox(15);
        controlsBox.setAlignment(Pos.CENTER);

        // Erstellt die Steuerungsbuttons für alle Richtungen mit aktuellen Tastenbelegungen
        HBox upControl = createControlButton("Up", mainInstance.upKey, "up");
        HBox downControl = createControlButton("Down", mainInstance.downKey, "down");
        HBox leftControl = createControlButton("Left", mainInstance.leftKey, "left");
        HBox rightControl = createControlButton("Right", mainInstance.rightKey, "right");

        // Zurück-Button zum Pausenmenü
        Button btnBack = createPauseButton("Back");
        btnBack.setOnAction(e -> {
            SnakeMainMenu.play_sound(1);  // Sound beim Klick abspielen
            showPauseOptions();  // Zurück zu den Pausenoptionen
        });

        // Fügt alle Steuerungselemente zum Container hinzu
        controlsBox.getChildren().addAll(
                upControl,
                downControl,
                leftControl,
                rightControl
        );

        // Dekorative Trennlinien für bessere visuelle Struktur
        Rectangle separator1 = createMenuSeparator();
        Rectangle separator2 = createMenuSeparator();

        // Alle Elemente zum Hauptcontainer hinzufügen
        pauseBoxControls.getChildren().addAll(
                title,
                separator1,
                controlsBox,
                separator2,
                btnBack
        );

        // Positioniert den Container in der Mitte des Bildschirms
        pauseOverlayControls.setCenter(pauseBoxControls);

        // Speichert die Referenz für späteren Zugriff und Anzeige
        this.pauseMenuOverlayControls = pauseOverlayControls;
    }

    /**
     * Hilfsmethode zum Erstellen der Steuerungsbuttons mit Beschriftung und Änderungsfunktion
     * @param label Bezeichnung der Steuerung (z.B. "Up")
     * @param currentKey Aktuell zugewiesene Taste
     * @param controlType Typ der Steuerung für die Identifizierung (z.B. "up")
     * @return HBox mit Beschriftung und Änderungsbutton
     */
    private HBox createControlButton(String label, KeyCode currentKey, String controlType) {
        HBox control = new HBox(10);
        control.setAlignment(Pos.CENTER);

        // Text mit aktueller Tastenbelegung
        Text keyText = FXGL.getUIFactoryService().newText(label + ": " + currentKey.getName(), Color.WHITE, 18);

        // Button zum Ändern der Tastenbelegung
        Button btnChange = new Button("Change");
        btnChange.setStyle(
                "-fx-background-color: #004d00;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 5px 10px;" +
                        "-fx-border-color: #2e8b57;" +
                        "-fx-border-width: 1px;"
        );
        // Speichert den Steuerungstyp für spätere Zuordnung bei Tastendruck
        btnChange.setUserData(controlType);

        // Event-Handler für den Änderungsbutton
        btnChange.setOnAction(e -> {
            SnakeMainMenu.play_sound(1);  // Klicksound
            setWaitingForKeyInput(btnChange, keyText, label);  // Wartemodus aktivieren
        });

        control.getChildren().addAll(keyText, btnChange);
        return control;
    }

    // Hilfsvariablen für die Verfolgung des aktiven Änderungszustands
    private Node waitingForKey = null;  // Aktiver Button, der auf Tasteneingabe wartet
    private Text waitingText = null;    // Textfeld, das aktualisiert werden soll

    /**
     * Versetzt das Spiel in den Wartezustand für eine neue Tastenzuweisung
     * @param button Der Button, der geklickt wurde
     * @param text Das zugehörige Textfeld
     * @param label Die Beschriftung der Steuerung
     */
    private void setWaitingForKeyInput(Button button, Text text, String label) {
        // Zurücksetzen des vorherigen Wartezustands, falls vorhanden
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

        // Neue Wartereferenzen setzen
        waitingForKey = button;
        waitingText = text;

        // Visuelles Feedback - Button hervorheben
        button.setStyle(
                "-fx-background-color: #008000;" +
                        "-fx-text-fill: yellow;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 5px 10px;" +
                        "-fx-border-color: yellow;" +
                        "-fx-border-width: 2px;"
        );
        // Text ändern zur Benutzeranweisung
        text.setText(label + ": Press any key...");

        // Bestehende Handler entfernen, um Konflikte zu vermeiden
        removeKeyPressHandlers();

        // Neuen Handler hinzufügen für den Tastendruck
        getGameScene().getRoot().addEventHandler(KeyEvent.KEY_PRESSED, this::handleControlsKeyPress);

        // Sicherstellen, dass die Szene den Fokus hat, um Tastatureingaben zu erhalten
        getGameScene().getRoot().requestFocus();
    }

    /**
     * Erstellt eine dekorative Trennlinie für die Menüs
     * @return Rectangle als Trennlinie
     */
    private Rectangle createMenuSeparator() {
        Rectangle separator = new Rectangle(250, 2);
        separator.setFill(Color.LIGHTGREEN);
        separator.setOpacity(0.5);
        return separator;
    }

    /**
     * Erstellt einen stilisierten Button im Snake-Thema für die Pausemenüs
     * @param text Buttonbeschriftung
     * @return Formatierter Button
     */
    private Button createPauseButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(300);

        // Normaler Zustand des Buttons
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

        // Hover-Effekt für den Button
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
        // Event-Handler für Mausinteraktionen
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(normalStyle));

        return button;
    }

    /**
     * Zeigt das Hauptpausemenü an und pausiert das Spiel
     */
    public void showPauseMenu() {
        isPaused = true;
        if (pauseMenuOverlay == null) {
            createPauseMenu();  // Lazy-Loading des Pausemenüs
        }

        // Optionsmenü ausblenden falls angezeigt
        if (pauseMenuOverlayOptions != null) {
            getGameScene().removeUINode(pauseMenuOverlayOptions);
        }

        // Pausiert das Spiel (Schlangenbewegung stoppen)
        snake.pauseGame();

        // Pausemenü zur Szene hinzufügen
        getGameScene().addUINode(pauseMenuOverlay);
    }

    /**
     * Zeigt die Pauseoptionen an (Untermenü des Pausemenüs)
     */
    public void showPauseOptions() {
        if (pauseMenuOverlayOptions == null) {
            createPauseOptions();  // Lazy-Loading der Pauseoptionen
        }

        // Steuerungsmenü ausblenden falls angezeigt
        if (pauseMenuOverlayControls != null) {
            getGameScene().removeUINode(pauseMenuOverlayControls);
        }

        // Pausemenü ausblenden ohne das Spiel fortzusetzen
        if (pauseMenuOverlay != null) {
            getGameScene().removeUINode(pauseMenuOverlay);
        }

        // Optionsmenü anzeigen
        getGameScene().addUINode(pauseMenuOverlayOptions);
    }

    /**
     * Zeigt das Steuerungsmenü innerhalb des Pausezustands an
     */
    public void showPauseControls() {
        if (pauseMenuOverlayControls == null) {
            createPauseControls();  // Lazy-Loading des Steuerungsmenüs
        }

        // Andere Menüs ausblenden
        if (pauseMenuOverlayOptions != null) {
            getGameScene().removeUINode(pauseMenuOverlayOptions);
        }
        if (pauseMenuOverlay != null) {
            getGameScene().removeUINode(pauseMenuOverlay);
        }

        // Steuerungsmenü anzeigen
        getGameScene().addUINode(pauseMenuOverlayControls);
    }

    /**
     * Entfernt alle Event-Handler für Tastaturereignisse, um Konflikte zu vermeiden
     */
    private void removeKeyPressHandlers() {
        // Entfernt bestehende Tastatur-Handler von der Szene
        getGameScene().getRoot().removeEventHandler(KeyEvent.KEY_PRESSED, this::handleControlsKeyPress);

        // Stellt sicher, dass alle anderen Handler ebenfalls entfernt werden
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

    /**
     * Blendet alle Pausemenüs aus und setzt den Pausezustand zurück
     */
    public void hidePauseMenu() {
        isPaused = false;

        // Alle Pausemenüs entfernen
        if (pauseMenuOverlay != null) {
            getGameScene().removeUINode(pauseMenuOverlay);
        }
        if (pauseMenuOverlayOptions != null) {
            getGameScene().removeUINode(pauseMenuOverlayOptions);
        }
        if (pauseMenuOverlayControls != null) {
            getGameScene().removeUINode(pauseMenuOverlayControls);
        }

        // Wartemodus zurücksetzen
        waitingForKey = null;
        waitingText = null;
    }

    /**
     * Aktualisiert die Tastatureingabe-Handler mit den aktuellen Tastenbelegungen
     * Wird nach Änderungen der Steuerung aufgerufen
     */
    private void refreshInputHandlers() {
        // Re-initialisiert die Eingabe-Handler mit den aktuellen Tastenbelegungen
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
        private VBox menuBox;        // Container für das Hauptmenü
        private VBox optionsBox;     // Container für das Optionsmenü
        private Node waitingForKey = null;  // Speichert den Button, der auf eine Tastatureingabe wartet
        private Text waitingText = null;    // Text, der während der Tastenzuweisung angezeigt wird

        public SnakeMainMenu() {
            super(MenuType.MAIN_MENU);  // Ruft den Konstruktor der übergeordneten Klasse auf und setzt den Menütyp
            createSnakeBackground();    // Erstellt den thematischen Hintergrund für das Menü

            // Separate Boxen für Hauptmenü und Optionsmenü
            menuBox = new VBox(15);     // VBox mit 15px Abstand zwischen den Elementen
            menuBox.setAlignment(Pos.CENTER);  // Zentriert die Elemente in der Box

            optionsBox = new VBox(15);  // VBox für das Optionsmenü
            optionsBox.setAlignment(Pos.CENTER);  // Zentriert die Elemente in der Box

            // Bereinigt vorhandene Event-Handler, um Duplikate zu vermeiden
            getContentRoot().removeEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);

            showMainMenu();  // Zeigt das Hauptmenü an
            getContentRoot().addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);  // Fügt den Tastatur-Event-Handler hinzu
        }

        private void cleanupKeyHandlers() {
            // Entfernt Tastatur-Event-Handler, um Duplikate zu vermeiden
            getContentRoot().removeEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);

            // Setzt den Wartezustand zurück
            waitingForKey = null;
            waitingText = null;
        }

        public static void play_sound(int soundId) {
            if (isSoundOn) {  // Prüft, ob Sound aktiviert ist
                switch (soundId) {
                    case 0:  // Essensgeräusch
                        String soundEating = Files.getUserDir(Files.DirectoryType.DOCUMENTS) + "/myGames/Snake/Sounds/eating.wav";
                        Media sound0 = new Media(new File(soundEating).toURI().toString());
                        MediaPlayer mediaPlayer0 = new MediaPlayer(sound0);
                        mediaPlayer0.play();
                        break;

                    case 1:  // Tastendruck-Geräusch
                        String soundButton = Files.getUserDir(Files.DirectoryType.DOCUMENTS) + "/myGames/Snake/Sounds/KlickSound.wav";
                        Media sound1 = new Media(new File(soundButton).toURI().toString());
                        MediaPlayer mediaPlayer1 = new MediaPlayer(sound1);
                        mediaPlayer1.play();
                        mediaPlayer1.setVolume(1.0);
                        break;

                    case 2:  // Game Over Geräusch
                        String soundGameOver = Files.getUserDir(Files.DirectoryType.DOCUMENTS) + "/myGames/Snake/Sounds/SoundGameOver.wav";
                        Media sound2 = new Media(new File(soundGameOver).toURI().toString());
                        MediaPlayer mediaPlayer2 = new MediaPlayer(sound2);
                        mediaPlayer2.play();
                        break;
                }
            }
        }

        private void handleKeyPress(KeyEvent event) {
            if (waitingForKey != null && waitingText != null) {  // Prüft, ob auf eine Tasteneingabe gewartet wird
                KeyCode pressedKey = event.getCode();  // Speichert den Code der gedrückten Taste
                GUI mainInstance = (GUI) FXGL.getApp();  // Holt die Hauptinstanz des Spiels

                // Weist die gedrückte Taste der entsprechenden Steuerung zu
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

                // Setzt den Button-Stil zurück
                waitingForKey.setStyle(createNormalControlStyle());
                waitingForKey = null;
                waitingText = null;

                // Wichtig: Informiert die Hauptklasse über die Änderungen
                mainInstance.refreshInputHandlers();

                event.consume();  // Verhindert, dass das Event weitergegeben wird
            }
        }

        private void showMainMenu() {
            cleanupKeyHandlers();  // Bereinigt alle Tastatur-Event-Handler

            menuBox.getChildren().clear();  // Löscht alle vorhandenen Elemente aus der menüBox

            // Entfernt optionsBox falls vorhanden
            getContentRoot().getChildren().remove(optionsBox);

            // Startet Menümusik, wenn das Hauptmenü angezeigt wird
            GUI mainInstance = (GUI) FXGL.getApp();
            mainInstance.initMenuMusic();

            // Erstellt den Titel des Spiels
            Text title = FXGL.getUIFactoryService().newText("SNAKE", Color.LIGHTGREEN, 72);
            title.setEffect(new DropShadow(10, Color.BLACK));  // Fügt einen Schatteneffekt hinzu

            // Erstellt den Start-Spiel-Button
            Button btnPlay = createSnakeButton("Start Game");
            btnPlay.setOnAction(e -> {
                play_sound(1);  // Spielt das Klick-Geräusch ab
                GUI mainInstance2 = (GUI) FXGL.getApp();
                mainInstance2.stopMenuMusic();  // Stoppt die Menümusik vor dem Spielstart
                fireNewGame();  // Startet ein neues Spiel
            });

            // Erstellt den Options-Button
            Button btnOptions = createSnakeButton("Options");
            btnOptions.setOnAction(e -> {
                play_sound(1);  // Spielt das Klick-Geräusch ab
                showOptionsMenu();  // Zeigt das Optionsmenü an
            });

            // Erstellt den Spiel-Beenden-Button
            Button btnEndGame = createSnakeButton("End Game");
            btnEndGame.setOnAction(e -> {
                play_sound(1);  // Spielt das Klick-Geräusch ab
                FXGL.getGameController().exit();  // Beendet das Spiel
            });

            // Fügt alle Elemente zur menuBox hinzu
            menuBox.getChildren().addAll(
                    title,
                    createSeparator(),  // Fügt einen Trennstrich ein
                    btnPlay,
                    btnOptions,
                    btnEndGame
            );

            // Positioniert die menuBox in der Mitte des Bildschirms
            menuBox.setTranslateX(getAppWidth() / 2.0 - 150);
            menuBox.setTranslateY(getAppHeight() / 2.0 - 200);

            // Fügt die menuBox zum Hauptcontainer hinzu, falls sie noch nicht vorhanden ist
            if (!getContentRoot().getChildren().contains(menuBox)) {
                getContentRoot().getChildren().add(menuBox);
            }

            // Fügt den Tastatur-Event-Handler hinzu
            getContentRoot().addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);
        }

        private void musicControl(Button btnMusic) {
            isMusicOn = !isMusicOn;  // Wechselt den Musikstatus (an/aus)
            btnMusic.setText("Music: " + (isMusicOn ? "ON" : "OFF"));  // Aktualisiert den Buttontext

            GUI mainInstance = (GUI) FXGL.getApp();
            if (isMusicOn) {  // Wenn Musik eingeschaltet wird
                if (menuMusicPlayer != null) {
                    menuMusicPlayer.play();  // Startet die Menümusik
                }
                if (backgroundMusicPlayer != null) {
                    backgroundMusicPlayer.play();  // Startet die Hintergrundmusik
                }
            } else {  // Wenn Musik ausgeschaltet wird
                if (menuMusicPlayer != null) {
                    menuMusicPlayer.pause();  // Pausiert die Menümusik
                }
                if (backgroundMusicPlayer != null) {
                    backgroundMusicPlayer.pause();  // Pausiert die Hintergrundmusik
                }
            }
        }

        private void soundControl(Button btnSound) {
            isSoundOn = !isSoundOn;  // Wechselt den Soundstatus (an/aus)
            btnSound.setText("Sound: " + (isSoundOn ? "ON" : "OFF"));  // Aktualisiert den Buttontext
        }

        public void showOptionsMenu() {
            // Entfernt das Hauptmenü
            getContentRoot().getChildren().remove(menuBox);

            optionsBox.getChildren().clear();  // Löscht alle vorhandenen Elemente aus der optionsBox

            // Erstellt Titel und Überschriften für das Optionsmenü
            Text optionsTitle = FXGL.getUIFactoryService().newText("Options", Color.LIGHTGREEN, 32);
            Text gameSizeTitel = FXGL.getUIFactoryService().newText("Size", Color.LIGHTGREEN, 26);
            Text speedGameTitel = FXGL.getUIFactoryService().newText("Speed", Color.LIGHTGREEN, 26);

            // Erstellt den Sound-Button
            Button btnSound = createSnakeButton("Sound: " + (isSoundOn ? "ON" : "OFF"));
            btnSound.setOnAction(e -> {
                play_sound(1);  // Spielt das Klick-Geräusch ab
                soundControl(btnSound);  // Wechselt den Soundstatus und aktualisiert den Button
            });

            // Erstellt den Musik-Button
            Button btnMusic = createSnakeButton("Music: " + (isMusicOn ? "ON" : "OFF"));
            btnMusic.setOnAction(e -> {
                play_sound(1);  // Spielt das Klick-Geräusch ab
                musicControl(btnMusic);  // Wechselt den Musikstatus und aktualisiert den Button
            });

            // Erstellt den Größen-Auswahlbereich
            ComboBox<String> sizeSelector = new ComboBox<>();
            sizeSelector.getItems().addAll("Small", "Medium", "Large");  // Fügt die Größenoptionen hinzu
            sizeSelector.setValue(selectedSize);  // Setzt die aktuelle Größe
            sizeSelector.setStyle(
                    "-fx-background-color: #004d00;" +
                            "-fx-color: white;" +
                            "-fx-font-size: 14px;"
            );
            sizeSelector.setOnAction(e -> {
                play_sound(1);  // Spielt das Klick-Geräusch ab
                selectedSize = sizeSelector.getValue();  // Speichert die ausgewählte Größe
            });

            // Erstellt den Geschwindigkeits-Auswahlbereich
            ComboBox<String> speedSelector = new ComboBox<>();
            speedSelector.getItems().addAll("Fast", "Medium", "Slow");  // Fügt die Geschwindigkeitsoptionen hinzu
            speedSelector.setValue(slectedSpeed);  // Setzt die aktuelle Geschwindigkeit
            speedSelector.setStyle(
                    "-fx-background-color: #004d00;" +
                            "-fx-color: white;" +
                            "-fx-font-size: 14px;"
            );
            speedSelector.setOnAction(e -> {
                play_sound(1);  // Spielt das Klick-Geräusch ab
                slectedSpeed = speedSelector.getValue();  // Speichert die ausgewählte Geschwindigkeit
                // Setzt die entsprechende Geschwindigkeit
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

            // Erstellt den Steuerungs-Button
            Button btnControls = createSnakeButton("Controls");
            btnControls.setOnAction(e -> {
                play_sound(1);  // Spielt das Klick-Geräusch ab
                showControlsMenu();  // Zeigt das Steuerungsmenü an
            });

            // Erstellt den Zurück-Button
            Button btnBack = createSnakeButton("Back");
            btnBack.setOnAction(e -> {
                play_sound(1);  // Spielt das Klick-Geräusch ab
                showMainMenu();  // Zeigt das Hauptmenü an
            });

            // Fügt alle Elemente zur optionsBox hinzu
            optionsBox.getChildren().addAll(
                    optionsTitle,
                    createSeparator(),  // Fügt einen Trennstrich ein
                    btnSound,
                    btnMusic,
                    btnControls,
                    createSeparator(),  // Fügt einen Trennstrich ein
                    gameSizeTitel,
                    sizeSelector,
                    speedGameTitel,
                    speedSelector,
                    createSeparator(),  // Fügt einen Trennstrich ein
                    btnBack
            );

            // Positioniert die optionsBox höher als das Hauptmenü
            optionsBox.setTranslateX(getAppWidth() / 2.0 - 150);
            optionsBox.setTranslateY(getAppHeight() / 2.0 - 300);

            // Fügt die optionsBox zum Hauptcontainer hinzu, falls sie noch nicht vorhanden ist
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
