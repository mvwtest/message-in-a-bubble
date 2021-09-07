package gr.tuc.senselab.messageinabubble;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import gr.tuc.senselab.messageinabubble.activities.LoginActivity;
import gr.tuc.senselab.messageinabubble.network.XmppConnection;
import java.net.UnknownHostException;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jxmpp.stringprep.XmppStringprepException;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class TestXmppConnection {

    private XmppConnection xmppConnection;

    @Before
    public void setup() {
        try {
            xmppConnection = createXmppConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private XmppConnection createXmppConnection()
            throws XmppStringprepException, UnknownHostException {
        LoginActivity loginActivity = Robolectric.buildActivity(LoginActivity.class).create()
                .start().resume().get();
        Context context = loginActivity.getApplicationContext();
        return new XmppConnection(context);
    }

    @Test
    public void testConnect() {
        try {
            assertFalse(xmppConnection.isConnected());
            xmppConnection.connect();
            assertTrue(xmppConnection.isConnected());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoginAdmin() {
        try {
            assertFalse(xmppConnection.isAuthenticated());
            xmppConnection.connect();
            assertFalse(xmppConnection.isAuthenticated());
            xmppConnection.login();
            assertTrue(xmppConnection.isAuthenticated());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDisconnect() {
        try {
            xmppConnection.connect();
            xmppConnection.disconnect();
            assertFalse(xmppConnection.isConnected());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetUsernameAdmin() {
        try {
            xmppConnection.connect();
            xmppConnection.login();
            String username = xmppConnection.getUsername();
            assertEquals("admin", username);
            xmppConnection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateUserAndLogin() {
        // We need to manually delete the user
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();
        String username = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        String password = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        try {
            xmppConnection.connect();
            xmppConnection.login();
            xmppConnection.createAccount(username, password);
            xmppConnection.disconnect();
            xmppConnection.connect();
            xmppConnection.login(username, password);
            assertTrue(xmppConnection.isAuthenticated());
            xmppConnection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSendMessage() {
        //TODO: Test sendMessage() function
        assertTrue(true);
    }
}