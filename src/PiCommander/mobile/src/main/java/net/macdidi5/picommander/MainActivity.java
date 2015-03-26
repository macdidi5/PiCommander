package net.macdidi5.picommander;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
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

    public static final String TOPIC = "PiCommander";
    public static final String TOPIC_STATUS = "PiCommanderStatus";
    public static final int QOS = 2;
    public static final int TIMEOUT = 5;

    private static String clientId = "PiCommanderAndroid";
    private static MqttClient mqttClient;

    private static final int REQUEST_CONNECT = 0;
    private static final int REQUEST_ITEM = 1;

    public static final String ADD_ITEM_ACTION =
            "net.macdidi5.picommander.ADD_ITEM";
    public static final String DELETE_ITEM_ACTION =
            "net.macdidi5.picommander.DELETE_ITEM";

    private static final String LOG_TAG = "MainActivity";

    private RelativeLayout main;
    private CommandPagerAdapter commandPagerAdapter;
    private ViewPager mypager;
    private ImageView connect_imageview;

    private static CommandAdapter controllerCommandAdapter, listenerCommandAdapter;
    private List<CommanderItem> controllerCommanderItems, listenerCommanderItems;

    private Fragment[] fragments;

    private boolean processMenu = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        controllerCommanderItems = TurtleUtil.getControllers(this);
        listenerCommanderItems = TurtleUtil.getListeners(this);

        controllerCommandAdapter = new CommandAdapter(this, controllerCommanderItems);
        listenerCommandAdapter = new CommandAdapter(this, listenerCommanderItems);

        fragments = new Fragment[] {
                CommandFragment.newInstance(0),
                CommandFragment.newInstance(1)
        };

        commandPagerAdapter = new CommandPagerAdapter(
                getSupportFragmentManager(), fragments);

        mypager = (ViewPager) findViewById(R.id.mypager);
        mypager.setAdapter(commandPagerAdapter);

        processViews();

        startService(new Intent(this, ListenService.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            // After add or delete item
            if (requestCode == REQUEST_ITEM) {
                processItem(data);
            }
            // After connect to MQTT broker
            else if (requestCode == REQUEST_CONNECT) {
                String brokerIp = data.getStringExtra("brokerIp");
                String brokerPort = data.getStringExtra("brokerPort");

                processConnect(brokerIp, brokerPort);
                processServiceConnect();
            }
        }

        processMenu = false;
    }

    private void processServiceConnect() {
        ServiceConnection serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName,
                                           IBinder iBinder) {
                ListenService.ListenServiceBinder binder =
                        (ListenService.ListenServiceBinder) iBinder;
                ListenService listenService = binder.getListenService();

                if (listenService != null) {
                    listenService.connect();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };

        Intent bindIntent = new Intent(this, ListenService.class);
        bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void processItem(Intent data) {
        int itemPosition = data.getIntExtra("itemPosition", -1);
        int commandType = data.getIntExtra("commandType", -1);
        String commandTypeValue = commandType == 0 ?
                TurtleUtil.CONTROLLER_COMMANDER :
                TurtleUtil.LISTENER_COMMANDER;

        // Add item
        if (itemPosition == -1) {
            String gpioName = data.getStringExtra("gpioName");
            String desc = data.getStringExtra("desc");
            int addressValue = data.getIntExtra("addressValue", -1);
            String mcpType = data.getStringExtra("mcpType");

            CommanderItem item = null;

            // Raspberry Pi GPIO
            if (addressValue == -1) {
                item = new CommanderItem(
                        gpioName, desc, commandTypeValue);
            }
            // GPIO Expander
            else {
                item = new ExpanderCommanderItem(
                        gpioName, desc, commandTypeValue, addressValue,
                        McpGpioExpander.fromString(mcpType));
            }

            if (commandType == 0) {
                controllerCommandAdapter.add(item);
                TurtleUtil.saveCommanders(this,
                        controllerCommandAdapter.getItems());
            }
            else {
                item.setHighDesc(data.getStringExtra("highDesc"));
                item.setLowDesc(data.getStringExtra("lowDesc"));
                item.setHighNotify(data.getBooleanExtra("highNotify", false));
                item.setLowNotify(data.getBooleanExtra("lowNotify", false));

                listenerCommandAdapter.add(item);
                TurtleUtil.saveCommanders(this,
                        listenerCommandAdapter.getItems());
            }
        }
        // Remove item
        else {
            if (commandType == 0) {
                controllerCommandAdapter.remove(itemPosition);
            }
            else {
                listenerCommandAdapter.remove(itemPosition);
            }

            TurtleUtil.deleteCommander(this, itemPosition, commandTypeValue);
        }

        // Controller or Listener
        if (commandType == 0) {
            controllerCommandAdapter.notifyDataSetChanged();
        }
        else {
            listenerCommandAdapter.notifyDataSetChanged();
        }

        if (itemPosition == -1 && mqttClient != null &&
                mqttClient.isConnected()) {
            if (commandType == 0) {
                controllerCommandAdapter.refresh();
            }
            else {
                listenerCommandAdapter.refresh();
            }
        }
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
        connect_imageview = (ImageView)findViewById(R.id.connect_imageview);
    }

    private void processConnect(String brokerIp, String brokerPort) {
        String broker = "tcp://" + brokerIp + ":" + brokerPort;

        try {
            clientId = clientId + System.currentTimeMillis();

            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(true);
            mqttConnectOptions.setConnectionTimeout(TIMEOUT);

            mqttClient = new MqttClient(broker, clientId, new MemoryPersistence());
            mqttClient.setCallback(new MqttCallbackHandler());
            mqttClient.connect(mqttConnectOptions);
            mqttClient.subscribe(TOPIC_STATUS);

            controllerCommandAdapter.refresh();
            listenerCommandAdapter.refresh();

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
            int commandType = mypager.getCurrentItem();

            Intent intent = new Intent(ADD_ITEM_ACTION);
            intent.putExtra("menuItemId", item.getItemId());
            intent.putExtra("commandType", commandType);

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
                String content = item.getGpioName() + "," + (
                        item.getCommandType().equals(TurtleUtil.CONTROLLER_COMMANDER) ?
                        getString(R.string.commander_status) :
                        getString(R.string.commander_listen));

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

            if (item.getCommandType().equals(TurtleUtil.LISTENER_COMMANDER)) {
                String statusText = item_switch.isChecked() ?
                        item.getHighDesc() : item.getLowDesc();
                item_desc.setText(statusText + "\n" + item.getDesc());
                item_switch.setClickable(false);
            }

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

                    if (item.getCommandType().equals(TurtleUtil.CONTROLLER_COMMANDER)) {
                        controllerCommandAdapter.notifyDataSetChanged();
                    }
                    else {
                        listenerCommandAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            //
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

        result = getCommanderItem(controllerCommanderItems, isExpander,
                gpioName, address, type);

        if (result == null) {
            result = getCommanderItem(listenerCommanderItems, isExpander,
                    gpioName, address, type);
        }

        return result;
    }

    public static CommanderItem getCommanderItem(List<CommanderItem> items,
                                           boolean isExpander,
                                           String gpioName,
                                           String address,
                                           String type) {
        CommanderItem result = null;

        for (CommanderItem item : items) {
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

    public class CommandPagerAdapter extends FragmentPagerAdapter {

        private Fragment[] fragments;

        public CommandPagerAdapter(FragmentManager fragmentManager,
                                   Fragment[] fragments) {
            super(fragmentManager);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "GPIO Controller";
                case 1:
                    return "GPIO Listener";
            }

            return null;
        }

    }

    public static class CommandFragment extends Fragment {

        private static final String KEY_POSITION = "position";

        public static CommandFragment newInstance(int position) {

            CommandFragment result = new CommandFragment();
            Bundle args = new Bundle();
            args.putInt(KEY_POSITION, position);
            result.setArguments(args);

            return result;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_commander, container, false);
            GridView item_gridview = (GridView)rootView.findViewById(R.id.item_gridview);

            int position = getArguments().getInt(KEY_POSITION);
            processControllers(item_gridview, position, getActivity());

            if (position == 0) {
                item_gridview.setAdapter(controllerCommandAdapter);
            }
            else if (position == 1) {
                item_gridview.setAdapter(listenerCommandAdapter);
            }

            return rootView;
        }
    }

    private static void processControllers(GridView gridview, final int commandType,
                                           final Context context) {
        // Click, control command block
        if (commandType == 0) {
            gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    if (mqttClient != null && mqttClient.isConnected()) {
                        controllerCommandAdapter.toggle(position);
                        controllerCommandAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        // Long click, delete command block
        gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                CommanderItem item;

                if (commandType == 0) {
                    item = (CommanderItem)controllerCommandAdapter.getItem(position);
                }
                else {
                    item = (CommanderItem)listenerCommandAdapter.getItem(position);
                }

                Intent intent = new Intent(DELETE_ITEM_ACTION);

                intent.putExtra("commandType", commandType);
                intent.putExtra("gpioName", item.getGpioName());
                intent.putExtra("desc", item.getDesc());
                intent.putExtra("itemPosition", position);

                ((Activity)context).startActivityForResult(intent, REQUEST_ITEM);
                return true;
            }
        });
    }

}
