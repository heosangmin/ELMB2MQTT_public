package jp.co.nttdatabizsys.elmb2mqtt.Echonet;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.nttdatabizsys.elmb2mqtt.common.Device;
import jp.co.nttdatabizsys.elmb2mqtt.util.Logger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EchonetDevice extends Device{
    /** UdpSocket */
    private UdpSocket socket;

    /** EOJ */
    private String EOJ;

    /** EPCマップ */
    private LinkedHashMap<String, EPC> epcMap;

    /** EOJソース */
    private final String SEOJ_DEFAULT = "05FF01";

    /** セットESV */
    private final String ESV_SET = "61";

    /** ゲットESV */
    private final String ESV_GET = "62";

    /** Logger */
    private Logger logger = Logger.getLogger();

    /** コンストラクター１ */
    public EchonetDevice(){
        this.socket = UdpSocket.getUdpSocket();
    }
    
    /** コンストラクター２ */
    public EchonetDevice(String ipAddress, int port, String topic, int qos, String EOJ) {
        super(ipAddress, port, topic, qos);
        this.EOJ = EOJ.replace("0x", "");
        this.socket = UdpSocket.getUdpSocket();
    }

    @Override
    public String toString() {
        return String.format("[Echonet Device][%s] ipAddress:%s, port:%d, topic:%s, qos:%d", getDeviceType(), getIpAddress(), getPort(), getTopic(), getQos());
    }

    /**
     * EOJを取得する。
     * @return EOJ
     */
    public String getEOJ() {return EOJ;}

    /**
     * EOJをセットする。
     * @param EOJ
     */
    public void setEOJ(String EOJ) {this.EOJ = EOJ.replace("0x", "");}

    /**
     * UDPソケットを取得する。
     * @return UDPソケット
     */
    public UdpSocket getSocket() {return socket;}

    /**
     * デバイスのEPCマップを取得する。
     * @return
     */
    public LinkedHashMap<String, EPC> getEpcMap() {return epcMap;}
    
    /**
     * デバイスのEPCマップをセットする。
     * @param epcMap
     */
    public void setEpcMap(LinkedHashMap<String, EPC> epcMap) {this.epcMap = epcMap;}

    /**
     * EPCに対し現在地を取得しJSON文字列として返却する。
     * @param EPC EPC文字列
     * @return 取得結果リストのJSON文字列
     */
    public String get(String EPC) {
        ArrayList<HashMap<String, String>> resultList = new ArrayList<>();
        if (!epcMap.get(EPC).getMqttId().equals("")) {
            String EDT = send(1, ESV_GET, EPC, "");
            if (EDT != null) {
                EDT = parseNumber(epcMap.get(EPC), EDT);
                resultList.add(createResultMap(epcMap.get(EPC).getMqttId(), EDT));
            }
        }
        return createResultListJson(resultList);
    }

    /**
     * デバイスの全EPCに対し現在地を取得しJSON文字列として返却する。
     * @return 取得結果リストのJSON文字列
     */
    public String getAll() {
        int iTID = 1; // ELメッセージのTransaction ID
        String resultJson = "";
        ArrayList<HashMap<String, String>> resultList = new ArrayList<>();
        
        // 全EPC取得、結果を作る。
        Iterator<String> keys = epcMap.keySet().iterator();
        while(keys.hasNext()) {
            String EPC = keys.next();
            if (!epcMap.get(EPC).getMqttId().equals("")) {
                String EDT = send(iTID++, ESV_GET, EPC, "");
                if (EDT != null) {
                    EDT = parseNumber(epcMap.get(EPC), EDT);
                    resultList.add(createResultMap(epcMap.get(EPC).getMqttId(), EDT));
                }
            }
        }
        
        resultJson = createResultListJson(resultList);

        return resultJson;
    }
    
    /**
     * EPCに対しEDTをセットする。
     * @param EPC EPC
     * @param EDT EDT
     */
    public String set(String EPC, String EDT) {
        EDT = toHexString(epcMap.get(EPC), EDT);
        return send(1, ESV_SET, EPC, EDT);
    }

    /**
     * EchonetデバイスへUDPソケットを送信する。
     * @param iTID トランザクションID番号
     * @param ESV ESV(set or get)
     * @param EPC EPC
     * @param EDT EDT(set時)
     * @return 受信データ(EDT)
     */
    public String send(int iTID, String ESV, String EPC, String EDT) {
        String resultStr = null;
        String ipAddress = getIpAddress();
        int port = getPort();
        String TID = String.format("%04d", iTID);
        String EOJ = getEOJ();
        String ESV_NAME = ESV.equals("62") ? "get" : "set";
        String ESV_RES = ESV.equals("62") ? "72" : "71";
        EPC = EPC.replace("0x", "");
        EDT = EDT.replace("0x", "");
        String PDC = String.format("%02X", EDT.length() / 2);
        String format = "[EL  ][%s][%15s] %s";
        
        // EchonetLiteメッセージ作成
        ELMessage msg = new ELMessage(TID,SEOJ_DEFAULT,EOJ,ESV,"01",EPC,PDC,EDT);

        // メッセージ送信と受信
        try {
            logger.log(String.format(format, ESV_NAME, ipAddress, msg.getSeparatedString()), true);
            socket.send(msg.getBytes(), ipAddress, port);
            Result result = socket.receive();
            
            // 受信した結果メッセージ
            ELMessage resultMsg = new ELMessage(result.getData());
            EDT = resultMsg.getEDTString();
            logger.log(String.format(format, ESV_NAME, ipAddress, resultMsg.getSeparatedString()), true);

            // get(62)にはget_res(72)、set(61)にはset_res(71)が正常
            if (resultMsg.getESVString().equals(ESV_RES)) {
                resultStr = EDT;
            }

        } catch (SocketTimeoutException e) {
            logger.log(String.format(format, ESV_NAME, ipAddress, "socket error: Receive timed out"), true);
        } catch (IOException e) {
            logger.log(String.format(format, ESV_NAME, ipAddress, "socket error") ,true);
            e.printStackTrace();
        }
        return resultStr;
    }

    /**
     * 受信したEDTを最終型に変換する。
     * @param epc
     * @param edt
     * @return 変換済みデータ
     */
    private String parseNumber(EPC epc, String edt) {
        Type type = epc.getType();
        Format format = epc.getFormat();
        float multiple = epc.getMultiple();

        if (type.equals(Type.NUMBER)) {
            switch(format) {
                case INT16:
                    short sedt = Short.parseShort(edt, 16);
                    return multiple != 0 ? Float.toString(sedt * multiple) : Integer.toString(sedt);
                case INT32:
                    int iedt = Integer.parseInt(edt, 16);
                    return multiple != 0 ? Float.toString(iedt * multiple) : Integer.toString(iedt);
                default: // uint8,16,32
                    long ledt = Long.parseLong(edt, 16);
                    return multiple != 0 ? Float.toString(ledt * multiple) : Long.toString(ledt);
            }
        } else if (type.equals(Type.STATE)) {
            short sedt = Short.parseShort(edt, 16);
            return multiple != 0 ? Float.toString(sedt * multiple) : Short.toString(sedt);
        } else {
            return "0x" + edt;
        }
    }

    /**
     * setするEDTを16進数に変換する。
     * @param epc
     * @param edt
     * @return 16進数の文字列
     */
    private String toHexString(EPC epc, String edt) { // edt have to be a number
        Type type = epc.getType();
        Format format = epc.getFormat();
        float multiple = epc.getMultiple();
        int size = epc.getSize();
        String hexEDT = "";
        edt = edt.replace("0x", "");
        if (type.equals(Type.NUMBER)) {
            long ledt = Long.parseLong(edt);
            hexEDT = multiple != 0 ? Long.toHexString((long) (ledt / multiple)) : Long.toHexString(ledt);
            if (hexEDT.length() % 2 != 0) {
                hexEDT = "0" + hexEDT;
            }
            switch (format) {
                case INT16:
                case UINT16:
                    for (int i = 0; i < 2 && (hexEDT.length() / 2 < 2); i++) {
                        hexEDT = "00" + hexEDT;
                    }
                    break;
                case INT32:
                case UINT32:
                    for (int i = 0; i < 4 && (hexEDT.length() / 2 < 4); i++) {
                        hexEDT = "00" + hexEDT;
                    }
                    break;
                default:
                    break;
            }
        } else {
            if (edt.length() % 2 != 0) {
                edt = "0" + edt;
            }
            for (int i = 0; i < size && (edt.length() / 2 < size); i++) {
                edt = "00" + edt;
            }
            hexEDT = edt;
        }
        return hexEDT;
    }

    /**
     * EPC取得結果のMAPを作成する。
     * @param name EPC名（MQTTID）
     * @param resultValue EPCから取得した値の文字列
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

    private String createResultListJson(ArrayList<HashMap<String, String>> resultList) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(resultList);
        } catch (JsonProcessingException e) {
            logger.log(String.format("[EL  ][get] %s", "json processing error") ,true);
            e.printStackTrace();
            return "";
        }
    }

    /**
     * EPCが16進数形(0x??)で存在するかをチェック
     */
    public boolean isValidKey(String EPC) {
        return isHexString(EPC) && epcMap.containsKey(EPC);
    }

    /**
     * EDTが正しい値かをチェック
     */
    public boolean isValidValue(String EPC, String EDT) {
        EPC epc = epcMap.get(EPC);
        if (epc.getType().equals(Type.NUMBER)) {
            Pattern pattern = Pattern.compile("^-?[0-9]+(\\.[0-9]+)?$");
            Matcher matcher = pattern.matcher(EDT);
            return matcher.matches();
        } else {
            return isHexString(EDT);
        }
    }

    /**
     * 16進数の文字列かを確認する。
     * @param str
     * @return
     */
    private boolean isHexString(String str) {
        if (str.length() % 2 > 0) {
            return false;
        }
        String pattern = "^0x[0-9a-fA-F]+$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(str);
        return m.find();
    }
}
