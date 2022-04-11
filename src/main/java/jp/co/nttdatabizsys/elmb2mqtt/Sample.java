package jp.co.nttdatabizsys.elmb2mqtt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import jp.co.nttdatabizsys.elmb2mqtt.Echonet.EPC;
import jp.co.nttdatabizsys.elmb2mqtt.Echonet.EchonetDevice;
import jp.co.nttdatabizsys.elmb2mqtt.Modbus.ModbusDevice;
import jp.co.nttdatabizsys.elmb2mqtt.common.IDevice;
import jp.co.nttdatabizsys.elmb2mqtt.common.MqttBroker;
import jp.co.nttdatabizsys.elmb2mqtt.common.ProtocolType;
import jp.co.nttdatabizsys.elmb2mqtt.common.TempProtocolType;
import jp.co.nttdatabizsys.elmb2mqtt.util.Logger;

/**
 * Modbus-MQTT間データ転送プログラムのコントローラー<br>
 * <br>
* <h2>共通設定ファイル（/etc/es_ver3/elbm2mqtt.conf）</h2>
* <br>
*共通設定、MQTTブロッカー設定をする。<br>
*<br>
* debugOutput: true（コンソール出力あり）、false（出力なし）※本番起動時にはfalseにすることをおすすめ<br>
* getOnly: true（ModbusデバイスからGetするがMQTTへパブリッシュはしない）、false（Get/Publish両方実行する）<br>
* mqtt_ip: MQTTブロッカーのIPアドレス<br>
* mqtt_port: MQTTブロッカーのポート番号<br>
* mqtt_qos: MQTTブロッカーのQoS<br>
* mqtt_qos_set: ModbusデバイスをデータをWrite(set)するときにパブリッシュするトピック<br>
* mqtt_qos_set_res: ModbusデバイスをデータをWrite(set)した結果をパブリッシュするトピック<br>
* <br>
* <h2>デバイス設定ファイル（/etc/es_ver3/devices/*.json）</h2>
* <br>
* Modbusデバイスを定義する。JSONフォーマットであること。<br>
* <br>
* protocolType: modbus（規定）<br>
* deviceType: toshiba_h2rex | enapter_electrolyser<br>
* ipAddress: IPアドレス<br>
* port: ポート番号<br>
* topic: Read(get)するトピック<br>
* qos: QoS<br>
* registerMap: 取得対象のアドレスをMap形式で定義<br>
*<br>
* "400000": {<br>
*			"address":0,<br>
*			"dataType": "Uint64",<br>
*			"name": "Unix Time",<br>
*			"comment": "Seconds from 1 January 1970 UTC. e.g. 02/29/2020 @ 3:15pm (UTC) represented as 1582989315."<br>
*		},<br>
*<br>
* キーは文字列（例："400000"）にすること。<br>
* キーとaddressは一致いないことに注意（実際にModbusデバイスのレジスタアドレスはaddress値になる）<br>
 * 
 */
public class Sample {
    /**基本設定ファイルパス */
    private String configFilePath;
    /**MQTTブロッカー（ブロッカーは１つだけ持つ）*/
    private MqttBroker broker;
    /**Modbusデバイスのリスト*/
    private ArrayList<IDevice> devices;
    /**設定ファイル参考 */
    private boolean debugOutput = false;
    /** 設定ファイル参考 */
    private boolean getOnly = false; 
    /** 設定ファイル参考 */    
    private String TOPIC_SET; 
    /** 設定ファイル参考 */    
    private int QOS_SET; 
    /** 設定ファイル参考 */    
    private String TOPIC_SET_RES; 
    /** 設定ファイル参考 */    
    private int QOS_SET_RES; 
    /** コンソール出力ユティリティ */    
    private Logger logger = Logger.getLogger();

    /**
     * プログラム起動時に設定ファイルを読み込み、デバイスクラスを作成する。
     * @throws MqttSecurityException MqttSecurityException
     * @throws MqttException MqttException
     * @throws FileNotFoundException FileNotFoundException
     * @throws IOException IOException
     */
    public Sample(String configFilePath) throws MqttSecurityException, MqttException, FileNotFoundException, IOException {

        this.configFilePath = configFilePath;
        Path configPath = Paths.get(this.configFilePath);
        String separator = FileSystems.getDefault().getSeparator();

        if (!Files.exists(Paths.get(this.configFilePath))) {
            System.out.println("config file does not exist. (" + this.configFilePath + ")");
            System.exit(1);
        }

        Properties props = new Properties();
        props.load(new FileInputStream(this.configFilePath));
        
        String format_mqtt = "[properties] mqtt | %s:%s | QoS=%s";

        // debug output on/off
        debugOutput = props.getProperty("debugOutput", "false").equals("true");
        logger.setOnoff(debugOutput);
        logger.log("[properties] debugOutput=%b", Boolean.toString(debugOutput));

        // get only mode (onの場合、MQTTへpublishしない)
        getOnly = props.getProperty("getOnly", "false").equals("true");
        logger.log(String.format("[properties] getOnly=%b", getOnly));

        // MQTTサーバー情報
        String mqtt_ip = props.getProperty("mqtt_ip");
        int mqtt_port = Integer.parseInt(props.getProperty("mqtt_port", "1883"));
        int mqtt_qos = Integer.parseInt(props.getProperty("mqtt_qos", "1"));
        logger.log(String.format(format_mqtt, mqtt_ip, mqtt_port, mqtt_qos));       
        broker = new MqttBroker(mqtt_ip, mqtt_port, configPath.getFileName().toString());
        broker.connect();

        // mqtt subscribe topic, qos
        // Echonet機器情報変更用topic情報
        TOPIC_SET = props.getProperty("mqtt_topic_set", "set/#");
        QOS_SET = Integer.parseInt(props.getProperty("mqtt_qos_set", "1"));
        logger.log(String.format("[properties] topic_set=%s | qos_set=%s", TOPIC_SET, QOS_SET));

        TOPIC_SET_RES = props.getProperty("mqtt_topic_set_res", "set_res");
        QOS_SET_RES = Integer.parseInt(props.getProperty("mqtt_qos_set_res", "1"));
        logger.log(String.format("[properties] topic_set_res=%s | qos_set_res=%s", TOPIC_SET_RES, QOS_SET_RES));
        
        devices = new ArrayList<IDevice>();

        // デバイス
        String deviceConfigDir = props.getProperty("deviceConfigDir", "devices");
        Path deviceConfigPath = Paths.get(configPath.getParent().toString() + separator + deviceConfigDir);
        if (!Files.exists(deviceConfigPath)) {
            System.out.println("device config directory does not exist. :" + deviceConfigPath);
            System.exit(1);
        }
        logger.log(String.format("[properties] deviceConfigDir=%s", deviceConfigPath));
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(deviceConfigPath, "*.json")) {
            for (Path file : stream) {
                if (Files.isReadable(file) && !Files.isDirectory(file)) {
                    byte[] jsonData = Files.readAllBytes(file);
                    ObjectMapper mapper = new ObjectMapper();
                    TempProtocolType tempProtocolType = mapper.readValue(jsonData, TempProtocolType.class);
                    if (tempProtocolType.getProtocolType() != null && tempProtocolType.getProtocolType().equals(ProtocolType.ECHONET_LITE)) {
                        EchonetDevice device = mapper.readValue(jsonData, EchonetDevice.class);
                        devices.add(device);
                        System.out.println(device);
                        // dumpEPCs(device);
                    } else if (tempProtocolType.getProtocolType() != null && tempProtocolType.getProtocolType().equals(ProtocolType.MODBUS)) {
                        ModbusDevice device = mapper.readValue(jsonData, ModbusDevice.class);
                        device.connect();
                        devices.add(device);
                        System.out.println(device);
                    }
                }
            }
        }
        catch (IOException | DirectoryIteratorException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void dumpEPCs(EchonetDevice ed) {
        LinkedHashMap<String,EPC> map = ed.getEpcMap();
        Iterator<String> keys = map.keySet().iterator();
        while(keys.hasNext()) {
            String key = keys.next();
            System.out.printf("[%s][%s] type:%s, format:%s, size:%d, multiple:%f\n",
                ed.getIpAddress(),
                key,
                map.get(key).getType(),
                map.get(key).getFormat(),
                map.get(key).getSize(),
                map.get(key).getMultiple()
            );
        }
    }

    /**
     * ELデバイス毎に対象項目「EPC」のデータ「EDT」を取得（get）しMQTTサーバーへ配布（publish）する。
     * @throws IOException IOException
     */
    public void getAndPublish() throws IOException {
        devices
            .stream()
            .forEach(device -> {
                try {
                    publish(device.getTopic(), device.getQos(), device.getAll());
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            });
    }

    /**
     * MQTTサーバーのあるトピックへデータを配布（publish)する。
     * @param topic トピック
     * @param qos Qos
     * @param payload ペイロード
     * @throws MqttException MqttException
     */
    private void publish(String topic, int qos, String payload) throws MqttException {
        if (!broker.isConnected()) {
            broker.connect();
        }
        if (!getOnly) {
            broker.publish(topic, qos, payload);
            logger.log(String.format("[mqtt][pub][%s] %s", topic, payload), true);
        }
    }

    /**
     * MQTTサーバーのあるトピックを購読（subscribe）する。<br>
     * 購読（subscribe）しているトピックにデータがpublishされた時の処理
     * <ol>
     * <li>topicからkeyを取得(echoent: EPC, modbus: register address)</li>
     * <li>mqttMessageからvalueを取得(echoent: EDT)</li>
     * <li>デバイスへsetする</li>
     * <li>成功の場合は、getしpublish</li>
     * <li>set結果をpublish</li>
     * </ol>
     * @throws MqttException MqttException
     */
    public void subscribe() throws MqttException {
            
        if (!broker.isConnected()) {
            broker.connect();
        }

        broker.setCallback(new MqttCallback(){

            @Override
            public void connectionLost(Throwable arg0) {
                logger.log("[mqtt][sub][error] connectionLost", true);
                try {
                    logger.log("[mqtt][sub][error] trying to reconnect...", true);
                    broker.reconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken arg0) {}

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                String format_mqtt = "[mqtt][sub][%s] %s";
                String format_device = "[%s] %s";
                String set_res;
                IDevice device;

                logger.log(String.format(format_mqtt, topic, mqttMessage), true);

                // topicを見てデバイスを探す。
                String topicWithoutPrefix = topic.replaceFirst("^" + TOPIC_SET.replace("#", ""), "");

                Optional<IDevice> optDeivce = devices.stream().filter(d -> (topicWithoutPrefix).startsWith(d.getTopic()+"/")).findFirst();
                if (optDeivce.isPresent()) {
                    device = optDeivce.get();
                } else {
                    logger.log(String.format(format_device, "error", "topic does not exist: " + topicWithoutPrefix), true);
                    return;
                }

                // key(echoent: EPC, modbus: register address) validation
                String key = topicWithoutPrefix.replaceAll(device.getTopic() + "/", "");
                if (!device.isValidKey(key)) {
                    logger.log(String.format(format_device, "error", "key error: " + key), true);
                    return;
                }

                // value(echoent: EDT, modbus: value) validation
                String value = mqttMessage.toString();
                if (!device.isValidValue(key, value)) {
                    logger.log(String.format(format_device, "error", "value error: " + value), true);
                    return;
                }

                // setを実施する。
                String setResult = device.set(key, value);
                if (setResult != null) {
                    String getResult = device.get(key);
                    if (getResult != null) {
                        publish(device.getTopic(), device.getQos(), getResult);
                    }
                    set_res = "OK";
                } else {
                    set_res = "NG";
                }
                // レスポンストピックへpublish
                publish(TOPIC_SET_RES + "/" + topicWithoutPrefix, QOS_SET_RES, set_res);
            }
        });

        broker.subscribe(TOPIC_SET, QOS_SET);
        logger.log("[MQTT] starting to subscribe", true);
    }

}
