package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.impl.BGRSServer.api.OpMessage;

public class ErrorMessage implements OpMessage<Short> {
    private final Short Opcode = 13;
    private short MessageOpcode;

    public ErrorMessage(short _messageOpcode){
        MessageOpcode = _messageOpcode;
    }

    public short getMessageOpcode() {
        return MessageOpcode;
    }

    public Short getOpcode() {
        return Opcode;
    }
}
