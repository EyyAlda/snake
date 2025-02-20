package net.snakegame.game;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Controller {
PlayingArea area = null;
Snake snake = null;

    public void create_playing_area(PlayingArea.Size size){
        area = new PlayingArea(size, this);
    }

    public void create_snake(){
        int[] size = area.get_playing_area_size();
        int starting_y = size[1] / 2;
        int starting_x = (int) Math.round(size[0] * 0.3);
        snake = new Snake(starting_x, starting_y);
    }

    public void FilesDownloader() {
        try {
            // Basisverzeichnis definieren wie in der GUI
            String baseDir = Files.getUserDir(Files.DirectoryType.DOCUMENTS) + "/dev/Snake";
            String soundsDir = baseDir + "/Sounds";

            // Erstelle einen Downloader mit dem Zielverzeichnis
            FilesManager downloader = new FilesManager(soundsDir);

            // Lade die Datei herunter
            Path downloadedFile = downloader.downloadFile("https://www.dropbox.com/scl/fo/npfrtm8vqyw878ei8y633/AKrgLugXbWuGID86Ky6kY_4?rlkey=s93dzqnrp1nu7nbf7utmk09i5&st=iken0jhe&dl=1");

            // Entpacke die ZIP-Datei direkt in das Sounds-Verzeichnis
            downloader.extractZip(downloadedFile.toString(), soundsDir);

            // Überprüfe, ob alle erwarteten Sound-Dateien vorhanden sind
            String[] expectedFiles = {
                    soundsDir + "/8bitGameMusic.wav",
                    soundsDir + "/GameMusic1.wav",
                    soundsDir + "/GameMusic2.wav",
                    soundsDir + "/MenuMusic.wav",
                    soundsDir + "/eating.wav",
                    soundsDir + "/KlickSound.wav",
                    soundsDir + "/SoundGameOver.wav"
            };

            // Optional: Überprüfung der extrahierten Dateien
            for (String file : expectedFiles) {
                if (!new File(file).exists()) {
                    System.err.println("Warnung: Datei nicht gefunden: " + file);
                }
            }

        } catch (IOException e) {
            System.err.println("Fehler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean check_for_snake_position(int x, int y){
        return snake.is_snake_at_position(x, y);
    }

    public void move_snake(){
        snake.move_snake();
    }
}