package gr.tuc.senselab.messageinabubble.utils.events;

import gr.tuc.senselab.messageinabubble.utils.Bubble;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NewMessageEvent {

    private final Bubble bubble;
    private final Exception exception;
}
