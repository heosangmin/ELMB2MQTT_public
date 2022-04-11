package jp.co.nttdatabizsys.elmb2mqtt.common;

/**
 * デバイスの親クラス
 */
public class Device implements IDevice{
    /**IPアドレス */
    private String ipAddress;
    /**ポート番号 */
    private int port;
    /**トピック */
    private String topic;
    /**QoS */
    private int qos;
    /**デバイスタイプ */
    private DeviceType deviceType;
    /**プロトコルタイプ */
    private ProtocolType protocolType;

    /**
     * デバイスインスタンスを生成する。
     */
    public Device() {}
    /**
     * デバイスインスタンスを生成する。
     * @param ipAddress IPアドレス
     * @param port ポート番号
     */
    public Device(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }
    /**
     * デバイスインスタンスを生成する。
     * @param ipAddress IPアドレス
     * @param port ポート番号
     * @param topic トピック
     * @param qos QoS
     */
    public Device(String ipAddress, int port, String topic, int qos) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.topic = topic;
        this.qos = qos;
    }

    public void setDeviceType(DeviceType deviceType) {this.deviceType = deviceType;}
    public void setProtocolType(ProtocolType protocolType) {this.protocolType = protocolType;}
    public void setIpAddress(String ipAddress) {this.ipAddress = ipAddress;}
    public void setPort(int port) {this.port = port;}
    public void setTopic(String topic) {this.topic = topic;}
    public void setQos(int qos) {this.qos = qos;}
    public String set(String key, String value) {return "";}

    public DeviceType getDeviceType() {return deviceType;}
    public ProtocolType getProtocolType() {return protocolType;}
    public String getIpAddress() {return ipAddress;}
    public int getPort() {return port;}
    public String getTopic() {return topic;}
    public int getQos() {return qos;}
    public String get(String key) {return "";}
    public String getAll() {return "";}

    public boolean isValidKey(String key) {return false;}
    public boolean isValidValue(String key, String value) {return false;}
}
