package es.unex.geoapp.locationmanager;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import es.unex.geoapp.MainActivity;
import es.unex.geoapp.R;

/**
 * Created by Javier on 18/10/2017.
 */

public class PermissionManager {

    private static final String TAG = "HEATMAP";
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;


    @TargetApi(23)
    public static boolean checkPermissions(final Activity activity, Context mContext) {
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }

    }

    @TargetApi(23)
    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults, final Activity activity, Context mContext) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(mContext,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                    }

                } else {

                    new AlertDialog.Builder(mContext)
                            .setTitle(R.string.title_location_permission)
                            .setMessage(R.string.text_location_permission)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Prompt the user once explanation has been shown
                                    ActivityCompat.requestPermissions(activity,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            MY_PERMISSIONS_REQUEST_LOCATION);
                                }
                            })
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        public void onDismiss(DialogInterface dialog) {
                        }
                    })
                    .create()
                            .show();
                }
                return;
            }

        }
    }

    public static Boolean getLocationPermissionState(Context context) {

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        return false;
        //return Boolean.valueOf(ContextCompat.checkSelfPermission(context, "android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED);

    }
}
