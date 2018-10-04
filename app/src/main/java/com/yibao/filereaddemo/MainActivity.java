package com.yibao.filereaddemo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yibao.filereaddemo.util.OnlineLrcUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEdit;
    /**
     * FileReadDemo
     */
    private TextView mTv;
    private static final String FAVORITE_FILE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/smartisan/music/favorite.txt/";
    private BufferedReader br;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        readFile();

    }

    private void initView() {
        mEdit = findViewById(R.id.edit);
        /*
      写入
     */
        Button btnInput = findViewById(R.id.btn_input);
        Button btnClear = findViewById(R.id.btn_clear);
        Button btnDelete = findViewById(R.id.btn_delete);
        btnInput.setOnClickListener(this);
        /*
      read_text
     */
        Button btnOut = findViewById(R.id.btn_out);
        btnOut.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        mTv = findViewById(R.id.tv);
    }

    @Override
    public void onClick(View v) {
        String content = mEdit.getText().toString().trim();
        switch (v.getId()) {
            case R.id.btn_clear:
//                mEdit.setText(null);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String lrcURL = OnlineLrcUtil.getLrcURL("童话", null);
                        Log.d("lsp", lrcURL);
                        OnlineLrcUtil.downPic(lrcURL, "林俊杰");
                    }
                }).start();
//                mTv.setText(lrcURL);
                break;
            case R.id.btn_input:
                writeFile(FAVORITE_FILE, content);
                readFile();
                break;
            case R.id.btn_out:
                readFile();
                break;
            case R.id.btn_delete:
                showText(content);

                break;
            default:
                break;
        }
    }

    @SuppressLint("CheckResult")
    private void showText(final String content) {
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
                            Toast.makeText(MainActivity.this, "该歌曲还没有收藏", Toast.LENGTH_SHORT).show();
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
                mTv.setText(null);
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

    private void writeFile(String directory, String s) {
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
            Toast.makeText(MainActivity.this, "写入成功", Toast.LENGTH_SHORT).show();
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
            mTv.setText(textContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return textContent;
    }


}
