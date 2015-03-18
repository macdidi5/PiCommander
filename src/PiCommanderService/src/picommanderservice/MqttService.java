package picommanderservice;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttService {
    
    private static final int QOS = 2;
    private MqttClient mqttClient, mqttClientStatus;
    
    public MqttService(String host, String port, String clientId) {
        String broker = "tcp://" + host + ":" + port;

        try {
            mqttClient = new MqttClient(
                    broker, clientId, new MemoryPersistence());
            mqttClientStatus = new MqttClient(
                    broker, clientId + "Status", new MemoryPersistence());
        }
        catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void connect() {
        MqttConnectOptions mqttConnectOptions = 
                new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(true);
        
        try {
            mqttClient.connect(mqttConnectOptions);
            mqttClientStatus.connect(mqttConnectOptions);
        }
        catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void publish(String topic, String message) {
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                mqttMessage.setQos(QOS);

        try {
            mqttClientStatus.publish(topic, mqttMessage);
        }
        catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void subscribe(String topic) {
        try {
            mqttClient.subscribe(topic);
        }
        catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void setCallback(MqttCallback mqttCallback) {
        mqttClient.setCallback(mqttCallback);
    }
    
    public void disConnect() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                mqttClientStatus.disconnect();
            }
            catch (MqttException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
}
