package jp.co.nttdatabizsys.elmb2mqtt.common;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * デバイスタイプの列挙型
 */
public enum DeviceType {

    /**EVタイプ（Beta） */
    EV("ev"),
    /**東芝H2Rex */
    TOSHIBA_H2REX("toshiba_h2rex"),
    /**Enapter Electrolyser */
    ENAPTER_ELECTROLYSER("enapter_electrolyser");

    /**デバイスタイプの文字列データ */
    private String key;

    /**
     * キーでデバイスタイプを生成する。
     * @param key 文字列キー
     */
    DeviceType(String key) {
        this.key = key;
    }

    /**
     * 文字列からデバイスタイプを取得する。
     * @param key デバイスタイプの文字列キー
     * @return デバイスタイプ
     */
    @JsonCreator
    public static DeviceType fromString(String key) {
        return key == null ? null : DeviceType.valueOf(key.toUpperCase());
    }

    /**
     * デバイスタイプのキー文字列を取得する。
     * @return デバイスタイプの文字列キー
     */
    @JsonValue
    public String getKey() {
        return key;
    }

    /**
     * デバイスタイプのキー文字列を取得する。
     * @return デバイスタイプの文字列キー
     */
    public String toValue() {
        switch (this) {
            case EV: return "ev";
            case TOSHIBA_H2REX: return "toshiba_h2rex";
            case ENAPTER_ELECTROLYSER: return "enapter_electrolyser";
        }
        return null;
    }

    /**
     * 文字列からデバイスタイプを取得する。
     * @param value 文字列キー
     * @return デバイスタイプ
     * @throws IOException IOException
     */
    public static DeviceType forValue(String value) throws IOException {
        if (value.equals("ev")) return EV;
        if (value.equals("toshiba_h2rex")) return TOSHIBA_H2REX;
        if (value.equals("enapter_electrolyser")) return ENAPTER_ELECTROLYSER;
        throw new IOException("Cannot deserialize Type");
    }
}