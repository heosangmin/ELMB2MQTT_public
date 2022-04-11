package jp.co.nttdatabizsys.elmb2mqtt.Echonet;

import java.net.InetAddress;

public class Result {
    private InetAddress inetAddress;
    private byte[] data;

    public Result(InetAddress inetAddress, byte[] data) {
        this.inetAddress = inetAddress;
        this.data = data;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public byte[] getData() {
        return data;
    }
}
