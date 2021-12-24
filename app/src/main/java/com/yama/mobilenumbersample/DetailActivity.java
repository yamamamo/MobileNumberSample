package com.yama.mobilenumbersample;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {
    TextView tv_name, tv_number;
    private ActivityResultContracts activityResultContract;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_detail);

        //Intent 로 데이터를 받아옵니다.
        Intent intent = getIntent ();
        String name = intent.getStringExtra ("name");
        String number = intent.getStringExtra ("number");

        tv_name = findViewById (R.id.tv_name);
        tv_number = findViewById (R.id.tv_number);

        //받아온 데이터를 TextView 에 띄워줍니다.
        tv_name.setText (name);
        tv_number.setText (number);

    }
}