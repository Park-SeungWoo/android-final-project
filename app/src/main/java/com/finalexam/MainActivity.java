package com.finalexam;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.finalexam.activities.OrderActivity;

public class MainActivity extends AppCompatActivity {

    Button goOrderBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        goOrderBtn = findViewById(R.id.goOrderBtn);
        goOrderBtn.setOnClickListener(new BtnOnClickListener());

    }

    class BtnOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent goOrderIntent = new Intent(MainActivity.this, OrderActivity.class);
            startActivity(goOrderIntent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);  // slide animation
        }
    }
}