package es.unex.heatmapsc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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
     * MarkerOptions for the map.
     */
    private MarkerOptions mMarkerOptions;
    /**
     * Location used to add the marker to the map.
     */
    private Location mLocation;
    /**
     * Marker for the icon.
     */
    private Marker mMarker;

    /**
     * Tracking servie
     */
    private Intent locationIntent = null;

    /**
     * Endpoints to interact with the rest services
     */
    private IPostDataService rest;


    private int startYear=0, startMonth, startDay, startHour, startMinute;
    private int endYear=0, endMonth, endDay, endHour, endMinute;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (locationIntent == null) {
            locationIntent = new Intent(this, LocationService.class);
        }
        // check location permission
        if (PermissionManager.checkPermissions(this, MainActivity.this)){
            startService(locationIntent);
        }

        if (mLocation == null) {
            mLocation = new Location(android.location.LocationManager.GPS_PROVIDER);
        }

        rest = IPostDataService.retrofit.create(IPostDataService.class);

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

    }



    public void setStartDate(int year, int month, int day){
        this.startYear=year;
        this.startMonth=month;
        this.startDay=day;
    }

    public void setEndDate(int year, int month, int day){
        this.endYear=year;
        this.endMonth=month;
        this.endDay=day;
    }

    public void setStartTime(int hour, int minute){
        this.startHour=hour;
        this.startMinute=minute;
    }

    public void setEndTime(int hour, int minute){
        this.endHour=hour;
        this.endMinute=minute;
    }



    public void getHeatMap(){


        EditText editLatitude = (EditText)findViewById(R.id.editLatitude);


        mLocation.setLatitude(Double.parseDouble(editLatitude.getText().toString()));

        EditText editLongitude = (EditText)findViewById(R.id.editLongitude);
        mLocation.setLongitude(Double.parseDouble(editLongitude.getText().toString()));

        EditText editRadius = (EditText)findViewById(R.id.editRadius);
        RADIUS = Integer.parseInt(editRadius.getText().toString());

        if(endYear!=0 && startYear!=0){
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
            if(startDate.before(endDate)) {


                final ProgressDialog progressDialog = new ProgressDialog (MainActivity.this);
                progressDialog.setTitle("HeatMap");
                progressDialog.setMessage("Getting users' locations..."); // Setting Message
                progressDialog.setCancelable(false);
                progressDialog.show();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {


                        getHeatMapPositions (new GetHeatMapMessage(startDate, endDate, mLocation.getLatitude(), mLocation.getLongitude(), RADIUS));

                        progressDialog.dismiss();
                    }
                }, 20000);
            }
            else{
                Log.e("HEATMAP", "End date is before star date");
                SnackBar snackbar = new SnackBar(MainActivity.this, "Start date should be before end date", "ok",new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Handle user action
                    }
                });
                snackbar.show();
            }
        }
        else{
            Log.e("HEATMAP", "No dates");
            SnackBar snackbar = new SnackBar(MainActivity.this, "Please select the dates first", "ok",new View.OnClickListener() {
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

                Log.w("HEATMAP: ", "Message received. Num. Points: " + locations.size());

                TableLayout inflate = (TableLayout) MainActivity.this.findViewById(R.id.tblLocations);
                TableRow row;
                TextView col1, col2, col3;

                row = new TableRow(MainActivity.this);
                col1 = new TextView(MainActivity.this);
                col1.setText("Latitude"+ "      ");
                row.addView(col1);

                col2 = new TextView(MainActivity.this);
                col2.setText("Longitude "+ "      ");
                row.addView(col2);

                col3 = new TextView(MainActivity.this);
                col3.setText("Frequency "+ "      ");
                row.addView(col3);

                inflate.addView(row);


                for(LocationFrequency location:locations){

                    Log.w("HEATMAP: ", "Point. Latitude " + location.getLatitude() + " Longitude: " + location.getLongitude() + "Frequency: " +location.getFrequency());

                    row = new TableRow(MainActivity.this);
                    col1 = new TextView(MainActivity.this);
                    col1.setText(location.getLatitude().toString() + "      ");
                    row.addView(col1);

                    col2 = new TextView(MainActivity.this);
                    col2.setText(location.getLongitude().toString()+ "      ");
                    row.addView(col2);

                    col3 = new TextView(MainActivity.this);
                    col3.setText(location.getFrequency().toString());
                    row.addView(col3);

                    inflate.addView(row);

                }

            }

            @Override
            public void onFailure(Call<List<LocationFrequency>> call, Throwable t) {
                Log.e("HEATMAP: ", "ERROR getting the users' locations " + t.getMessage());
            }
        });

    }

}


