package jp.co.nttdatabizsys.elmb2mqtt.common;

/**
 * デバイスインタフェース
 */
public interface IDevice {
    /**
     * デバイスタイプを設定する。
     * @param type デバイスタイプ
     */
    public void setDeviceType(DeviceType type);
    /**
     * プロトコルタイプ設定する。
     * @param type プロトコルタイプ
     */
    public void setProtocolType(ProtocolType type);
    /**
     * IPアドレスを設定する。
     * @param ipAddress IPアドレス
     */
    public void setIpAddress(String ipAddress);
    /**
     * ポート番号を設定する。
     * @param port ポート番号
     */
    public void setPort(int port);
    /**
     * トピックを設定する。
     * @param topic トピック
     */
    public void setTopic(String topic);
    /**
     * QoSを設定する。
     * @param qos QoS
     */
    public void setQos(int qos);
    /**
     * デバイスの特定アドレスにデータを書く。
     * @param key アドレス（ModBusの場合、設定情報「registerMap」のキー）
     * @param value 書くデータ
     * @return セット結果（空文字の場合セット失敗）
     */
    public String set(String key, String value);

    /**
     * デバイスタイプを取得する。
     * @return デバイスタイプ
     */
    public DeviceType getDeviceType();
    /**
     * プロトコルタイプを取得する。
     * @return プロトコルタイプ
     */
    public ProtocolType getProtocolType();
    /**
     * IPアドレスを取得する。
     * @return IPアドレス
     */
    public String getIpAddress();
    /**
     * ポート番号を取得する。
     * @return ポート番号
     */
    public int getPort();
    /**
     * トピックを取得する。
     * @return トピック
     */
    public String getTopic();
    /**
     * QoSを取得する。
     * @return QoS
     */
    public int getQos();
    /**
     * デバイスの全アドレスに対しGetを実行する。
     * @return 取得した結果データ（普通はJSONの文字列）
     */
    public String getAll();
    /**
     * 特定アドレスのデータを取得する。
     * @param key アドレス
     * @return 取得した結果データ
     */
    public String get(String key);

    /**
     * 有効なアドレスかを検証する
     * @param key アドレス
     * @return 検証結果
     */
    public boolean isValidKey(String key);
    /**
     * セットする値を検証する。
     * @param value セットするデータ
     * @return 検証結果
     */
    public boolean isValidValue(String key, String value);
}
