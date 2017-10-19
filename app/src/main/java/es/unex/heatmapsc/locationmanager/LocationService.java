package es.unex.heatmapsc.locationmanager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import es.unex.heatmapsc.model.LocationFrequency;
import es.unex.heatmapsc.rest.IPostDataService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Javier on 18/10/2017.
 */

public class LocationService extends Service {
    /**
     * Seconds to send
     */
    long MILISECONDS_REFRESH = 60000;

    private Timer timer;

    private GPSTracker gps;

    PowerManager.WakeLock wakeLock;

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

        rest = IPostDataService.retrofit.create(IPostDataService.class);

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
            Call<String> call = rest.postLocation(new LocationFrequency(gps.getLatitude(), gps.getLongitude(), 1));

            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    Log.i("HEATMAP", "Location posted" );
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e("ERROR: ", "Error posting the location. " + t.getMessage());
                }
            });

        } else {
            //gps.showSettingsAlert();
            Log.e("HEATMAP", "Can not get the location");
        }
    }

}