package ru.gb;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


public class AuthHandler extends SimpleChannelInboundHandler<AuthMsg> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AuthMsg msg) throws Exception {

        if (msg.getMessageType() == AbstractMsgTypes.AUTH) {
            String userDir = DBConnector.checkAuth(msg.getLogin(), msg.getPassword());
            msg.setLoginSuccessful(userDir);
            ctx.fireChannelRead(msg);

        } else if (msg.getMessageType() == AbstractMsgTypes.REGISTRATION) {
            DBConnector.addUser(msg.getLogin(), msg.getPassword());
            ctx.writeAndFlush(msg);
        }
    }

}