package net.snakegame.game;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
                System.err.println("Datei nicht vorhanden: " + filePath);
                allFilesExist = false; // Wenn eine Datei fehlt, setze dies auf false
            }
        }
        return allFilesExist; // Gibt true zurück, wenn alle Dateien vorhanden sind
    }

    /**
     * Asynchronously downloads a file and updates progress
     * 
     * @param url The URL to download from
     * @param progressCallback A callback that receives progress updates (0.0 to 1.0)
     * @param statusCallback A callback that receives status messages
     * @return CompletableFuture with the downloaded file path
     */
    public CompletableFuture<Path> downloadFileAsync(String url, Consumer<Double> progressCallback, Consumer<String> statusCallback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                statusCallback.accept("Initiating download from: " + url);
                URL fileUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) fileUrl.openConnection();

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

                statusCallback.accept("Downloading: " + fileName);

                long fileSize = connection.getContentLengthLong();
                long totalBytesRead = 0;

                try (InputStream inputStream = connection.getInputStream();
                     BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                     OutputStream outputStream = Files.newOutputStream(saveFilePath);
                     BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {

                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;
                    while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                        bufferedOutputStream.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        
                        // Update progress
                        if (fileSize > 0) {
                            double progress = (double) totalBytesRead / fileSize;
                            progressCallback.accept(progress);
                        }
                    }
                    bufferedOutputStream.flush();
                }

                statusCallback.accept("Download completed: " + saveFilePath);
                return saveFilePath;
            } catch (Exception e) {
                statusCallback.accept("Download error: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Asynchronously extracts a ZIP file and updates progress
     * 
     * @param zipFilePath The path to the ZIP file
     * @param extractDir The directory to extract to
     * @param progressCallback A callback that receives progress updates (0.0 to 1.0)
     * @param statusCallback A callback that receives status messages
     * @return CompletableFuture that completes when extraction is done
     */
    public CompletableFuture<Void> extractZipAsync(String zipFilePath, String extractDir,
                                                 Consumer<Double> progressCallback, Consumer<String> statusCallback) {
        return CompletableFuture.runAsync(() -> {
            try {
                statusCallback.accept("Preparing to extract: " + zipFilePath);
                Path destDir = Paths.get(extractDir).toAbsolutePath().normalize();
                Path zipPath = Paths.get(zipFilePath).toAbsolutePath().normalize();

                if (!Files.exists(zipPath)) {
                    throw new IOException("ZIP-Datei nicht gefunden: " + zipFilePath);
                }

                Files.createDirectories(destDir);

                // First count entries to track progress
                try (ZipFile countZipFile = new ZipFile(zipPath.toFile())) {
                    int totalEntries = countZipFile.size();
                    int currentEntry = 0;

                    try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
                        Enumeration<? extends ZipEntry> entries = zipFile.entries();

                        while (entries.hasMoreElements()) {
                            ZipEntry entry = entries.nextElement();
                            String sanitizedName = sanitizeFileName(entry.getName());
                            Path entryPath = destDir.resolve(sanitizedName).normalize();

                            // Update progress
                            currentEntry++;
                            double progress = (double) currentEntry / totalEntries;
                            progressCallback.accept(progress);
                            
                            // Überprüfe, ob der entpackte Pfad innerhalb des Zielverzeichnisses liegt
                            if (!entryPath.startsWith(destDir)) {
                                statusCallback.accept("Skipping suspicious path: " + entry.getName());
                                continue;
                            }

                            if (entry.isDirectory()) {
                                Files.createDirectories(entryPath);
                            } else {
                                statusCallback.accept("Extracting: " + sanitizedName);
                                Files.createDirectories(entryPath.getParent());
                                try (InputStream is = zipFile.getInputStream(entry)) {
                                    Files.copy(is, entryPath, StandardCopyOption.REPLACE_EXISTING);
                                }
                            }
                        }
                    }
                }

                // Lösche die ZIP-Datei nach erfolgreichem Entpacken
                Files.deleteIfExists(zipPath);
                statusCallback.accept("Extraction completed to: " + extractDir);
            } catch (Exception e) {
                statusCallback.accept("Extraction error: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Performs the download and extraction process with progress tracking
     * 
     * @param url The URL to download from
     * @param extractDir The directory to extract to
     * @param progressCallback A callback that receives progress updates (0.0 to 1.0)
     * @param statusCallback A callback that receives status messages
     * @return CompletableFuture that completes when the entire process is done
     */
    public CompletableFuture<Void> downloadAndExtractAsync(String url, String extractDir,
                                                         Consumer<Double> progressCallback, Consumer<String> statusCallback) {
        statusCallback.accept("Starting download and extraction process");
        return downloadFileAsync(url, progress -> progressCallback.accept(progress * 0.6), statusCallback)
                .thenCompose(zipPath -> {
                    // After download (which is 60% of total progress), start extraction (remaining 40%)
                    return extractZipAsync(zipPath.toString(), extractDir,
                            progress -> progressCallback.accept(0.6 + progress * 0.4), statusCallback);
                });
    }

    // The original methods are kept for compatibility, but they now delegate to the async versions
    public Path downloadFile(String url) throws IOException {
        try {
            return downloadFileAsync(url, progress -> {}, message -> {}).join();
        } catch (Exception e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw new IOException(e);
        }
    }

    public void extractZip(String zipFilePath, String extractDir) throws IOException {
        try {
            extractZipAsync(zipFilePath, extractDir, progress -> {}, message -> {}).join();
        } catch (Exception e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw new IOException(e);
        }
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
