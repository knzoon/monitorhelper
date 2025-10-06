package se.knzoon.knzoonmonitor.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import se.knzoon.knzoonmonitor.MainActivity;
import se.knzoon.knzoonmonitor.R;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    public static final String CHANNEL_ID = "turf_effort_error_channel"; // Keep consistent
    public static final int NOTIFICATION_ID_BASE = 101; // Base ID

    // Method to create the channel, call this from Application or MainActivity onCreate
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created or already exists.");
            } else {
                Log.e(TAG, "NotificationManager is null. Cannot create notification channel.");
            }
        }
    }

    public static void sendErrorNotification(Context context, String title, String message) {
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Good for bringing existing task to front
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_error)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message)) // For longer messages
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Check for POST_NOTIFICATIONS permission before attempting to notify
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Cannot show notification from worker.");
                // Optionally, inform the user via a different mechanism if this is critical,
                // but workers usually operate silently or rely on prior permission.
                return;
            }
        }

        // Use a dynamic notification ID or a fixed one if you want to update the same notification
        // For distinct errors, a dynamic ID might be better, or add a timestamp to the message.
        // For simplicity, using a base ID + current time to make it somewhat unique.
        int notificationId = NOTIFICATION_ID_BASE + (int) (System.currentTimeMillis() % 10000);
        try {
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Error notification sent: " + title);
        } catch (SecurityException e) {
            // This catch is mostly for pre-emptive safety, the check above should handle it.
            Log.e(TAG, "SecurityException trying to send notification (permission likely missing).", e);
        }
    }
}
