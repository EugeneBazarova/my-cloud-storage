package ru.gb;

public class RenameMsg extends AbstractMsg {

    private FileInfo file;
    private String name;

    public RenameMsg(FileInfo file, String name) {
        super(AbstractMsgTypes.RENAME_FILE);
        this.file = file;
        this.name = name;
    }

    public FileInfo getFile() {
        return file;
    }

    public String getName() {
        return name;
    }
}