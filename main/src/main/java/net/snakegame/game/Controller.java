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
            String basepathDir = Files.getUserDir(Files.DirectoryType.DOCUMENTS) + "/dev/Snake";
            String soundspathDir = basepathDir + "/Sounds";

            // Erstelle einen Downloader mit dem Zielverzeichnis
            FilesManager downloader = new FilesManager(soundspathDir);

            // Erwartete Dateien im Sounds-Verzeichnis
            String[] expectedFiles = {
                    soundspathDir + "/8bitGameMusic.wav",
                    soundspathDir + "/GameMusic1.wav",
                    soundspathDir + "/GameMusic2.wav",
                    soundspathDir + "/MenuMusic.wav",
                    soundspathDir + "/eating.wav",
                    soundspathDir + "/KlickSound.wav",
                    soundspathDir + "/SoundGameOver.wav"
            };

            // Überprüfen, ob alle Dateien vorhanden sind mit FilesManager Methode
            if (downloader.checkFilesExist(expectedFiles)) {
                System.out.println("Alle Dateien sind vorhanden, kein Download erforderlich.");
            } else {
                System.out.println("Eine oder mehrere Dateien fehlen, starte den Download...");

                // Lade die Datei herunter
                Path downloadedFile = downloader.downloadFile("https://www.dropbox.com/scl/fo/npfrtm8vqyw878ei8y633/AKrgLugXbWuGID86Ky6kY_4?rlkey=s93dzqnrp1nu7nbf7utmk09i5&st=iken0jhe&dl=1");

                // Entpacke die ZIP-Datei direkt in das Sounds-Verzeichnis
                downloader.extractZip(downloadedFile.toString(), soundspathDir);

                // Nach dem Entpacken noch einmal prüfen, ob alle Dateien jetzt vorhanden sind
                if (downloader.checkFilesExist(expectedFiles)) {
                    System.out.println("Alle Dateien erfolgreich heruntergeladen und entpackt.");
                } else {
                    System.err.println("Es fehlen weiterhin Dateien nach dem Download.");
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