package gr.tuc.senselab.messageinabubble.utils;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import androidx.annotation.NonNull;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class LocationListenerImpl implements LocationListener {

    private final MyLocationNewOverlay locationOverlay;

    public LocationListenerImpl(MyLocationNewOverlay locationOverlay) {
        this.locationOverlay = locationOverlay;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // The method is an intentionally blank override
    }

    /**
     * @deprecated The super method is deprecated.
     */
    @Override
    @Deprecated
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // The method is an intentionally blank override
    }

    @Override
    public void onProviderEnabled(String provider) {
        locationOverlay.enableMyLocation();
    }

    @Override
    public void onProviderDisabled(String provider) {
        locationOverlay.disableMyLocation();
    }
}
