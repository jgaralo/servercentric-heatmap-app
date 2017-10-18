package es.unex.geoapp.rest;

import java.util.List;

import es.unex.geoapp.model.GetHeatMapMessage;
import es.unex.geoapp.model.LocationFrequency;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Javier on 18/10/2017.
 */
public interface IPostDataService {

    @Headers(
         "Accept: application/json"
    )

    /*@POST("/postContent")
    void postContentCallback(@Query("content") byte[] content, Callback<Integer> callback);*/

    @POST("/postLocation")
    void postLocation(@Body LocationFrequency location, Callback<Integer> callback);

    @GET("/getHeatMap")
    Call<List<LocationFrequency>> getHeatMap(@Body GetHeatMapMessage heatMapMessage, Callback<Integer> callback);

}
