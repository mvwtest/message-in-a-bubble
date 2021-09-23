package gr.tuc.senselab.messageinabubble.utils.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginFailedEvent {

    private final Exception exception;

}
