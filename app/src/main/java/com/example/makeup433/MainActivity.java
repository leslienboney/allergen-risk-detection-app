package com.example.makeup433;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnManageAllergens;
    private Button btnDetect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnManageAllergens = findViewById(R.id.btnManageAllergens);
        btnDetect          = findViewById(R.id.btnDetect);

        // Go to allergen list screen
        btnManageAllergens.setOnClickListener(v ->
                startActivity(new Intent(this, AllergenManagementActivity.class))
        );

        // Go to detection screen
        btnDetect.setOnClickListener(v ->
                startActivity(new Intent(this, DetectionActivity.class))
        );
    }
}