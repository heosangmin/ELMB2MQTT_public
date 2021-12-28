package jp.co.nttdatabizsys.modbus;

public enum DataType {
    // name(bytes, registers)
    Boolean(2,1),
    Int16(2,1),
    Uint16(2,1),
    Uint32(4,2),
    Uint64(8,4),
    Uint128(16,8),
    Float32(4,2),
    Events(128,64);
    public int bytes;
    public int registers;
    private DataType(int bytes, int registers) {
        this.bytes = bytes;
        this.registers = registers;
    }
}