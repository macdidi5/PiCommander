package net.macdidi5.picommander;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class ConnectActivity extends Activity {

    private EditText ip_textview, port_textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_connect);

        checkNetwork();
        processViews();
    }

    public void clickOK(View view) {
        String brokerIp = ip_textview.getText().toString();
        String brokerPort = port_textview.getText().toString();

        TurtleUtil.saveBrokerIP(this, brokerIp);
        TurtleUtil.saveBrokerPort(this, brokerPort);

        Intent intent = getIntent();
        intent.putExtra("brokerIp", brokerIp);
        intent.putExtra("brokerPort", brokerPort);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void checkNetwork() {
        if (!TurtleUtil.checkNetwork(this)) {
            AlertDialog.Builder ab = new AlertDialog.Builder(this);
            ab.setMessage(R.string.connection_require);
            ab.setTitle(android.R.string.dialog_alert_title);
            ab.setIcon(android.R.drawable.ic_dialog_alert);
            ab.setCancelable(false);
            ab.setPositiveButton(getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
            ab.show();
        }
    }

    private void processViews() {
        ip_textview = (EditText)findViewById(R.id.ip_textview);
        port_textview = (EditText)findViewById(R.id.port_textview);

        ip_textview.setText(TurtleUtil.getBrokerIP(this));
        port_textview.setText(TurtleUtil.getBrokerPort(this));
    }

}
