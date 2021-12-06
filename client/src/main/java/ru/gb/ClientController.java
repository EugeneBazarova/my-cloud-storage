package ru.gb;


import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
public class ClientController implements Initializable {

    @FXML
    VBox signInPane;

    @FXML
    TextField loginField;

    @FXML
    TextField passwordField;

    @FXML
    Label alertMsg;

    @FXML
    Button btnSignIn;

    @FXML
    Button btnSignUp;

    @FXML
    HBox lblBottomSingIn;

    @FXML
    HBox lblBottomSingUp;

    @FXML
    VBox mainPane;

    @FXML
    TextField clientSearch;

    @FXML
    TextField serverSearch;

    @FXML
    TableView<FileInfo> userFilesList;

    @FXML
    TableView<FileInfo> serverFilesList;

    @FXML
    TextField clientPath;

    @FXML
    TextField serverPath;

    @FXML
    Button closeButton;

    private ServerHandler serverHandler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {


        try {
            serverHandler = new ServerHandler(this);
        } catch (IOException e) {
            e.printStackTrace();
        }


        describeTable(userFilesList);
        describeTable(serverFilesList);


        clientSearch.textProperty().addListener(observable -> {
            String filter = clientSearch.getText();
            if (filter == null || filter.length() == 0) {
                updateClientFilesList(Paths.get(clientPath.getText()));
            } else {
                try {
                    userFilesList.getItems().clear();
                    userFilesList.getItems().addAll(FileSystemMethods.searchInCurrentDir(filter, clientPath.getText()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        serverSearch.textProperty().addListener(observable -> {
            String filter = serverSearch.getText();
            if (filter == null || filter.length() == 0) {
                serverHandler.sendMessage(new UpdFilesList(serverPath.getText()));
            } else {
                serverHandler.sendMessage(new UpdFilesList(serverPath.getText(), filter));
            }
        });

    }

    public void updateClientFilesList(Path path) {
        try {
            clientPath.setText(path.normalize().toAbsolutePath().toString());
            userFilesList.getItems().clear();
            userFilesList.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            userFilesList.sort();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void describeTable(TableView table) {

        table.setEditable(true);

        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("File name");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        fileNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        fileNameColumn.setOnEditCommit(this::editFileName);
        fileNameColumn.setPrefWidth(600);

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("File size");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setPrefWidth(200);
        fileSizeColumn.setCellFactory(column -> new TableCell<FileInfo, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(formatBytesCount(item));
                }
            }
        });

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TableColumn<FileInfo, String> fileDateChangeColumn = new TableColumn<>("Last modification");
        fileDateChangeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastChange().format(dateTimeFormatter)));
        fileDateChangeColumn.setMinWidth(300);

        table.getColumns().addAll(fileNameColumn, fileSizeColumn, fileDateChangeColumn);

    }

    private void editFileName(TableColumn.CellEditEvent<FileInfo, String> event) {
        String newName = event.getNewValue();
        if (!"".equals(newName)) {
            TablePosition<FileInfo, String> position = event.getTablePosition();
            int row = position.getRow();
            FileInfo file = event.getTableView().getItems().get(row);
            if (event.getTableView().equals(userFilesList)) {
                if (file.isNew()) {
                    try {
                        FileSystemMethods.createDirectory(clientPath.getText() + File.separator + newName);
                        file.setFilename(newName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        FileSystemMethods.rename(file, newName);
                        file.setFilename(newName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (event.getTableView().equals(serverFilesList)) {
                serverHandler.sendMessage(new RenameMsg(file, newName));
                serverHandler.sendMessage(new UpdFilesList(serverPath.getText()));
            }
        }

    }


    private String formatBytesCount(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "kMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }


    public void clientTableClick(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            Path path = Paths.get(clientPath.getText()).resolve(userFilesList.getSelectionModel().getSelectedItem().getFilename());
            if (Files.isDirectory(path)) {
                updateClientFilesList(path);
            }
        }
    }


    public void serverTableClick(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            FileInfo currentFile = serverFilesList.getSelectionModel().getSelectedItem();
            if (currentFile.isDirectory()) {
                serverHandler.sendMessage(
                        new UpdFilesList(
                                (serverPath.getText() == null ? " " : serverPath.getText() + File.separator) + currentFile.getFilename()));
            }
        }
    }


    public void setClientPathUp(ActionEvent actionEvent) {
        Path parent = Paths.get(clientPath.getText()).getParent();
        if (parent != null) {
            updateClientFilesList(parent);
        }
    }


    public void setServerPathUp(ActionEvent actionEvent) {
        Path parent = Paths.get(serverPath.getText()).getParent();
        if (parent != null) {
            serverHandler.sendMessage(new UpdFilesList(parent.toString()));
        } else {
            serverHandler.sendMessage(new UpdFilesList());
        }
    }


    public void copyToServer() {
        FileInfo currentFile = userFilesList.getSelectionModel().getSelectedItem();
        if (currentFile != null) {
            try {
                if (currentFile.isDirectory()) {

                    FileSystemMethods.filesList(Paths.get(currentFile.getPathToFile()))
                            .forEach(
                                    file -> {
                                        try {
                                            serverHandler.sendMessage(new MoveMsg(file.toAbsolutePath(), Paths.get(clientPath.getText()), MoveMsg.Destination.TO_SERVER));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                            );
                } else {
                    serverHandler.sendMessage(
                            new MoveMsg(Paths.get(currentFile.getPathToFile()).toAbsolutePath(), Paths.get(clientPath.getText()), MoveMsg.Destination.TO_SERVER));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverHandler.sendMessage(new UpdFilesList(serverPath.getText()));
        }
    }


    public void copyFromServer() {
        FileInfo currentFile = serverFilesList.getSelectionModel().getSelectedItem();
        if (currentFile != null) {
            serverHandler.sendMessage(new MoveMsg(currentFile, MoveMsg.Destination.TO_CLIENT));
        }
    }

    public void signIn(ActionEvent actionEvent) {
        alertMsg.setText("[ INCORRECT LOGIN OR PASSWORD ]");
        serverHandler.sendMessage(new AuthMsg(loginField.getText(), passwordField.getText(), AbstractMsgTypes.AUTH));
    }

    public void signUp(ActionEvent actionEvent) {
        String login = loginField.getText();
        if ("".equals(login)) {
            alertMsg.setVisible(true);
            alertMsg.setText("[ LOGIN CAN'T BE EMPTY! ]");
            return;
        }
        serverHandler.sendMessage(new AuthMsg(login, passwordField.getText(), AbstractMsgTypes.REGISTRATION));
        goToSingIn(new ActionEvent());
    }

    public void setAuthorized(boolean isAuthorized) {
        if (isAuthorized) {
            signInPane.setVisible(false);
            mainPane.setVisible(true);
            Path defaultClientPath = Paths.get("cloud-storage");
            updateClientFilesList(defaultClientPath);
            serverHandler.sendMessage(new UpdFilesList());
        } else {
            alertMsg.setVisible(true);
        }
    }

    public void goToSingUp(ActionEvent actionEvent) {
        btnSignIn.setVisible(false);
        lblBottomSingIn.setVisible(false);
        btnSignUp.setVisible(true);
        lblBottomSingUp.setVisible(true);
        alertMsg.setVisible(false);
    }

    public void goToSingIn(ActionEvent actionEvent) {
        btnSignIn.setVisible(true);
        lblBottomSingIn.setVisible(true);
        btnSignUp.setVisible(false);
        lblBottomSingUp.setVisible(false);
        alertMsg.setVisible(false);
    }


    public void addDirOnClient(ActionEvent actionEvent) {
        userFilesList.getItems().add(new FileInfo(clientPath.getText(), true));
        int tablePosition = userFilesList.getItems().size() - 1;
        TableColumn<FileInfo, ?> fileInfoTableColumn = userFilesList.getColumns().get(0);
        userFilesList.edit(tablePosition, fileInfoTableColumn);
    }

    public void removeOnClient(ActionEvent actionEvent) {
        FileInfo currentFile = userFilesList.getSelectionModel().getSelectedItem();
        if (currentFile.isDirectory()) {
            try {
                FileSystemMethods.removeFolder(Paths.get(currentFile.getPathToFile()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Files.delete(Paths.get(currentFile.getPathToFile()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        updateClientFilesList(Paths.get(clientPath.getText()));
    }

    public void CloseButtonAction(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    public void renameOnClient(ActionEvent actionEvent) {
        int tablePosition = userFilesList.getSelectionModel().getSelectedIndex();
        TableColumn<FileInfo, ?> fileInfoTableColumn = userFilesList.getColumns().get(0);
        userFilesList.edit(tablePosition, fileInfoTableColumn);
    }

    public void addDirOnServer(ActionEvent actionEvent) {
        serverFilesList.getItems().add(new FileInfo(serverPath.getText(), true));
        int tablePosition = serverFilesList.getItems().size() - 1;
        TableColumn<FileInfo, ?> fileInfoTableColumn = serverFilesList.getColumns().get(0);
        serverFilesList.edit(tablePosition, fileInfoTableColumn);
    }

    public void renameOnServer(ActionEvent actionEvent) {
        int tablePosition = serverFilesList.getSelectionModel().getSelectedIndex();
        TableColumn<FileInfo, ?> fileInfoTableColumn = serverFilesList.getColumns().get(0);
        serverFilesList.edit(tablePosition, fileInfoTableColumn);
    }

    public void removeOnServer(ActionEvent actionEvent) {
        FileInfo currentFile = serverFilesList.getSelectionModel().getSelectedItem();
        serverHandler.sendMessage(new DeleteMsg(currentFile));
        serverHandler.sendMessage(new UpdFilesList(serverPath.getText()));
    }
}