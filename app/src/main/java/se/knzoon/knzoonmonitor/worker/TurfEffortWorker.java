package se.knzoon.knzoonmonitor.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;

import retrofit2.Response;
import se.knzoon.knzoonmonitor.model.TurfEffort;
import se.knzoon.knzoonmonitor.network.ApiService;
import se.knzoon.knzoonmonitor.network.RetrofitClientInstance;
import se.knzoon.knzoonmonitor.notification.NotificationHelper;

public class TurfEffortWorker extends Worker {

    private static final String TAG = "TurfEffortWorker";
    public static final String KEY_USERNAME = "USERNAME_TO_MONITOR";
    private String usernameToMonitor = "praktikus"; // Placeholder

    public TurfEffortWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "TurfEffortWorker: Work starting.");

        // Get username from input data if provided, otherwise use default
        String inputUsername = getInputData().getString(KEY_USERNAME);
        if (inputUsername != null && !inputUsername.isEmpty()) {
            usernameToMonitor = inputUsername;
        } else {
            // Potentially fetch the username from SharedPreferences if it was saved by the MainActivity
            // For now, we'll stick to a default or what was passed.
            Log.w(TAG, "No username provided to worker, using default: " + usernameToMonitor);
        }


        ApiService service = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);
        // Note: Retrofit's Call.execute() is synchronous and is okay to use inside a Worker's doWork()
        // because doWork() is already executed on a background thread.
        try {
            Response<TurfEffort> response = service.turfeffort(usernameToMonitor).execute();

            if (response.isSuccessful() && response.body() != null) {
                TurfEffort turfEffort = response.body();
                Log.i(TAG, "API call successful for " + usernameToMonitor + ": " + turfEffort.toString());
                // Optionally, you could send a success notification or store the result
                // For this example, we focus on error notifications.
            } else {
                String errorMsg = "Failed to fetch turfeffort for " + usernameToMonitor + ". Code: " + response.code();
                String errorBodyString = "Unknown error";
                if (response.errorBody() != null) {
                    try {
                        errorBodyString = response.errorBody().string();
                    } catch (IOException e) {
                        Log.e(TAG, "Error parsing error body for " + usernameToMonitor, e);
                    }
                }
                Log.e(TAG, errorMsg + " Details: " + errorBodyString);
                NotificationHelper.sendErrorNotification(
                        getApplicationContext(),
                        "API Error: " + response.code(),
                        "Could not fetch turf effort for " + usernameToMonitor + ". " + errorBodyString
                );
                return Result.failure(); // Indicate work failed, could be retried based on policy
            }
        } catch (IOException e) {
            Log.e(TAG, "API call failed for " + usernameToMonitor, e);
            NotificationHelper.sendErrorNotification(
                    getApplicationContext(),
                    "Network Error",
                    "Failed to connect for " + usernameToMonitor + ": " + e.getMessage()
            );
            return Result.retry(); // Indicate work should be retried
        }

        Log.d(TAG, "TurfEffortWorker: Work finished successfully for " + usernameToMonitor);
        return Result.success();
    }
}
