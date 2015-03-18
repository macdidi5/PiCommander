package net.macdidi5.picommander;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class TurtleUtil {

    private static SharedPreferences sp = null;

    public static final String KEY_BROKER_IP = "BROKER_IP";
    public static final String KEY_BROKER_PORT = "BROKER_PORT";

    public static final String KEY_BUTTON = "PI_COMMANDER_BUTTON";

    /**
     * Read MQTT broker IP Address
     *
     * @param context Android Context
     * @return MQTT broker IP Address
     */
    public static String getBrokerIP(Context context) {
        return getSharedPreferences(context).getString(
                KEY_BROKER_IP, context.getString(R.string.default_broker_ip));
    }

    /**
     * Read MQTT broker Port number
     *
     * @param context Android Context
     * @return MQTT broker port number
     */
    public static String getBrokerPort(Context context) {
        return getSharedPreferences(context).getString(
                KEY_BROKER_PORT, context.getString(R.string.default_broker_port));
    }

    /**
     * Save MQTT broker IP Address
     *
     * @param context Android Context
     * @param ip MQTT broker IP Address
     */
    public static void saveBrokerIP(Context context, String ip) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
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
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(KEY_BROKER_PORT, port);
        editor.commit();
    }

    /**
     * Save user define command blocks
     *
     * @param context Android Context
     * @param items Command block objects
     */
    public static void saveCommanders(Context context, List<CommanderItem> items) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();

        for (int i = 0; i < items.size(); i++) {
            CommanderItem item = items.get(i);

            String data = item.getGpioName() + "," +
                    item.getDesc() + "," +
                    item.getHighDesc() + "," +
                    item.getLowDesc();

            if (item instanceof ExpanderCommanderItem) {
                ExpanderCommanderItem eci = (ExpanderCommanderItem)item;
                data += "," + eci.getAddress() + "," +
                        eci.getType();
            }

            editor.putString(KEY_BUTTON + String.format("%02d", i), data);
        }

        editor.commit();
    }

    /**
     * Save user define command block
     *
     * @param context Android Context
     * @param position GridView item position
     */
    public static void deleteCommander(Context context, int position) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(KEY_BUTTON + String.format("%02d", position));
        editor.commit();
    }

    /**
     * Read user define command blocks
     *
     * @param context Android Context
     * @return All command block objects
     */
    public static List<CommanderItem> getCommanders(Context context) {
        List<CommanderItem> result = new ArrayList<>();
        SharedPreferences sp = getSharedPreferences(context);

        int counter = 0;

        while (true) {
            String key = KEY_BUTTON + String.format("%02d", counter);
            String content = sp.getString(key, null);

            if (content == null) {
                break;
            }

            String[] ds = content.split(",");

            CommanderItem item = null;

            if (ds.length == 4) {
                item = new CommanderItem(ds[0], ds[1], ds[2], ds[3]);
            }
            else if (ds.length == 6) {
                item = new ExpanderCommanderItem(ds[0], ds[1], ds[2], ds[3],
                        Integer.parseInt(ds[4]), McpGpioExpander.fromString(ds[5]));
            }

            if (item != null) {
                result.add(item);
                counter++;
            }
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

}
