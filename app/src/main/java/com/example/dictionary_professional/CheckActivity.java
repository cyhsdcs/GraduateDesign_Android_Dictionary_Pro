package com.example.dictionary_professional;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CheckActivity extends AppCompatActivity {

    private ListView mList;
    private CardAdapter mAdapter;
    private Button btn_yes;
    private Button btn_no;
    final Handler handler = new Handler();
    public String[] array;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        mList = findViewById(R.id.listview);
        array = getIntent().getStringArrayExtra("info");
        btn_yes = findViewById(R.id.btn_yes);
        btn_no = findViewById(R.id.btn_no);


        init();
        init_btn();


    }

    private void init(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = HttpUrlConnectionUtils.getSign(array[0], array[1]);
                    JSONObject data = jsonObject.getJSONObject("data");

                    List<Card> list = null;
                    int counter = 0;
                    boolean flag = false;
                    for (Iterator<String> it = data.keys(); it.hasNext(); ) {
                        String key = it.next();
                        if(key.equals(array[2]))
                        {
                            flag = true;
                            JSONArray value = data.getJSONArray(key);
                            list = new ArrayList<>();
                            for(int i = 0 ; i < value.length();i++) {
                                list.add(new Card(counter, key, value.get(i).toString()));
                                counter++;
                            }
                        }
                    }
                    if(!flag)
                        moveToRecord(array);
                    else{
                        initializeListView(list);
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                    moveToRecord(array);
                } catch (JSONException e) {
                    e.printStackTrace();
                    moveToRecord(array);
                }
            }
        }).start();

    }

    private void init_btn(){
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToRecord(array);
            }
        });

        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckActivity.this.finish();
            }
        });
    }

    private void moveToRecord(String[] array){
        Intent intent = new Intent(CheckActivity.this, RecordActivity.class);
        intent.putExtra("info", array);
        startActivity(intent);
        CheckActivity.this.finish();
    }

    private void initializeListView(List<Card> list){
        mAdapter = new CardAdapter(this, list);

        handler.post(new Runnable() {
            @Override
            public void run() {
                mList.setAdapter(mAdapter);
            }
        });


    }
}