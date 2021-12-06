package ru.gb;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSystemMethods {

    public static List<FileInfo> searchInCurrentDir(String fileName, String searchDirectory) throws IOException {
        try (Stream<Path> files = Files.walk(Paths.get(searchDirectory))) {
            return files
                    .filter(f -> f.getFileName().toString().toUpperCase(Locale.ROOT).contains(fileName.toUpperCase(Locale.ROOT)))
                    .map(FileInfo::new)
                    .collect(Collectors.toList());
        }
    }

    public static List<Path> filesList(Path directory) throws IOException {
        try (Stream<Path> files = Files.walk(directory)) {
            return files
                    .collect(Collectors.toList());
        }
    }

    public static void createDirectory(String dir) throws IOException {
        Files.createDirectory(Paths.get(dir));
    }

    public static void rename(FileInfo file, String name) throws IOException {
        Path path = Paths.get(file.getPathToFile());
        Files.move(path, path.getParent().resolve(name));
    }

    public static void removeFolder(Path path) throws IOException {
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    public static Long getDirectorySize(Path path) throws IOException {
        AtomicLong size = new AtomicLong(0);

        Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                size.addAndGet(attrs.size());
                return FileVisitResult.CONTINUE;
            }
        });

        return size.get();
    }
}
