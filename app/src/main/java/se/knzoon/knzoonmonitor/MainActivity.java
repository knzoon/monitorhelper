package se.knzoon.knzoonmonitor;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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

    private void fetchTurfEffort(String searchTerm) {
        myTextView.setText("Fetching data with Retrofit...");
        ApiService service = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);
        Call<TurfEffort> call = service.turfeffort(searchTerm);

        call.enqueue(new Callback<TurfEffort>() {
            @Override
            public void onResponse(Call<TurfEffort> call, Response<TurfEffort> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TurfEffort turfEffort = response.body();
                    myTextView.setText(turfEffort.toString());
                } else {
                    myTextView.setText("Failed to fetch turfeffort. Code: " + response.code());
                    try {
                        // Log error body if present
                        Log.e(TAG, "Error body: " + (response.errorBody() != null ? response.errorBody().string() : "null"));
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<TurfEffort> call, Throwable t) {
                myTextView.setText("API call failed: " + t.getMessage());
                Log.e(TAG, "API call failed", t);
                Toast.makeText(MainActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
