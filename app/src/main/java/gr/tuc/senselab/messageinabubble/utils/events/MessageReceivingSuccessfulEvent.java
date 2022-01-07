package gr.tuc.senselab.messageinabubble.utils.events;

import gr.tuc.senselab.messageinabubble.utils.BubbleDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageReceivingSuccessfulEvent {

    private final BubbleDto bubbleDto;
    private final String sender;

}
