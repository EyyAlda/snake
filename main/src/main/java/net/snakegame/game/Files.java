package net.snakegame.game;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Files {

    /**
     * Liste mit User Directories in Linux, die mit dem Befehl `xdg-user-dir` gefunden werden können
     */
    public static enum DirectoryType {
        DOCUMENTS,
        DOWNLOAD,
        VIDEOS,
        MUSIC,
        DESKTOP,
        PICTURES
    }

    /**Funktion zum dynamischen Laden von Dateipfaden unter Linux-Systemen
     * Es wird der Bash-Befehl xdg-user-dir mit dem Zusatz in der dirType variable ausgeführt.
     * Zurückgegeben wird der volle Pfad zum Ordner
     * Bsp: xdg-user-dir DOCUMENTS -> /home/<username>/Dokumente
     * @author Lennard Rütten
     * @param dirType
     * @return String
     */
    public static String getXdgUserDir(DirectoryType dirType) {
        String command = "xdg-user-dir " + dirType.toString();
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String path = reader.readLine();

            // Wait for the process to complete
            int exitCode = process.waitFor();
            if (exitCode == 0 && path != null && !path.isEmpty()) {
                return path;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

}
