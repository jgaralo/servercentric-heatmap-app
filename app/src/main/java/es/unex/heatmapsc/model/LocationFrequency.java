package es.unex.heatmapsc.model;

/**
 * Created by Javier on 13/10/2017.
 */
public class LocationFrequency {
    Double latitude;
    Double longitude;
    Integer frequency;

    public LocationFrequency() {}

    public LocationFrequency(Double latitude, Double longitude, Integer frequency) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.frequency = frequency;
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

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public void incFrequency(){ this.frequency ++;}

    @Override
    public String toString() {
        return "LocationFrequency{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", frequency=" + frequency +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocationFrequency that = (LocationFrequency) o;

        if (latitude != null ? !latitude.equals(that.latitude) : that.latitude != null)
            return false;
        return longitude != null ? longitude.equals(that.longitude) : that.longitude == null;

    }

    @Override
    public int hashCode() {
        int result = latitude != null ? latitude.hashCode() : 0;
        result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
        return result;
    }
}
