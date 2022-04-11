package jp.co.nttdatabizsys.elmb2mqtt.Modbus;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;
import de.re.easymodbus.modbusclient.ModbusClient.RegisterOrder;

/**
 * Modbusレジスタ
 */
public class Register{
    /**アドレス */
    public int address;
    /**データタイプ */
    public DataType dataType;
    /**最終データタイプ */
    public String dataTypeFinal = "raw";
    /**レジスタ名 */
    public String name;
    /**コメント */
    public String comment;
    /**定数 */
    public float multiple;
    /** MQTT ID */
    public String mqttId;
    /**Modbusクライアント */
    protected ModbusClient modbusClient;

    /**
     * IPアドレスを設定する。
     * @param address IPアドレス
     */
    public void setAddress(int address) {this.address = address;}
    /**
     * データタイプを設定する。
     * @param dataType データタイプ
     */
    public void setDataType(DataType dataType) {this.dataType = dataType;}
    /**
     * 最終データタイプを設定する。
     * @param dataTypeFinal 最終データタイプ
     */
    public void setDataTypeFinal(String dataTypeFinal) {this.dataTypeFinal = dataTypeFinal;}
    /**
     * レジスタ名を設定する。
     * @param name レジスタ名
     */
    public void setName(String name) {this.name = name;}
    /**
     * コメントを設定する。
     * @param comment コメント
     */
    public void setComment(String comment) {this.comment = comment;}
    /**
     * 定数を設定する。
     * @param multiple 定数
     */
    public void setMultiple(float multiple) {this.multiple = multiple;}
    /**
     * MQTT IDを設定する。
     * @param mqttId MQTT ID
     */
    public void setMqttId(String mqttId) {this.mqttId = mqttId;}
    /**
     * Modbusクライアントを設定する。
     * @param modbusClient Modbusクライアント
     */
    public void setModbusClient(ModbusClient modbusClient) {this.modbusClient = modbusClient;}

    /**
     * IPアドレスを取得する。
     * @return IPアドレス
     */
    public int getAddress() {return this.address;}
    /**
     * データタイプを取得する。
     * @return データタイプ
     */
    public DataType getDataType() {return this.dataType;}
    /**
     * 最終データタイプを取得する。
     * @return 最終データタイプ
     */
    public String getDataTypeFinal() {return this.dataTypeFinal;}
    /**
     * レジスタ名を取得する。
     * @return レジスタ名
     */
    public String getName() {return this.name;}
    /**
     * コメントを取得する。
     * @return コメント
     */
    public String getComment() {return this.comment;}
    /**
     * Modbusクライアントを取得する。
     * @return Modbusクライアント
     */
    public ModbusClient getModbusClient() {return modbusClient;}
    /**
     * 定数を取得する。
     * @return 定数
     */
    public float getMultiple() {return multiple;}
    /**
     * MQTT IDを取得する。
     * @return MQTT ID
     */
    public String getMqttId() {return mqttId;}
    /**
     * （ホールディングレジスタ）レジスタのデータを読み込む。
     * @return 読み込み結果
     * @throws UnknownHostException UnknownHostException
     * @throws SocketException SocketException
     * @throws ModbusException ModbusException
     * @throws IOException IOException
     */
    public ByteBuffer readHoldingRegister() throws UnknownHostException, SocketException, ModbusException, IOException, SocketTimeoutException {
        int[] regs = modbusClient.ReadHoldingRegisters(address, dataType.registers);
        return read(regs);
    }

    /**
     * （インプットレジスタ）レジスタのデータを読み込む。
     * @return 読み込み結果
     * @throws UnknownHostException UnknownHostException
     * @throws SocketException SocketException
     * @throws ModbusException ModbusException
     * @throws IOException IOException
     */
    public ByteBuffer readInputRegister() throws UnknownHostException, SocketException, ModbusException, IOException, SocketTimeoutException {
        int[] regs = modbusClient.ReadInputRegisters(address, dataType.registers);
        return read(regs);
    }

    /**
     * レジスタから読み込んだバイト配列結果をバイトバッファで返却する。
     * @param regs レジスタから読み込んだバイト配列結果
     * @return バイトバッファ結果
     */
    private ByteBuffer read(int[] regs) {
        ByteBuffer buf = ByteBuffer.allocate(regs.length * 2);
        for (int i = 0; i < regs.length; i++) {
            buf.put((byte) ((regs[i] >>> 8) & 0xFF));
            buf.put((byte) (regs[i] & 0xFF));
        }
        buf.position(0);
        return buf;
    }

    /**
     * レジスタにInt型のデータを書き込む。
     * @param value 書き込みデータ
     * @throws UnknownHostException UnknownHostException
     * @throws SocketException SocketException
     * @throws ModbusException ModbusException
     * @throws IOException IOException
     */
    public void write(int value) throws UnknownHostException, SocketException, ModbusException, IOException, SocketTimeoutException {
        if (dataType.registers  == 1) {
            modbusClient.WriteSingleRegister(address, value);
        } else if (dataType.registers  == 2) {
            int reg0 = (int)((value & 0xFFFF0000) >>> 16);
            int reg1 = (int)(value & 0xFFFF);
            int[] regs = {reg0,reg1};
            modbusClient.WriteMultipleRegisters(address, regs);
        }
    }

    /**
     * レジスタにFloat型のデータを書き込む。
     * @param value 書き込みデータ
     * @throws UnknownHostException UnknownHostException
     * @throws SocketException SocketException
     * @throws ModbusException ModbusException
     * @throws IOException IOException
     */
    public void write(float value) throws UnknownHostException, SocketException, ModbusException, IOException, SocketTimeoutException {
        int[] regs = ModbusClient.ConvertFloatToTwoRegisters(value, RegisterOrder.HighLow);
        modbusClient.WriteMultipleRegisters(address, regs);
    }

    /**
     * レジスタにLong型のデータを書き込む。
     * @param value 書き込みデータ
     * @throws UnknownHostException UnknownHostException
     * @throws SocketException SocketException
     * @throws ModbusException ModbusException
     * @throws IOException IOException
     */
    public void write(long value) throws UnknownHostException, SocketException, ModbusException, IOException, SocketTimeoutException {
        // Uint64
        int reg0 = (int)((value & 0xFFFF000000000000L) >>> 48);
        int reg1 = (int)((value & 0xFFFF00000000L) >>> 32);
        int reg2 = (int)((value & 0xFFFF0000) >>> 16);
        int reg3 = (int)(value & 0xFFFF);
        int[] regs = {reg0,reg1,reg2,reg3};
        modbusClient.WriteMultipleRegisters(address, regs);
    }
}