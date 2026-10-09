package com.example.actividad_8;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {

    public static final String EXTRA_NAME = "com.example.actividad_8.EXTRA_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        MainActivity.applySystemBarInsets(findViewById(R.id.second));

        // Read the value sent by MainActivity.
        String name = getIntent().getStringExtra(EXTRA_NAME);
        if (name == null) {
            name = "";
        }

        TextView textReceivedName = findViewById(R.id.textReceivedName);
        TextView textWelcome = findViewById(R.id.textWelcome);
        textReceivedName.setText(name);
        textWelcome.setText(getString(R.string.second_message, name));

        Button buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish());
    }
}
