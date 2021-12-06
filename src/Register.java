import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;

public class Register{
    public int address;
    public DataType dataType;
    public String dataTypeFinal = "raw";
    public String name;
    public String comment;
    protected ModbusClient modbusClient;

    public void setAddress(int address) {this.address = address;}
    public void setDataType(DataType dataType) {this.dataType = dataType;}
    public void setDataTypeFinal(String dataTypeFinal) {this.dataTypeFinal = dataTypeFinal;}
    public void setName(String name) {this.name = name;}
    public void setComment(String comment) {this.comment = comment;}
    public void setModbusClient(ModbusClient modbusClient) {this.modbusClient = modbusClient;}

    public int getAddress() {return this.address;}
    public DataType getDataType() {return this.dataType;}
    public String getDataTypeFinal() {return this.dataTypeFinal;}
    public String getName() {return this.name;}
    public String getComment() {return this.comment;}
    public ModbusClient getModbusClient() {return modbusClient;}

    public void writeUint64(long value) throws UnknownHostException, SocketException, ModbusException, IOException {
        int reg0 = (int)(value & 0xFFFF);
        int reg1 = (int)((value & 0xFFFF0000) >> 16);
        int reg2 = (int)((value & 0xFFFF00000000L) >> 32);
        int reg3 = (int)((value & 0xFFFF000000000000L) >> 48);
        int[] regs = {reg0,reg1,reg2,reg3};
        
        modbusClient.WriteMultipleRegisters(address, regs);
    }

    public boolean readBooleanHR() throws UnknownHostException, SocketException, ModbusException, IOException {
        int result = modbusClient.ReadHoldingRegisters(address, 1)[0];
        return result != 0;
    }

    public boolean readBooleanIR() throws UnknownHostException, SocketException, ModbusException, IOException {
        int result = modbusClient.ReadInputRegisters(address, 1)[0];
        return result != 0;
    }

    public float readFloat32HR() throws UnknownHostException, SocketException, ModbusException, IOException {
        int[] regs = modbusClient.ReadHoldingRegisters(address, 2);
        return ModbusClient.ConvertRegistersToFloat(regs);
    }

    public float readFloat32IR() throws UnknownHostException, SocketException, ModbusException, IOException {
        int[] regs = modbusClient.ReadInputRegisters(address, 2);
        return ModbusClient.ConvertRegistersToFloat(regs);
    }

    public byte[] readUint32HR() throws UnknownHostException, SocketException, ModbusException, IOException {
        int regs[] = modbusClient.ReadHoldingRegisters(address, 2);
        byte[] bytes = {
            (byte) ((regs[1] >> 8) & 0xFF),
            (byte) (regs[1] & 0xFF),
            (byte) ((regs[0] >> 8) & 0xFF),
            (byte) (regs[0] & 0xFF)
        };
        return bytes;
    }

    public byte[] readUint32IR() throws UnknownHostException, SocketException, ModbusException, IOException {
        int regs[] = modbusClient.ReadInputRegisters(address, 2);
        byte[] bytes = {
            (byte) ((regs[1] >> 8) & 0xFF),
            (byte) (regs[1] & 0xFF),
            (byte) ((regs[0] >> 8) & 0xFF),
            (byte) (regs[0] & 0xFF)
        };
        return bytes;
    }
    
    public int readUint16HR() throws UnknownHostException, SocketException, ModbusException, IOException {
        return modbusClient.ReadHoldingRegisters(address, 1)[0];
    }
    
    public int readUint16IR() throws UnknownHostException, SocketException, ModbusException, IOException {
        return modbusClient.ReadInputRegisters(address, 1)[0];
    }
  
}