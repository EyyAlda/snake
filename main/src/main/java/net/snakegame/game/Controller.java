package net.snakegame.game;

import java.io.IOException;
import java.nio.file.Path;

public class Controller {
    public boolean gameOver = false; // Zustandsvariable, um das Ende des Spiels anzuzeigen
    GUI gui;
    TestGUI testGUI;

    // Basisverzeichnis für das Spiel definieren, das mit dem Dokumentenverzeichnis des Benutzers kombiniert wird
    String basepathDir = Files.getUserDir(Files.DirectoryType.DOCUMENTS) + "/myGames/Snake";
    String soundspathDir = basepathDir + "/Sounds"; // Verzeichnis für die Sounds des Spiels
    FilesManager downloader; // Verwalter zum Herunterladen und Überprüfen von Dateien

    // Array, das die Pfade der zu erwartenden Sounddateien speichert
    String[] expectedFiles = {
            soundspathDir + "/8bitGameMusic.wav",
            soundspathDir + "/GameMusic1.wav",
            soundspathDir + "/GameMusic2.wav",
            soundspathDir + "/MenuMusic.wav",
            soundspathDir + "/eating.wav",
            soundspathDir + "/KlickSound.wav",
            soundspathDir + "/SoundGameOver.wav"
    };

    // Standardkonstruktor der Klasse Controller
    public Controller(){
        try {
            // Erstellen eines FilesManager-Objekts zum Verwalten von Sounddateien
            downloader = new FilesManager(soundspathDir);
        } catch (IOException e) {
            e.printStackTrace(); // Fehlerprotokollierung im Falle einer Ausnahme
        }
    }

    // Überladener Konstruktor mit TestGUI als Parameter
    public Controller(TestGUI gui){
        this.testGUI = gui;
    }

    // Methode zum Überprüfen, ob alle erwarteten Sounddateien vorhanden sind
    public boolean checkFiles(){
        return downloader.checkFilesExist(expectedFiles);
    }

    // Methode zum Herunterladen und Überprüfen von Sounddateien
    public void FilesDownloader() {
        try {
          
            // Überprüfen, ob alle Dateien im Sound-Verzeichnis vorhanden sind
            if (downloader.checkFilesExist(expectedFiles)) {
                System.out.println("Alle Dateien sind vorhanden, kein Download erforderlich.");
            } else {
                System.out.println("Eine oder mehrere Dateien fehlen, starte den Download...");

                // Datei von der angegebenen URL herunterladen
                Path downloadedFile = downloader.downloadFile("https://www.dropbox.com/scl/fo/npfrtm8vqyw878ei8y633/AKrgLugXbWuGID86Ky6kY_4?rlkey=s93dzqnrp1nu7nbf7utmk09i5&st=iken0jhe&dl=1");

                // ZIP-Datei wird nach dem Herunterladen ins Sound-Verzeichnis entpackt
                downloader.extractZip(downloadedFile.toString(), soundspathDir);

                // Überprüfen, ob nach dem Entpacken alle Dateien vorhanden sind
                if (downloader.checkFilesExist(expectedFiles)) {
                    System.out.println("Alle Dateien erfolgreich heruntergeladen und entpackt.");
                } else {
                    System.err.println("Es fehlen weiterhin Dateien nach dem Download.");
                }
            }

        } catch (IOException e) {
            System.err.println("Fehler: " + e.getMessage()); // Fehlerbehandlung im Falle eines IOExceptions
            e.printStackTrace(); // Detaillierte Protokollierung der Fehlerursache
        }
    }
}
