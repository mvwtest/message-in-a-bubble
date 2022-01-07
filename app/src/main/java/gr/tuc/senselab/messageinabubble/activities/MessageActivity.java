package gr.tuc.senselab.messageinabubble.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import gr.tuc.senselab.messageinabubble.R;
import gr.tuc.senselab.messageinabubble.services.XmppConnectionService;
import gr.tuc.senselab.messageinabubble.utils.BubbleDto;

import java.util.Objects;

public class MessageActivity extends AppCompatActivity {

    private TextInputLayout receiverTextInputLayout;
    private TextInputLayout messageTextInputLayout;

    private XmppConnectionService xmppConnectionService;
    private boolean isServiceBound = false;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            XmppConnectionService.LocalBinder binder = (XmppConnectionService.LocalBinder) service;
            xmppConnectionService = binder.getService();
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isServiceBound = false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        getUIElements();
        bindService(new Intent(this, XmppConnectionService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    private void getUIElements() {
        Button sendButton = findViewById(R.id.send);
        sendButton.setOnClickListener(this::onSendButtonClick);

        receiverTextInputLayout = findViewById(R.id.receiver);
        messageTextInputLayout = findViewById(R.id.message);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            unbindService(serviceConnection);
        }
    }

    public void onSendButtonClick(View view) {
        receiverTextInputLayout.setError(null);
        messageTextInputLayout.setError(null);

        String receiver = Objects.requireNonNull(receiverTextInputLayout.getEditText()).getText()
                .toString();
        String bubbleBody = Objects.requireNonNull(messageTextInputLayout.getEditText()).getText()
                .toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(receiver)) {
            receiverTextInputLayout.setError(getString(R.string.error_field_required));
            focusView = receiverTextInputLayout;
            cancel = true;
        }

        if (TextUtils.isEmpty(bubbleBody)) {
            messageTextInputLayout.setError(getString(R.string.error_field_required));
            focusView = messageTextInputLayout;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            if (isServiceBound) {
                double latitude = getIntent().getDoubleExtra("latitude", 0);
                double longitude = getIntent().getDoubleExtra("longitude", 0);
                BubbleDto bubbleDto = new BubbleDto(latitude, longitude, bubbleBody);
                xmppConnectionService.sendMessage(bubbleDto, receiver);
            }
            finish();
        }
    }
}
