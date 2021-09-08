package gr.tuc.senselab.messageinabubble.network;

import gr.tuc.senselab.messageinabubble.utils.Bubble;
import gr.tuc.senselab.messageinabubble.utils.events.NewMessageEvent;
import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONObject;
import org.jxmpp.jid.EntityBareJid;

public class IncomingChatMessageListenerImpl implements IncomingChatMessageListener {

    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        String body = message.getBody();
        try {
            JSONObject json = new JSONObject(body);
            double latitude = json.getDouble("latitude");
            double longitude = json.getDouble("longitude");
            String messageBody = json.getString("message");
            String sender = from.asEntityBareJidString().split("@")[0];
            Bubble bubble = new Bubble(latitude, longitude, messageBody, sender, null);

            EventBus.getDefault().postSticky(new NewMessageEvent(bubble, null));
        } catch (Exception e) {
            EventBus.getDefault().postSticky(new NewMessageEvent(null, e));
        }
    }
}
