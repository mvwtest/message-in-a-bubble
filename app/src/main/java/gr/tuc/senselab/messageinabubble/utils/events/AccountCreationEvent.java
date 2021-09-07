package gr.tuc.senselab.messageinabubble.utils.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccountCreationEvent {

    private final Exception exception;

}
