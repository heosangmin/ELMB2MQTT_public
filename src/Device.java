import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import de.re.easymodbus.modbusclient.ModbusClient;

public class Device {
    private String ipAddress = "127.0.0.1";
    private int port = 502;
    private String topic;
    private int qos;

    private ModbusClient modbusClient;

    private HashMap<String, HoldingRegister> holdingRegisters;
    private HashMap<String, InputRegister> inputRegisters;

    public static enum DeviceType {
        ENAPTER_ELECTROLYSER,
        TOSHIBA_H2REX,
        UNKNOWN
    }

    private DeviceType deviceType;

    public Device() {}

    public Device(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public void setDeviceType(DeviceType type) {
        this.deviceType = type;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void connect() throws UnknownHostException, IOException {
        if (modbusClient == null) {
            modbusClient = new ModbusClient(ipAddress, port);
        }
        if (!modbusClient.isConnected()) {
            modbusClient.Connect();
            setRegistersModbusClient();
        }
    }

    public void disconnect() throws IOException {
        if (modbusClient.isConnected()) {
            modbusClient.Disconnect();
        }
    }

    public void setIpAddress(String ipAddress) {this.ipAddress = ipAddress;}
    public void setPort(int port) {this.port = port;}
    public void setTopic(String topic) {this.topic = topic;}
    public void setQos(int qos) {this.qos = qos;}
    public void setHoldingRegisters(HashMap<String, HoldingRegister> holdingRegisters) {this.holdingRegisters = holdingRegisters;}
    public void setInputRegisters(HashMap<String, InputRegister> inputRegisters) {this.inputRegisters = inputRegisters;}

    public String getIpAddress() {return this.ipAddress;}
    public int getPort() {return this.port;}
    public String getTopic() {return this.topic;}
    public int getQos() {return this.qos;}
    public HashMap<String, HoldingRegister> getHoldingRegisters() {return this.holdingRegisters;}
    public HashMap<String, InputRegister> getInputRegisters() {return this.inputRegisters;}

    private void setRegistersModbusClient() {
        Set<String> keys = holdingRegisters.keySet();
        for (String key : keys) {
            IRegister register = holdingRegisters.get(key);
            register.setModbusClient(modbusClient);
        }
    }

    public HoldingRegister getHoldingRegister(int address) {
        return holdingRegisters.get(Integer.toString(address));
    }

    public byte[] convertFinalData(IRegister register, ByteBuffer buf) {
        String dataTypeFinal = register.getDataTypeFinal();
        String payload;
        switch (dataTypeFinal) {
            case "ipv4":
                payload = convertIpv4(buf);    
                break;
            case "ChassisSerialNumber":
                payload = convertChassisSerialNumber(buf);    
                break;
            case "ascii":
                payload = new String(buf.array());    
                break;
            case "uuid":
                payload = convertUUID(buf);    
                break;
            case "hex":
                payload = convertHex(buf);    
                break;
            case "numDotNum":
                payload = convertNumDotNum(buf);    
                break;
            case "3octets":
                payload = convert3octets(buf);    
                break;
            default: // raw
                payload = convertRaw(buf, register.getDataType());    
                break;
        }
        return payload.getBytes();
    }

    private String convert3octets(ByteBuffer buf) {
        String payload = "";
        byte[] bytes = buf.array();
        payload = String.format("%X:%X:%X",bytes[1],bytes[2],bytes[3]);
        return payload;
    }

    private String convertNumDotNum(ByteBuffer buf) {
        String payload = "";
        byte[] bytes = buf.array();
        payload = String.format("%d.%d",bytes[0],bytes[1]);
        return payload;
    }

    private String convertHex(ByteBuffer buf) {
        byte[] bytes = buf.array();
        String payload = "0x";
        for (byte b : bytes) {
            payload = payload + String.format("%X", b);
        }
        return payload;
    }

    private String convertUUID(ByteBuffer buf) {
        byte[] bytes = buf.array();
        String payload = "";
        String format = "%X%X%X%X-%X%X-%X%X-%X%X-%X%X%X%X%X%X";
        payload = String.format(
            format,
            bytes[0],bytes[1],bytes[2],bytes[3],
            bytes[4],bytes[5],
            bytes[6],bytes[7],
            bytes[8],bytes[9],
            bytes[10],bytes[11],bytes[12],bytes[13],bytes[14],bytes[15]
        );
        return payload;
    }

    private String convertRaw(ByteBuffer buf, DataType dataType) {
        String payload = "";
        if (dataType.equals(DataType.Boolean) || dataType.equals(DataType.Uint16)) {
            payload = Integer.toUnsignedString(Short.toUnsignedInt(buf.getShort()));
        } else if (dataType.equals(DataType.Int16)) {
            payload = Short.toString(buf.getShort());
        } else if (dataType.equals(DataType.Float32)) {
            payload = Float.toString(buf.getFloat());
        } else if (dataType.equals(DataType.Uint32)) {
            payload = Integer.toUnsignedString(buf.getInt());
        } else if (dataType.equals(DataType.Uint64)) {
            payload = Long.toUnsignedString(buf.getLong());
        } else if (dataType.equals(DataType.Uint128)) {
            payload = "";
        }
        return payload;
    }

    private String convertIpv4(ByteBuffer buf) {
        byte[] bytes = buf.array();
        String val1 = Integer.toString(Byte.toUnsignedInt(bytes[0]));
        String val2 = Integer.toString(Byte.toUnsignedInt(bytes[1]));
        String val3 = Integer.toString(Byte.toUnsignedInt(bytes[2]));
        String val4 = Integer.toString(Byte.toUnsignedInt(bytes[3]));
        return String.format("%s.%s.%s.%s", val1, val2, val3, val4);
    }

    private String convertChassisSerialNumber(ByteBuffer buf) {
        // ChassisSerialNumberは、64bitであることを前提とする。
        // https://handbook.enapter.com/electrolyser/el21_firmware/1.8.1/modbus_tcp_communication_interface.html#example-reading-chassis-serial-number
        long v = buf.getLong();
        if (v <= 0) {
            return "";
        }

        char[] binaries = Long.toBinaryString(v).toCharArray();
        int size = binaries.length;
        int offset = 64 - size;
        
        try {
            String productUnicode = new String(Arrays.copyOfRange(binaries, 0, 11 - offset)); // ~10 bits
            String yearMonth = new String(Arrays.copyOfRange(binaries, 11 - offset, 22 - offset)); // 11 bits
            String day = new String(Arrays.copyOfRange(binaries, 22 - offset, 27 - offset)); // 5 bits
            String chassisNumber = new String(Arrays.copyOfRange(binaries, 27 - offset, 51 - offset)); // 24 bits
            String order = new String(Arrays.copyOfRange(binaries, 51 - offset, 56 - offset)); // 5 bits
            String site = new String(Arrays.copyOfRange(binaries, 56 - offset, 64 - offset)); // 8 bits

            int iProductUnicode = Integer.parseInt(productUnicode, 2);
            int iProductUnicode1 = iProductUnicode % 32 + 64;
            int iProductUnicode2 = iProductUnicode / 32 + 64;
            int iYearMonth = Integer.parseInt(yearMonth, 2);
            int iYear = iYearMonth / 12;
            int iMonth = iYearMonth % 12;
            int iDay = Integer.parseInt(day, 2);
            int iChassisNumber = Integer.parseInt(chassisNumber, 2);
            int iOrder = Integer.parseInt(order, 2) + 64;
            site = (Integer.parseInt(site, 2) == 0) ? "PI" : "SA";
    
            return String.format("%c%c%02d%02d%d%d%c%s", iProductUnicode1, iProductUnicode2, iYear, iMonth, iDay, iChassisNumber, iOrder, site);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return "";
        }
    }

}
