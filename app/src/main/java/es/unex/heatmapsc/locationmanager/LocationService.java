package es.unex.heatmapsc.locationmanager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import es.unex.heatmapsc.model.LocationBean;
import es.unex.heatmapsc.model.LocationFrequency;
import es.unex.heatmapsc.rest.IPostDataService;
import okhttp3.ResponseBody;
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
    long MILISECONDS_REFRESH = 50000;

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

    /*Sends Location to Main for add the point in the map with location by broadcast*/
    private void sendLocation() {

        Intent intent = new Intent();
        intent.putExtra("lat", gps.getLatitude());
        intent.putExtra("long",gps.getLongitude());
        intent.setAction("NOW");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Tracking the location", Toast.LENGTH_SHORT).show();
        if (gps == null) {
            gps = new GPSTracker(this);
        }

        sendLocation();

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
            Call<ResponseBody> call = rest.postLocation(new LocationBean(gps.getLatitude(), gps.getLongitude()));

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.i("HEATMAP", "Location posted" );
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("ERROR: ", "Error posting the location. " + t.getMessage());
                }
            });

        } else {
            //gps.showSettingsAlert();
            Log.e("HEATMAP", "Can not get the location");
        }
    }

}