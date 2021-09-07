package gr.tuc.senselab.messageinabubble.activities;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.textfield.TextInputLayout;
import gr.tuc.senselab.messageinabubble.R;
import gr.tuc.senselab.messageinabubble.services.XmppConnectionService;
import gr.tuc.senselab.messageinabubble.utils.events.LoginEvent;
import java.util.ArrayList;
import java.util.Objects;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_RESPONSE_CODE = 1;
    private static final String[] PERMISSIONS_REQUIRED = {
            ACCESS_FINE_LOCATION,
            WRITE_EXTERNAL_STORAGE,
    };
    private TextInputLayout usernameTextInputLayout;
    private TextInputLayout passwordTextInputLayout;
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
        setContentView(R.layout.activity_login);

        requestPermissions();
        getUIElements();
        startXmppConnectionService();
    }

    private void startXmppConnectionService() {
        startService(new Intent(this, XmppConnectionService.class));
        bindService(new Intent(this, XmppConnectionService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    private void getUIElements() {
        Button loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(this::onLogInButtonClick);

        Button createAccountButton = findViewById(R.id.create_account);
        createAccountButton.setOnClickListener(this::onCreateAccountButtonClick);

        usernameTextInputLayout = findViewById(R.id.username);
        passwordTextInputLayout = findViewById(R.id.password);
    }

    private void requestPermissions() {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : PERMISSIONS_REQUIRED) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            String[] permissions = new String[permissionsToRequest.size()];
            permissions = permissionsToRequest.toArray(permissions);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RESPONSE_CODE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, XmppConnectionService.class));
        if (isServiceBound) {
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

    public void onLogInButtonClick(View view) {
        usernameTextInputLayout.setError(null);
        passwordTextInputLayout.setError(null);

        String jid = Objects.requireNonNull(usernameTextInputLayout.getEditText()).getText()
                .toString();
        String password = Objects.requireNonNull(passwordTextInputLayout.getEditText())
                .getText()
                .toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            passwordTextInputLayout.setError(getString(R.string.error_field_required));
            focusView = passwordTextInputLayout;
            cancel = true;
        }

        if (TextUtils.isEmpty(jid)) {
            usernameTextInputLayout.setError(getString(R.string.error_field_required));
            focusView = usernameTextInputLayout;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            if (isServiceBound) {
                xmppConnectionService.connect(jid, password);
            }
        }
    }

    public void onCreateAccountButtonClick(View view) {
        startActivity(new Intent(this, CreateAccountActivity.class));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginEvent(LoginEvent event) {
        Exception exception = event.getException();
        if (exception == null) {
            Toast.makeText(LoginActivity.this, "Connection Successful", Toast.LENGTH_LONG).show();
            startActivity(new Intent(LoginActivity.this, MapActivity.class));
        } else {
            Toast.makeText(LoginActivity.this, "Connection Failed", Toast.LENGTH_LONG).show();
            exception.printStackTrace();
        }
    }
}
