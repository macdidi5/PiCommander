package net.macdidi5.picommander;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TurtleUtil {

    private static SharedPreferences sp = null;

    public static final String KEY_BROKER_IP = "BROKER_IP";
    public static final String KEY_BROKER_PORT = "BROKER_PORT";

    public static final String KEY_COMMANDER = "PI_COMMANDER";

    public static final String CONTROLLER_COMMANDER = "CONTROLLER";
    public static final String LISTENER_COMMANDER = "LISTENER";

    /**
     * Read MQTT broker IP Address
     *
     * @param context Android Context
     * @return MQTT broker IP Address
     */
    public static String getBrokerIP(Context context) {
        return getSharedPreferences(context).getString(KEY_BROKER_IP,
                context.getString(R.string.default_broker_ip));
    }

    /**
     * Read MQTT broker Port number
     *
     * @param context Android Context
     * @return MQTT broker port number
     */
    public static String getBrokerPort(Context context) {
        return getSharedPreferences(context).getString(KEY_BROKER_PORT,
                context.getString(R.string.default_broker_port));
    }

    /**
     * Save MQTT broker IP Address
     *
     * @param context Android Context
     * @param ip MQTT broker IP Address
     */
    public static void saveBrokerIP(Context context, String ip) {
        SharedPreferences.Editor editor =
                getSharedPreferences(context).edit();
        editor.putString(KEY_BROKER_IP, ip);
        editor.commit();
    }

    /**
     * Save MQTT broker Port number
     *
     * @param context Android Context
     * @return MQTT broker port number
     */
    public static void saveBrokerPort(Context context, String port) {
        SharedPreferences.Editor editor =
                getSharedPreferences(context).edit();
        editor.putString(KEY_BROKER_PORT, port);
        editor.commit();
    }

    /**
     * Save user define command blocks
     *
     * @param context Android Context
     * @param items Command block objects
     */
    public static void saveCommanders(Context context,
                                      List<CommanderItem> items) {
        SharedPreferences.Editor editor =
                getSharedPreferences(context).edit();

        for (int i = 0; i < items.size(); i++) {
            CommanderItem item = items.get(i);

            String data = item.getGpioName() + "," +
                    item.getDesc() + "," +
                    item.getHighDesc() + "," +
                    item.getLowDesc() + "," +
                    item.getCommandType() + "," +
                    item.isHighNotify() + "," +
                    item.isLowNotify();

            if (item instanceof ExpanderCommanderItem) {
                ExpanderCommanderItem eci = (ExpanderCommanderItem)item;
                data += ("," + eci.getAddress() + "," +
                        eci.getType());
            }

            editor.putString(KEY_COMMANDER + item.getCommandType() +
                    String.format("%02d", i), data);

            Log.d("======", KEY_COMMANDER + item.getCommandType() +
                    String.format("%02d", i) + "---" + data);
        }

        editor.commit();
    }

    /**
     * Save user define command block
     *
     * @param context Android Context
     * @param position GridView item position
     */
    public static void deleteCommander(Context context, int position,
                                       String commandType) {
        SharedPreferences.Editor editor =
                getSharedPreferences(context).edit();
        editor.remove(KEY_COMMANDER  + commandType +
                String.format("%02d", position));
        editor.commit();
    }

    public static void logPref(Context context) {
        SharedPreferences sp = getSharedPreferences(context);

        Set<String> keys = sp.getAll().keySet();

        for (String key : keys) {
            Log.d("logPref=====", key + ": " + sp.getString(key, ""));
        }
    }


    public static List<CommanderItem> getControllers(Context context) {
        return readCommanders(context, CONTROLLER_COMMANDER);
    }

    public static List<CommanderItem> getListeners(Context context) {
        return readCommanders(context, LISTENER_COMMANDER);
    }

    /**
     * Read user define command blocks
     *
     * @param context Android Context
     * @return All command block objects
     */
    private static List<CommanderItem> readCommanders(Context context,
                                                      String commandType) {
        List<CommanderItem> result = new ArrayList<>();
        SharedPreferences sp = getSharedPreferences(context);

        int counter = 0;
        String keyPrefix = KEY_COMMANDER + commandType;

        while (true) {
            String key = keyPrefix + String.format("%02d", counter);
            String content = sp.getString(key, null);

            if (content == null) {
                break;
            }

            String[] ds = content.split(",");

            CommanderItem item = null;

            if (ds.length == 7) {
                item = new CommanderItem(ds[0], ds[1], ds[2], ds[3], ds[4],
                        Boolean.parseBoolean(ds[5]), Boolean.parseBoolean(ds[6]));
            }
            else if (ds.length == 9) {
                item = new ExpanderCommanderItem(ds[0], ds[1], ds[2], ds[3],
                        ds[4], Boolean.parseBoolean(ds[5]), Boolean.parseBoolean(ds[6]),
                        Integer.parseInt(ds[7]),
                        McpGpioExpander.fromString(ds[8]));
            }

            if (item != null) {
                result.add(item);
            }

            counter++;
        }

        return result;
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        if (sp == null) {
            return PreferenceManager.getDefaultSharedPreferences(context);
        }
        else {
            return sp;
        }
    }

    public static boolean checkNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        if (info == null || !info.isConnected()) {
            return false;
        }

        return true;
    }

}
