package jp.co.nttdatabizsys.elmb2mqtt.Modbus;

/**
 * Modbusレジスタのデータタイプ列挙型
 */
public enum DataType {
    // name(bytes, registers)
    /**Boolean: 2 bytes, 1 register */
    Boolean(2,1),
    /**Int16: 2 bytes, 1 register */
    Int16(2,1),
    /**Uint16: 2 bytes, 1 register */
    Uint16(2,1),
    /** Int32: 4 bytes, 2 registers  */
    Int32(4,2),
    /** Uint32: 4 bytes, 2 registers  */
    Uint32(4,2),
    /**Uint64: 8 bytes, 4 registers */
    Uint64(8,4),
    /**Uint128: 16 bytes, 8 registers */
    Uint128(16,8),
    /**Float32: 4 bytes, 2 registers */
    Float32(4,2),
    /**(Enapter only)Events: 66 bytes, 33 registers */
    Events(66,33);
    /**バイト数 */
    public int bytes;
    /**レジスタ数 */
    public int registers;
    
    /**
     * Modbusレジスタデータタイプを生成する。
     * @param bytes バイト数
     * @param registers レジスタ数
     */
    private DataType(int bytes, int registers) {
        this.bytes = bytes;
        this.registers = registers;
    }
}
