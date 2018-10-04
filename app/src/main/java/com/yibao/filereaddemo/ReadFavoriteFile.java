package com.yibao.filereaddemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * @ Author: Luoshipeng
 * @ Name:   ReadFavoriteFile
 * @ Email:  strangermy98@gmail.com
 * @ Time:   2018/8/30/ 16:59
 * @ Des:    读取歌曲收藏文件
 */
public class ReadFavoriteFile {
    private static final String FAVORITE_FILE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/smartisan/music/favorite_demo.txt/";
    @SuppressLint("CheckResult")
    private void showText(final Context context, final String content) {
        Observable.just(readFile())
                .map(new Function<String, Boolean>() {
                    @Override
                    public Boolean apply(String s) {
                        return s.contains(content);
                    }
                })
                .map(new Function<Boolean, List<String>>() {
                    @Override
                    public List<String> apply(Boolean aBoolean) {
                        return deleteText(FAVORITE_FILE, content, aBoolean);
                    }
                })
                .map(new Function<List<String>, Boolean>() {
                    @Override
                    public Boolean apply(List<String> list) {

                        return againWrite(FAVORITE_FILE, list);
                    }
                })
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) {
                        if (aBoolean) {
                            readFile();
                        } else {
                            Toast.makeText(context, "该歌曲还没有收藏", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
    }


    private List<String> deleteText(String filePath, String str, Boolean aBoolean) {
        List<String> list = new ArrayList<>();
        if (!aBoolean) {
            return list;
        } else {
            File file = new File(filePath);
            BufferedReader reader;
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
                for (; ; ) {
                    String text = reader.readLine();
                    if (text == null) {
                        break;

                    } else {
                        if (text.equals(str)) {
                            continue;
                        }
                        list.add(text);
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return list;

        }

    }

    private boolean againWrite(String filePath, List<String> list) {
        boolean a = false;
        String nullString = "";
        FileOutputStream outputStream;
        try {
            File file = new File(filePath);
            outputStream = new FileOutputStream(file, false);
            if (!file.exists()) {
                a = false;
            } else if (list == null || list.size() < 1) {
//                mTv.setText(null);
                outputStream.write(nullString.getBytes());
                a = false;
            } else {

                for (String s : list) {
                    outputStream.write(s.getBytes());
                    outputStream.write("\n".getBytes());
                }
                outputStream.close();
                a = true;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return a;
    }

    private void writeFile(Context context,String directory, String s) {
        File file = new File(directory);

        //实例化一个输出流
        FileOutputStream out;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            out = new FileOutputStream(file, true);
            //把文字转化为字节数组
            byte[] bytes = s.getBytes();
            //写入字节数组到文件
            out.write(bytes);
            out.write("\n".getBytes());
            //关闭输入流
            out.close();
            Toast.makeText(context, "写入成功", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public String readFile() {
        String textContent = null;
        try {
            //实例化一个输入流
            FileInputStream input = new FileInputStream(FAVORITE_FILE);
            //把文件中的所有内容转换为byte字节数组
            byte[] bytes = new byte[input.available()];
            //读取内容
            input.read(bytes);
            //关闭输入流
            input.close();
            //把bytes字节数组转化为文字
            textContent = new String(bytes);
//            mTv.setText(textContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return textContent;
    }

}
