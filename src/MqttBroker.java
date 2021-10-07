import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttBroker {
    IMqttClient mqttClient;
    String address;
    int port;
    String publisherId;

    public MqttBroker(String address, int port, String publisherId) {
        this.address = address;
        this.port = port;
        this.publisherId = publisherId;
    }

    public void connect() throws MqttException {
        String broker = "tcp://" + address + ":" + port;
        MemoryPersistence persistence = new MemoryPersistence();
        mqttClient = new MqttClient(broker, publisherId, persistence);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(5);

        mqttClient.connect(options);
        System.out.println("[mqtt] connected");
    }

    public void disconnect() throws MqttException {
        if (mqttClient.isConnected()) {
            mqttClient.disconnect();
            System.out.println("[mqtt] disconnected");
        }
    }

    public void reconnect() throws MqttException {
        mqttClient.reconnect();
    }

    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }

    public void setCallback(MqttCallback mqttCallback) {
        mqttClient.setCallback(mqttCallback);
    }

    public void publish(String topic, int qos, String payload) throws MqttPersistenceException, MqttException {
        MqttMessage msg = new MqttMessage(payload.getBytes());
        msg.setQos(qos);
        mqttClient.publish(topic, msg);
    }

    public void publish(String topic, int qos, byte[] payload) throws MqttPersistenceException, MqttException {
        mqttClient.publish(topic, payload, qos, true);
    }

    public void subscribe(String topic, int qos) throws MqttSecurityException, MqttException {
        mqttClient.subscribe(topic, qos);
    }

}
