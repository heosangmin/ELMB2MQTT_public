package jp.co.nttdatabizsys.modbus;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;

public interface IRegister {
    public void setAddress(int address);
    public void setDataType(DataType dataType);
    public void setDataTypeFinal(String dataTypeFinal);
    public void setName(String name);
    public void setComment(String comment);
    public void setModbusClient(ModbusClient modebusClient);
    
    public int getAddress();
    public DataType getDataType();
    public String getDataTypeFinal();
    public String getName();
    public String getComment();
    public ModbusClient getModbusClient();

    public ByteBuffer read() throws UnknownHostException, SocketException, ModbusException, IOException;
}