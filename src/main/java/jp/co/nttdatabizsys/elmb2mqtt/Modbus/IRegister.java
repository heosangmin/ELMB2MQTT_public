package jp.co.nttdatabizsys.elmb2mqtt.Modbus;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;

/**
 * Modbusレジスタインスタンス
 */
public interface IRegister {
    /**
     * IPアドレスを設定する。
     * @param address IPアドレス
     */
    public void setAddress(int address);
    /**
     * データタイプを設定する。
     * @param dataType データタイプ
     */
    public void setDataType(DataType dataType);
    /**
     * 最終データタイプを設定する。
     * @param dataTypeFinal 最終データタイプ
     */
    public void setDataTypeFinal(String dataTypeFinal);
    /**
     * レジスタ名を設定する。
     * @param name レジスタ名
     */
    public void setName(String name);
    /**
     * コメントを設定する。
     * @param comment コメント
     */
    public void setComment(String comment);
    /**
     * Modbusクライアントを設定する。
     * @param modbusClient Modbusクライアント
     */
    public void setModbusClient(ModbusClient modbusClient);
    
    /**
     * IPアドレスを取得する。
     * @return IPアドレス
     */
    public int getAddress();
    /**
     * データタイプを取得する。
     * @return データタイプ
     */
    public DataType getDataType();
    /**
     * 最終データタイプを取得する。
     * @return 最終データタイプ
     */
    public String getDataTypeFinal();
    /**
     * レジスタ名を取得する。
     * @return レジスタ名
     */
    public String getName();
    /**
     * コメントを取得する。
     * @return コメント
     */
    public String getComment();
    /**
     * Modbusクライアントを取得する。
     * @return Modbusクライアント
     */
    public ModbusClient getModbusClient();

    /**
     * レジスタのデータを読み込む。
     * @return レジスタのデータ
     * @throws UnknownHostException UnknownHostException
     * @throws SocketException SocketException
     * @throws ModbusException ModbusException
     * @throws IOException IOException
     */
    public ByteBuffer read() throws UnknownHostException, SocketException, ModbusException, IOException;
}