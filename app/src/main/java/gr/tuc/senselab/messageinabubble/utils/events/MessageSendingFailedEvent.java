package gr.tuc.senselab.messageinabubble.utils.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageSendingFailedEvent {

    private final Exception exception;

}
