package es.unex.geoapp.model;

import java.util.List;

/**
 * Created by Javier on 10/10/2017.
 */

public class HeatMapInfoMessage {

    /** Location of users */
    List<LocationFrequency> locationList;


    public HeatMapInfoMessage(String senderId, List<LocationFrequency> locationList) {
        this.locationList = locationList;
    }

    public List<LocationFrequency> getLocationList() {
        return locationList;
    }

    public void setLocationList(List<LocationFrequency> locationList) {
        this.locationList = locationList;
    }

}
