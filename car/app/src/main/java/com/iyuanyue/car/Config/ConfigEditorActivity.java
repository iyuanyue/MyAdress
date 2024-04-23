package com.iyuanyue.car.Config;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iyuanyue.car.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.Map;

public class ConfigEditorActivity extends AppCompatActivity {

    private EditText editTextConfigContent;
    private static final String FILE_NAME = "config.json";
    private Map<String, Map<String, Map<String, String>>> configData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_editor);

        // 初始化界面控件
        editTextConfigContent = findViewById(R.id.editText_config_content);
        Button buttonSaveConfig = findViewById(R.id.button_save_config);
        Button buttonDefaultSettings = findViewById(R.id.button_default_settings);
        Button buttonCancel = findViewById(R.id.button_cancel);

        // 加载并显示config.json文件内容
        loadConfigContent();

        // 设置按钮的点击事件监听器
        buttonSaveConfig.setOnClickListener(v -> saveConfigContent());
        buttonDefaultSettings.setOnClickListener(v -> loadDefaultSettings());
        buttonCancel.setOnClickListener(v -> finish()); // 直接关闭当前页面
    }

    // 加载默认设置
    private void loadDefaultSettings() {
        // 从 assets 文件夹中加载默认配置文件
        try {
            InputStream inputStream = getAssets().open("config.json");
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }
            // 将默认配置文件内容显示到编辑框中
            editTextConfigContent.setText(content.toString());
            Toast.makeText(this, "加载默认设置成功", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "加载默认设置失败", Toast.LENGTH_SHORT).show();
        }
    }

    // 加载并显示config.json文件内容
    private void loadConfigContent() {
        FileInputStream fis = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try {
            fis = openFileInput(FILE_NAME);
            Log.d("ConfigEditorActivity", "文件路径: " + getFilesDir() + "/" + FILE_NAME); // 日志：文件路径
            reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
                if (reader.ready()) {
                    content.append("\n"); // 添加换行符
                }
            }
            Log.d("ConfigEditorActivity", "文件内容: " + content.toString()); // 日志：文件内容
            // 格式化JSON字符串
            String formattedContent = formatJson(content.toString());
            // 将格式化后的config.json文件内容显示到EditText中
            editTextConfigContent.setText(formattedContent);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 格式化JSON字符串
    private String formatJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.toString(4); // 使用4个空格进行缩进
        } catch (JSONException e) {
            e.printStackTrace();
            return json; // 返回原始JSON字符串
        }
    }


    // 保存config.json文件内容
    private void saveConfigContent() {
        String newContent = editTextConfigContent.getText().toString();
        FileOutputStream fos = null;
        BufferedWriter writer = null;
        try {
            fos = openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(newContent);
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
            // 关闭当前 Activity，并传递结果给 MainActivity
            setResult(RESULT_OK);
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}