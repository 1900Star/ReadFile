package com.yibao.filereaddemo.util;

/**
 * @ Author: Luoshipeng
 * @ Name:   OnlineLrcUtil
 * @ Email:  strangermy98@gmail.com
 * @ Time:   2018/10/3/ 15:52
 * @ Des:    TODO
 */

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class OnlineLrcUtil {
    private static String TAG = "OnlineLrcUtil";
    private static String lrcRootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/smartisan/music/lyricsa/";
    public static final String queryLrcURLRoot = "http://geci.me/api/lyric/";

    public static String getQueryLrcURL(String title, String artist) {
        String str = queryLrcURLRoot + Encode(title);
        return artist == null ? str : str + "/" + Encode(artist);
    }

    public static String getLrcURL(String title, String artist) {
        String queryLrcURLStr = getQueryLrcURL(title, artist);
        try {
            URL url = new URL(queryLrcURLStr);
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));

            StringBuilder sb = new StringBuilder();
            String temp;
            while ((temp = in.readLine()) != null) {
                sb.append(temp);
            }
//            原生Json解析
//            JSONObject jObject = new JSONObject(sb.toString());
//            int count = jObject.getInt("count");
//            int index = count == 0 ? 0 : new Random().nextInt() % count;
//            JSONArray jArray = jObject.getJSONArray("result");
//            JSONObject obj = jArray.getJSONObject(index);
//            return obj.getString("lrc");
            // Gson解析
            Gson gson = new Gson();
            OnlineLyricBean o = gson.fromJson(sb.toString(), OnlineLyricBean.class);
            int count1 = o.getCount();
            int indexs = count1 == 0 ? 0 : new Random().nextInt() % count1;
            List<OnlineLyricBean.ResultBean> result = o.getResult();
            return result.get(indexs).getLrc();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // 歌手，歌曲名中的空格进行转码
    private static String Encode(String str) {

        try {
            return URLEncoder.encode(str.trim(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return str;

    }

    // 歌词文件网络地址，歌词文件本地缓冲地址
    public static void downPic(String url, final String str) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder().url(url).addHeader("Accept-Encoding", "identity")
                .build();

        okHttpClient
                .newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        InputStream is;
                        byte[] buf = new byte[1024 * 4];
                        int len;
                        int off = 0;
                        FileOutputStream fos;
                        ResponseBody body = response.body();
                        if (body != null) {
                            is = body.byteStream();
                            try {
                                fos = new FileOutputStream(getFile(str));
                                while ((len = is.read(buf)) != -1) {
                                    fos.write(buf, off, len);
                                }
                                fos.flush();
                                fos.close();
                                is.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
//                        observable.onNext(SyncStateContract.Constants.FIRST_DWON);
//                        observable.onComplete();
                    }
                });
    }

    private static File getFile(String str) {
        File file = new File(lrcRootPath);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.d(TAG, "===创建失败");
            }
        }
        return new File(file + "/", str + "$$" + str + ".lrc");
    }
}

