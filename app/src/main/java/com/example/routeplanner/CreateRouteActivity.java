package com.example.routeplanner;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Stack;

public class CreateRouteActivity extends FragmentActivity
        implements OnMapReadyCallback {

    // The entry point to the Places API.
    private PlacesClient mPlacesClient;

    private static final String TAG = CreateRouteActivity.class.getSimpleName();
    private GoogleMap map;
    private CameraPosition cameraPosition;
    private Stack<Marker> markerStack = new Stack<Marker>();
    private Stack<Polyline> polylineStack = new Stack<Polyline>();
    private CheckBox cycleCheckBox;

    private ArrayList<Float> routeLength = new ArrayList<Float>();
    private Polyline cycleLine;



    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (DTU, Denmark) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(55.7858105, 12.5195605);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;

    // Keys for storing activity state
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        setContentView(R.layout.activity_create_route);

        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        mPlacesClient = Places.createClient(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync((OnMapReadyCallback) this);
    }

    /**
     * Callback method called when map is ready for use, and can be manipulated
     * From google:
     *    "If Google Play services is not installed on the device, the user will be prompted to install
     *    it inside the SupportMapFragment. This method will only be triggered once the user has
     *    installed Google Play services and returned to the app."
     *
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        initializeListeners();

        // Get permission from user to use device location
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }

    private void initializeListeners() {

        // Set up checkbox
        cycleCheckBox = findViewById(R.id.checkBox);
        cycleCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked() && markerStack.size() > 1) {
                    float cycleDist = getDistanceBetweenMarkers(markerStack.get(0), markerStack.peek());
                    routeLength.add(cycleDist);
                    updateCalcLengthText();
                    cycleLine = createPolylineBetweenMarkers(markerStack.get(0), markerStack.peek());
                } else if (markerStack.size() > 1) {
                    routeLength.remove(routeLength.size()-1);
                    updateCalcLengthText();
                    cycleLine.remove();
                }
            }
        });

        // Sets a listener on the map to handle the event of
        // the user long pressing anywhere on the map that is not a marker
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if (markerStack.size() != 0) {
                    markerStack.peek().setDraggable(false);
                }

                // Creates new marker at the pressed point
                MarkerOptions newMarkerOptions = new MarkerOptions()
                        .position(latLng)
                        .title("New marker")
                        .draggable(true);
                Marker newMarker = map.addMarker(newMarkerOptions);

                // Creates a line between the given points
                if (!markerStack.isEmpty()) {
                    Polyline line = createPolylineBetweenMarkers(markerStack.peek(), newMarker);
                    polylineStack.push(line);
                }

                Toast.makeText(getApplicationContext(), "New marker added" ,Toast.LENGTH_SHORT).show();

                // Push current position and marker on top of respective stacks
                if (markerStack.size() >= 1) {
                    float result = getDistanceBetweenMarkers(newMarker, markerStack.peek());

                    // If checkbox is checked, replace final cycle length
                    // with new final cycle length
                    if ( cycleCheckBox.isChecked() ) {
                        if (markerStack.size() > 1) {
                            routeLength.remove(routeLength.size()-1);
                        }

                        routeLength.add(result);
                        float newDist = getDistanceBetweenMarkers(markerStack.get(0), newMarker);
                        routeLength.add(newDist);

                        // Replace cycle line
                        if (markerStack.size() > 1) {
                            cycleLine.remove();
                        }
                        cycleLine = createPolylineBetweenMarkers(markerStack.get(0), newMarker);
                    } else {
                        routeLength.add(result);
                    }
                    updateCalcLengthText();
                }
                markerStack.push(newMarker);
            }
        });

        // Sets a listener to respond to drag events
        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            private ArrayList<LatLng> latLngList = new ArrayList<LatLng>();

            @Override
            public void onMarkerDragStart(Marker marker) {
                if (!(markerStack.size() == 1)) {
                    markerStack.pop();
                    polylineStack.pop().remove();
                    routeLength.remove(routeLength.size()-1);
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) { }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                float result = getDistanceBetweenMarkers(markerStack.peek(), marker);
                routeLength.add(result);
                updateCalcLengthText();
                Polyline line = createPolylineBetweenMarkers(markerStack.peek(), marker);
                markerStack.push(marker);
                polylineStack.push(line);
            }
        });

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), DEFAULT_ZOOM));

                Toast.makeText(getApplicationContext(), "Marker clicked", Toast.LENGTH_SHORT).show();

                // Return true to mark the event is consumed
                // and prevent any extra UI menus to appear
                return true;
            }
        });
    }

    /*
     * Method taken from Maps SDK for Android - Documentation
     * https://developers.google.com/maps/documentation/android-sdk/intro
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    /*
     * Method taken from Maps SDK for Android - Documentation
     * https://developers.google.com/maps/documentation/android-sdk/intro
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }
    /*
    * Method taken from Maps SDK for Android - Documentation
    * https://developers.google.com/maps/documentation/android-sdk/intro
    */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult =  mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }
    /*
     * Method taken from Maps SDK for Android - Documentation
     * https://developers.google.com/maps/documentation/android-sdk/intro
     */
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }

    // Creates a polyline on the map between two given markers
    protected Polyline createPolylineBetweenMarkers(Marker A, Marker B) {
        Polyline line = map.addPolyline(new PolylineOptions()
                .add(A.getPosition(), B.getPosition())
                .width(10)
                .color(Color.RED));
        return line;
    }

    // Updates current length of route to display
    public float updateCalcLengthText() {
        TextView calcLength = (TextView) findViewById(R.id.textView_calc_length);
        float totalRouteLength = 0;
        for (int i = 0; i < routeLength.size(); i++) {
            totalRouteLength += routeLength.get(i);
        }

        float distance = totalRouteLength/1000;
        float num = (float) Math.round(distance*100)/100;
        calcLength.setText(num + " km");
        return num;
    }

    protected float getDistanceBetweenMarkers(Marker A, Marker B) {
        float[] results = new float[1];
        LatLng firstMarkerPosition = A.getPosition();
        LatLng lastMarkerPosition = B.getPosition();
        Location.distanceBetween(
                firstMarkerPosition.latitude,
                firstMarkerPosition.longitude,
                lastMarkerPosition.latitude,
                lastMarkerPosition.longitude,
                results
        );
        return results[0];
    }

    public ArrayList<Float> getRouteLength() {
        return routeLength;
    }

    public Polyline getCycleLine() {
        return cycleLine;
    }

    public Stack<Marker> getMarkerStack() {
        return markerStack;
    }

    public Stack<Polyline> getPolylineStack() {
        return polylineStack;
    }

    public GoogleMap getMap() {
        return map;
    }

    public void setCycleLine(Polyline cycleLine) {
        this.cycleLine = cycleLine;
    }

    public CheckBox getCycleCheckBox() {
        return cycleCheckBox;
    }

    public String getRouteName() {
        EditText text = findViewById(R.id.editText_route_name);
        return text.getText().toString();
    }
}