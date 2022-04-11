package jp.co.nttdatabizsys.elmb2mqtt.common;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * プロトコルタイプの列挙型
 */
public enum ProtocolType {

    /**Echonet-Lite */
    ECHONET_LITE("echonet_lite"),
    /**Modbus */
    MODBUS("modbus");

    /**プロトコルタイプの文字列キー */
    private String key;

    /**
     * キーでプロトコルタイプを生成する。
     * @param key 文字列キー
     */
    ProtocolType(String key) {
        this.key = key;
    }

    /**
     * 文字列からプロトコルタイプを取得する。
     * @param key プロトコルタイプの文字列キー
     * @return プロトコルタイプ
     */
    @JsonCreator
    public static ProtocolType fromString(String key) {
        return key == null ? null : ProtocolType.valueOf(key.toUpperCase());
    }

    /**
     * プロトコルタイプのキー文字列を取得する。
     * @return プロトコルタイプの文字列キー
     */
    @JsonValue
    public String getKey() {
        return key;
    }

    /**
     * プロトコルタイプのキー文字列を取得する。
     * @return プロトコルタイプの文字列キー
     */
    public String toValue() {
        switch (this) {
            case ECHONET_LITE: return "echonet_lite";
            case MODBUS: return "modbus";
        }
        return null;
    }

    /**
     * 文字列からプロトコルタイプを取得する。
     * @param value 文字列キー
     * @return プロトコルタイプ
     * @throws IOException IOException
     */
    public static ProtocolType forValue(String value) throws IOException {
        if (value.equals("echonet_lite")) return ECHONET_LITE;
        if (value.equals("modbus")) return MODBUS;
        throw new IOException("Cannot deserialize Type");
    }
}
