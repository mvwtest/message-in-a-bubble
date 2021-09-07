package gr.tuc.senselab.messageinabubble.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import gr.tuc.senselab.messageinabubble.R;
import gr.tuc.senselab.messageinabubble.services.XmppConnectionService;
import gr.tuc.senselab.messageinabubble.utils.Bubble;
import gr.tuc.senselab.messageinabubble.utils.LocationListenerImpl;
import gr.tuc.senselab.messageinabubble.utils.MapEventsReceiverImpl;
import gr.tuc.senselab.messageinabubble.utils.events.NewMessageEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MapActivity extends AppCompatActivity {

    private static final double ZOOM_LEVEL = 7.0;
    private static final double STARTING_LATITUDE = 37.56424;
    private static final double STARTING_LONGITUDE = 22.80762;

    private LocationListener locationListener;
    private LocationManager locationManager;
    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;

    private XmppConnectionService xmppConnectionService;
    private boolean isServiceBound = false;
    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            XmppConnectionService.LocalBinder binder = (XmppConnectionService.LocalBinder) service;
            xmppConnectionService = binder.getService();
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isServiceBound = false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initializeMapView();

        Intent intent = new Intent(this, XmppConnectionService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void initializeMapView() {
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        mapView = findViewById(R.id.map_view);
        mapView.setTileSource(TileSourceFactory.MAPNIK);

        mapView.setMultiTouchControls(true);
        mapView.setClickable(true);
        mapView.setUseDataConnection(true);

        RotationGestureOverlay rotationOverlay = new RotationGestureOverlay(mapView);
        rotationOverlay.setEnabled(true);
        mapView.getOverlays().add(rotationOverlay);

        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        mapView.getOverlays().add(locationOverlay);

        IMapController mapController = mapView.getController();
        mapController.setZoom(ZOOM_LEVEL);
        GeoPoint startPoint = new GeoPoint(STARTING_LATITUDE, STARTING_LONGITUDE);
        mapController.setCenter(startPoint);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListenerImpl(locationOverlay);

        MapEventsReceiverImpl mapEventsReceiverImpl = new MapEventsReceiverImpl(this);
        MapEventsOverlay overlayEvents = new MapEventsOverlay(mapEventsReceiverImpl);
        mapView.getOverlays().add(overlayEvents);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                        locationListener);
            }

            if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                        locationListener);
            }

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps();
            } else {
                //FIXME: Location appears after we come back to this activity
                locationOverlay.enableMyLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        if (!locationOverlay.isMyLocationEnabled()) {
            locationOverlay.enableMyLocation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

        if (locationOverlay.isMyLocationEnabled()) {
            locationOverlay.disableMyLocation();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (isServiceBound) {
            xmppConnectionService.disconnect();
            unbindService(serviceConnection);
        }

        if (locationOverlay.isMyLocationEnabled()) {
            locationOverlay.disableMyLocation();
        }

        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewMessageEvent(NewMessageEvent event) {
        //FIXME: Event is not received
        Exception exception = event.getException();
        if (exception == null) {
            createBubble(event.getBubble());
        } else {
            exception.printStackTrace();
        }
    }

    private void createBubble(Bubble bubble) {
        Marker messageMarker = new Marker(mapView);
        Location location = new Location("");
        location.setLatitude(bubble.getLatitude());
        location.setLongitude(bubble.getLongitude());
        messageMarker.setPosition(new GeoPoint(location));
        messageMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        messageMarker.setTitle(bubble.getMessageBody());
        mapView.getOverlays().add(messageMarker);
    }

    private void buildAlertMessageNoGps() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> startActivity(
                        new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());

        AlertDialog alert = builder.create();
        alert.show();
    }
}
