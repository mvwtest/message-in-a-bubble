package gr.tuc.senselab.messageinabubble.network;

import gr.tuc.senselab.messageinabubble.utils.Bubble;
import gr.tuc.senselab.messageinabubble.utils.events.MessageReceivingFailedEvent;
import gr.tuc.senselab.messageinabubble.utils.events.MessageReceivingSuccessfulEvent;
import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONObject;
import org.jxmpp.jid.EntityBareJid;

public class IncomingChatMessageListenerImpl implements IncomingChatMessageListener {

    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        String messageBody = message.getBody();
        try {
            JSONObject json = new JSONObject(messageBody);
            double latitude = json.getDouble("latitude");
            double longitude = json.getDouble("longitude");
            String bubbleBody = json.getString("message");
            String sender = from.asEntityBareJidString().split("@")[0];
            Bubble bubble = new Bubble(latitude, longitude, bubbleBody);

            EventBus.getDefault().postSticky(new MessageReceivingSuccessfulEvent(bubble, sender));
        } catch (Exception e) {
            EventBus.getDefault().postSticky(new MessageReceivingFailedEvent(e));
        }
    }
}
