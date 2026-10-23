package com.example.sharedshoppinglist;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.SocketTimeoutException;

public class ListActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<CheckBox> itemChecks = new ArrayList<>();
    private LinearLayout itemsContainer;
    private CheckBox selectAll;
    private MaterialButton doneButton;
    private ProgressBar progressBar;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        itemsContainer = findViewById(R.id.itemsContainer);
        selectAll = findViewById(R.id.selectAllCheckBox);
        doneButton = findViewById(R.id.doneButton);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.emptyText);

        selectAll.setOnCheckedChangeListener((button, checked) -> {
            for (CheckBox item : itemChecks) item.setChecked(checked);
            updateDoneButton();
        });
        doneButton.setOnClickListener(v -> removeSelected());
        loadList();
    }

    private void loadList() {
        setLoading(true);
        executor.execute(() -> {
            try {
                String response = ApiClient.getList();
                runOnUiThread(() -> showItems(response));
            } catch (Exception e) {
                Log.e("ListActivity", "Could not load shopping list", e);
                runOnUiThread(() -> showError(userErrorMessage(e)));
            } finally {
                runOnUiThread(() -> setLoading(false));
            }
        });
    }

    private String userErrorMessage(Exception error) {
        Throwable cause = error;
        while (cause.getCause() != null && cause.getCause() != cause) cause = cause.getCause();
        if (cause instanceof SocketTimeoutException) {
            return "השרת לא ענה בזמן. אם הוא התעורר עכשיו, נסו לטעון שוב בעוד דקה.";
        }
        String detail = error.getMessage();
        if (detail == null || detail.trim().isEmpty()) detail = error.getClass().getSimpleName();
        return "עדכון הרשימה נכשל: " + detail;
    }

    private void showItems(String response) {
        itemsContainer.removeAllViews();
        itemChecks.clear();
        selectAll.setChecked(false);
        String[] rows = response.split("\\r?\\n");
        for (String row : rows) {
            if (row.trim().isEmpty()) continue;
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(row);
            checkBox.setTextColor(0xFF21372E);
            checkBox.setTextSize(17);
            checkBox.setPadding(8, 10, 8, 10);
            checkBox.setBackgroundColor(0xFFFFFFFF);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = 8;
            itemsContainer.addView(checkBox, params);
            itemChecks.add(checkBox);
            checkBox.setOnCheckedChangeListener((button, checked) -> updateDoneButton());
        }
        emptyText.setVisibility(itemChecks.isEmpty() ? View.VISIBLE : View.GONE);
        updateDoneButton();
    }

    private void removeSelected() {
        List<String> selected = new ArrayList<>();
        for (CheckBox checkBox : itemChecks) {
            if (checkBox.isChecked()) selected.add(checkBox.getText().toString());
        }
        if (selected.isEmpty()) return;
        setLoading(true);
        String payload = android.text.TextUtils.join(",", selected);
        executor.execute(() -> {
            try {
                ApiClient.removeItems(payload);
                runOnUiThread(() -> Toast.makeText(this, "המוצרים הוסרו מהרשימה", Toast.LENGTH_SHORT).show());
                loadList();
            } catch (Exception e) {
                runOnUiThread(() -> showError("לא ניתן לעדכן את הרשימה. נסו שוב."));
                runOnUiThread(() -> setLoading(false));
            }
        });
    }

    private void updateDoneButton() {
        boolean anySelected = false;
        boolean allSelected = !itemChecks.isEmpty();
        for (CheckBox checkBox : itemChecks) {
            anySelected |= checkBox.isChecked();
            allSelected &= checkBox.isChecked();
        }
        doneButton.setVisibility(anySelected ? View.VISIBLE : View.GONE);
        if (selectAll.isChecked() != allSelected) {
            selectAll.setOnCheckedChangeListener(null);
            selectAll.setChecked(allSelected);
            selectAll.setOnCheckedChangeListener((button, checked) -> {
                for (CheckBox item : itemChecks) item.setChecked(checked);
                updateDoneButton();
            });
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        doneButton.setEnabled(!loading);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
