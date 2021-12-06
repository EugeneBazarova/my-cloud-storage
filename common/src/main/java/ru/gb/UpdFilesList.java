package ru.gb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UpdFilesList extends AbstractMsg {

    private String dirPath;
    private String filter;
    private List<FileInfo> filesList = new ArrayList<>();

    public UpdFilesList() {
        super(AbstractMsgTypes.UPDATE_FILES_LIST);
    }

    public UpdFilesList(String dirPath) {
        super(AbstractMsgTypes.UPDATE_FILES_LIST);
        if (!"".equals(dirPath)) {
            this.dirPath = dirPath;
        }
    }

    public UpdFilesList(String dirPath, String filter) {
        super(AbstractMsgTypes.UPDATE_FILES_LIST);
        this.dirPath = dirPath;
        this.filter = filter;
    }

    public String getDirPath() {
        return dirPath;
    }

    public List<FileInfo> getFilesList() {
        return filesList;
    }

    public void fillFilesList(Path path) throws IOException {
        if (filter == null) {
            filesList = Files.list(path).map(FileInfo::new).collect(Collectors.toList());
        } else {
            filesList = FileSystemMethods.searchInCurrentDir(filter, path.toString());
        }

    }
}
