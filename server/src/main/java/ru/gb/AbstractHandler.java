package ru.gb;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;



public class AbstractHandler extends ChannelInboundHandlerAdapter {

    private Path userRootDir;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {


        AbstractMsgTypes msgType = ((AbstractMsg) msg).getMessageType();
        if (msgType == AbstractMsgTypes.UPDATE_FILES_LIST) {

            UpdFilesList updFilesList = (UpdFilesList) msg;
            Path directory = updFilesList.getDirPath() == null ? userRootDir : userRootDir.resolve(updFilesList.getDirPath());
            updFilesList.fillFilesList(directory);
            ctx.writeAndFlush(updFilesList);

        } else if (msgType == AbstractMsgTypes.MOVE_FILE) {
            MoveMsg message = (MoveMsg) msg;
            if (message.getDestination() == MoveMsg.Destination.TO_SERVER) {
                Path newFilePath = userRootDir.resolve(message.getPath());
                if (message.isDirectory()) {
                    if (!Files.exists(newFilePath)) {
                        Files.createDirectory(newFilePath);
                    }
                } else {
                    Files.write(newFilePath, message.getData(), StandardOpenOption.CREATE);
                }
            } else if (message.getDestination() == MoveMsg.Destination.TO_CLIENT) {

                if (message.isDirectory()) {
                    FileSystemMethods.filesList(Paths.get(message.getPath()).toAbsolutePath())
                            .forEach(
                                    file -> {
                                        try {
                                            ctx.writeAndFlush(new MoveMsg(file, userRootDir.toAbsolutePath(), MoveMsg.Destination.TO_CLIENT));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                            );
                } else {
                    ctx.writeAndFlush(new MoveMsg(Paths.get(message.getPath()).toAbsolutePath(), userRootDir.toAbsolutePath(), MoveMsg.Destination.TO_CLIENT));
                }
            }

        } else if (msgType == AbstractMsgTypes.AUTH) {
            AuthMsg authMsg = (AuthMsg) msg;
            if (authMsg.isLoginSuccessful()) {
                userRootDir = Paths.get("cloud-storage", authMsg.getUserDir());
                if (!Files.exists(userRootDir)) {
                    Files.createDirectory(userRootDir);
                }
            }
            ctx.writeAndFlush(msg);

        }else if (msgType == AbstractMsgTypes.RENAME_FILE) {

            FileInfo file = ((RenameMsg) msg).getFile();
            String name = ((RenameMsg) msg).getName();
            if (file.isNew()) {
                try {
                    FileSystemMethods.createDirectory(userRootDir.resolve(file.getPathToFile() + File.separator + name).toString());
                    file.setFilename(name);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    FileSystemMethods.rename(file, name);
                    file.setFilename(name);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else if (msgType == AbstractMsgTypes.DELETE_FILE) {
            FileInfo file = ((DeleteMsg) msg).getFile();
            if (file.isDirectory()) {
                try {
                    FileSystemMethods.removeFolder(Paths.get(file.getPathToFile()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Files.delete(Paths.get(file.getPathToFile()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            ctx.writeAndFlush(msg);
        }
    }
}