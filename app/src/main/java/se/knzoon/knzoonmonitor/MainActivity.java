package se.knzoon.knzoonmonitor;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.knzoon.knzoonmonitor.model.TurfEffort;
import se.knzoon.knzoonmonitor.network.ApiService;
import se.knzoon.knzoonmonitor.network.RetrofitClientInstance;
import se.knzoon.knzoonmonitor.notification.NotificationHelper;
import se.knzoon.knzoonmonitor.worker.TurfEffortWorker;

public class MainActivity extends AppCompatActivity {
    private TextView myTextView;
    private EditText usernameEditText;
    private static final String TAG = "MainActivity";

    private static final String UNIQUE_WORK_NAME = "turfEffortPeriodicWork";

    // Declare the launcher at the top of your Activity/Fragment
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. You can now post notifications.
                    Log.d(TAG, "POST_NOTIFICATIONS permission granted.");
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied.
                    Log.w(TAG, "POST_NOTIFICATIONS permission denied.");
                    Toast.makeText(this, "Notification permission denied. Error notifications will not be shown.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        NotificationHelper.createNotificationChannel(this); // Create channel
        requestNotificationPermission(); // Request permission

        Button scheduleButton = findViewById(R.id.myButton);
        Button cancelButton = findViewById(R.id.cancelButton);
        myTextView = findViewById(R.id.textView);
        usernameEditText = findViewById(R.id.usernameEditText);

        scheduleButton.setText("Start/Update Monitoring");

        scheduleButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();

            if (TextUtils.isEmpty(username)) {
                Toast.makeText(MainActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
                return; // Don't proceed if username is empty
            }

            // You might want to save this username to SharedPreferences so the worker can access it
            // if the app is not running or if it needs to pick up the latest username.
            // For now, we'll pass it as input data to the worker.
            scheduleTurfEffortWork(username);
            Toast.makeText(MainActivity.this, "Monitoring scheduled for " + username, Toast.LENGTH_LONG).show();
            myTextView.setText("Turf effort monitoring for '" + username + "' is scheduled to run every 15 minutes.");
        });

        // --- Set OnClickListener for the NEW Cancel button ---
        cancelButton.setOnClickListener(v -> {
            WorkManager.getInstance(getApplicationContext()).cancelUniqueWork(UNIQUE_WORK_NAME);
            Toast.makeText(MainActivity.this, "Monitoring cancelled.", Toast.LENGTH_SHORT).show();
            myTextView.setText("Monitoring has been cancelled.");
            Log.i(TAG, "User cancelled unique work: " + UNIQUE_WORK_NAME);
        });

    }

    private void scheduleTurfEffortWork(String username) {
        // Create input data for the worker
        Data inputData = new Data.Builder()
                .putString(TurfEffortWorker.KEY_USERNAME, username)
                .build();

        // Define constraints (e.g., network needed)
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Only run when network is available
                .build();

        PeriodicWorkRequest turfEffortRequest =
                new PeriodicWorkRequest.Builder(TurfEffortWorker.class, 15, TimeUnit.MINUTES)
                        // For flex time, if needed: .setFlexTimeInterval(5, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .setInputData(inputData)
                        // .addTag("turf_monitoring") // Optional tag
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE, // Or KEEP if you don't want to replace if already scheduled
                turfEffortRequest
        );

        Log.i(TAG, "Periodic work scheduled for " + username + " with interval 15 minutes.");
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU is API 33
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // Permission is already granted
                Log.d(TAG, "POST_NOTIFICATIONS permission already granted.");
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: Optionally, show an educational UI explaining why the permission is needed.
                // Then, request the permission.
                Log.i(TAG, "Showing rationale for POST_NOTIFICATIONS permission.");
                // For this example, we'll just request it directly.
                // In a real app, you might show a dialog first.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

}
