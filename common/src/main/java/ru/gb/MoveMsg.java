package ru.gb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MoveMsg extends AbstractMsg {

    public enum Destination {
        TO_CLIENT,
        TO_SERVER
    }

    private String path;
    private boolean isDirectory;
    private Destination destination;
    private byte[] data;

    public MoveMsg(Path path, Path rootDir, Destination destination) throws IOException {
        super(AbstractMsgTypes.MOVE_FILE);
        this.path = rootDir.relativize(path).toString();
        this.destination = destination;
        if (Files.isDirectory(path)) {
            this.isDirectory = true;
        } else {
            this.isDirectory = false;
            this.data = Files.readAllBytes(path);
        }
    }

    public MoveMsg(FileInfo file, Destination destination) {
        super(AbstractMsgTypes.MOVE_FILE);
        this.path = file.getPathToFile();
        this.destination = destination;
        this.isDirectory = file.isDirectory();
    }

    public Destination getDestination() {
        return destination;
    }

    public byte[] getData() {
        return data;
    }

    public String getPath() {
        return path;
    }

    public boolean isDirectory() {
        return isDirectory;
    }
}