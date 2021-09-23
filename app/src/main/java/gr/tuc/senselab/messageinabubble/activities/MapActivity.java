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
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import gr.tuc.senselab.messageinabubble.R;
import gr.tuc.senselab.messageinabubble.services.XmppConnectionService;
import gr.tuc.senselab.messageinabubble.utils.Bubble;
import gr.tuc.senselab.messageinabubble.utils.LocationListenerImpl;
import gr.tuc.senselab.messageinabubble.utils.MapEventsReceiverImpl;
import gr.tuc.senselab.messageinabubble.utils.events.MessageReceivingFailedEvent;
import gr.tuc.senselab.messageinabubble.utils.events.MessageReceivingSuccessfulEvent;
import gr.tuc.senselab.messageinabubble.utils.events.MessageSendingFailedEvent;
import gr.tuc.senselab.messageinabubble.utils.events.MessageSendingSuccessfulEvent;
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

        GpsMyLocationProvider locationProvider = new GpsMyLocationProvider(this);
        locationOverlay = new MyLocationNewOverlay(locationProvider, mapView);
        mapView.getOverlays().add(locationOverlay);

        IMapController mapController = mapView.getController();
        mapController.setZoom(ZOOM_LEVEL);
        mapController.setCenter(new GeoPoint(STARTING_LATITUDE, STARTING_LONGITUDE));

        MapEventsReceiverImpl mapEventsReceiver = new MapEventsReceiverImpl(this);
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mapEventsReceiver);
        mapView.getOverlays().add(mapEventsOverlay);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListenerImpl(locationOverlay);

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
                //FIXME: Location appears after we return to MapActivity
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
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSendMessageSuccessfulEvent(MessageSendingSuccessfulEvent event) {
        createBubble(event.getBubble(), event.getReceiver());
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onIncomingMessageSuccessfulEvent(MessageReceivingSuccessfulEvent event) {
        createBubble(event.getBubble(), event.getSender());
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSendMessageFailedEvent(MessageSendingFailedEvent event) {
        Toast.makeText(MapActivity.this, "Message Sending Failed", Toast.LENGTH_LONG).show();
        event.getException().printStackTrace();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onIncomingMessageFailedEvent(MessageReceivingFailedEvent event) {
        Toast.makeText(MapActivity.this, "Message Receiving Failed", Toast.LENGTH_LONG).show();
        event.getException().printStackTrace();
    }

    private void createBubble(Bubble bubble, String username) {
        //TODO: find a way to distinguish incoming messages from sent ones.
        Marker marker = new Marker(mapView);

        Location location = new Location("");
        location.setLatitude(bubble.getLatitude());
        location.setLongitude(bubble.getLongitude());
        marker.setPosition(new GeoPoint(location));

        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        String bubbleBody = username + ": " + bubble.getBody();
        marker.setTitle(bubbleBody);

        mapView.getOverlays().add(marker);
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
