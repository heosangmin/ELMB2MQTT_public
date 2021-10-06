import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;

public class Device {
    private String ipAddress = "127.0.0.1";
    private int port = 502;

    private ModbusClient modbusClient;

    private Register[] holdingRegisters; // RW
    private Register[] inputRegisters; // R

    public Device() {

    }

    public Device(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public void connect() throws UnknownHostException, IOException {
        modbusClient = new ModbusClient(ipAddress, port);
        modbusClient.Connect();
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }

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

    public void writeUint64(int address, long value) throws UnknownHostException, SocketException, ModbusException, IOException {
        int reg0 = (int)(value & 0xFFFF);
        int reg1 = (int)((value & 0xFFFF0000) >> 16);
        int reg2 = (int)((value & 0xFFFF00000000L) >> 32);
        int reg3 = (int)((value & 0xFFFF000000000000L) >> 48);
        int[] regs = {reg0,reg1,reg2,reg3};
        
        modbusClient.WriteMultipleRegisters(address, regs);
    }

    public long readUint64(int address) throws UnknownHostException, SocketException, ModbusException, IOException {
        int[] regs = modbusClient.ReadHoldingRegisters(address, 4);
        long value = 0;
        long reg0 = regs[0] & 0xFFFF;
        long reg1 = (regs[1] & 0xFFFF) << 16;
        long reg2 = (regs[2] & 0xFFFFL) << 32;
        long reg3 = (regs[3] & 0xFFFFL) << 48;
        
        value =  reg0 + reg1 + reg2 + reg3;

        return value;
    }

    

    public void writeBoolean(int address, int value) throws UnknownHostException, SocketException, ModbusException, IOException {
        modbusClient.WriteSingleRegister(address, value);
    }

    public boolean readBooleanHR(int address) throws UnknownHostException, SocketException, ModbusException, IOException {
        int result = modbusClient.ReadHoldingRegisters(address, 1)[0];
        return result != 0;
    }

    public boolean readBooleanIR(int address) throws UnknownHostException, SocketException, ModbusException, IOException {
        int result = modbusClient.ReadInputRegisters(address, 1)[0];
        return result != 0;
    }

    public void writeFloat32(int address, float value) throws UnknownHostException, SocketException, ModbusException, IOException {
        int[] regs = ModbusClient.ConvertFloatToTwoRegisters(value);
        modbusClient.WriteMultipleRegisters(address, regs);
    }

    public float readFloat32HR(int address) throws UnknownHostException, SocketException, ModbusException, IOException {
        int[] regs = modbusClient.ReadHoldingRegisters(address, 2);
        return ModbusClient.ConvertRegistersToFloat(regs);
    }

    public float readFloat32IR(int address) throws UnknownHostException, SocketException, ModbusException, IOException {
        int[] regs = modbusClient.ReadInputRegisters(address, 2);
        return ModbusClient.ConvertRegistersToFloat(regs);
    }

    public void writeUint32(int address, int value) throws UnknownHostException, SocketException, ModbusException, IOException {
        int regs[] = ModbusClient.ConvertDoubleToTwoRegisters(value);
        modbusClient.WriteMultipleRegisters(address, regs);
    }

    public int readUint32HR(int address) throws UnknownHostException, SocketException, ModbusException, IOException {
        int regs[] = modbusClient.ReadHoldingRegisters(address, 2);
        return ModbusClient.ConvertRegistersToDouble(regs);
    }

    public int readUint32IR(int address) throws UnknownHostException, SocketException, ModbusException, IOException {
        int regs[] = modbusClient.ReadInputRegisters(address, 2);
        return ModbusClient.ConvertRegistersToDouble(regs);
    }

    public void writeUint16(int address, int value) throws UnknownHostException, SocketException, ModbusException, IOException {
        modbusClient.WriteSingleRegister(address, value);
    }

    public int readUint16HR(int address) throws UnknownHostException, SocketException, ModbusException, IOException {
        return modbusClient.ReadHoldingRegisters(address, 1)[0];
    }

    public int readUint16IR(int address) throws UnknownHostException, SocketException, ModbusException, IOException {
        return modbusClient.ReadInputRegisters(address, 1)[0];
    }
}
