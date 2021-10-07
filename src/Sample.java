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
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import de.re.easymodbus.exceptions.ModbusException;


public class Sample {
    private final String CONFIG = "modbus-sample.properties";
    private final String JSON_ENAPTER_ELECTROLYSER = "EnapterElectrolyser.json";

    private final String TYPE_ENAPTER_ELECTROLYSER = "EnapterElectrolyser";

    private ArrayList<Device> devices;
    private boolean debugOutput = false;
    private boolean getOnly = false;
    private boolean publishRaw = true;

    private MqttBroker broker;

    public Sample() throws FileNotFoundException, IOException {
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

        broker = new MqttBroker(mqtt_ip, mqtt_port, "mqtt_id");

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

        Timestamp timestamp;
        String format = "[ModBus][read][%s] %s";
        String format_register = "[ModBus][%s] addr: %5d | type: %13s | typeFinal: %s | name: %s";

        if (!broker.isConnected()) {
            broker.connect();
        }

        for ( Device device : devices ) {

            device.connect();
            String topic = device.getTopic();
            int qos = device.getQos();

            device.writeUint64(0, 1633599629749L); // temp
            device.writeUint32(4020, 0xC0A80201); // temp
            device.writeUint32(4022, 0xFFFFFF00); // temp
            device.writeUint64(4026, ByteBuffer.wrap("AA2101019SPI".getBytes()).getLong()); // temp

            for (Register hr : device.getHoldingRegisters()) {
                String regName = hr.getName();
                int regAddress = hr.getAddress();
                DataType dataType = hr.getDataType();
                String dataTypeFinal = hr.getDataTypeFinal();

                System.out.println(String.format(format_register, "HR", regAddress, dataType, dataTypeFinal, regName));
                
                ByteBuffer buf = device.readHoldingRegister(regAddress, dataType.registers);
                buf.position(0);

                if (publishRaw) {
                    broker.publish(topic + "/hr/" + regAddress, qos, buf.array());
                } else {
                    String payload = "";
                    if (dataTypeFinal.equals("raw")) {
                        if (dataType.equals(DataType.Boolean)) {
                            payload = Integer.toUnsignedString(Short.toUnsignedInt(buf.getShort()));
                        } else if (dataType.equals(DataType.Float32)) {
                            payload = Float.toString(buf.getFloat());
                        } else if (dataType.equals(DataType.Uint16)) {
                            payload = Integer.toUnsignedString(Short.toUnsignedInt(buf.getShort()));
                        } else if (dataType.equals(DataType.Uint32)) {
                            payload = Integer.toUnsignedString(buf.getInt());
                        } else if (dataType.equals(DataType.Uint64)) {
                            payload = Long.toUnsignedString(buf.getLong());
                        } else if (dataType.equals(DataType.Uint128)) {
                            payload = "TODO";
                        }
                    } else if (dataTypeFinal.equals("ipv4")) {
                        byte[] byte4 = buf.array();
                        String val1 = Integer.toString(Byte.toUnsignedInt(byte4[0]));
                        String val2 = Integer.toString(Byte.toUnsignedInt(byte4[1]));
                        String val3 = Integer.toString(Byte.toUnsignedInt(byte4[2]));
                        String val4 = Integer.toString(Byte.toUnsignedInt(byte4[3]));
                        payload = String.format("%s.%s.%s.%s", val1, val2, val3, val4);
                    }
    
                    broker.publish(topic + "/hr/" + regAddress , qos, payload);
                }
            }

            // for (Register hr : device.getInputRegisters()) {
            //     String regName = hr.getName();
            //     int regAddress = hr.getAddress();
            //     DataType dataType = hr.getDataType();
            //     String dataTypeFinal = hr.getDataTypeFinal();

            //     System.out.println(String.format(format_register, "IR", regAddress, dataType, dataTypeFinal, regName));
                
            //     // 1. read HoldingRegister
            //     ByteBuffer buf = device.readInputRegister(regAddress, dataType.registers);

            //     // 2. convert data type (if it has a final data type)
                
            //     // 3. publish
            //     broker.publish(topic + "/ir/" + regAddress , qos, buf.array());
            // }

            device.disconnect();

            // temp
            broker.disconnect();
        }

    }
    
}
