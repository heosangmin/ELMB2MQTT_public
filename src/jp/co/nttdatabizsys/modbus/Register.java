package jp.co.nttdatabizsys.modbus;

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
}