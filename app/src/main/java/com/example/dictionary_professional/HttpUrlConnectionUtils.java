package com.example.dictionary_professional;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Environment;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

public class HttpUrlConnectionUtils {
    public static String getSignUrl = "http://175.178.47.182:80/getSign/";
    public static String getAudioUrl = "http://175.178.47.182:80/getAudio?location=";

    public static String post(String actionUrl, Map<String, File> files) throws IOException {

        String BOUNDARY = java.util.UUID.randomUUID().toString();
        String PREFIX = "--", LINEND = "\r\n";
        String MULTIPART_FROM_DATA = "multipart/form-data";
        String CHARSET = "UTF-8";
        URL uri = new URL(actionUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) uri.openConnection();
        httpURLConnection.setReadTimeout(5 * 1000);
        httpURLConnection.setDoInput(true);// 允许输入
        httpURLConnection.setDoOutput(true);// 允许输出
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setRequestMethod("POST"); // Post方式
        httpURLConnection.setRequestProperty("connection", "keep-alive");
        httpURLConnection.setRequestProperty("Charsert", "UTF-8");
        httpURLConnection.setRequestProperty("Content-Type", MULTIPART_FROM_DATA
                + ";boundary=" + BOUNDARY);


        DataOutputStream outStream = new DataOutputStream(httpURLConnection
                .getOutputStream());


        // 发送文件数据
        if (files != null)
            for (Map.Entry<String, File> file : files.entrySet()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(PREFIX);
                stringBuilder.append(BOUNDARY);
                stringBuilder.append(LINEND);
                stringBuilder.append("Content-Disposition: form-data; name=\"file\"; filename=\""+ file.getKey() + "\"" + LINEND);
                stringBuilder.append("Content-Type: application/octet-stream; charset="
                        + CHARSET + LINEND);
                stringBuilder.append(LINEND);
                outStream.write(stringBuilder.toString().getBytes());
                InputStream is = new FileInputStream(file.getValue());
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }

                is.close();
                outStream.write(LINEND.getBytes());
            }

        // 请求结束标志
        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
        outStream.write(end_data);
        outStream.flush();

        // 得到响应码
        int res = httpURLConnection.getResponseCode();
        InputStream in = httpURLConnection.getInputStream();
        InputStreamReader isReader = new InputStreamReader(in);
        BufferedReader bufReader = new BufferedReader(isReader);
        String line = null;
        String data = "OK";

        while ((line = bufReader.readLine()) == null)
            data += line;

        if (res == 200) {
            int ch;
            StringBuilder sb2 = new StringBuilder();
            while ((ch = in.read()) != -1) {
                sb2.append((char) ch);
            }
        }
        outStream.close();
        httpURLConnection.disconnect();
        return in.toString();
    }


    public static JSONObject getSign(String language, String str) throws IOException, JSONException {
        URL url = new URL(getSignUrl+language+"/"+str);
        return get(url);
    }


    public static JSONObject get(URL url) throws IOException, JSONException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setConnectTimeout(3000);
        httpURLConnection.setRequestMethod("GET");

        int responseCode = httpURLConnection.getResponseCode();
        InputStream inputStream = null;
        JSONObject json = null;

        if(responseCode == HttpURLConnection.HTTP_OK){
            inputStream = httpURLConnection.getInputStream();
            json = new JSONObject(readInputStream(inputStream));
        }
        httpURLConnection.disconnect();
        return json;
    }



    public static String getAudio(String location) throws Exception {
        String path = null;

        URL url = new URL(getAudioUrl+location);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setConnectTimeout(3000);
        httpURLConnection.setRequestMethod("GET");
        String fileName = location.substring(location.length()-19);

        int responseCode = httpURLConnection.getResponseCode();
        InputStream inputStream = null;
        JSONObject json = null;

        if(responseCode == HttpURLConnection.HTTP_OK){
            File dest = new File(Environment.getExternalStorageDirectory()+"/voice_cache/");
            if(!dest.exists()) {
                dest.mkdir();
            }
            path = Environment.getExternalStorageDirectory()+"/voice_cache/";
            InputStream inStream = httpURLConnection.getInputStream();// 通过输入流获取html数据
            byte[] data = readInputStream2(inStream);// 得到html的二进制数据
            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(path+fileName));//把byte写入文件
            dataOutputStream.write(data);
            dataOutputStream.flush();
            path += fileName;
        }
        httpURLConnection.disconnect();
        return path;
    }

    private static String readInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String tmp;
        StringBuilder sb = new StringBuilder();
        while ((tmp = reader.readLine()) != null) {
            sb.append(tmp).append("\n");
        }
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.setLength(sb.length() - 1);
        }
        reader.close();
        System.out.println(sb);
        return sb.toString();
    }


    public static byte[] readInputStream2(InputStream inStream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while( (len=inStream.read(buffer)) != -1 ){
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }


}
