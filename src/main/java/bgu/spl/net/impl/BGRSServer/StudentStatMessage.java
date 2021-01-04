package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.impl.BGRSServer.api.OpMessage;

public class StudentStatMessage implements OpMessage<Short> {

    private final Short Opcode = 8;
    private String userName;

    public StudentStatMessage(){
        userName = null;
    }

    public Short getOpcode() {
        return Opcode;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
