package jp.co.nttdatabizsys.elmb2mqtt.Echonet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

import jp.co.nttdatabizsys.elmb2mqtt.util.Logger;

public class UdpSocket {
    private static final int BYTE_SIZE = 1024;
    private static UdpSocket udpSocket;
    
    private MulticastSocket socket;
    
    private Logger logger = Logger.getLogger();

    public static UdpSocket getUdpSocket() {
        if (udpSocket == null) {
            udpSocket = new UdpSocket();
        }
        return udpSocket;
    }

    private UdpSocket() {
        try {
            socket = new MulticastSocket(3610);
            socket.setSoTimeout(3000);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[UdpSocket][error] failed to create udp socket 3610");
            return;
        }
        logger.log("[socket] opened", true);
    }

    public void close() {
        socket.close();
        logger.log("[socket] closed", true);
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void send(byte[] msg, String address, int port) throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress(address, port);
        DatagramPacket packet = new DatagramPacket(msg, msg.length, socketAddress);

        socket.send(packet);
    }

    public Result receive() throws IOException {
        byte[] buf = new byte[BYTE_SIZE];
        byte[] data;
        int len;
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        socket.receive(packet);

        len = packet.getLength();
        data = new byte[len];
        
        System.arraycopy(packet.getData(), 0, data, 0, len);

        Result result = new Result(packet.getAddress(), data);

        return result;
    }
}