import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;

public class Device {
    private String ipAddress = "127.0.0.1";
    private int port = 502;
    private String topic;
    private int qos;

    private ModbusClient modbusClient;

    private Register[] holdingRegisters; // RW
    private Register[] inputRegisters; // R

    public static enum DeviceType {
        ENAPTER_ELECTROLYSER,
        TOSHIBA_H2REX
    }

    private DeviceType deviceType;

    public Device() {}

    public Device(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public void setDeviceType(DeviceType type) {
        this.deviceType = type;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void connect() throws UnknownHostException, IOException {
        if (modbusClient == null) {
            modbusClient = new ModbusClient(ipAddress, port);
        }
        if (!modbusClient.isConnected()) {
            //System.out.println("connecting...");
            modbusClient.Connect();
        }
    }

    public void disconnect() throws IOException {
        if (modbusClient.isConnected()) {
            modbusClient.Disconnect();
        }
    }

    public void setIpAddress(String ipAddress) {this.ipAddress = ipAddress;}
    public void setPort(int port) {this.port = port;}
    public void setTopic(String topic) {this.topic = topic;}
    public void setQos(int qos) {this.qos = qos;}

    public String getIpAddress() {return this.ipAddress;}
    public int getPort() {return this.port;}
    public String getTopic() {return this.topic;}
    public int getQos() {return this.qos;}

    public void setHoldingRegisters(Register[] holdingRegisters) {
        this.holdingRegisters = holdingRegisters;
    }

    public Register[] getHoldingRegisters() {
        return this.holdingRegisters;
    }

    public void setInputRegisters(Register[] inputRegisters) {
        this.inputRegisters = inputRegisters;
    }

    public Register[] getInputRegisters() {
        return this.inputRegisters;
    }

    public ByteBuffer readHoldingRegister(int address, int registers) throws UnknownHostException, SocketException, ModbusException, IOException {
        int[] regs = modbusClient.ReadHoldingRegisters(address, registers);
        ByteBuffer buf = ByteBuffer.allocate(regs.length * 2);
        for (int i = regs.length - 1; i > -1; i--) {
            buf.put((byte) ((regs[i] >> 8) & 0xFF));
            buf.put((byte) (regs[i] & 0xFF));
        }
        return buf;
    }

    public ByteBuffer readInputRegister(int address, int registers) throws UnknownHostException, SocketException, ModbusException, IOException {
        int[] regs = modbusClient.ReadInputRegisters(address, registers);
        ByteBuffer buf = ByteBuffer.allocate(regs.length * 2);
        for (int i = regs.length - 1; i > -1; i--) {
            buf.put((byte) ((regs[i] >> 8) & 0xFF));
            buf.put((byte) (regs[i] & 0xFF));
        }
        return buf;
    }

    public void writeUint64(int address, long value) throws UnknownHostException, SocketException, ModbusException, IOException {
        int reg0 = (int)(value & 0xFFFF);
        int reg1 = (int)((value & 0xFFFF0000) >> 16);
        int reg2 = (int)((value & 0xFFFF00000000L) >> 32);
        int reg3 = (int)((value & 0xFFFF000000000000L) >> 48);
        int[] regs = {reg0,reg1,reg2,reg3};
        
        modbusClient.WriteMultipleRegisters(address, regs);
    }

    public byte[] readUint64HR(int address) throws UnknownHostException, SocketException, ModbusException, IOException {
        int[] regs = modbusClient.ReadHoldingRegisters(address, 4);
        byte[] bytes = {
            (byte) ((regs[3] >> 8) & 0xFF),
            (byte) (regs[3] & 0xFF),
            (byte) ((regs[2] >> 8) & 0xFF),
            (byte) (regs[2] & 0xFF),
            (byte) ((regs[1] >> 8) & 0xFF),
            (byte) (regs[1] & 0xFF),
            (byte) ((regs[0] >> 8) & 0xFF),
            (byte) (regs[0] & 0xFF)
        };
        return bytes;
    }

    public byte[] readUint64IR(int address) throws UnknownHostException, SocketException, ModbusException, IOException {
        int[] regs = modbusClient.ReadHoldingRegisters(address, 4);
        byte[] bytes = {
            (byte) ((regs[3] >> 8) & 0xFF),
            (byte) (regs[3] & 0xFF),
            (byte) ((regs[2] >> 8) & 0xFF),
            (byte) (regs[2] & 0xFF),
            (byte) ((regs[1] >> 8) & 0xFF),
            (byte) (regs[1] & 0xFF),
            (byte) ((regs[0] >> 8) & 0xFF),
            (byte) (regs[0] & 0xFF)
        };
        return bytes;
    }

    public byte[] readUint128IR(int address) throws UnknownHostException, SocketException, ModbusException, IOException {
        int[] regs = modbusClient.ReadHoldingRegisters(address, 8);
        byte[] bytes = {
            (byte) ((regs[7] >> 8) & 0xFF),
            (byte) (regs[7] & 0xFF),
            (byte) ((regs[6] >> 8) & 0xFF),
            (byte) (regs[6] & 0xFF),
            (byte) ((regs[5] >> 8) & 0xFF),
            (byte) (regs[5] & 0xFF),
            (byte) ((regs[4] >> 8) & 0xFF),
            (byte) (regs[4] & 0xFF),
            (byte) ((regs[3] >> 8) & 0xFF),
            (byte) (regs[3] & 0xFF),
            (byte) ((regs[2] >> 8) & 0xFF),
            (byte) (regs[2] & 0xFF),
            (byte) ((regs[1] >> 8) & 0xFF),
            (byte) (regs[1] & 0xFF),
            (byte) ((regs[0] >> 8) & 0xFF),
            (byte) (regs[0] & 0xFF)
        };
        return bytes;
    }

    public boolean readBooleanHR(int address) throws UnknownHostException, SocketException, ModbusException, IOException {
        int result = modbusClient.ReadHoldingRegisters(address, 1)[0];
        return result != 0;
    }

    public boolean readBooleanIR(int address) throws UnknownHostException, SocketException, ModbusException, IOException {
        int result = modbusClient.ReadInputRegisters(address, 1)[0];
        return result != 0;
    }

    public float readFloat32HR(int address) throws UnknownHostException, SocketException, ModbusException, IOException {
        int[] regs = modbusClient.ReadHoldingRegisters(address, 2);
        return ModbusClient.ConvertRegistersToFloat(regs);
    }

    public float readFloat32IR(int address) throws UnknownHostException, SocketException, ModbusException, IOException {
        int[] regs = modbusClient.ReadInputRegisters(address, 2);
        return ModbusClient.ConvertRegistersToFloat(regs);
    }

    public byte[] readUint32HR(int address) throws UnknownHostException, SocketException, ModbusException, IOException {
        int regs[] = modbusClient.ReadHoldingRegisters(address, 2);
        byte[] bytes = {
            (byte) ((regs[1] >> 8) & 0xFF),
            (byte) (regs[1] & 0xFF),
            (byte) ((regs[0] >> 8) & 0xFF),
            (byte) (regs[0] & 0xFF)
        };
        return bytes;
    }

    public byte[] readUint32IR(int address) throws UnknownHostException, SocketException, ModbusException, IOException {
        int regs[] = modbusClient.ReadInputRegisters(address, 2);
        byte[] bytes = {
            (byte) ((regs[1] >> 8) & 0xFF),
            (byte) (regs[1] & 0xFF),
            (byte) ((regs[0] >> 8) & 0xFF),
            (byte) (regs[0] & 0xFF)
        };
        return bytes;
    }
    
    public int readUint16HR(int address) throws UnknownHostException, SocketException, ModbusException, IOException {
        return modbusClient.ReadHoldingRegisters(address, 1)[0];
    }
    
    public int readUint16IR(int address) throws UnknownHostException, SocketException, ModbusException, IOException {
        return modbusClient.ReadInputRegisters(address, 1)[0];
    }

    public void writeInt16(int address, int value) throws UnknownHostException, SocketException, ModbusException, IOException {
        modbusClient.WriteSingleRegister(address, value);
    }

    public void writeInt32(int address, int value) throws UnknownHostException, SocketException, ModbusException, IOException {
        int regs[] = ModbusClient.ConvertDoubleToTwoRegisters(value);
        modbusClient.WriteMultipleRegisters(address, regs);
    }

    public void writeFloat32(int address, float value) throws UnknownHostException, SocketException, ModbusException, IOException {
        int[] regs = ModbusClient.ConvertFloatToTwoRegisters(value);
        modbusClient.WriteMultipleRegisters(address, regs);
    }
}
