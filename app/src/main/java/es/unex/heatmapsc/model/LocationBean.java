package es.unex.heatmapsc.model;

/**
 * Created by Javier on 13/10/2017.
 */
public class LocationBean {
    Double latitude;
    Double longitude;

    public LocationBean() {}

    public LocationBean(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
