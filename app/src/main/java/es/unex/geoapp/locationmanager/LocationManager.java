package es.unex.geoapp.locationmanager;

import android.graphics.PointF;
import android.location.Location;
import android.util.Log;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import es.unex.geoapp.model.LocationFrequency;

/**
 * Created by Javier on 11/10/2017.
 */

public class LocationManager {

    public static boolean mapFinishedFlag = true;


/*    public static List <LocationFrequency> convertNimbeesLocations (List<NimbeesLocation> nimLocs){
        List <LocationFrequency> locationFreqs = new ArrayList<LocationFrequency>();
        for (NimbeesLocation nimLoc: nimLocs ) {
            LocationFrequency lFrequency = new LocationFrequency(nimLoc.getLatitude(), nimLoc.getLongitude(), 1);
            //Log.e("HEATMAP", nimLoc.getLatitude() + " " + nimLoc.getLongitude() + " " + nimLoc.getStartDate()+ " " + nimLoc.getEndDate());
            locationFreqs.add(lFrequency);
        }
        return locationFreqs;
    }*/



    public static List<LocationFrequency> filterLocation(List<LocationFrequency> locations, double latitude, double longitude, double radius) {
        List<LocationFrequency> results=new ArrayList<LocationFrequency>();
        PointF origin = new PointF((float)latitude, (float)longitude);
        PointF north = calculateDerivedPosition(origin, radius, 0);
        PointF east = calculateDerivedPosition(origin, radius, 90);
        PointF south = calculateDerivedPosition(origin, radius, 180);
        PointF west = calculateDerivedPosition(origin, radius, 270);
        for(LocationFrequency location:locations){
            location = setBuckets(location);
            if(location.getLatitude()>south.x && location.getLatitude()<north.x
                    && location.getLongitude()>west.y && location.getLongitude()<east.y){
                results.add(location);
            }
        }
        return results;
    }
    /**
     * Calculates the end-point from a given source at a given range (meters)
     * and bearing (degrees). This methods uses simple geometry equations to
     * calculate the end-point.
     *
     * @param point
     *            Point of origin
     * @param range
     *            Range in meters
     * @param bearing
     *            Bearing in degrees
     * @return End-point from the source given the desired range and bearing.
     */
    private static PointF calculateDerivedPosition(PointF point, double range, double bearing){
        double EarthRadius = 6371000; // m

        double latA = Math.toRadians(point.x);
        double lonA = Math.toRadians(point.y);
        double angularDistance = range / EarthRadius;
        double trueCourse = Math.toRadians(bearing);

        double lat = Math.asin(
                Math.sin(latA) * Math.cos(angularDistance) +
                        Math.cos(latA) * Math.sin(angularDistance)
                                * Math.cos(trueCourse));

        double dlon = Math.atan2(
                Math.sin(trueCourse) * Math.sin(angularDistance)
                        * Math.cos(latA),
                Math.cos(angularDistance) - Math.sin(latA) * Math.sin(lat));

        double lon = ((lonA + dlon + Math.PI) % (Math.PI * 2)) - Math.PI;

        lat = Math.toDegrees(lat);
        lon = Math.toDegrees(lon);

        PointF newPoint = new PointF((float) lat, (float) lon);

        return newPoint;
    }

    /**
     * This method calculates the position and time buckets for the latitude and longitude received. The closer
     * the coordinates are to the poles, the smaller is the area covered by the bucket. The time bucket for a time string using 15 minutes buckets
     *
     */
    private static LocationFrequency setBuckets(LocationFrequency location) {
        //Lat+long bucket
        Double latBucket = null;
        Double lonBucket = null;
        DecimalFormat df = new DecimalFormat("#.####");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(symbols);
        df.setRoundingMode(RoundingMode.DOWN);
        latBucket = Double.parseDouble(df.format(location.getLatitude()));
        lonBucket = Double.parseDouble(df.format(location.getLongitude()));
        //Bucketed location
        return new LocationFrequency(latBucket,lonBucket,1);
    }

    public static List <LocationFrequency> aggreateLocations(List <LocationFrequency> locations){
        List<LocationFrequency> locationFreqs = new ArrayList<LocationFrequency>();
        for (LocationFrequency element : locations) {
            LocationFrequency location = searchLocation(locationFreqs, element);
            if (location != null) {
                location.incFrequency();
            } else {
                locationFreqs.add(element);
            }
        }
        return locationFreqs;
    }

    public static LocationFrequency searchLocation (List <LocationFrequency> locations, LocationFrequency location){
        for (LocationFrequency element:locations  ) {
            if (element.equals(location)){
                return  element;
            }
        }
        return null;
    }

/*    public static void storeLocations(List<LocationFrequency> locationList) {
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("heatmap.realm")
                .build();
        Realm realm= Realm.getInstance(config);
        if(!LocationManager.mapFinishedFlag) {
            realm.beginTransaction();
            for (LocationFrequency location : locationList) {
                realm.copyToRealm(location);
            }
            realm.commitTransaction();
        }
        realm.close();
    }*/

/*    public static List<LocationFrequency> getLocations() {
        LocationManager.mapFinishedFlag=true;

        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("heatmap.realm")
                .build();
        Realm realm= Realm.getInstance(config);
        realm.beginTransaction();
        List<LocationFrequency> result = realm.where(LocationFrequency.class).findAll();
        result= aggreateLocations(result);
        realm.commitTransaction();
        return result;
    }

    public static void clearLocations() {
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("heatmap.realm")
                .build();
        Realm realm= Realm.getInstance(config);
        realm.beginTransaction();
        realm.where(LocationFrequency.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();
        realm.close();
    }*/
}
