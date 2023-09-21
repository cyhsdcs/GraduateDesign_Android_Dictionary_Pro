package com.example.dictionary_professional;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;



import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RecordActivity extends AppCompatActivity {

    int timeCount; // 录音时长 计数
    final int TIME_COUNT = 0x101;
    boolean isRecording;
    Thread timeThread;
    public boolean hasRecorded;
    public MediaRecorder mMediaRecorder;
    public MediaPlayer mMediaPlayer;
    public String fileName;
    public String filePath;
    public Button btnStart;
    public Button btnStop;
    public Button btnPlay;
    public Button btnConfirm;
    public TextView textTime;
    public Handler myHandler;

    public String[] array;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        array = getIntent().getStringArrayExtra("info");

        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        textTime = findViewById(R.id.text_time);
        btnPlay = findViewById(R.id.btn_play);
        btnConfirm = findViewById(R.id.btn_confirm2);

        btnStop.setEnabled(false);
        btnPlay.setEnabled(false);

        hasRecorded = false;
        isRecording = false;

        myHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                switch ((msg.what)){
                    case TIME_COUNT:
                        int count = (int) msg.obj;
                        System.out.println("count == "+count);
                        textTime.setText(FormatMiss(count));
                        break;
                }
            }
        };


        btnStart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                if(hasRecorded){
                    cancel();
                    hasRecorded = false;
                }

                startRecord();
                isRecording = true;
                timeThread = new Thread(new Runnable(){
                    @Override
                    public void run(){
                        countTime();
                    }
                });
                timeThread.start();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                btnStart.setEnabled(true);
                btnStart.setText(R.string.restart_record);
                btnStop.setEnabled(false);
                btnPlay.setEnabled(true);
                stopRecord();
                isRecording = false;
                hasRecorded = true;
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasRecorded) {
                    try {
                        playMusic();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String url = "http://175.178.47.182:80/upload/"+array[0]+"/"+array[1]+"/"+array[2];
                            File file = new File(filePath);
                            Map<String, File> map = new HashMap<String, File>();
                            map.put(fileName, file);
                            try {
                                HttpUrlConnectionUtils.post( url, map);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            cancel();
                            finish();
                        }
                    }).start();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

    public void startRecord() {
        // 开始录音
        /* ①Initial：实例化MediaRecorder对象 */
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        try {
            /* ②setAudioSource/setVideoSource */
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置麦克风
            /*
             * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
             * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
             */
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样 */
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            fileName = DateFormat.format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA)) + ".m4a";
            File destDir = new File(Environment.getExternalStorageDirectory() + "/test/");
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            filePath = Environment.getExternalStorageDirectory() + "/test/" + fileName;
            /* ③准备 */
            mMediaRecorder.setOutputFile(filePath);
            mMediaRecorder.prepare();
            /* ④开始 */
            mMediaRecorder.start();
        } catch (IllegalStateException e) {
            Log.i("failed!", e.getMessage());
        } catch (IOException e) {
            Log.i("failed!", e.getMessage());
        }
    }

    public void stopRecord() {
        try {
            //下面三个参数必须加，不加的话会奔溃，在MediaRecorder.stop();
            //报错为：RuntimeException:stop failed
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setOnInfoListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            mMediaRecorder.stop();
        } catch (IllegalStateException e) {
            // TODO: handle exception
            Log.i("Exception", Log.getStackTraceString(e));
            cancel();

        }catch (RuntimeException e) {
            // TODO: handle exception
            Log.i("Exception", Log.getStackTraceString(e));
            cancel();
        }catch (Exception e) {
            // TODO: handle exception
            Log.i("Exception", Log.getStackTraceString(e));
            cancel();
        }

    }

    //结束录音异常处理
    private void cancel(){
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;

        File file = new File(filePath);
        if (file.isFile() && file.exists())
            file.delete();
        filePath = "";
    }

    private void countTime() {
        while (isRecording) {
            System.out.println("正在录音");
            timeCount++;
            Message msg = Message.obtain();
            msg.what = TIME_COUNT;
            msg.obj = timeCount;
            myHandler.sendMessage(msg);
            try {
                timeThread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("结束录音");
        timeCount = 0;
        Message msg = Message.obtain();
        msg.what = TIME_COUNT;
        msg.obj = timeCount;
        myHandler.sendMessage(msg);
    }

    public static String FormatMiss(int miss) {
        String hh = miss / 3600>9?miss/3600 + "" : "0" + miss / 3600;
        String mm = (miss % 3600) / 60>9 ? (miss % 3600) / 60 + "" : "0" + (miss % 3600) / 60;
        String ss = (miss % 3600) % 60>9 ? (miss % 3600) % 60 + "" : "0" + (miss % 3600) % 60;
        return hh + ":" + mm + ":" + ss;
    }

    public void playMusic() throws IOException {
        if(mMediaPlayer == null){
            mMediaPlayer = new MediaPlayer();
        }

        mMediaPlayer.reset();
        mMediaPlayer.setDataSource(filePath);
        mMediaPlayer.prepare();
        mMediaPlayer.start();
    }


}