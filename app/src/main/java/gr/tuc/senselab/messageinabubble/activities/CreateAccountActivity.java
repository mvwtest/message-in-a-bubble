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
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import gr.tuc.senselab.messageinabubble.R;
import gr.tuc.senselab.messageinabubble.services.XmppConnectionService;
import gr.tuc.senselab.messageinabubble.utils.events.AccountCreationFailedEvent;
import gr.tuc.senselab.messageinabubble.utils.events.AccountCreationSuccessfulEvent;
import java.util.Objects;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class CreateAccountActivity extends AppCompatActivity {

    private TextInputLayout usernameTextInputLayout;
    private TextInputLayout passwordTextInputLayout;
    private TextInputLayout passwordVerificationTextInputLayout;

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
        setContentView(R.layout.activity_create_account);
        getUIElements();
        bindService(new Intent(this, XmppConnectionService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    private void getUIElements() {
        Button registerButton = findViewById(R.id.register);
        registerButton.setOnClickListener(this::onRegisterButtonClick);

        usernameTextInputLayout = findViewById(R.id.username);
        passwordTextInputLayout = findViewById(R.id.password);
        passwordVerificationTextInputLayout = findViewById(R.id.password_verification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            xmppConnectionService.disconnect();
            unbindService(serviceConnection);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public void onRegisterButtonClick(View view) {
        usernameTextInputLayout.setError(null);
        passwordTextInputLayout.setError(null);

        String username = Objects.requireNonNull(usernameTextInputLayout.getEditText()).getText()
                .toString();
        String password = Objects.requireNonNull(passwordTextInputLayout.getEditText()).getText()
                .toString();
        String passwordVerification = Objects.requireNonNull(
                passwordVerificationTextInputLayout.getEditText()).getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!password.equals(passwordVerification)) {
            passwordTextInputLayout.setError(getString(R.string.error_passwords_do_not_match));
            focusView = passwordVerificationTextInputLayout;
            cancel = true;
        }

        if (TextUtils.isEmpty(passwordVerification)) {
            passwordTextInputLayout.setError(getString(R.string.error_field_required));
            focusView = passwordVerificationTextInputLayout;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            passwordTextInputLayout.setError(getString(R.string.error_field_required));
            focusView = passwordTextInputLayout;
            cancel = true;
        }

        if (TextUtils.isEmpty(username)) {
            usernameTextInputLayout.setError(getString(R.string.error_field_required));
            focusView = usernameTextInputLayout;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            if (isServiceBound) {
                xmppConnectionService.createAccount(username, password);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAccountCreationEvent(AccountCreationSuccessfulEvent event) {
        Toast.makeText(this, "Account successfully created", Toast.LENGTH_LONG).show();
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAccountCreationFailedEvent(AccountCreationFailedEvent event) {
        Toast.makeText(this, "Error while trying to create account", Toast.LENGTH_LONG).show();
        event.getException().printStackTrace();
    }
}
