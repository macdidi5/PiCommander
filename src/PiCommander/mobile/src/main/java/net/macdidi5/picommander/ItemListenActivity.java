package net.macdidi5.picommander;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;

public class ItemListenActivity extends Activity {

    private EditText high_desc_edittext, low_desc_edittext;
    private CheckBox high_notify_checkbox, low_notify_checkbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_item_listen);

        processViews();
    }

    public void clickAdd(View view) {
        String highDesc = high_desc_edittext.getText().toString();
        String lowDesc = low_desc_edittext.getText().toString();
        boolean highNotify = high_notify_checkbox.isChecked();
        boolean lowNotify = low_notify_checkbox.isChecked();

        Intent intent = getIntent();

        intent.putExtra("highDesc", highDesc);
        intent.putExtra("lowDesc", lowDesc);
        intent.putExtra("highNotify", highNotify);
        intent.putExtra("lowNotify", lowNotify);
        setResult(Activity.RESULT_OK, intent);

        finish();
    }

    private void processViews() {
        high_desc_edittext = (EditText)findViewById(R.id.high_desc_edittext);
        low_desc_edittext = (EditText)findViewById(R.id.low_desc_edittext);

        high_notify_checkbox = (CheckBox)findViewById(R.id.high_notify_checkbox);
        low_notify_checkbox = (CheckBox)findViewById(R.id.low_notify_checkbox);
    }

}
