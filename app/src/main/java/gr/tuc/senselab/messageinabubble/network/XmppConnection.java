package gr.tuc.senselab.messageinabubble.network;


import android.content.Context;

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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import gr.tuc.senselab.messageinabubble.utils.BubbleDto;
import gr.tuc.senselab.messageinabubble.utils.PropertiesUtil;

public class XmppConnection {

    private final XMPPTCPConnection connection;
    private ChatManager chatManager;
    private final Context context;


    public XmppConnection(Context context) throws UnknownHostException, XmppStringprepException {
        this.context = context;
        AndroidSmackInitializer.initialize(context);

        InetAddress address = InetAddress.getByName(
                PropertiesUtil.getProperty("host", context));
        DomainBareJid domain = JidCreate.domainBareFrom(
                PropertiesUtil.getProperty("xmppDomainName", context));
        XMPPTCPConnectionConfiguration configuration = XMPPTCPConnectionConfiguration.builder()
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setXmppDomain(domain)
                .setHostAddress(address)
                .setPort(Integer.parseInt(PropertiesUtil.getProperty("port", context)))
                .enableDefaultDebugger()
                .build();

        connection = new XMPPTCPConnection(configuration);
    }

    public void connect() throws InterruptedException, XMPPException, SmackException, IOException {
        connection.connect();
    }

    public void login() throws InterruptedException, XMPPException, SmackException, IOException {
        login(PropertiesUtil.getProperty("adminUsername", context),
                PropertiesUtil.getProperty("adminPassword", context));
    }

    public void login(String username, String password)
            throws InterruptedException, IOException, SmackException, XMPPException {
        connection.login(username, password);

        chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addIncomingListener(new IncomingChatMessageListenerImpl());

        ReconnectionManager.getInstanceFor(connection).enableAutomaticReconnection();
    }

    public void sendMessage(BubbleDto bubbleDto, String receiver)
            throws XmppStringprepException, SmackException.NotConnectedException,
            InterruptedException, JSONException {
        EntityBareJid to = JidCreate.entityBareFrom(
                receiver + "@" + PropertiesUtil.getProperty("xmppDomainName", context));

        JSONObject messageBody = new JSONObject();
        messageBody.put("latitude", bubbleDto.getLatitude());
        messageBody.put("longitude", bubbleDto.getLongitude());
        messageBody.put("message", bubbleDto.getBody());

        Message message = connection.getStanzaFactory()
                .buildMessageStanza()
                .to(to)
                .setBody(messageBody.toString())
                .build();

        Chat chat = chatManager.chatWith(to);
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
