package com.example.dictionary_professional;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {


    public RequestPermission requestPermission;
    public Button btn_confirm;
    public TextView language;
    public TextView word;
    public TextView sign;
    public String[] array;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        language = findViewById(R.id.language_text);
        word = findViewById(R.id.str_text);
        sign = findViewById(R.id.sign_text);
        btn_confirm = findViewById(R.id.btn_confirm);
        if(requestPermission == null) {
            requestPermission = new RequestPermission();
        }
        requestPermission.RequestPermission(this);

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                array = new String[3];
                array[0] = language.getText().toString();
                array[1] = word.getText().toString();
                array[2] = sign.getText().toString();

                moveToCheck(array);
            }
        });
    }

    private void moveToCheck(String[] array){
        Intent intent = new Intent(MainActivity.this, CheckActivity.class);
        intent.putExtra("info", array);
        startActivity(intent);
    }




}