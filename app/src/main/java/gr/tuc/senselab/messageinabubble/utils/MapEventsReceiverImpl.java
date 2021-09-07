package gr.tuc.senselab.messageinabubble.utils;

import android.content.Context;
import android.content.Intent;
import gr.tuc.senselab.messageinabubble.activities.MessageActivity;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;

public class MapEventsReceiverImpl implements MapEventsReceiver {

    private final Context context;

    public MapEventsReceiverImpl(Context appContext) {
        context = appContext;
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        return false;
    }

    @Override
    public boolean longPressHelper(final GeoPoint p) {
        Intent intent = new Intent(context, MessageActivity.class);
        intent.putExtra("latitude", p.getLatitude());
        intent.putExtra("longitude", p.getLongitude());
        context.startActivity(intent);
        return true;
    }
}
