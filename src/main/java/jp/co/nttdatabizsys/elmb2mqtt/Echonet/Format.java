package jp.co.nttdatabizsys.elmb2mqtt.Echonet;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Format {
    UINT8("uint8"),
    UINT16("uint16"),
    UINT32("uint32"),
    INT16("int16"),
    INT32("int32");

    /**プロトコルタイプの文字列キー */
    private String key;

    /**
     * キーでプロトコルタイプを生成する。
     * @param key 文字列キー
     */
    Format(String key) {
        this.key = key;
    }

    /**
     * 文字列からプロトコルタイプを取得する。
     * @param key プロトコルタイプの文字列キー
     * @return プロトコルタイプ
     */
    @JsonCreator
    public static Format fromString(String key) {
        return key == null ? null : Format.valueOf(key.toUpperCase());
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
            case UINT8: return "uint8";
            case UINT16: return "uint16";
            case UINT32: return "uint32";
            case INT16: return "int16";
            case INT32: return "int32";
        }
        return null;
    }

    /**
     * 文字列からプロトコルタイプを取得する。
     * @param value 文字列キー
     * @return プロトコルタイプ
     * @throws IOException IOException
     */
    public static Format forValue(String value) throws IOException {
        if (value.equals("uint8")) return UINT8;
        if (value.equals("uint16")) return UINT16;
        if (value.equals("uint32")) return UINT32;
        if (value.equals("int16")) return INT16;
        if (value.equals("int32")) return INT32;
        throw new IOException("Cannot deserialize Type");
    }
}
