package com.iyuanyue.car.Config;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigManager {

    private static final String CONFIG_FILE_NAME = "config.json";

    // 从 assets 文件夹中读取配置文件并解析为 Config 对象
    public static Config loadConfig(Context context) {
        Gson gson = new GsonBuilder().create();
        try {
            InputStream inputStream = context.getAssets().open(CONFIG_FILE_NAME);
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            return gson.fromJson(reader, Config.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
