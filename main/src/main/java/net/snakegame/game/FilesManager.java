package net.snakegame.game;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;

public class FilesManager {
    private final Path downloadDir;
    private static final int BUFFER_SIZE = 8192;
    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 30000;

    public FilesManager(String downloadDirPath) throws IOException {
        this.downloadDir = Paths.get(downloadDirPath).toAbsolutePath().normalize();
        Files.createDirectories(downloadDir);
    }

    public boolean checkFilesExist(String[] expectedFiles) {
        boolean allFilesExist = true;
        for (String filePath : expectedFiles) {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("Datei fehlt: " + filePath);
                allFilesExist = false; // Wenn eine Datei fehlt, setze dies auf false
            }
        }
        return allFilesExist; // Gibt true zurück, wenn alle Dateien vorhanden sind
    }


    public Path downloadFile(String url) throws IOException {
        URL fileUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) fileUrl.openConnection();

        try {
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Download fehlgeschlagen. HTTP Code: " + responseCode);
            }

            String fileName = getFileNameFromHeader(connection);
            Path saveFilePath = downloadDir.resolve(fileName).normalize();

            // Überprüfe, ob der finale Pfad innerhalb des Download-Verzeichnisses liegt
            if (!saveFilePath.startsWith(downloadDir)) {
                throw new SecurityException("Ungültiger Dateiname: " + fileName);
            }

            System.out.println("Lade herunter: " + fileName);

            try (InputStream inputStream = connection.getInputStream();
                 BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                 OutputStream outputStream = Files.newOutputStream(saveFilePath);
                 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                    bufferedOutputStream.write(buffer, 0, bytesRead);
                }
                bufferedOutputStream.flush();
            }

            System.out.println("Download erfolgreich: " + saveFilePath);
            return saveFilePath;
        } finally {
            connection.disconnect();
        }
    }

    public void extractZip(String zipFilePath, String extractDir) throws IOException {
        Path destDir = Paths.get(extractDir).toAbsolutePath().normalize();
        Path zipPath = Paths.get(zipFilePath).toAbsolutePath().normalize();

        if (!Files.exists(zipPath)) {
            throw new IOException("ZIP-Datei nicht gefunden: " + zipFilePath);
        }

        Files.createDirectories(destDir);

        try (java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(zipPath.toFile())) {
            Enumeration<? extends java.util.zip.ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                java.util.zip.ZipEntry entry = entries.nextElement();
                String sanitizedName = sanitizeFileName(entry.getName());
                Path entryPath = destDir.resolve(sanitizedName).normalize();

                // Überprüfe, ob der entpackte Pfad innerhalb des Zielverzeichnisses liegt
                if (!entryPath.startsWith(destDir)) {
                    System.out.println("Überspringe verdächtigen Pfad: " + entry.getName());
                    continue;
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    try (InputStream is = zipFile.getInputStream(entry)) {
                        Files.copy(is, entryPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }

        // Lösche die ZIP-Datei nach erfolgreichem Entpacken
        Files.deleteIfExists(zipPath);
    }

    private String getFileNameFromHeader(HttpURLConnection connection) throws IOException {
        String disposition = connection.getHeaderField("Content-Disposition");
        String fileName = null;

        if (disposition != null) {
            Pattern pattern = Pattern.compile("filename\\*=UTF-8''(.+)|filename=\"(.+?)\"");
            Matcher matcher = pattern.matcher(disposition);
            if (matcher.find()) {
                fileName = matcher.group(1) != null ?
                        URLDecoder.decode(matcher.group(1), "UTF-8") :
                        matcher.group(2);
            }
        }

        if (fileName == null) {
            String path = connection.getURL().getPath();
            fileName = path.substring(path.lastIndexOf('/') + 1);
        }

        return sanitizeFileName(fileName);
    }

    private String sanitizeFileName(String fileName) {
        // Entferne ungültige Zeichen und normalisiere Pfadtrenner
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_")
                .replace('\\', '/')
                .replaceAll("^\\.+", "") // Entferne führende Punkte
                .replaceAll("/\\.+/", "/"); // Entferne versteckte Verzeichnisse
    }

}