package jp.co.nttdatabizsys.elmb2mqtt.Echonet;

public class ELMessage {
    private String EHD = "1081"; // 2 byte
    private String TID = ""; // 2 byte
    private String SEOJ = ""; // 3 byte
    private String DEOJ = ""; // 3 byte
    private String ESV = ""; // 1 byte
    private String OPC = ""; // 1 byte
    private String EPC = ""; // 1 byte
    private String PDC = ""; // 1 byte
    private String EDT = ""; // rest of it

    private byte[] byteMsg;

    /**
     * コンストラクター１
     * @param TID
     * @param SEOJ
     * @param DEOJ
     * @param ESV
     * @param OPC
     * @param EPC
     * @param PDC
     * @param EDT
     */
    public ELMessage(String TID, String SEOJ, String DEOJ, String ESV, String OPC, String EPC, String PDC, String EDT){
        this.TID = TID.toUpperCase();
        this.SEOJ = SEOJ.toUpperCase();
        this.DEOJ = DEOJ.toUpperCase();
        this.ESV = ESV.toUpperCase();
        this.OPC = OPC.toUpperCase();
        this.EPC = EPC.toUpperCase();
        this.PDC = PDC.toUpperCase();
        this.EDT = EDT.toUpperCase();
    }

    /**
     * コンストラクター２
     * @param msg
     */
    public ELMessage(byte[] msg) { // 今回のサンプルではGETの結果データを作成するときに使います。
        this.byteMsg = msg;
        String stringMsg = encodeHexString(msg);
        EHD = stringMsg.substring(0, 4).toUpperCase();
        TID = stringMsg.substring(4, 8).toUpperCase();
        SEOJ = stringMsg.substring(8, 14).toUpperCase();
        DEOJ = stringMsg.substring(14, 20).toUpperCase();
        ESV = stringMsg.substring(20, 22).toUpperCase();
        OPC = stringMsg.substring(22, 24).toUpperCase();
        EPC = stringMsg.substring(24, 26).toUpperCase();
        PDC = stringMsg.substring(26, 28).toUpperCase();
        EDT = stringMsg.substring(28).toUpperCase();
    }
    
    /**
     * メッセージを文字列として表現する。
     * @return メッセージ文字列
     */
    public String getSeparatedString() {
        return String.format("%s %s %s %s %s %s %s %s %s",EHD,TID,SEOJ,DEOJ,ESV,OPC,EPC,PDC,EDT);
    }
    
    /**
     * メッセージをバイト配列として取得する。
     * @return
     */
    public byte[] getBytes() {
        return decodeHexString(EHD + TID + SEOJ + DEOJ + ESV + OPC + EPC + PDC + EDT);
    }

    /**
     * 16進数の文字列をバイト配列に変換する。
     * @param hexString
     * @return
     */
    public byte[] decodeHexString(String hexString) {
        if (hexString.length() % 2 == 1) {
            throw new IllegalArgumentException("Invalid hexadecimal String supplied.");
        }
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            bytes[i / 2] = hexToByte(hexString.substring(i, i + 2));
        }
        return bytes;
    }

    /**
     * バイト配列を16進数の文字列に変換する。
     * @param byteArray
     * @return
     */
    public static String encodeHexString(byte[] byteArray) {
        StringBuilder sb = new StringBuilder();
        for (byte tmp : byteArray) {
            sb.append(String.format("%02x", tmp));
        }
        return sb.toString();
    }

    /**
     * バイトを16進数の文字列に変換うする。
     * @param num
     * @return
     */
    public String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

    /**
     * 16進数の文字列をバイトに変換する。
     * @param hexString
     * @return
     */
    public byte hexToByte(String hexString) {
        int firstDigit = toDigit(hexString.charAt(0));
        int secondDigit = toDigit(hexString.charAt(1));
        return (byte) ((firstDigit << 4) + secondDigit);
    }

    /**
     * 16進数の文字(char)を数字コードに変換する。
     * @param hexChar
     * @return
     */
    public int toDigit(char hexChar) {
        int digit = Character.digit(hexChar, 16);
        if (digit == -1) {
            throw new IllegalArgumentException("Invalid Hexadecial Character:" + hexChar);
        }
        return digit;
    }

    /**
     * EDTを取得する。
     * @return
     */
    public String getEDTString() {
        return EDT;
    }

    /**
     * EDTのバイト配列を取得する。
     * @return
     */
    public byte[] getEDTBytes() {
        return byteMsg;
    }

    /**
     * ESVを取得する。
     * @return
     */
    public String getESVString() {
        return ESV;
    }

    @Override
    public String toString() {
        return EHD + TID + SEOJ + DEOJ + ESV + OPC + EPC + PDC + EDT;
    }      
}