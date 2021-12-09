package jp.co.nttdatabizsys.modbus;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import de.re.easymodbus.exceptions.ModbusException;


public class Sample {
    private final String CONFIG = "modbus-sample.properties";
    private final String JSON_ENAPTER_ELECTROLYSER = "EnapterElectrolyser.json";
    private final String JSON_TOSHIBA_H2REX = "ToshibaH2Rex.json";

    private final String TYPE_ENAPTER_ELECTROLYSER = "EnapterElectrolyser";
    private final String TYPE_TOSHIBA_H2REX = "ToshibaH2Rex";

    private ArrayList<Device> devices;
    private boolean debugOutput = false;
    private boolean getOnly = false;

    private String TOPIC_SET;
    private int QOS_SET;

    private String TOPIC_SET_RES;
    private int QOS_SET_RES;

    private MqttBroker broker;
    private Logger logger;

    public Sample() throws Exception {
        devices = new ArrayList<Device>();

        Properties prop = new Properties();
        prop.load(new FileInputStream(CONFIG));

        String format_mqtt = "[properties] mqtt | %s:%s | QoS=%s | interval=%s";
        String format_device = "[properties] device #%s | %s | %s:%s | topic=%s";

        // debug output on/off
        debugOutput = prop.getProperty("debug_output", "false").equals("true");
        System.out.println(String.format("[properties] debug_output=%b", debugOutput));

        logger = Logger.getLogger(debugOutput);

        // get only mode (onの場合、MQTTへpublishしない)
        getOnly = prop.getProperty("get_only", "false").equals("true");
        System.out.println(String.format("[properties] get_only=%b", getOnly));

        // MQTTサーバー情報
        String mqtt_ip = prop.getProperty("mqtt_ip");
        int mqtt_port = Integer.parseInt(prop.getProperty("mqtt_port", "1883"));
        int mqtt_qos = Integer.parseInt(prop.getProperty("mqtt_qos", "1"));
        long mqtt_publish_interval = Long.parseLong(prop.getProperty("mqtt_publish_interval", "60000"));
        System.out.println(String.format(format_mqtt, mqtt_ip, mqtt_port, mqtt_qos, mqtt_publish_interval));

        broker = MqttBroker.getBroker(mqtt_ip, mqtt_port, "mqtt_id_modbus");

        // mqtt subscribe topic, qos
        // SET用topic情報
        TOPIC_SET = prop.getProperty("mqtt_topic_set", "set/#");
        QOS_SET = Integer.parseInt(prop.getProperty("mqtt_qos_set", "1"));
        System.out.println(String.format("[properties] topic_set=%s | qos_set=%s", TOPIC_SET, QOS_SET));

        TOPIC_SET_RES = prop.getProperty("mqtt_topic_set_res", "set_res");
        QOS_SET_RES = Integer.parseInt(prop.getProperty("mqtt_qos_set_res", "1"));
        System.out.println(String.format("[properties] topic_set_res=%s | qos_set_res=%s", TOPIC_SET_RES, QOS_SET_RES));

        // Modbus機器情報（device）
        Device.DeviceType deviceType = Device.DeviceType.UNKNOWN;
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
                deviceType = Device.DeviceType.ENAPTER_ELECTROLYSER;
            } else if (type.equals(TYPE_TOSHIBA_H2REX)){
                jsonData = Files.readAllBytes(Paths.get(JSON_TOSHIBA_H2REX));
                deviceType = Device.DeviceType.TOSHIBA_H2REX;
            } else {
                System.out.println("error: type " + type + " is not supported.");
                throw new Exception("unsupported device type: " + type);
            }

            ObjectMapper mapper = new ObjectMapper();
            Device device = mapper.readValue(jsonData, Device.class);
            device.setDeviceType(deviceType);
            device.setIpAddress(ip);
            device.setPort(port);
            device.setTopic(topic);
            device.setQos(qos);
            device.connect();
            devices.add(device);
        }
    }

    public void readAndPublishAll() throws UnknownHostException, SocketException, ModbusException, IOException, MqttPersistenceException, MqttException {

        String format_register = "[ModB][%s] addr: %5d | type: %7s | typeFinal: %s | name: %s";

        for ( Device device : devices ) {

            String topic = device.getTopic();
            int qos = device.getQos();

            // Holding Register
            HashMap<String, HoldingRegister> holdingRegisterMap = device.getHoldingRegisters();
            Set<String> keys = holdingRegisterMap.keySet();
            for (String key : keys) {
                IRegister register = holdingRegisterMap.get(key);
                logger.logWithTimestamp(String.format(format_register,"hr",register.getAddress(),register.getDataType(),register.getDataTypeFinal(),register.getName()));
                byte[] payload = device.convertFinalData(register, register.read());
                publish(topic + "/hr/" + register.getAddress() , qos, payload);
            }

            // Input Register
            HashMap<String, InputRegister> inputRegisterMap = device.getInputRegisters();
            keys = inputRegisterMap.keySet();
            for (String key : keys) {
                IRegister register = inputRegisterMap.get(key);
                logger.logWithTimestamp(String.format(format_register,"ir",register.getAddress(),register.getDataType(),register.getDataTypeFinal(),register.getName()));
                byte[] payload = device.convertFinalData(register, register.read());
                publish(topic + "/ir/" + register.getAddress() , qos, payload);
            }
       
        }
    }

    // publish(トピック, QOS, 送信データ)
    // MQTTサーバーのあるトピックへデータを配布（pusish)する。
    private void publish(String topic, int qos, byte[] payload) throws MqttException {
        
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String format = "[MQTT][pub][%s][%s] = %s";
        broker.publish(topic, qos, payload);
        logger.logWithTimestamp(String.format(format, timestamp, topic, new String(payload)));
    }

    // subscribe()
    // MQTTサーバーのあるトピックを購読（subscribe）する。
    public void subscribe() throws MqttException {

        broker.setCallback(new MqttCallback(){

            @Override
            public void connectionLost(Throwable arg0) {
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken arg0) {
            }

            // 購読（subscribe）しているMQTTトピックにデータがpublishされた時の処理
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                String format_mqtt = "[MQTT][sub][%s] %s";
                String format_modbus = "[ModB][%s] %s";
                String address;
                String res = "NG";

                logger.logWithTimestamp(String.format(format_mqtt, topic, mqttMessage));

                // topicを見てデバイスを探す。
                topic = topic.replaceFirst("^" + TOPIC_SET.replace("#", ""), "");
                Device device = getDeviceByTopic(topic);
                if (device == null) {
                    logger.logWithTimestamp(String.format(format_modbus, "ERR", "topic does not exist: " + topic));
                    return;
                }
                    
                // register番号を取得
                address = topic.replace(device.getTopic() + "/hr/", "");

                // addressが有効な番号かチェック
                Pattern pattern = Pattern.compile("\\d+"); // 数字？
                HashMap<String, HoldingRegister> holdingRegisterMap = device.getHoldingRegisters();
                HoldingRegister register = holdingRegisterMap.get(address);
                if (pattern.matcher(address).matches() && register != null) {
                    // レジスタにwrite
                    try {
                        int qos = device.getQos();
                        int intValue = Integer.parseInt(mqttMessage.toString());
                        register.write(intValue);
                        byte[] payload = device.convertFinalData(register, register.read());
                        publish(topic, qos, payload);
                        res = "OK";
                    } catch (NumberFormatException nfe) {
                        System.out.println("NumberFormatException: " + nfe.getMessage());
                    }
                }
                publish(TOPIC_SET_RES + "/" + topic, QOS_SET_RES, res.getBytes());
            }
        });

        broker.subscribe(TOPIC_SET, QOS_SET);
        logger.logWithTimestamp("[MQTT] starting to subscribe");

    }

    private Device getDeviceByTopic(String topic) {
        for (Device device : devices) {
            if (topic.startsWith(device.getTopic() + "/")) {
                return device;
            }
        }
        return null;
    }

    public void sendKeepAlive() throws UnknownHostException, SocketException, ModbusException, IOException {
        for ( Device device : devices ) {
            if (device.getDeviceType().equals(Device.DeviceType.TOSHIBA_H2REX)) {
                int sec = java.util.Calendar.getInstance().get(java.util.Calendar.SECOND);
                HoldingRegister hr = device.getHoldingRegister(7);
                hr.write(sec);
            }
        }
    }
    
}
