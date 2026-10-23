package com.example.sharedshoppinglist;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialButton listButton = findViewById(R.id.openListButton);
        MaterialButton addButton = findViewById(R.id.addItemButton);
        listButton.setOnClickListener(v -> startActivity(new Intent(this, ListActivity.class)));
        addButton.setOnClickListener(v -> startActivity(new Intent(this, AddActivity.class)));
    }
}
