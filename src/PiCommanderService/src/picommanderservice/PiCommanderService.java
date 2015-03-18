package picommanderservice;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class PiCommanderService {

    private static final String USAGE = 
            "Usage: GPIO n,ON/OFF/STATUS/LISTEN[,Address,Type]";
    
    private static final String TOPIC = "PiCommander";
    private static final String TOPIC_STATUS = "PiCommanderStatus";
    private static String broker_host = "localhost";
    private static String broker_port = "1883";
    private static final String clientId = "PiCommanderService";
    
    private static boolean exit = false;
    
    public static void main(String[] args) {
        
        if (args.length == 1) {
            broker_host = args[0];
        }
        
        if (args.length == 2) {
            broker_port = args[1];
        }        

        final GpioController gpio = GpioFactory.getInstance();
        
        final MqttService service = 
                new MqttService(broker_host, broker_port, clientId);
        
        final GpioPi gpioPi = new GpioPi(gpio);
        final GpioExpander gpioExpander = new GpioExpander(gpio);
        
        class MqttCallbackHandler implements MqttCallback {
        
            private final GpioController gpio;
        
            public MqttCallbackHandler(GpioController gpio) {
                this.gpio = gpio;
            }

            @Override
            public void connectionLost(Throwable throwable) {
                System.out.println("PiCommanderService Disconnect...");
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) 
                    throws Exception {
                String message = new String(mqttMessage.getPayload());
                System.out.println(message);
            
                String[] messageArray = message.split(",");
                int length = messageArray.length;
            
                if (!(length == 2 || length == 4)) {
                    System.out.println(USAGE);
                    return;
                }

                if (messageArray[0].equals("EXIT")) {
                    exit = true;
                    return;
                }

                GpioPinDigitalMultipurpose pin = null;
                
                if (length == 2) {
                    pin = gpioPi.getPin(messageArray[0]);
                }
                else if (length == 4) {
                    int address = Integer.parseInt(messageArray[2]);
                    pin = gpioExpander.getPin(address, 
                            McpGpioExpander.fromString(messageArray[3]), 
                            messageArray[0]);
                }
            
                if (pin == null) {
                    System.out.println(USAGE + "2");
                    return;
                }
            
                switch (messageArray[1]) {
                    case "ON":
                        pin.high();
                        break;
                    case "OFF":
                        pin.low();
                        break;
                    case "LISTEN":
                        // Not yet...
                        break;
                }
            
                String status = pin.isHigh() ? "ON" : "OFF";
                String publishMessage = pin.getName() + "," + status;
                
                if (length == 4) {
                    publishMessage += ("," + messageArray[2] + "," + messageArray[3]);
                }
                
                service.publish(TOPIC_STATUS, publishMessage);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                
            }

        }
        
        service.setCallback(new MqttCallbackHandler(gpio));
        service.connect();
        service.subscribe(TOPIC);
        
        System.out.println("PiCommanderService Ready...");
        
        while (!exit) {
            delay(250);
        }
        
        System.out.println("PiCommanderService Bye...");
        
        service.disConnect();
        gpio.shutdown();
    }
    
    private static void delay(int ms) {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException e) {
            System.out.println(e.toString());
        }
    }
    
}
