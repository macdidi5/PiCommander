package net.macdidi5.picommander;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class ListenService extends Service {

    private MqttClient mqttClient;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (TurtleUtil.checkNetwork(this)) {
            connect();
        }
        else {
            Log.d("ListenService", "onStartCommand: Connection required.");
        }

        return Service.START_STICKY;
    }

    public void connect() {
        Log.d("=========", "connect1");
        try {
            if (mqttClient == null || !mqttClient.isConnected()) {
                Log.d("=========", "connect2");
                String broker = "tcp://" + TurtleUtil.getBrokerIP(this) +
                        ":" + TurtleUtil.getBrokerPort(this);
                String clientId = "PiCommanderAndroidService" +
                        System.currentTimeMillis();
                mqttClient = new MqttClient(broker, clientId,
                        new MemoryPersistence());
                mqttClient.setCallback(new MqttListenServiceHandler(this));

                MqttConnectOptions mqttConnectOptions =
                        new MqttConnectOptions();
                mqttConnectOptions.setCleanSession(true);
                mqttConnectOptions.setConnectionTimeout(MainActivity.TIMEOUT);

                mqttClient.connect(mqttConnectOptions);
                mqttClient.subscribe(MainActivity.TOPIC_STATUS);
                Log.d("=========", "connect3");
            }
        }
        catch (MqttException me) {
            Log.d("ListenService", "connect: " + me.toString());
        }
    }

    @Override
    public void onDestroy() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
            }
            catch (MqttException me) {
                Log.d("ListenService", me.toString());
            }
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ListenServiceBinder();
    }

    public class ListenServiceBinder extends Binder {
        public ListenService getListenService() {
            return ListenService.this;
        }
    }

    private class MqttListenServiceHandler implements MqttCallback {

        private Context context;

        public MqttListenServiceHandler(Context context) {
            this.context = context;
        }

        @Override
        public void connectionLost(Throwable throwable) {

        }

        @Override
        public void messageArrived(String s, MqttMessage mqttMessage)
                throws Exception {
            String message = new String(mqttMessage.getPayload());

            final String[] messageArray = message.split(",");

            if (!(messageArray.length == 2 || messageArray.length == 4)) {
                Log.d("ListenService", "Message context malformed");
                return;
            }

            boolean isExpander = (messageArray.length == 4);

            String gpioName = messageArray[0];
            String address = null;
            String type = null;

            if (isExpander) {
                address = messageArray[2];
                type = messageArray[3];
            }

            CommanderItem item = MainActivity.getCommanderItem(
                    TurtleUtil.getListeners(context), isExpander,
                    gpioName, address, type);

            if (item != null) {
                item.setStatus(messageArray[1].equals(getString(R.string.commander_on)));

                if ((item.isStatus() && item.isHighNotify()) ||
                        (!item.isStatus() && item.isLowNotify())) {
                    String nm = item.isStatus() ?
                            item.getHighDesc() :
                            item.getLowDesc();
                    nm = item.getDesc() + ":" + nm;

                    NotificationCompat.Builder builder =
                            new NotificationCompat.Builder(context);

                    PendingIntent pendingIntent =
                            PendingIntent.getActivity(context, 0, new Intent(), 0);

                    builder.setSmallIcon(android.R.drawable.ic_popup_reminder)
                           .setTicker(context.getString(R.string.app_name))
                           .setContentTitle(context.getString(R.string.app_name))
                           .setContentText(nm)
                           .setDefaults(Notification.DEFAULT_SOUND)
                           .setContentIntent(pendingIntent)
                           .setAutoCancel(true);

                    NotificationManager manager =(NotificationManager)
                            getSystemService(Context.NOTIFICATION_SERVICE);
                    Notification notification = builder.build();
                    manager.notify(0, notification);
                }
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

        }
    }

}
