package net.snakegame.game;

import java.io.IOException;
import java.nio.file.Path;

public class Controller {
    public boolean gameOver = false;
    GUI gui;
    TestGUI testGUI;

    // Basisverzeichnis definieren wie in der GUI
    String basepathDir = Files.getUserDir(Files.DirectoryType.DOCUMENTS) + "/myGames/Snake";
    String soundspathDir = basepathDir + "/Sounds";
    FilesManager downloader;

    // Erstelle einen Downloader mit dem Zielverzeichnis
    String[] expectedFiles = {
        soundspathDir + "/8bitGameMusic.wav",
        soundspathDir + "/GameMusic1.wav",
        soundspathDir + "/GameMusic2.wav",
        soundspathDir + "/MenuMusic.wav",
        soundspathDir + "/eating.wav",
        soundspathDir + "/KlickSound.wav",
        soundspathDir + "/SoundGameOver.wav"
    };

    public Controller(){
        try {
            downloader = new FilesManager(soundspathDir);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Controller(TestGUI gui){
        this.testGUI = gui;        
    }

    public void startGame(String[] args){
        FilesDownloader();
        gui = new GUI(this);
        gui.start_gui(args);
    }
    
    public boolean checkFiles(){
        return downloader.checkFilesExist(expectedFiles);
    }

    public void FilesDownloader() {
        try {

            // Erwartete Dateien im Sounds-Verzeichnis

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
}
