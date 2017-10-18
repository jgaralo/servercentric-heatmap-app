package es.unex.geoapp.locationmanager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import es.unex.geoapp.model.LocationFrequency;
import es.unex.geoapp.rest.IPostDataService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Javier on 18/10/2017.
 */

public class LocationService extends Service {

    /**
     * Seconds to send
     */
    long MILISECONDS_REFRESH = 1000;

    private Timer timer;

    private GPSTracker gps;

    PowerManager.WakeLock wakeLock;

    private RestAdapter restAdapter;
    
    private IPostDataService rest;

    @Override
    public void onCreate() {

        PowerManager pm = (PowerManager) getSystemService(this.POWER_SERVICE);

        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DoNotSleep");

        wakeLock.acquire();

        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();

        rest = restAdapter.create(IPostDataService.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Tracking the location", Toast.LENGTH_SHORT).show();

        if (gps == null) {
            gps = new GPSTracker(this);
        }
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                postGPSPosition();
            }
        }, 0, MILISECONDS_REFRESH);


        // If we get killed, after returning from here, restart
        return START_REDELIVER_INTENT;
    }
    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        if (timer != null){
            timer.cancel();
        }
        stopSelf();

        if (wakeLock!=null){
            wakeLock.release();
        }

        super.onDestroy();

        Toast.makeText(this, "Location tracking stopped", Toast.LENGTH_SHORT).show();
    }

    public void postGPSPosition() {

        if (gps.canGetLocation()) {



            Callback<Integer> callback = new Callback<Integer>() {

                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    Log.i("HEATMAP", "Location posted");
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    Log.e("ERROR: ", "Error posting the location. " + t.getMessage());
                }
            };

            rest.postLocation(new LocationFrequency(gps.getLatitude(), gps.getLongitude(), 1), callback);
        } else {
            gps.showSettingsAlert();
        }
    }

}