package net.macdidi5.picommander;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.List;

public class MainActivity extends ActionBarActivity {

    private RelativeLayout main;
    private GridView gridview;
    private ImageView connect_imageview;
    private CommandAdapter commandAdapter;
    private List<CommanderItem> commanderItems;

    private static final String TOPIC = "PiCommander";
    private static final String TOPIC_STATUS = "PiCommanderStatus";
    private static final int QOS = 2;
    private static String clientId = "PiCommanderAndroid";
    private static MqttClient mqttClient;

    private static final int REQUEST_CONNECT = 0;
    private static final int REQUEST_ITEM = 1;

    public static final String ADD_ITEM_ACTION =
            "net.macdidi5.picommander.ADD_ITEM";
    public static final String DELETE_ITEM_ACTION =
            "net.macdidi5.picommander.DELETE_ITEM";

    private boolean processMenu = false;

    private static final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        commanderItems = TurtleUtil.getCommanders(this);

        processViews();
        processControllers();

        commandAdapter = new CommandAdapter(this, commanderItems);
        gridview.setAdapter(commandAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            // After add or delete item
            if (requestCode == REQUEST_ITEM) {
                int itemPosition = data.getIntExtra("itemPosition", -1);

                // Add item
                if (itemPosition == -1) {
                    String gpioName = data.getStringExtra("gpioName");
                    String desc = data.getStringExtra("desc");
                    int address = data.getIntExtra("address", -1);
                    String mcpType = data.getStringExtra("mcpType");

                    CommanderItem item = null;

                    // Raspberry Pi GPIO
                    if (address == -1) {
                        item = new CommanderItem(gpioName, desc);
                    }
                    // GPIO Expander
                    else {
                        item = new ExpanderCommanderItem(
                                gpioName, desc, address,
                                McpGpioExpander.fromString(mcpType));
                    }

                    commandAdapter.add(item);
                }
                // Remove item
                else {
                    commandAdapter.remove(itemPosition);
                    TurtleUtil.deleteCommander(this, itemPosition);
                }

                TurtleUtil.saveCommanders(this, commandAdapter.getItems());
                commandAdapter.notifyDataSetChanged();

                if (mqttClient != null && mqttClient.isConnected()) {
                    commandAdapter.refresh();
                }
            }
            // After connect to MQTT broker
            else if (requestCode == REQUEST_CONNECT) {
                String brokerIp = data.getStringExtra("brokerIp");
                String brokerPort = data.getStringExtra("brokerPort");

                processConnect(brokerIp, brokerPort);
            }
        }

        processMenu = false;
    }

    @Override
    public void onDestroy() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
            }
            catch (MqttException me) {
                Log.d(LOG_TAG, me.toString());
            }
        }

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void processViews() {
        main = (RelativeLayout)findViewById(R.id.main);
        gridview = (GridView)findViewById(R.id.gridview);
        connect_imageview = (ImageView)findViewById(R.id.connect_imageview);
    }

    private void processControllers() {
        // Click, control command block
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (mqttClient != null && mqttClient.isConnected()) {
                    commandAdapter.toggle(position);
                    commandAdapter.notifyDataSetChanged();
                }
            }
        });

        // Long click, delete command block
        gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                CommanderItem item = (CommanderItem)commandAdapter.getItem(position);

                Intent intent = new Intent(DELETE_ITEM_ACTION);
                intent.putExtra("gpioName", item.getGpioName());
                intent.putExtra("desc", item.getDesc());
                intent.putExtra("itemPosition", position);

                startActivityForResult(intent, REQUEST_ITEM);
                return true;
            }
        });
    }

    private void processConnect(String brokerIp, String brokerPort) {
        String broker = "tcp://" + brokerIp + ":" + brokerPort;

        try {
            clientId = clientId + System.currentTimeMillis();

            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(true);

            mqttClient = new MqttClient(broker, clientId, new MemoryPersistence());
            mqttClient.setCallback(new MqttCallbackHandler());
            mqttClient.connect(mqttConnectOptions);
            mqttClient.subscribe(TOPIC_STATUS);

            commandAdapter.refresh();

            main.setBackgroundResource(R.drawable.background_enable);
            connect_imageview.setImageResource(R.drawable.mq_connected);

            Toast.makeText(this, R.string.message_connected,
                    Toast.LENGTH_LONG).show();
        }
        catch (MqttException me) {
            Toast.makeText(this, R.string.message_connect_failed,
                    Toast.LENGTH_LONG).show();
        }
    }

    public void clickAdd(MenuItem item) {
        if (!processMenu) {
            processMenu = true;

            Intent intent = new Intent(ADD_ITEM_ACTION);
            intent.putExtra("menuItemId", item.getItemId());
            startActivityForResult(intent, REQUEST_ITEM);
        }
    }

    public void clickConnect(MenuItem item) {
        if (!processMenu) {
            processMenu = true;

            startActivityForResult(new Intent(this, ConnectActivity.class),
                    REQUEST_CONNECT);
        }
    }

    private class CommandAdapter extends BaseAdapter {

        private Context context;
        private List<CommanderItem> commanderItems;

        public CommandAdapter(Context context, List<CommanderItem> commanderItems) {
            this.context = context;
            this.commanderItems = commanderItems;
        }

        public void add(CommanderItem commanderItem) {
            commanderItems.add(commanderItem);
        }

        public void toggle(int position) {
            CommanderItem item = commanderItems.get(position);
            item.setStatus(!item.isStatus());

            String gpioName = item.getGpioName();
            String content = gpioName + "," + (item.isStatus() ?
                    getString(R.string.commander_on) :
                    getString(R.string.commander_off));

            // GPIO Expander
            if (item instanceof ExpanderCommanderItem) {
                ExpanderCommanderItem eci = (ExpanderCommanderItem)item;
                content += "," + eci.getAddress() + "," + eci.getType();
            }

            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(QOS);

            try {
                mqttClient.publish(TOPIC, message);
            }
            catch (MqttException me) {
                Log.d(LOG_TAG, me.toString());
            }
        }

        public void remove(int position) {
            commanderItems.remove(position);
        }

        public List<CommanderItem> getItems() {
            return commanderItems;
        }

        public void refresh() {
            for (CommanderItem item : commanderItems) {
                String content = item.getGpioName() + "," +
                        getString(R.string.commander_status);

                if (item instanceof ExpanderCommanderItem) {
                    ExpanderCommanderItem eci = (ExpanderCommanderItem) item;
                    content += ("," + eci.getAddress() + "," + eci.getType());
                }

                MqttMessage message = new MqttMessage(content.getBytes());
                message.setQos(QOS);

                try {
                    mqttClient.publish(TOPIC, message);
                }
                catch (MqttException me) {
                    Log.d("CommandAdapter", me.toString());
                }
            }
        }

        @Override
        public int getCount() {
            return commanderItems.size();
        }

        @Override
        public Object getItem(int position) {
            return commanderItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout view;

            if (convertView == null) {
                view = new LinearLayout(context);
                inflater.inflate(R.layout.commander_item, view, true);
            }
            else {
                view = (LinearLayout)convertView;
            }

            TextView item_desc = (TextView)view.findViewById(R.id.item_desc);
            Switch item_switch = (Switch)view.findViewById(R.id.item_switch);

            CommanderItem item = commanderItems.get(position);
            item_desc.setText(item.getDesc());
            item_switch.setChecked(item.isStatus());

            return view;
        }
    }

    private class MqttCallbackHandler implements MqttCallback {

        @Override
        public void connectionLost(Throwable throwable) {
            main.setBackgroundResource(R.drawable.background_disable);
            connect_imageview.setImageResource(R.drawable.mq_disconnected);
            Log.d(LOG_TAG, throwable.toString());
        }

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
            String message = new String(mqttMessage.getPayload());
            final String[] messageArray = message.split(",");

            if (!(messageArray.length == 2 || messageArray.length == 4)) {
                Log.d(LOG_TAG, "Message context malformed");
                return;
            }

            final CommanderItem item = getCommanderItem(messageArray);

            if (item == null) {
                Log.d(LOG_TAG, "Item does not exist");
                return;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    item.setStatus(messageArray[1].equals(getString(R.string.commander_on)));
                    commandAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            //Log.d(LOG_TAG, "deliveryComplete");
        }

    }

    private CommanderItem getCommanderItem(String[] messageArray) {
        boolean isExpander = (messageArray.length == 4);

        String gpioName = messageArray[0];
        String address = null;
        String type = null;

        // GPIO Expander
        if (isExpander) {
            address = messageArray[2];
            type = messageArray[3];
        }

        CommanderItem result = null;

        for (CommanderItem item : commanderItems) {
            // GPIO Expander
            if (isExpander) {
                if (item instanceof ExpanderCommanderItem) {
                    ExpanderCommanderItem eci = (ExpanderCommanderItem) item;

                    if (eci.getGpioName().equals(gpioName) &&
                            eci.getAddress() == Integer.parseInt(address) &&
                            eci.getType() == McpGpioExpander.fromString(type)) {
                        result = item;
                        break;
                    }
                }
            }
            // Raspberry Pi GPIO
            else {
                if (item.getGpioName().equals(gpioName)) {
                    result = item;
                    break;
                }
            }
        }

        return result;
    }

}
