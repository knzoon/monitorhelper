// RetrofitClientInstance.java
package se.knzoon.knzoonmonitor.network; // Example package

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClientInstance {
    private static Retrofit retrofit;
    private static final String BASE_URL = "https://painthelper.knzoon.se/";

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // For logging network requests (optional, but very useful for debugging)
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY); // Log request and response lines and their respective headers and bodies (if present).
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // Use Gson for JSON parsing
                    .client(httpClient.build()) // Add the OkHttp client with logging
                    .build();
        }
        return retrofit;
    }
}