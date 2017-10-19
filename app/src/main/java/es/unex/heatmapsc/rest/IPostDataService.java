package es.unex.heatmapsc.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import es.unex.heatmapsc.model.GetHeatMapMessage;
import es.unex.heatmapsc.model.LocationFrequency;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
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


    @POST("/test/location")
    Call<String>  postLocation(@Body LocationFrequency location);

    @POST("/test/heatmap")
    Call<List<LocationFrequency>> getHeatMap(@Body GetHeatMapMessage heatMapMessage);


    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create();


    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://69erlqmd9h.execute-api.eu-central-1.amazonaws.com")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();


}
