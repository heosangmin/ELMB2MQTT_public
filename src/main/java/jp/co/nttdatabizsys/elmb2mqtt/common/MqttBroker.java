package jp.co.nttdatabizsys.elmb2mqtt.common;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import jp.co.nttdatabizsys.elmb2mqtt.util.Logger;

/**
 * MQTTブロッカークラス
 */
public class MqttBroker {
    /**MQTTクライアント */
    IMqttClient mqttClient;
    /**IPアドレス */
    String address;
    /**ポート番号 */
    int port;
    /**パブリッシャーID */
    String publisherId;
    /**コンソールロギングユティリティ */
    Logger logger = Logger.getLogger();

    /**
     * MQTTブロッカーを生成する。
     * @param address IPアドレス
     * @param port ポート番号
     * @param publisherId パブリッシャーID
     */
    public MqttBroker(String address, int port, String publisherId) {
        this.address = address;
        this.port = port;
        this.publisherId = publisherId;
    }

    /**
     * MQTTブロッカー（mosquitto）へ接続する。
     * @throws MqttException MqttException
     */
    public void connect() throws MqttException {
        String broker = "tcp://" + address + ":" + port;
        MemoryPersistence persistence = new MemoryPersistence();
        mqttClient = new MqttClient(broker, publisherId, persistence);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(5);

        mqttClient.connect(options);
        logger.log("[mqtt] connected", true);
    }

    /**
     * MQTTブロッカーとの接続を切る。
     * @throws MqttException MqttException
     */
    public void disconnect() throws MqttException {
        if (mqttClient.isConnected()) {
            mqttClient.disconnect();
            logger.log("[mqtt] disconnected", true);
        }
    }

    /**
     * MQTTブロッカーと再接続する。
     * @throws MqttException MqttException
     */
    public void reconnect() throws MqttException {
        mqttClient.reconnect();
    }

    /**
     * MQTTブロッカーと接続されているか確認する。
     * @return 接続状態
     */
    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }

    /**
     * MQTTブロッカーにサブスクライバーのコールバックを設定する。
     * @param mqttCallback mqttCallback
     */
    public void setCallback(MqttCallback mqttCallback) {
        mqttClient.setCallback(mqttCallback);
    }

    /**
     * MQTTブロッカーへパブリッシュする。
     * @param topic トピック
     * @param qos QoS
     * @param payload ペイロード
     * @throws MqttPersistenceException MqttPersistenceException
     * @throws MqttException MqttException
     */
    public void publish(String topic, int qos, String payload) throws MqttPersistenceException, MqttException {
        MqttMessage msg = new MqttMessage(payload.getBytes());
        msg.setQos(qos);
        mqttClient.publish(topic, msg);
    }

    /**
     * MQTTブロッカーへパブリッシュする。
     * @param topic トピック
     * @param qos QoS
     * @param payload ペイロード
     * @throws MqttPersistenceException MqttPersistenceException
     * @throws MqttException MqttException
     */
    public void publish(String topic, int qos, byte[] payload) throws MqttPersistenceException, MqttException {
        mqttClient.publish(topic, payload, qos, true);
    }

    /**
     * MQTTブロッカーの特定トピックをサブスクライブする。
     * @param topic トピック
     * @param qos QoS
     * @throws MqttSecurityException MqttSecurityException
     * @throws MqttException MqttException
     */
    public void subscribe(String topic, int qos) throws MqttSecurityException, MqttException {
        mqttClient.subscribe(topic, qos);
    }

}
