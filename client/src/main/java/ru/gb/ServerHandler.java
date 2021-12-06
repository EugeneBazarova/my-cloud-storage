package ru.gb;


import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;


import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerHandler {

    private ExecutorService executorService;

    private Socket socket;
    private ObjectDecoderInputStream in;
    private ObjectEncoderOutputStream out;

    private ClientController clientStage;

    public ServerHandler(ClientController clientController) throws IOException {
        startConnection();
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(serverListener);
        clientStage = clientController;
    }


    public void startConnection() throws IOException {
        socket = new Socket("localhost", 8188);
        in = new ObjectDecoderInputStream(socket.getInputStream(), 256*1024*1024);
        out = new ObjectEncoderOutputStream(socket.getOutputStream());
    }


    public <T extends AbstractMsg> void sendMessage(T msg) {
        try {
            if (msg.getMessageType() == AbstractMsgTypes.UPDATE_FILES_LIST) {
                clientStage.serverFilesList.getItems().clear();
            }
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object readObject() throws IOException, ClassNotFoundException {
        return in.readObject();
    }

    public void shutdown() {
        try {
            socket.close();
            executorService.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Runnable serverListener = () -> {
        try {
            while (true) {
                Object msg = readObject();
                if (((AbstractMsg) msg).getMessageType() == AbstractMsgTypes.UPDATE_FILES_LIST) {

                    UpdFilesList updFilesList = (UpdFilesList) msg;

                    clientStage.serverPath.setText(updFilesList.getDirPath());
                    clientStage.serverFilesList.getItems().addAll(updFilesList.getFilesList());

                } else if (((AbstractMsg) msg).getMessageType() == AbstractMsgTypes.AUTH) {

                    clientStage.setAuthorized(((AuthMsg) msg).isLoginSuccessful());

                } else if (((AbstractMsg) msg).getMessageType() == AbstractMsgTypes.MOVE_FILE) {

                    MoveMsg moveMsg = (MoveMsg) msg;
                    Path newFilePath = Paths.get(clientStage.clientPath.getText(), moveMsg.getPath());
                    if (moveMsg.isDirectory()) {
                        if (!Files.exists(newFilePath)) {
                            Files.createDirectory(newFilePath);
                        }
                    } else {
                        Files.write(newFilePath, moveMsg.getData(), StandardOpenOption.CREATE);
                    }

                    clientStage.updateClientFilesList(Paths.get(clientStage.clientPath.getText()));

                }
            }
        } catch (SocketException e) {
            System.out.println("exit");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    };

}