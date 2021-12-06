package ru.gb;

public class DeleteMsg extends AbstractMsg {

    private final FileInfo file;

    public DeleteMsg(FileInfo file) {
        super(AbstractMsgTypes.DELETE_FILE);
        this.file = file;
    }

    public FileInfo getFile() {
        return file;
    }
}