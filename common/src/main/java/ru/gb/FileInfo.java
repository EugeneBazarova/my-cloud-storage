package ru.gb;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class FileInfo implements Serializable {

    private String filename;
    private long size;
    private LocalDateTime lastChange;
    private boolean isDirectory = false;
    private String pathToFile;
    private boolean isNew;

    public FileInfo(String folder, boolean isDirectory){
        this.isDirectory = isDirectory;
        this.lastChange = LocalDateTime.now();
        this.pathToFile = folder;
        this.isNew = true;
    }

    public FileInfo(Path path) {
        try {
            this.filename = path.getFileName().toString();
            if (Files.isDirectory(path)) {
                isDirectory = true;
                try {
                    this.size = FileSystemMethods.getDirectorySize(path);
                } catch (Exception e) {
                    this.size = 0L;
                }
            } else {
                this.size = Files.size(path);
            }
            this.pathToFile = path.toString();
            this.lastChange = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneOffset.ofHours(3));
            this.isNew = false;
        } catch (IOException e) {
            throw new RuntimeException("[ ERROR: SMTH WRONG WITH FILE ]");
        }
    }

    public String getFilename() {
        return filename;
    }

    public long getSize() {
        return size;
    }

    public LocalDateTime getLastChange() {
        return lastChange;
    }

    public String getPathToFile() {
        return pathToFile;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setFilename(String filename) {
        this.filename = filename;
        Path folder = Paths.get(this.pathToFile);
        if (!isNew) {
            folder = folder.getParent();
        }
        this.pathToFile = folder.resolve(filename).toString();
        isNew = false;
    }
}
