package jp.co.nttdatabizsys.elmb2mqtt.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 設定ファイルからデバイス情報を読み込むときに、プロトコルタイプを判定するため一時的に使うクラス
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TempProtocolType {
    /**プロトコルタイプ */
    private ProtocolType protocolType;
    /**
     * プロトコルタイプを設定する。
     * @param protocolType プロトコルタイプ
     */
    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }
    /**
     * プロトコルタイプを取得する。
     * @return プロトコルタイプ
     */
    public ProtocolType getProtocolType() {
        return protocolType;
    }
}
