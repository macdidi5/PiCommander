package net.macdidi5.picommander;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemActivity extends Activity {

    private Spinner gpio_spinner, address_spinner;
    private TextView type_textview;
    private EditText desc_edittext;
    private ImageButton add_or_delete_item;

    private boolean isAdd = false;
    private int itemPosition;
    private int addMenuItemId;
    private int commandType;
    private String mcpType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_item);

        Intent intent = getIntent();
        String action = intent.getAction();
        isAdd = action.equals(MainActivity.ADD_ITEM_ACTION);
        commandType = intent.getIntExtra("commandType", -1);

        processViews();
        processControllers();

        addMenuItemId = intent.getIntExtra("menuItemId", -1);
        mcpType = getString((addMenuItemId == R.id.add_mcp23008_menu) ?
                        R.string.mcp23008_name : R.string.mcp23017_name);

        switch (addMenuItemId) {
            // Raspberry GPIO
            case R.id.add_pi_gpio_menu:
                type_textview.setVisibility(View.GONE);
                address_spinner.setVisibility(View.GONE);
                break;
            // GPIO Expander
            case R.id.add_mcp23008_menu:
            case R.id.add_mcp23017_menu:
                type_textview.setText(mcpType);

                ArrayAdapter<CharSequence> adapterAddress =
                        ArrayAdapter.createFromResource(this,
                        R.array.mcp_addresses, R.layout.select_gpio_view);
                adapterAddress.setDropDownViewResource(R.layout.select_gpio_view);
                address_spinner.setAdapter(adapterAddress);
                break;
        }

        // Add command block
        if (isAdd) {
            refreshGpioSpinner();
        }
        // Delete command block
        else {
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                    this, R.layout.select_gpio_view,
                    new String[]{intent.getStringExtra("gpioName")});
            adapter.setDropDownViewResource(R.layout.select_gpio_view);
            gpio_spinner.setAdapter(adapter);
        }

        // Delete command block
        if (!isAdd) {
            String desc = intent.getStringExtra("desc");
            itemPosition = intent.getIntExtra("itemPosition", 0);
            desc_edittext.setText(desc);

            String type = intent.getStringExtra("type");

            if (type == null) {
                type_textview.setVisibility(View.GONE);
                address_spinner.setVisibility(View.GONE);
            }

            gpio_spinner.setEnabled(false);
            desc_edittext.setEnabled(false);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }

    public void clickAddOrDelete(View view) {
        Intent intent = getIntent();
        intent.putExtra("commandType", commandType);

        // Add command block
        if (isAdd) {
            String gpioName = gpio_spinner.getSelectedItem().toString();
            String desc = desc_edittext.getText().toString();

            if (desc == null || desc.length() == 0) {
                desc = gpioName;
            }

            if (addMenuItemId != R.id.add_pi_gpio_menu) {
                String address = address_spinner.getSelectedItem().toString();
                int addressValue = Integer.parseInt(address.substring(2), 16);

                intent.putExtra("addressValue", addressValue);
                intent.putExtra("mcpType", mcpType);

                if (desc.equals(gpioName)) {
                    desc = address + ":" + desc;
                }
            }

            intent.putExtra("gpioName", gpioName);
            intent.putExtra("desc", desc);

            // Controller
            if (commandType == 0) {
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
            // Listener
            else {
                Bundle bundle = intent.getExtras();
                Intent intentListen =
                        new Intent(this, ItemListenActivity.class);
                intentListen.putExtras(bundle);
                startActivityForResult(intentListen, 0);
            }
        }
        // Delete command block
        else {
            intent.putExtra("itemPosition", itemPosition);
            intent.putExtra("commandType", commandType);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }

    }

    private void processViews() {
        gpio_spinner = (Spinner)findViewById(R.id.gpio_spinner);
        address_spinner = (Spinner)findViewById(R.id.address_spinner);
        type_textview = (TextView)findViewById(R.id.type_textview);
        desc_edittext = (EditText)findViewById(R.id.desc_edittext);
        add_or_delete_item = (ImageButton)findViewById(R.id.add_or_delete_item);


        if (isAdd) {
            add_or_delete_item.setImageResource(
                    commandType == 0 ?
                    android.R.drawable.ic_menu_add :
                    android.R.drawable.ic_menu_info_details);
        }
        else {
            add_or_delete_item.setImageResource(
                    android.R.drawable.ic_menu_delete);
        }
    }

    private void processControllers() {
        address_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView,
                                       View view, int i, long l) {
                refreshGpioSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void refreshGpioSpinner() {
        List<CommanderItem> controllerItems = TurtleUtil.getControllers(this);
        List<CommanderItem> listenerItems = TurtleUtil.getListeners(this);

        String[] gpioNames;

        switch (addMenuItemId) {
            case R.id.add_mcp23008_menu:
                gpioNames = getResources().getStringArray(
                        R.array.mcp23008_gpio_pin_names);
                break;
            case R.id.add_mcp23017_menu:
                gpioNames = getResources().getStringArray(
                        R.array.mcp23017_gpio_pin_names);
                break;
            default:
                gpioNames = getResources().getStringArray(
                        R.array.gpio_pin_names);
                break;
        }

        String[] gpioLeft = gpioNames;

        if (controllerItems.size() > 0) {
            gpioLeft = removeArray(gpioNames, controllerItems);
        }

        if (listenerItems.size() > 0) {
            gpioLeft = removeArray(gpioLeft, listenerItems);
        }

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                this, R.layout.select_gpio_view, gpioLeft);
        adapter.setDropDownViewResource(R.layout.select_gpio_view);
        gpio_spinner.setAdapter(adapter);
    }

    private String[] removeArray(String[] source, List<CommanderItem> items) {
        List<String> list = new ArrayList<>(Arrays.asList(source));
        boolean isExpander;

        for (CommanderItem item : items) {
            isExpander = (item instanceof ExpanderCommanderItem);

            if (addMenuItemId == R.id.add_pi_gpio_menu) {
                if (!isExpander) {
                    list.remove(item.getGpioName());
                }
            }
            else {
                if (isExpander) {
                    ExpanderCommanderItem eci = (ExpanderCommanderItem)item;

                    String address = address_spinner.getSelectedItem().toString();
                    int addressValue = Integer.parseInt(address.substring(2), 16);

                    if (eci.getAddress() == addressValue) {
                        list.remove(eci.getGpioName());
                    }
                }
            }
        }

        String[] result = new String[list.size()];

        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }

        return result;
    }

}