package jp.co.nttdatabizsys.elmb2mqtt.Echonet;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Type {
    NUMBER("number"),
    RAW("raw"),
    STATE("state"),
    DATE("date"),
    TIME("time"),
    OBJECT("object");

    /**タイプの文字列キー */
    private String key;

    /**
     * キーでタイプを生成する。
     * @param key 文字列キー
     */
    Type(String key) {
        this.key = key;
    }

    /**
     * 文字列からタイプを取得する。
     * @param key タイプの文字列キー
     * @return タイプ
     */
    @JsonCreator
    public static Type fromString(String key) {
        return key == null ? null : Type.valueOf(key.toUpperCase());
    }

    /**
     * タイプのキー文字列を取得する。
     * @return タイプの文字列キー
     */
    @JsonValue
    public String getKey() {
        return key;
    }

    /**
     * タイプのキー文字列を取得する。
     * @return タイプの文字列キー
     */
    public String toValue() {
        switch (this) {
            case NUMBER: return "number";
            case RAW: return "raw";
            case STATE: return "state";
            case DATE: return "date";
            case TIME: return "time";
            case OBJECT: return "object";
        }
        return null;
    }

    /**
     * 文字列からタイプを取得する。
     * @param value 文字列キー
     * @return タイプ
     * @throws IOException IOException
     */
    public static Type forValue(String value) throws IOException {
        if (value.equals("number")) return NUMBER;
        if (value.equals("raw")) return RAW;
        if (value.equals("state")) return STATE;
        if (value.equals("date")) return DATE;
        if (value.equals("time")) return TIME;
        if (value.equals("object")) return OBJECT;
        throw new IOException("Cannot deserialize Type");
    }
}
