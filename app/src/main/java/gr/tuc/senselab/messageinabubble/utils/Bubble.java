package gr.tuc.senselab.messageinabubble.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Bubble {

    private final double latitude;
    private final double longitude;
    private final String body;
    private final String sender;
    private final String receiver;
}
