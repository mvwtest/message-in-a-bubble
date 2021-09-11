package gr.tuc.senselab.messageinabubble.network;


import android.content.Context;
import gr.tuc.senselab.messageinabubble.utils.Bubble;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.android.AndroidSmackInitializer;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;

public class XmppConnection {

    // 127.0.0.1 for local openfire server, 10.0.2.2 running in android vm
    private static final String HOST = "10.0.2.2";
    private static final String XMPP_DOMAIN_NAME = "207459e2de01";
    private static final int PORT = 5222;
    private static final String ADMIN_USERNAME = "nick";
    private static final String ADMIN_PASSWORD = "123";

    private final XMPPTCPConnection connection;
    private ChatManager chatManager;


    public XmppConnection(Context context) throws UnknownHostException, XmppStringprepException {
        AndroidSmackInitializer.initialize(context);

        InetAddress address = InetAddress.getByName(HOST);
        DomainBareJid domain = JidCreate.domainBareFrom(XMPP_DOMAIN_NAME);
        XMPPTCPConnectionConfiguration configuration = XMPPTCPConnectionConfiguration.builder()
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setXmppDomain(domain)
                .setHostAddress(address)
                .setPort(PORT)
                .enableDefaultDebugger()
                .build();

        connection = new XMPPTCPConnection(configuration);
    }

    public void connect() throws InterruptedException, XMPPException, SmackException, IOException {
        connection.connect();
    }

    public void login() throws InterruptedException, XMPPException, SmackException, IOException {
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void login(String username, String password)
            throws InterruptedException, IOException, SmackException, XMPPException {
        connection.login(username, password);

        chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addIncomingListener(new IncomingChatMessageListenerImpl());

        ReconnectionManager.getInstanceFor(connection).enableAutomaticReconnection();
    }

    public void sendMessage(Bubble bubble)
            throws XmppStringprepException, SmackException.NotConnectedException,
            InterruptedException, JSONException {
        EntityBareJid receiver = JidCreate.entityBareFrom(
                bubble.getReceiver() + "@" + XMPP_DOMAIN_NAME);

        JSONObject messageBody = new JSONObject();
        messageBody.put("latitude", bubble.getLatitude());
        messageBody.put("longitude", bubble.getLongitude());
        messageBody.put("message", bubble.getBody());

        Message message = connection.getStanzaFactory()
                .buildMessageStanza()
                .to(receiver)
                .setBody(messageBody.toString())
                .build();

        Chat chat = chatManager.chatWith(receiver);
        chat.send(message);
    }

    public void createAccount(String username, String password)
            throws XMPPException.XMPPErrorException, SmackException.NotConnectedException,
            InterruptedException, SmackException.NoResponseException, XmppStringprepException {
        AccountManager accountManager = AccountManager.getInstance(connection);
        if (accountManager.supportsAccountCreation()) {
            accountManager.sensitiveOperationOverInsecureConnection(true);
            accountManager.createAccount(Localpart.from(username), password);
        }
    }

    public void disconnect() {
        connection.disconnect();
    }

    public boolean isConnected() {
        return connection.isConnected();
    }

    public boolean isAuthenticated() {
        return connection.isAuthenticated();
    }

    public String getUsername() {
        return connection.getUser().asEntityBareJidString().split("@")[0];
    }
}
