package ru.gb;

import java.io.Serializable;

public class AbstractMsg implements Serializable {

    private final AbstractMsgTypes messageType;

    public AbstractMsg(AbstractMsgTypes messageType) {
        this.messageType = messageType;
    }

    public AbstractMsgTypes getMessageType() {
        return messageType;
    }

}
