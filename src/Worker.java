import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import de.re.easymodbus.exceptions.ModbusException;


public class Worker {
    private final String CONFIG = "mb2mqtt.properties";
    private final String JSON_ENAPTER_ELECTROLYSER = "EnapterElectrolyser.json";

    private final String TYPE_ENAPTER_ELECTROLYSER = "EnapterElectrolyser";

    private ArrayList<Device> devices;
    private boolean debugOutput = false;
    private boolean getOnly = false;
    private boolean publishRaw = true;

    private MqttBroker broker;

    public Worker() throws FileNotFoundException, IOException {
        devices = new ArrayList<Device>();

        Properties prop = new Properties();
        prop.load(new FileInputStream(CONFIG));

        String format_mqtt = "[properties] mqtt | %s:%s | QoS=%s | interval=%s";
        String format_device = "[properties] device #%s | %s | %s:%s | topic=%s";

        // debug output on/off
        debugOutput = prop.getProperty("debug_output", "false").equals("true");
        System.out.println(String.format("[properties] debug_output=%b", debugOutput));

        // get only mode (onの場合、MQTTへpublishしない)
        getOnly = prop.getProperty("get_only", "false").equals("true");
        System.out.println(String.format("[properties] get_only=%b", getOnly));

        // publish mode (true: byte[]をpublish)
        publishRaw = prop.getProperty("publish_raw", "true").equals("true");
        System.out.println(String.format("[properties] publish_raw=%b", publishRaw));

        // MQTTサーバー情報
        String mqtt_ip = prop.getProperty("mqtt_ip");
        int mqtt_port = Integer.parseInt(prop.getProperty("mqtt_port", "1883"));
        int mqtt_qos = Integer.parseInt(prop.getProperty("mqtt_qos", "1"));
        long mqtt_publish_interval = Long.parseLong(prop.getProperty("mqtt_publish_interval", "60000"));
        System.out.println(String.format(format_mqtt, mqtt_ip, mqtt_port, mqtt_qos, mqtt_publish_interval));

        broker = new MqttBroker(mqtt_ip, mqtt_port, "mqtt_id_modbus");

        // publish mode

        // Modbus機器情報（device）
        byte[] jsonData;
        int device_count = Integer.parseInt(prop.getProperty("device_count", "1"));
        for (int i=1; i < device_count+1; i++) {
            String type = prop.getProperty("device_" + Integer.toString(i) + "_type");
            String ip = prop.getProperty("device_" + Integer.toString(i) + "_ip");
            int port = Integer.parseInt(prop.getProperty("device_" + Integer.toString(i) + "_port"));
            String topic = prop.getProperty("device_" + Integer.toString(i) + "_topic");
            int qos = Integer.parseInt(prop.getProperty("device_" + Integer.toString(i) + "_qos"));
            System.out.println(String.format(format_device, i, type, ip, port, topic));

            if (type.equals(TYPE_ENAPTER_ELECTROLYSER)){
                jsonData = Files.readAllBytes(Paths.get(JSON_ENAPTER_ELECTROLYSER));
                ObjectMapper mapper = new ObjectMapper();
                Device device = mapper.readValue(jsonData, Device.class);    
                device.setIpAddress(ip);
                device.setPort(port);
                device.setTopic(topic);
                device.setQos(qos);
                devices.add(device);
            } else {
                System.out.println("error: type " + type + " is not supported.");
                System.exit(-1);
            }
        }


    }

    public void readAndPublishAll() throws UnknownHostException, SocketException, ModbusException, IOException, MqttPersistenceException, MqttException {

        String format_register = "[ModBus][%s] addr: %5d | type: %7s | typeFinal: %s | name: %s";

        if (!broker.isConnected()) {
            broker.connect();
        }

        for ( Device device : devices ) {

            device.connect();
            String topic = device.getTopic();
            int qos = device.getQos();

            // device.writeUint64(0, 1633599629749L); // temp
            // device.writeUint32(4020, 0xC0A80201); // temp
            // device.writeUint32(4022, 0xFFFFFF00); // temp
            // device.writeUint64(4026, 586601960808264448L); // temp
            // device.writeUint16(4046, 514); // temp

            for (Register hr : device.getHoldingRegisters()) {
                String regName = hr.getName();
                int regAddress = hr.getAddress();
                DataType dataType = hr.getDataType();
                String dataTypeFinal = hr.getDataTypeFinal();

                if (debugOutput) {
                    System.out.println(String.format(format_register, "HR", regAddress, dataType, dataTypeFinal, regName));
                }
                
                ByteBuffer buf = device.readHoldingRegister(regAddress, dataType.registers);
                buf.position(0);

                if (publishRaw) {
                    publish(topic + "/hr/" + regAddress, qos, buf.array());
                } else {
                    String payload = "";
                    if (dataTypeFinal.equals("raw")) {
                        payload = parseRaw(buf, dataType);
                    } else if (dataTypeFinal.equals("ipv4")) {
                        payload = parseIpv4(buf);
                    } else if (dataTypeFinal.equals("ChassisSerialNumber")) {
                        payload = parseChassisSerialNumber(buf);
                    }
    
                    publish(topic + "/hr/" + regAddress , qos, payload.getBytes());
                }
            }

            for (Register ir : device.getInputRegisters()) {
                String regName = ir.getName();
                int regAddress = ir.getAddress();
                DataType dataType = ir.getDataType();
                String dataTypeFinal = ir.getDataTypeFinal();

                ByteBuffer buf = device.readInputRegister(regAddress, dataType.registers);
                buf.position(0);

                if (debugOutput) {
                    System.out.println(String.format(format_register, "IR", regAddress, dataType, dataTypeFinal, regName));
                }
            
                if (publishRaw) {
                    publish(topic + "/ir/" + regAddress, qos, buf.array());
                } else {
                    String payload = "";
                    if (dataTypeFinal.equals("raw")) {
                        payload = parseRaw(buf, dataType);
                    } else if (dataTypeFinal.equals("ascii")) {
                        payload = new String(buf.array());
                    } else if (dataTypeFinal.equals("uuid")) {
                        payload = parseUUID(buf);
                    } else if (dataTypeFinal.equals("hex")) {
                        payload = parseHex(buf);
                    } else if (dataTypeFinal.equals("numDotNum")) {
                        payload = parseNumDotNum(buf);
                    } else if (dataTypeFinal.equals("3octets")) {
                        payload = parse3octets(buf);
                    }

                    publish(topic + "/ir/" + regAddress , qos, payload.getBytes());
                }
            }

            device.disconnect();

            // temp
            broker.disconnect();
        }

    }

    // publish(トピック, QOS, 送信データ)
    // MQTTサーバーのあるトピックへデータを配布（pusish)する。
    private void publish(String topic, int qos, byte[] payload) throws MqttException {
        if (!broker.isConnected()) {
            broker.connect();
        }
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String format = "[mqtt][pub][%s][%s] = %s";
        broker.publish(topic, qos, payload);
        if (debugOutput) {
            System.out.println(String.format(format, timestamp, topic, new String(payload)));
        }
    }

    private String parse3octets(ByteBuffer buf) {
        String payload = "";
        byte[] bytes = buf.array();
        payload = String.format("%X:%X:%X",bytes[1],bytes[2],bytes[3]);
        return payload;
    }

    private String parseNumDotNum(ByteBuffer buf) {
        String payload = "";
        byte[] bytes = buf.array();
        payload = String.format("%d.%d",bytes[0],bytes[1]);
        return payload;
    }

    private String parseHex(ByteBuffer buf) {
        byte[] bytes = buf.array();
        String payload = "0x";
        for (byte b : bytes) {
            payload = payload + String.format("%X", b);
        }
        return payload;
    }

    private String parseUUID(ByteBuffer buf) {
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

    private String parseRaw(ByteBuffer buf, DataType dataType) {
        String payload = "";
        if (dataType.equals(DataType.Boolean) || dataType.equals(DataType.Uint16)) {
            payload = Integer.toUnsignedString(Short.toUnsignedInt(buf.getShort()));
        } else if (dataType.equals(DataType.Float32)) {
            payload = Float.toString(buf.getFloat());
        } else if (dataType.equals(DataType.Uint32)) {
            payload = Integer.toUnsignedString(buf.getInt());
        } else if (dataType.equals(DataType.Uint64)) {
            payload = Long.toUnsignedString(buf.getLong());
        } else if (dataType.equals(DataType.Uint128)) {
            payload = "TODO";
        }
        return payload;
    }

    private String parseIpv4(ByteBuffer buf) {
        byte[] bytes = buf.array();
        String val1 = Integer.toString(Byte.toUnsignedInt(bytes[0]));
        String val2 = Integer.toString(Byte.toUnsignedInt(bytes[1]));
        String val3 = Integer.toString(Byte.toUnsignedInt(bytes[2]));
        String val4 = Integer.toString(Byte.toUnsignedInt(bytes[3]));
        return String.format("%s.%s.%s.%s", val1, val2, val3, val4);
    }

    private String parseChassisSerialNumber(ByteBuffer buf) {
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

            // System.out.println(String.format(format_csn, "productUnicode", productUnicode, Integer.parseInt(productUnicode, 2)));
            // System.out.println(String.format(format_csn, "yearMonth", yearMonth, Integer.parseInt(yearMonth, 2)));
            // System.out.println(String.format(format_csn, "day", day, Integer.parseInt(day, 2)));
            // System.out.println(String.format(format_csn, "chassisNumber", chassisNumber, Integer.parseInt(chassisNumber, 2)));
            // System.out.println(String.format(format_csn, "order", order, Integer.parseInt(order, 2)));
            // System.out.println(String.format(format_csn, "site", site , Integer.parseInt(site, 2)));

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
