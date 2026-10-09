package com.example.actividad_8;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private EditText editName;
    private TextView textFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        applySystemBarInsets(findViewById(R.id.main));

        editName = findViewById(R.id.editName);
        textFeedback = findViewById(R.id.textFeedback);
        Button buttonGreet = findViewById(R.id.buttonGreet);
        Button buttonContinue = findViewById(R.id.buttonContinue);

        // Show a personalized greeting on this screen.
        buttonGreet.setOnClickListener(v -> {
            String name = readValidatedName();
            if (name != null) {
                textFeedback.setText(getString(R.string.greeting, name));
            }
        });

        // Explicit Intent: the destination activity is named directly.
        buttonContinue.setOnClickListener(v -> {
            String name = readValidatedName();
            if (name != null) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                intent.putExtra(SecondActivity.EXTRA_NAME, name);
                startActivity(intent);
            }
        });
    }

    /** Returns the trimmed name, or null after showing an error if it is empty. */
    private String readValidatedName() {
        String name = editName.getText().toString().trim();
        if (name.isEmpty()) {
            editName.setError(getString(R.string.error_empty_name));
            editName.requestFocus();
            textFeedback.setText(R.string.error_empty_name);
            return null;
        }
        return name;
    }

    /** Keeps content clear of the status and navigation bars (edge-to-edge is enforced on API 35+). */
    static void applySystemBarInsets(android.view.View root) {
        int left = root.getPaddingLeft();
        int top = root.getPaddingTop();
        int right = root.getPaddingRight();
        int bottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
