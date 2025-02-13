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
        PICTURES;

         // Get the Windows known folder ID
         private String getWindowsFolderId() {
            switch (this) {
                case DOCUMENTS: return "{F42EE2D3-909F-4907-8871-4C22FC0BF756}";
                case DOWNLOAD: return "{374DE290-123F-4565-9164-39C4925E467B}";
                case VIDEOS: return "{18989B1D-99B5-455B-841C-AB7C74E4DDFC}";
                case MUSIC: return "{4BD8D571-6D19-48D3-BE97-422220080E43}";
                case DESKTOP: return "{B4BFCC3A-DB2C-424C-B029-7FE99A87C641}";
                case PICTURES: return "{33E28130-4E1E-4676-835A-98395C3BC3BB}";
                default: return null;
            }
        }
    }

    public static String getUserDir(DirectoryType dirType) {
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("windows")) {
            return getWindowsUserDir(dirType);
        } else if (os.contains("linux") || os.contains("unix")) {
            return getXdgUserDir(dirType);
        } else if (os.contains("mac")) {
            return getMacUserDir(dirType);
        }
        
        return null;
    }

    private static String getWindowsUserDir(DirectoryType dirType) {
        String command = "powershell.exe -Command \"[Environment]::GetFolderPath([Environment+SpecialFolder]::" + 
            getWindowsFolderName(dirType) + ")\"";
        
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String path = reader.readLine();
            
            int exitCode = process.waitFor();
            if (exitCode == 0 && path != null && !path.isEmpty()) {
                return path;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**Funktion zum dynamischen Laden von Dateipfaden unter Linux-Systemen
     * Es wird der Bash-Befehl xdg-user-dir mit dem Zusatz in der dirType variable ausgeführt.
     * Zurückgegeben wird der volle Pfad zum Ordner
     * Bsp: xdg-user-dir DOCUMENTS -> /home/<username>/Dokumente
     * @author Lennard Rütten
     * @param dirType
     * @return String
     */
    private static String getXdgUserDir(DirectoryType dirType) {
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

    private static String getMacUserDir(DirectoryType dirType) {
        // Mac OS X user directories are in known locations
        String userHome = System.getProperty("user.home");
        switch (dirType) {
            case DOCUMENTS: return userHome + "/Documents";
            case DOWNLOAD: return userHome + "/Downloads";
            case VIDEOS: return userHome + "/Movies";
            case MUSIC: return userHome + "/Music";
            case DESKTOP: return userHome + "/Desktop";
            case PICTURES: return userHome + "/Pictures";
            default: return null;
        }
    }

    private static String getWindowsFolderName(DirectoryType dirType) {
        switch (dirType) {
            case DOCUMENTS: return "MyDocuments";
            case DOWNLOAD: return "Downloads";
            case VIDEOS: return "MyVideos";
            case MUSIC: return "MyMusic";
            case DESKTOP: return "Desktop";
            case PICTURES: return "MyPictures";
            default: return null;
        }
    }

}
