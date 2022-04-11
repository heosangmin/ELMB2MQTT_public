package jp.co.nttdatabizsys.elmb2mqtt.Modbus;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;
import jp.co.nttdatabizsys.elmb2mqtt.common.Device;
import jp.co.nttdatabizsys.elmb2mqtt.util.Logger;

/**
 * Modbusデバイス
 */
public class ModbusDevice extends Device {

    /**Modbusクライアント（EasyModbus） */
    private ModbusClient modbusClient;

    /**レジスタバップ（レジスタ集） */
    private LinkedHashMap<String, Register> registerMap;

    /**コンソールロギングユティリティ */
    private Logger logger = Logger.getLogger();

    /**
     * Modbusデバイスを生成する。
     */
    public ModbusDevice() {}
    /**
     * Modbusデバイスを生成する。
     * @param ipAddress IPアドレス
     * @param port ポート番号
     */
    public ModbusDevice(String ipAddress, int port) {
        super(ipAddress, port);
    }

    /**
     * デバイス情報を文字列で返却する。
     * @return デバイス情報
     */
    @Override
    public String toString() {
        return String.format("[Modbus Device][%s] ipAddress:%s, port:%d, topic:%s, qos:%d", getDeviceType(), getIpAddress(), getPort(), getTopic(), getQos());
    }

    /**
     * modbusClient(EasyModbus)でデバイスに接続する。
     * @throws UnknownHostException UnknownHostException
     * @throws IOException IOException
     */
    public void connect() throws UnknownHostException, IOException {
        if (modbusClient == null) {
            modbusClient = new ModbusClient(getIpAddress(), getPort());
        }
        
        if (!modbusClient.isConnected()) {
            modbusClient.Connect();
            modbusClient.setConnectionTimeout(30000);
            registerMap.forEach((address, register) -> {
                register.setModbusClient(modbusClient);
            });
        }
    }

    /**
     * デバイスとの接続を切る。
     * @throws IOException IOException
     */
    public void disconnect() throws IOException {
        if (modbusClient.isConnected()) {
            modbusClient.Disconnect();
        }
    }

    /**
     * レジスタマップを設定する。（設定ファイルから自動で読み込むため、直接呼び出しはしない）
     * @param registerMap レジスタマップ
     */
    public void setRegisterMap(LinkedHashMap<String, Register> registerMap) {this.registerMap = registerMap;}
    /**
     * レジスタマップを取得する。
     * @return レジスタマップ
     */
    public LinkedHashMap<String, Register> getRegisterMap() {return this.registerMap;}

    /**
     * レジスタアドレスを検証する。<br>
     * 3または4で始まる4~5桁数字かを確認する。
     * @return 検証結果
     * @param key レジスタアドレス
     */
    public boolean isValidKey(String key) {
        Pattern pattern = Pattern.compile("^[34][0-9]{4,5}$");
        Matcher matcher = pattern.matcher(key);
        if (!matcher.matches() || registerMap.get(key) == null) {
            return false;
        }
        return true;
    }

    /**
     * レジスタにセットするデータを検証する。<br>
     * 数字、ドット（.）、ハイフン（-）、コロン（:）構成を確認する。
     * @return 検証結果
     * @param value レジスタにセットするデータ
     */
    public boolean isValidValue(String key, String value) {
        Pattern pattern = Pattern.compile("^[0-9\\.\\-:]+$");
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }

    /**
     * レジスタの特定アドレスにデータを書き込む。<br>
     * <p>
     * 3で始まるレジスタは「インプットレジスタ」、4で始まるレジスタは「ホールディングレジスタ」
     * </p>
     * <p>
     * 「インプットレジスタ」の場合、読み込みのみ可能<br>
     * 「ホールディングレジスタ」の場合、書き込むと呼び出し両方可能
     * </p>
     * <p>
     * 書き込み時にレジスタのデータタイプに合わせて呼び出すメソッドが異なる。
     * </p>
     * 処理可能なデータタイプは、
     * <ul>
     * <li>Int16, Uint16, Uint32, Boolean: Int型として書き込む</li>
     * <li>Float32: Float型として書き込む</li>
     * <li>Uint64: Long型として書き込む</li>
     * </ul>
     * その他の場合、エラー
     * @return Set結果
     * @param key レジスタアドレス
     * @param value レジスタにセットするデータ
     */
    public String set(String key, String value) {

        if (!modbusClient.isConnected()) {
            logger.log("modbus client is not connected. reconnecting...", true);
            try {
                modbusClient.Connect();
            } catch (IOException e) {
                e.printStackTrace();
                logger.log("modbus client connections failed", true);
                return "";
            }
        }

        String result = "";

        if (key.startsWith("3")) {
            logger.log(String.format("[error] cannot set a value in input register %s", key), true);
            return null;
        }

        if (key.startsWith("4")) {
            try {
                Register register = registerMap.get(key);
                if (register.getDataType().equals(DataType.Int16)
                    || register.getDataType().equals(DataType.Uint16)
                    || register.getDataType().equals(DataType.Uint32)
                    || register.getDataType().equals(DataType.Boolean)
                ) {
                    register.write(Integer.parseInt(value));
                } else if (register.getDataType().equals(DataType.Float32)) {
                    register.write(Float.parseFloat(value));
                } else if (register.getDataType().equals(DataType.Uint64)) {
                    register.write(Long.parseLong(value));
                } else {
                    logger.log(String.format("[error] writing data type %s is not supported.", register.getDataType()), true);
                    return null;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return null;
            } catch (ModbusException | IOException e) {
                try {
                    modbusClient.Disconnect();
                    logger.log("[error] reconnecting...", true);
                    modbusClient.Connect();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return null;
            }
        }

        return result;
    }

    /**
     * レジスタからデータを読み込む。
     * <ul>
     * <li>time: 取得時刻</li>
     * <li>value: 取得データ</li>
     * </ul>
     * @param key レジスタアドレス
     * @return 読み込み結果（JSONに変換するため、MAPに格納している）
     */
    private HashMap<String, String> read(String key) {
        Register register = registerMap.get(key);
        ByteBuffer resultBuffer;
        String resultString;
        HashMap<String, String> resultMap = new HashMap<>();
        try {
            if (key.startsWith("3")) {
                resultBuffer = register.readInputRegister();
            } else {
                resultBuffer = register.readHoldingRegister();
            }
            resultString = new String(convertFinalData(register, resultBuffer));
            resultMap = createResultMap(register.getMqttId(), resultString);
            logger.log(String.format("[ModB][get] %s (%s) : %s", key, register.getName(), resultString), true);
        } catch (ModbusException | IOException e) {
            e.printStackTrace();
            try {
                modbusClient.Disconnect();
                logger.log("[error] reconnecting...", true);
                modbusClient.Connect();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return resultMap;
    }

    /**
     * レジスタからデータを読み込む。読み込み結果はJSON型の文字列で変換する。
     * @return 読み込み結果リストのJSON文字列
     * @param key レジスタアドレス
     */
    public String get(String key) {

        if (!modbusClient.isConnected()) {
            logger.log("modbus client is not connected. reconnecting...", true);
            try {
                modbusClient.Connect();
            } catch (IOException e) {
                e.printStackTrace();
                logger.log("modbus client connections failed", true);
                return "";
            }
        }

        String resultJson = "";
        ArrayList<HashMap<String, String>> resultList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        Register register = registerMap.get(key);
        if (register.getDataTypeFinal().equals("bitmask") && register.getDataType().equals(DataType.Uint16)) {
            short bitmaskValue = Short.parseShort(read(key).get("value"));
            for (int i = 0; i < 16; i++) {
                short value = (short) ((bitmaskValue >>> i) & 1);
                resultList.add(createResultMap(register.getMqttId() + "/" + Integer.toString(i), Short.toString(value)));
            }
        } else {
            resultList.add(read(key));
        }

        try {
            resultJson = mapper.writeValueAsString(resultList);
        } catch (JsonProcessingException e) {
            logger.log(String.format("[ModB][get] %s", "json processing error") ,true);
            e.printStackTrace();
        }
        return resultJson;
    }

    /**
     * デバイスの全レジスタからデータを取得しJSON型の文字列で変換する。
     * @return 読み込み結果リストのJSON文字列
     */
    public String getAll() {

        if (!modbusClient.isConnected()) {
            logger.log("modbus client is not connected. reconnecting...", true);
            try {
                modbusClient.Connect();
            } catch (IOException e) {
                e.printStackTrace();
                logger.log("modbus client connections failed", true);
                return "";
            }
        }

        String resultJson = "";
        
        ArrayList<HashMap<String, String>> resultList = new ArrayList<>();

        Iterator<String> keys = registerMap.keySet().iterator();
        while(keys.hasNext()) {
            String key = keys.next();
            
            // レジスタの最終型が2バイト（１レジスタ）bitmaskの場合、
            // 各ビットごとに分けて結果MAPを作成する。
            Register register = registerMap.get(key);

            if (register.getMqttId().equals("")) {
                continue;
            }

            if (register.getDataTypeFinal().equals("bitmask") && register.getDataType().equals(DataType.Uint16)) {
                short bitmaskValue = Short.parseShort(read(key).get("value"));
                for (int i = 0; i < 16; i++) {
                    short value = (short) ((bitmaskValue >>> i) & 1);
                    resultList.add(createResultMap(register.getMqttId() + "/" + Integer.toString(i), Short.toString(value)));
                }
            } else {
                resultList.add(read(key));
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            // resultJson = mapper.writeValueAsString(resultMap);
            resultJson = mapper.writeValueAsString(resultList);
        } catch (JsonProcessingException e) {
            logger.log(String.format("[ModB][get] %s", "json processing error") ,true);
            e.printStackTrace();
        }

        return resultJson;
    }

    /**
     * レジスタ取得結果のMAPを作成する。
     * @param name レジスタ名（MQTTID）
     * @param resultValue レジスタから取得した値の文字列
     * @return 結果MAP
     */
    private HashMap<String, String> createResultMap(String name, String resultValue) {
        HashMap<String, String> resultMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:'00'X");
        OffsetDateTime odt = OffsetDateTime.now();
        resultMap.put("name", name);
        resultMap.put("value", resultValue);
        resultMap.put("time", odt.format(formatter));
        return resultMap;
    }

    /**
     * （Enapter Electrolyser用）<br>
     * レジスタの最終データタイプに変換する。<br>
     * 詳しくは仕様を確認する。<br>
     * @param register レジスタ
     * @param buf 読み込み結果（バイトバッファ）
     * @return 読み込み結果（バイト配列）
     */
    private byte[] convertFinalData(Register register, ByteBuffer buf) {
        String dataTypeFinal = register.getDataTypeFinal();
        String payload;
        switch (dataTypeFinal) {
            case "ipv4":
                payload = convertIpv4(buf);    
                break;
            case "ChassisSerialNumber":
                payload = convertChassisSerialNumber(buf);
                break;
            case "ascii":
                payload = new String(buf.array());
                break;
            case "uuid":
                payload = convertUUID(buf);
                break;
            case "hex":
                payload = convertHex(buf);
                break;
            case "numDotNum":
                payload = convertNumDotNum(buf);
                break;
            case "3octets":
                payload = convert3octets(buf);
                break;
            case "Events":
                payload = convertEvents(buf);
                break;
            default: // raw
                payload = convertRaw(buf, register.getDataType(), register.getMultiple());
                break;
        }
        return payload.getBytes();
    }

    /**
     * 読み込み結果をレジスタタイプに合わせて変換し返却する。
     * <ul>
     * <li>Boolean, Uint16: 符号なしIntに変換</li>
     * <li>Int16: Shortに変換</li>
     * <li>Float32: Floatに変換</li>
     * <li>Uint32: 符号なしIntに変換</li>
     * <li>Uint64: 符号なしLongに変換</li>
     * <li>その他：未対応</li>
     * </ul>
     * @param buf 読み込み結果（バイトバッファ）
     * @param dataType レジスタのデータタイプ
     * @return 読み込み結果（文字列）
     */
    private String convertRaw(ByteBuffer buf, DataType dataType, float multiple) {
        String payload = "";
        if (dataType.equals(DataType.Boolean)) {
            payload = buf.getShort() == 0 ? "0" : "1";
        } else if (dataType.equals(DataType.Uint16)) {
            if (multiple != 0) {
                payload = Float.toString(multiple * (float)Short.toUnsignedInt(buf.getShort()));
            } else {
                payload = Integer.toUnsignedString(Short.toUnsignedInt(buf.getShort()));
            }
        } else if (dataType.equals(DataType.Int16)) {
            if (multiple != 0) {
                payload = Float.toString(multiple * (float)buf.getShort());
            } else {
                payload = Short.toString(buf.getShort());
            }
        } else if (dataType.equals(DataType.Float32)) {
            payload = Float.toString(buf.getFloat());
        } else if (dataType.equals(DataType.Int32)) {
            if (multiple != 0) {
                payload = Float.toString(multiple * (float)buf.getInt());
            } else {
                payload = Integer.toString(buf.getInt());
            }
        } else if (dataType.equals(DataType.Uint32)) {
            if (multiple != 0) {
                payload = Float.toString(multiple * Integer.toUnsignedLong(buf.getInt()));
            } else {
                payload = Integer.toUnsignedString(buf.getInt());
            }
        } else if (dataType.equals(DataType.Uint64)) {
            payload = Long.toUnsignedString(buf.getLong());
        } else {
            logger.log("[error] unknown data type", true);
        }
        return payload;
    }

    /**
     * （Enapter Electrolyser用）<br>
     * バイトを3octets（例　FF:FF:FF）に変換する。
     * @param buf 読み込み結果（バイトバッファ）
     * @return 3octetsに変換した読み込み結果
     */
    private String convert3octets(ByteBuffer buf) {
        String payload = "";
        byte[] bytes = buf.array();
        payload = String.format("%X:%X:%X",bytes[1],bytes[2],bytes[3]);
        return payload;
    }

    /**
     * （Enapter Electrolyser用）<br>
     * バイトを実数（例　1.5）に変換する。
     * @param buf 読み込み結果（バイトバッファ）
     * @return 実数に変換した読み込み結果
     */
    private String convertNumDotNum(ByteBuffer buf) {
        String payload = "";
        byte[] bytes = buf.array();
        payload = String.format("%d.%d",bytes[0],bytes[1]);
        return payload;
    }

    /**
     * （Enapter Electrolyser用）<br>
     * バイトを16進数に変換する。
     * @param buf 読み込み結果（バイトバッファ）
     * @return 16進数に変換した読み込み結果
     */
    private String convertHex(ByteBuffer buf) {
        byte[] bytes = buf.array();
        String payload = "0x";
        for (byte b : bytes) {
            payload = payload + String.format("%X", b);
        }
        return payload;
    }

    /**
     * （Enapter Electrolyser用）<br>
     * バイトをUUIDに変換する。
     * @param buf 読み込み結果（バイトバッファ）
     * @return UUIDに変換した読み込み結果
     */
    private String convertUUID(ByteBuffer buf) {
        byte[] bytes = buf.array();
        String payload = "";
        String format = "%X%X%X%X-%X%X-%X%X-%X%X-%X%X%X%X%X%X";
        payload = String.format(
            format,
            bytes[0],bytes[1],bytes[2],bytes[3],
            bytes[4],bytes[5],
            bytes[6],bytes[7],
            bytes[8],bytes[9],
            bytes[10],bytes[11],bytes[12],bytes[13],bytes[14],bytes[15]
        );
        return payload;
    }

    /**
     * （Enapter Electrolyser用）<br>
     * バイトをIPv4型に変換する。
     * @param buf 読み込み結果（バイトバッファ）
     * @return IPv4に変換した読み込み結果
     */
    private String convertIpv4(ByteBuffer buf) {
        byte[] bytes = buf.array();
        String val1 = Integer.toString(Byte.toUnsignedInt(bytes[0]));
        String val2 = Integer.toString(Byte.toUnsignedInt(bytes[1]));
        String val3 = Integer.toString(Byte.toUnsignedInt(bytes[2]));
        String val4 = Integer.toString(Byte.toUnsignedInt(bytes[3]));
        return String.format("%s.%s.%s.%s", val1, val2, val3, val4);
    }

    /**
     * （Enapter Electrolyser用）<br>
     * バイトをEnapterのChassisSerialNumber型に変換する。
     * @param buf 読み込み結果（バイトバッファ）
     * @return ChassisSerialNumberに変換した読み込み結果
     */
    private String convertChassisSerialNumber(ByteBuffer buf) {
        // ChassisSerialNumberは、64bitであることを前提とする。
        long v = buf.getLong();
        if (v <= 0) {
            return "";
        }

        char[] binaries = Long.toBinaryString(v).toCharArray();
        int size = binaries.length;
        int offset = 64 - size;
        
        try {
            String productUnicode = new String(Arrays.copyOfRange(binaries, 0, 11 - offset)); // ~10 bits
            String yearMonth = new String(Arrays.copyOfRange(binaries, 11 - offset, 22 - offset)); // 11 bits
            String day = new String(Arrays.copyOfRange(binaries, 22 - offset, 27 - offset)); // 5 bits
            String chassisNumber = new String(Arrays.copyOfRange(binaries, 27 - offset, 51 - offset)); // 24 bits
            String order = new String(Arrays.copyOfRange(binaries, 51 - offset, 56 - offset)); // 5 bits
            String site = new String(Arrays.copyOfRange(binaries, 56 - offset, 64 - offset)); // 8 bits

            int iProductUnicode = Integer.parseInt(productUnicode, 2);
            int iProductUnicode1 = iProductUnicode % 32 + 64;
            int iProductUnicode2 = iProductUnicode / 32 + 64;
            int iYearMonth = Integer.parseInt(yearMonth, 2);
            int iYear = iYearMonth / 12;
            int iMonth = iYearMonth % 12;
            int iDay = Integer.parseInt(day, 2);
            int iChassisNumber = Integer.parseInt(chassisNumber, 2);
            int iOrder = Integer.parseInt(order, 2) + 64;
            site = (Integer.parseInt(site, 2) == 0) ? "PI" : "SA";
    
            return String.format("%c%c%02d%02d%d%d%c%s", iProductUnicode1, iProductUnicode2, iYear, iMonth, iDay, iChassisNumber, iOrder, site);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * （Enapter Electrolyser用）<br>
     * イベント配列を変換する。
     * 頭の１レジスタはイベントコード数を表す。
     * イベントコードの数分イベントコード（16進数表現 ex:0x118A）を取得する。
     * @param buf 読み込み結果（バイトバッファ）
     * @return 16進数に変換した読み込み結果
     */
    private String convertEvents(ByteBuffer buf) {
        byte[] bytes = buf.array();
        String payload = "";
        List<String> codeList = new ArrayList<>();
        short count = (short)((bytes[0] << 8) | bytes[1]) ;
        if (count > 0) {
            for (short i = 0; i < count; i += 2) {
                codeList.add(String.format("0x%X%X", bytes[i+2], bytes[i+3]));
            }
            payload = String.join(",", codeList);
        }
        return payload;
    }
    
}
