package gr.tuc.senselab.messageinabubble.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import gr.tuc.senselab.messageinabubble.network.XmppConnection;
import gr.tuc.senselab.messageinabubble.utils.Bubble;
import gr.tuc.senselab.messageinabubble.utils.events.AccountCreationEvent;
import gr.tuc.senselab.messageinabubble.utils.events.LoginEvent;
import gr.tuc.senselab.messageinabubble.utils.events.NewMessageEvent;
import org.greenrobot.eventbus.EventBus;

public class XmppConnectionService extends Service {

    private final IBinder binder = new LocalBinder();

    private boolean isThreadActive;
    private Thread thread;
    private Handler threadHandler;

    private XmppConnection xmppConnection;

    private Context context;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getApplicationContext();
        startThread();
        return Service.START_STICKY;
    }

    private void startThread() {
        if (!isThreadActive) {
            isThreadActive = true;
            if (thread == null || !thread.isAlive()) {
                thread = new Thread(() -> {
                    Looper.prepare();
                    threadHandler = new Handler(Looper.getMainLooper());
                    try {
                        xmppConnection = new XmppConnection(context);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Looper.loop();
                });
                thread.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
        isThreadActive = false;
    }

    public void connect(String username, String password) {
        threadHandler.post(() -> {
            try {
                if (xmppConnection.isAuthenticated()) {
                    if (xmppConnection.getUsername().equals(username)) {
                        return;
                    } else {
                        xmppConnection.disconnect();
                    }
                }
                if (!xmppConnection.isConnected()) {
                    xmppConnection.connect();
                }
                xmppConnection.login(username, password);
                EventBus.getDefault().post(new LoginEvent(null));
            } catch (Exception e) {
                EventBus.getDefault().post(new LoginEvent(e));
            }
        });
    }

    public void disconnect() {
        threadHandler.post(() -> {
            if (xmppConnection.isConnected()) {
                xmppConnection.disconnect();
            }
        });
    }

    public void sendMessage(Bubble bubble) {
        threadHandler.post(() -> {
            try {
                xmppConnection.sendMessage(bubble);
                EventBus.getDefault().postSticky(new NewMessageEvent(bubble, null));
            } catch (Exception e) {
                EventBus.getDefault().postSticky(new NewMessageEvent(bubble, e));
            }
        });
    }

    public void createAccount(String username, String password) {
        threadHandler.post(() -> {
            try {
                if (xmppConnection.isAuthenticated()) {
                    xmppConnection.disconnect();
                }
                if (!xmppConnection.isConnected()) {
                    xmppConnection.connect();
                }
                xmppConnection.login();
                xmppConnection.createAccount(username, password);
                xmppConnection.disconnect();
                EventBus.getDefault().post(new AccountCreationEvent(null));
            } catch (Exception e) {
                EventBus.getDefault().post(new AccountCreationEvent(e));
            }
        });
    }

    public class LocalBinder extends Binder {

        public XmppConnectionService getService() {
            return XmppConnectionService.this;
        }
    }
}
