package se.knzoon.knzoonmonitor.network; // Example package

import se.knzoon.knzoonmonitor.model.TurfEffort; // Import your User model

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    // Base URL: https://painthelper.knzoon.se/
    // Endpoint: api/turfeffort
    // Query parameter: username
    @GET("api/turfefforts")
    Call<TurfEffort> turfeffort(@Query("username") String username);
}