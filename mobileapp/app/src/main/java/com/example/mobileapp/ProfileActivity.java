package com.example.mobileapp;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scroll), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        ((EditText)findViewById(R.id.et_first_name)).setText("Andrew");
        ((EditText)findViewById(R.id.et_last_name)).setText("Wilson");
        ((EditText)findViewById(R.id.et_email)).setText("andrewwilson@email.com");
        ((EditText)findViewById(R.id.et_address)).setText("Bradford");
        ((EditText)findViewById(R.id.et_phone)).setText("065 123 1233");
        ((TextView)findViewById(R.id.tv_full_name)).setText("Andrew Wilson");
        ((TextView)findViewById(R.id.tv_email)).setText("andrewwilson@email.com");

    }

}