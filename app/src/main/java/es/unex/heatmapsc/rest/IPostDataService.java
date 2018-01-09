package es.unex.heatmapsc.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;
import java.util.List;

import es.unex.heatmapsc.model.GetHeatMapMessage;
import es.unex.heatmapsc.model.LocationBean;
import es.unex.heatmapsc.model.LocationFrequency;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Javier on 18/10/2017.
 */
public interface IPostDataService {

    @Headers(
         "Accept: application/json"
    )

    @POST("/alpha/locations")
    Call<ResponseBody>  postLocation(@Body LocationBean location);

    @GET("/alpha/heatmaps")
    Call<List<LocationFrequency>> getHeatMap(@Query("period.from") Date from,
                                             @Query("period.to") Date to,
                                             @Query("area.center.latitude") Double latitude,
                                             @Query("area.center.longitude") Double longitude,
                                             @Query("area.radius")Double radius);


    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create();


    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://vgvnaqc8j8.execute-api.us-east-1.amazonaws.com")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();


}
