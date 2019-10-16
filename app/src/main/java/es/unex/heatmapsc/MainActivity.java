package es.unex.heatmapsc;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.views.Slider;
import com.gc.materialdesign.widgets.SnackBar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import es.unex.heatmapsc.datemanager.DatePickerFragment;
import es.unex.heatmapsc.locationmanager.GPSTracker;
import es.unex.heatmapsc.locationmanager.LocationService;
import es.unex.heatmapsc.locationmanager.PermissionManager;
import es.unex.heatmapsc.model.GetHeatMapMessage;
import es.unex.heatmapsc.model.LocationFrequency;
import es.unex.heatmapsc.rest.IPostDataService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    /**
     * Request Google Accounts.
     */
    private final int PICK_ACCOUNT_REQUEST = 0;

    /**
     * The radius value (by default 10 meters).
     */
    private int RADIUS = 100;

    // -UI ELEMENTS-
    /**
     * Slider to select the radius value.
     */
    private Slider mSlider;
    /**
     * Button to send the message.
     */
    private ButtonRectangle mButtonSend;
    /**
     * Button to pick the start date.
     */
    private ButtonRectangle mButtonStartDate;
    /**
     * Button to pick the end date.
     */
    private ButtonRectangle mButtonEndDate;
    /**
     * TextView to show the radius of action.
     */
    private TextView mTextViewDistance;
    /**
     * Google map.
     */
    public GoogleMap mGoogleMap;
    /**
     * MarkerOptions for the map.
     */
    private MarkerOptions mMarkerOptions;
    /**
     * Circle to draw arround the Marker with the radius.
     */
    private CircleOptions mCircleOptions;
    /**
     * Location used to add the marker to the map.
     */
    private Location mLocation;
    /**
     * Marker for the icon.
     */
    private Marker mMarker;
    /**
     * Circle for the radius of the icon.
     */
    private Circle mCircle;

    /**
     * Tracking servie
     */
    private Intent locationIntent = null;

    /**
     * Endpoints to interact with the rest services
     */
    private IPostDataService rest;


    private int startYear = 0, startMonth, startDay, startHour, startMinute;
    private int endYear = 0, endMonth, endDay, endHour, endMinute;
    private TileOverlay tileOverlay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tileOverlay = null;


        if (locationIntent == null) {
            locationIntent = new Intent(this, LocationService.class);
        }
        // check location permission
        if (PermissionManager.checkPermissions(this, MainActivity.this)) {
            startService(locationIntent);
        }

        if (mLocation == null) {
            mLocation = new Location(android.location.LocationManager.GPS_PROVIDER);
        }

        rest = IPostDataService.retrofit.create(IPostDataService.class);

        mTextViewDistance = (TextView) findViewById(R.id.textViewDistance);
        mSlider = (Slider) findViewById(R.id.seekBar);
        // This Listener change the value in the distance field and draw the new circle with the given radius
        mSlider.setOnValueChangedListener(new Slider.OnValueChangedListener() {
            @Override
            public void onValueChanged(int i) {
                // Change the test in the UI
                mTextViewDistance.setText(i + " M");
                // Delete old circle and draw the new circle with the radius given
                drawCircle(i);
                // Update the global variable RADIUS with the new value
                RADIUS = i;
            }
        });

        mButtonSend = (ButtonRectangle) findViewById(R.id.buttonSend);
        // This Listener call sendMessage on press button with radius and message
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("date", "start");
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.setArguments(bundle);
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                setUpMap();
                //getLocation();


                mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng point) {
                        mLocation = new Location(android.location.LocationManager.GPS_PROVIDER);
                        mLocation.setLatitude(point.latitude);
                        mLocation.setLongitude(point.longitude);
                        mGoogleMap.clear();
                        mCircleOptions = new CircleOptions().fillColor(0x5500ff00).strokeWidth(0l);
                        mMarkerOptions = new MarkerOptions().position(point).title("Heat map center");//.icon(icon);
                        mMarker = mGoogleMap.addMarker(mMarkerOptions);
                        mGoogleMap.setMyLocationEnabled(true);
                        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
                        drawCircle(RADIUS);
                    }
                });
            }
        });


    }

    /**
     * Update the variable mLocation with the location using the Nimbees Location service.
     */
    private void getLocation() {
        GPSTracker gpsTracker = new GPSTracker(this);
        mLocation = gpsTracker.getLocation();
        setUpMap();
    }

    /*Callback result of permissions check*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (PermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults, this, getApplicationContext())) {
            startService(locationIntent);
        }
    }

    /**
     * Initial configuration of the map with the actual location of the user.
     */
    private void setUpMap() {
        // Crear all map elements
        mGoogleMap.clear();
        // Set map type
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        // If we cant find the location now, we call a Network Provider location
        //addLocation();
    }


    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(broadcastReceiver, new IntentFilter("NOW"));
    }

    /*Broadcast to receive location for adding in the map*/
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("LOCATION", intent.getDoubleExtra("lat", 0) + " : " + intent.getDoubleExtra("long", 0));
            mLocation.setLatitude(intent.getDoubleExtra("lat", 0));
            mLocation.setLongitude(intent.getDoubleExtra("long", 0));
            addLocation();

        }
    };

    /*Add the red marker with the location*/
    private void addLocation() {
        if (mLocation != null) {
            // Create a LatLng object for the current location
            LatLng latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            // Show the current location in Google Map
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
            // Draw the first circle in the map
            mCircleOptions = new CircleOptions().fillColor(0x5500ff00).strokeWidth(0l);
            // Zoom in the Google Map
            //mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            // Zoom in the Google Map
            //icon = BitmapDescriptorFactory.fromResource(R.drawable.logo2);

            // Creation and settings of Marker Options
            mMarkerOptions = new MarkerOptions().position(latLng).title("You are here!");//.icon(icon);
            // Creation and addition to the map of the Marker
            mMarker = mGoogleMap.addMarker(mMarkerOptions);
            // set the initial map radius and draw the circle
            drawCircle(RADIUS);
        }
    }

    /**
     * Change radius and icon on map when change the radius in the bar.
     *
     * @param radius
     */
    private void drawCircle(int radius) {
        if (mCircle != null) {
            mCircle.remove();
        }
        // We defined a new circle center options with location and radius
        mCircleOptions.center(new LatLng(mLocation.getLatitude(), mLocation.getLongitude())).radius(radius); // In meters
        // We add here to the map the new circle
        mCircle = mGoogleMap.addCircle(mCircleOptions);
    }

    public void setStartDate(int year, int month, int day) {
        this.startYear = year;
        this.startMonth = month;
        this.startDay = day;
    }

    public void setEndDate(int year, int month, int day) {
        this.endYear = year;
        this.endMonth = month;
        this.endDay = day;
    }

    public void setStartTime(int hour, int minute) {
        this.startHour = hour;
        this.startMinute = minute;
    }

    public void setEndTime(int hour, int minute) {
        this.endHour = hour;
        this.endMinute = minute;
    }


    public void getHeatMap() {

        if (tileOverlay != null) {
            tileOverlay.remove();
        }
        if (endYear != 0 && startYear != 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set(Calendar.YEAR, startYear);
            calendar.set(Calendar.MONTH, startMonth);
            calendar.set(Calendar.DAY_OF_MONTH, startDay);
            calendar.set(Calendar.HOUR_OF_DAY, startHour);
            calendar.set(Calendar.MINUTE, startMinute);
            final Date startDate = calendar.getTime();
            calendar.clear();
            calendar.set(Calendar.YEAR, endYear);
            calendar.set(Calendar.MONTH, endMonth);
            calendar.set(Calendar.DAY_OF_MONTH, endDay);
            calendar.set(Calendar.HOUR_OF_DAY, endHour);
            calendar.set(Calendar.MINUTE, endMinute);
            final Date endDate = calendar.getTime();
            if (startDate.before(endDate)) {


                final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setTitle("HeatMap");
                progressDialog.setMessage("Getting users' locations..."); // Setting Message
                progressDialog.setCancelable(false);
                progressDialog.show();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {

                        getHeatMapPositions(new GetHeatMapMessage(startDate, endDate, mLocation.getLatitude(), mLocation.getLongitude(), RADIUS));

                        progressDialog.dismiss();
                    }
                }, 20000);
            } else {
                Log.e("HEATMAP", "End date is before star date");
                SnackBar snackbar = new SnackBar(MainActivity.this, "Start date should be before end date", "ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Handle user action
                    }
                });
                snackbar.show();
            }
        } else {
            Log.e("HEATMAP", "No dates");
            SnackBar snackbar = new SnackBar(MainActivity.this, "Please select the dates first", "ok", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle user action
                }
            });
            snackbar.show();
        }
    }

    public void getHeatMapPositions(GetHeatMapMessage heatMapMessage) {

        Call<List<LocationFrequency>> call = rest.getHeatMap(heatMapMessage.getBeginDate(), heatMapMessage.getEndDate(), heatMapMessage.getLatitude(), heatMapMessage.getLongitude(), heatMapMessage.getRadius());

        call.enqueue(new Callback<List<LocationFrequency>>() {
            @Override
            public void onResponse(Call<List<LocationFrequency>> call, Response<List<LocationFrequency>> response) {


                List<LocationFrequency> locations = response.body();
                Log.e("HEATMAP: ", "Locations received " + response.body().size());

//                for(int i=0; i<locations.size(); i++){
//                    Log.i("Locations: ",locations.toString());
//                }

                List<WeightedLatLng> points = new ArrayList<WeightedLatLng>();
                for (LocationFrequency location : locations) {
                    points.add(new WeightedLatLng(new LatLng(location.getLatitude(), location.getLongitude()), location.getFrequency()));
                }
                if (points.size() > 0) {
                    HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                            .weightedData(points)
                            .build();
                    tileOverlay = MainActivity.this.mGoogleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                    if (mCircle != null) {
                        mCircle.remove();
                    }
                }

            }

            @Override
            public void onFailure(Call<List<LocationFrequency>> call, Throwable t) {
                Log.e("HEATMAP: ", "ERROR getting the users' locations " + t.getMessage());
            }
        });

    }

}


