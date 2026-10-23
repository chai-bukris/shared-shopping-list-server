package com.example.sharedshoppinglist;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.SocketTimeoutException;

public class AddActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TextInputEditText itemInput;
    private MaterialButton submitButton;
    private MaterialButton backHomeButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        itemInput = findViewById(R.id.itemInput);
        submitButton = findViewById(R.id.submitButton);
        backHomeButton = findViewById(R.id.backHomeButton);
        progressBar = findViewById(R.id.progressBar);
        submitButton.setOnClickListener(v -> submitItem());
        backHomeButton.setOnClickListener(v -> finish());
    }

    private void submitItem() {
        String rawItem = itemInput.getText() == null ? "" : itemInput.getText().toString();
        if (rawItem.trim().isEmpty()) {
            itemInput.setError("כתבו מה חסר בבית");
            return;
        }
        submitButton.setEnabled(false);
        itemInput.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            try {
                ApiClient.addItem(rawItem);
                runOnUiThread(() -> {
                    Toast.makeText(this, "נוסף לרשימה", Toast.LENGTH_SHORT).show();
                    itemInput.setText("");
                    itemInput.setEnabled(true);
                    itemInput.requestFocus();
                    ((android.view.inputmethod.InputMethodManager)
                            getSystemService(INPUT_METHOD_SERVICE))
                            .showSoftInput(itemInput,
                                    android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                });
            } catch (Exception e) {
                Log.e("AddActivity", "Could not add item", e);
                runOnUiThread(() -> Toast.makeText(this,
                        userErrorMessage(e),
                        Toast.LENGTH_LONG).show());
            } finally {
                runOnUiThread(() -> {
                    submitButton.setEnabled(true);
                    itemInput.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }

    private String userErrorMessage(Exception error) {
        Throwable cause = error;
        while (cause.getCause() != null && cause.getCause() != cause) cause = cause.getCause();
        if (cause instanceof SocketTimeoutException) {
            return "השרת לא ענה בזמן. אם הוא התעורר עכשיו, נסו שוב בעוד דקה.";
        }
        String detail = error.getMessage();
        if (detail == null || detail.trim().isEmpty()) detail = error.getClass().getSimpleName();
        return "ההוספה נכשלה: " + detail;
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
