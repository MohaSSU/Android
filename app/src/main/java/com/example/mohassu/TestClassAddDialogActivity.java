package com.example.mohassu;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mohassu.DialogFragment.ClassAddDialogFragment;

public class TestClassAddDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test_button);

        Button testButton = findViewById(R.id.testBtn);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClassAddDialogFragment classAddDialogFragment = new ClassAddDialogFragment();
                classAddDialogFragment.show(getSupportFragmentManager(), "ClassAddDialog");
            }
        });
    }
}