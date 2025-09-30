package se.knzoon.knzoonmonitor;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
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
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.knzoon.knzoonmonitor.model.TurfEffort;
import se.knzoon.knzoonmonitor.network.ApiService;
import se.knzoon.knzoonmonitor.network.RetrofitClientInstance;

public class MainActivity extends AppCompatActivity {
    private TextView myTextView;
    private EditText usernameEditText;
    private static final String TAG = "MainActivity";
    private static final String CHANNEL_ID = "turf_effort_error_channel";
    private static final int NOTIFICATION_ID = 101;

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

        createNotificationChannel(); // Create channel on app start
        requestNotificationPermission(); // Request permission

        Button myButton = findViewById(R.id.myButton);
        myTextView = findViewById(R.id.textView);
        usernameEditText = findViewById(R.id.usernameEditText);

        myButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();

            if (TextUtils.isEmpty(username)) {
                Toast.makeText(MainActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
                return; // Don't proceed if username is empty
            }

            // Action to perform when the button is clicked
            fetchTurfEffort(username);
        });
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

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name); // You'll need to add this string resource
            String description = getString(R.string.channel_description); // And this one
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created.");
            } else {
                Log.e(TAG, "NotificationManager is null. Cannot create notification channel.");
            }
        }
    }

    private void fetchTurfEffort(String username) {
        myTextView.setText("Fetching data with Retrofit...");
        ApiService service = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);
        Call<TurfEffort> call = service.turfeffort(username);

        call.enqueue(new Callback<TurfEffort>() {
            @Override
            public void onResponse(Call<TurfEffort> call, Response<TurfEffort> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TurfEffort turfEffort = response.body();
                    myTextView.setText(turfEffort.toString());
                    Log.d(TAG, "API Response for " + username + ": " + turfEffort.toString());
                } else {
                    String errorMsg = "Failed to fetch turfeffort for " + username + ". Code: " + response.code();
                    myTextView.setText(errorMsg);
                    String errorBodyString = "Unknown error";
                    try {
                        errorBodyString = response.errorBody() != null ? response.errorBody().string() : "No additional error info";
                        Log.e(TAG, "Error body: " + errorBodyString);
                        myTextView.append("\nError Details: " + errorBodyString);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                    // Send notification on failure
                    sendErrorNotification("API Error: " + response.code(),
                            "Could not fetch turf effort for " + username + ". " + errorBodyString);
                }
            }

            @Override
            public void onFailure(Call<TurfEffort> call, Throwable t) {
                myTextView.setText("API call failed: " + t.getMessage());
                Log.e(TAG, "API call failed", t);
                Toast.makeText(MainActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                sendErrorNotification("Network Error",
                        "Failed to connect: " + t.getMessage());
            }
        });
    }

    private void sendErrorNotification(String title, String message) {
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MainActivity.class); // Or any other activity you want to open
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_error) // Create this drawable
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // Set the intent that will fire when the user taps the notification
                .setAutoCancel(true); // Automatically removes the notification when the user taps it

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            // This can happen if the POST_NOTIFICATIONS permission is not granted on Android 13+
            Log.e(TAG, "Missing POST_NOTIFICATIONS permission. Cannot show notification.", e);
            Toast.makeText(this, "Notification permission missing. Cannot show error notification.", Toast.LENGTH_LONG).show();
            // Consider guiding the user to grant the permission
        }
    }
}
