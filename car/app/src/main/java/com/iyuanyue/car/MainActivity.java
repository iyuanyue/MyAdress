package com.iyuanyue.car;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iyuanyue.car.Config.ConfigEditorActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private MyAdapter appAdapter;
    private static final int REQUEST_PERMISSION_CODE = 123;
    private ImageButton btnWifi;
    private NetworkReceiver networkReceiver;
    // 声明配置数据变量
    private Map<String, Map<String, Map<String, String>>> configData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 检查并加载配置文件
        loadConfigData();

        // 设置 RecyclerView_app
        setupAppRecyclerView();

        // 请求权限
        requestStoragePermission();

        // 设置 RecyclerView_homework
        setupHomeworkRecyclerView();

        // 设置 RecyclerView_nearby
        setupNearbyRecyclerView();

        // 设置 RecyclerView_address
        setupAddressRecyclerView();

        // 初始化 Wi-Fi 按钮图标
        initWifiButton();

        // 启动计时器
        startTimer();

        // 设置 btnMenu 按钮的点击事件监听器
        setupMenuButton();
    }

    // 加载和解析配置文件
    private void loadConfigData() {
        try {
            // 检查配置文件是否存在
            File configFile = new File(getFilesDir(), "config.json");
            if (!configFile.exists()) {
                // 如果配置文件不存在，则从 assets 文件夹中复制
                copyConfigFromAssets();
            }

            // 从应用私有文件目录中读取配置文件
            FileInputStream inputStream = new FileInputStream(configFile);

            // 使用 Gson 解析 JSON 数据
            Gson gson = new Gson();
            InputStreamReader reader = new InputStreamReader(inputStream);
            Type configType = new TypeToken<Map<String, Map<String, Map<String, String>>>>() {}.getType();
            configData = gson.fromJson(reader, configType);

            // 关闭输入流
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            // 在发生异常时给出用户友好的提示
            Toast.makeText(this, "无法加载配置文件", Toast.LENGTH_SHORT).show();
        }
    }

    // 从 assets 目录复制配置文件到应用私有文件目录
    private void copyConfigFromAssets() {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = getAssets().open("config.json");
            File outFile = new File(getFilesDir(), "config.json");
            outputStream = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            Toast.makeText(this, "配置文件已复制到应用私有文件目录", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 设置 RecyclerView_app
    private void setupAppRecyclerView() {
        RecyclerView appRecyclerView = findViewById(R.id.recyclerView_app);
        int appSpanCount = 4; // 每行显示的图标数量
        GridLayoutManager appLayoutManager = new GridLayoutManager(this, appSpanCount);
        appRecyclerView.setLayoutManager(appLayoutManager);

        // 检查配置数据是否为空
        if (configData != null && configData.containsKey("apps")) {
            List<String> appDataset = new ArrayList<>();
            List<String> appIconTypes = new ArrayList<>();

            // 从配置数据中获取应用的名称和包名
            Map<String, Map<String, String>> appConfig = configData.get("apps");
            for (Map.Entry<String, Map<String, String>> entry : appConfig.entrySet()) {
                appDataset.add(entry.getValue().get("name"));
                appIconTypes.add(entry.getValue().get("icon"));
            }

            // 创建并设置适配器，将 isAppRecyclerView 设置为 true
            MyAdapter appAdapter = new MyAdapter(this, appDataset, appIconTypes, configData, true);
            appRecyclerView.setAdapter(appAdapter);
        }
    }


    // 请求权限
    private void requestStoragePermission() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_CODE);
        } else {
            // 如果权限已被授予，则更新应用程序列表
            updateAppList();
        }
    }

    // 设置 RecyclerView_homework
    private void setupHomeworkRecyclerView() {
        RecyclerView homeworkRecyclerView = findViewById(R.id.recyclerView_homework);
        int homeworkSpanCount = 3; // 每行显示的图标数量
        GridLayoutManager homeworkLayoutManager = new GridLayoutManager(this, homeworkSpanCount);
        homeworkRecyclerView.setLayoutManager(homeworkLayoutManager);

        // 检查配置数据是否为空
        if (configData != null && configData.containsKey("homework")) {
            List<String> homeworkDataset = new ArrayList<>();
            List<String> homeworkIconTypes = new ArrayList<>();

            // 从配置数据中获取作业的名称和图标类型
            Map<String, Map<String, String>> homeworkConfig = configData.get("homework");
            for (Map.Entry<String, Map<String, String>> entry : homeworkConfig.entrySet()) {
                homeworkDataset.add(entry.getValue().get("name"));
                homeworkIconTypes.add(entry.getValue().get("icon"));
            }

            // 创建并设置适配器
            MyAdapter homeworkAdapter = new MyAdapter(this, homeworkDataset, homeworkIconTypes, configData,false);
            homeworkRecyclerView.setAdapter(homeworkAdapter);
        }
    }

    // 设置 RecyclerView_nearby
    private void setupNearbyRecyclerView() {
        RecyclerView nearbyRecyclerView = findViewById(R.id.recyclerView_nearby);
        int nearbySpanCount = 4; // 每行显示的图标数量
        GridLayoutManager nearbyLayoutManager = new GridLayoutManager(this, nearbySpanCount);
        nearbyRecyclerView.setLayoutManager(nearbyLayoutManager);

        // 检查配置数据是否为空
        if (configData != null && configData.containsKey("nearby")) {
            List<String> nearbyDataset = new ArrayList<>();
            List<String> nearbyIconTypes = new ArrayList<>();

            // 从配置数据中获取附近地点的名称和图标类型
            Map<String, Map<String, String>> nearbyConfig = configData.get("nearby");
            for (Map.Entry<String, Map<String, String>> entry : nearbyConfig.entrySet()) {
                nearbyDataset.add(entry.getValue().get("name"));
                nearbyIconTypes.add(entry.getValue().get("icon"));
            }

// 创建并设置适配器
            MyAdapter nearbyAdapter = new MyAdapter(this, nearbyDataset, nearbyIconTypes, configData,false);
            nearbyRecyclerView.setAdapter(nearbyAdapter);
        }
    }


    // 设置 RecyclerView_address
    private void setupAddressRecyclerView() {
        RecyclerView addressRecyclerView = findViewById(R.id.recyclerView_address);
        int addressSpanCount = 3; // 每行显示的图标数量
        GridLayoutManager addressLayoutManager = new GridLayoutManager(this, addressSpanCount);
        addressRecyclerView.setLayoutManager(addressLayoutManager);

        // 检查配置数据是否为空
        if (configData != null && configData.containsKey("address")) {
            List<String> addressDataset = new ArrayList<>();
            List<String> addressIconTypes = new ArrayList<>();

            // 从配置数据中获取地址的名称和图标类型
            Map<String, Map<String, String>> addressConfig = configData.get("address");
            for (Map.Entry<String, Map<String, String>> entry : addressConfig.entrySet()) {
                addressDataset.add(entry.getValue().get("name"));
                addressIconTypes.add(entry.getValue().get("icon"));
            }

// 创建并设置适配器
            MyAdapter addressAdapter = new MyAdapter(this, addressDataset, addressIconTypes, configData,false);
            addressRecyclerView.setAdapter(addressAdapter);
        }
    }


    // 初始化 Wi-Fi 按钮图标
    private void initWifiButton() {
        btnWifi = findViewById(R.id.btnWifi);
        updateWifiIcon();
        btnWifi.setOnClickListener(v -> openWiFiSettings());
    }

    // 更新 Wi-Fi 按钮图标
    private void updateWifiIcon() {
        if (isWifiConnected()) {
            btnWifi.setImageResource(R.drawable.icon_wifi_1);
        } else {
            btnWifi.setImageResource(R.drawable.icon_wifi_0);
        }
    }

    // 检查 Wi-Fi 是否已连接
    private boolean isWifiConnected() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected();
    }

    // 打开 WiFi 设置
    public void openWiFiSettings() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // 没有可用的应用程序来处理该 Intent
            Toast.makeText(this, "无法打开 Wi-Fi 设置", Toast.LENGTH_SHORT).show();
        }
    }

    // 启动计时器
    private void startTimer() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(MainActivity.this::updateDateTime);
            }
        }, 0, 1000); // 每隔1秒更新一次时间
    }

    // 更新日期时间文本
    private void updateDateTime() {
        TextView textDateTime = findViewById(R.id.textDateTime);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        String dateTime = dateFormat.format(new Date());
        textDateTime.setText(dateTime);
    }


    // 设置 btnMenu 按钮的点击事件监听器
    private void setupMenuButton() {
        findViewById(R.id.btnMenu).setOnClickListener(this::showPopupMenu);
    }

    // 显示弹出菜单
    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(item -> {
            // 处理菜单项的点击事件
            int id = item.getItemId();
            if (id == R.id.menu_settings) {
                // 处理设置菜单项的点击事件
                openConfigFileForEditing();
                return true;
            } else if (id == R.id.menu_about) {
                // 处理关于菜单项的点击事件
                return true;
            } else if (id == R.id.menu_exit) {
                // 处理退出菜单项的点击事件
                finish(); // 关闭当前 Activity
                return true;
            } else {
                return false;
            }
        });
        // 从菜单资源文件中加载菜单项
        popupMenu.inflate(R.menu.menu_main);
        // 显示弹出菜单
        popupMenu.show();
    }

    // 启动 ConfigEditorActivity 并等待结果返回
    private static final int REQUEST_CONFIG_EDIT = 1;

    // 打开 config.json 文件进行编辑
    private void openConfigFileForEditing() {
        // 创建一个新的 Activity 或者 Dialog 来编辑配置文件
        Intent intent = new Intent(this, ConfigEditorActivity.class);
        startActivityForResult(intent, REQUEST_CONFIG_EDIT); // 使用请求码 REQUEST_CONFIG_EDIT
    }

    // 更新应用程序列表
    private void updateAppList() {
        List<String> presetAppNames = new ArrayList<>();
        List<String> presetAppIcons = new ArrayList<>();

        // 更新应用程序列表
        if (appAdapter != null) {
            appAdapter.setDataSet(presetAppNames, presetAppIcons);
            appAdapter.notifyDataSetChanged();
        }
    }
    // 处理从 ConfigEditorActivity 返回的结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CONFIG_EDIT) {
            if (resultCode == RESULT_OK) {
                // 这里你可以更新主页面的UI，比如更新RecyclerView等
                // 比如重新加载配置数据
                loadConfigData();
                // 然后更新RecyclerView等
                setupAppRecyclerView();
                setupHomeworkRecyclerView();
                setupNearbyRecyclerView();
                setupAddressRecyclerView();
                // 其他UI更新操作
            }
        }
    }




    // 处理权限请求结果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户已授予权限，更新应用程序列表
                updateAppList();
            } else {
                // 用户拒绝了权限请求，显示权限说明对话框
                showPermissionExplanationDialog();
            }
        }
    }

    // 显示权限说明对话框
    private void showPermissionExplanationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("权限说明");
        builder.setMessage("应用需要访问存储权限以显示应用列表，请允许权限以继续使用应用。");
        builder.setPositiveButton("确定", (dialog, which) -> {
            // 重新请求权限
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_CODE);
        });
        builder.setNegativeButton("取消", (dialog, which) -> {
            // 用户取消了权限请求，可以在这里添加相应的逻辑
        });
        builder.create().show();
    }

    // 注册和注销广播接收器
    @Override
    protected void onResume() {
        super.onResume();
        loadConfigData(); // 重新加载配置数据
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterNetworkReceiver();
    }

    private void registerNetworkReceiver() {
        if (networkReceiver == null) {
            networkReceiver = new NetworkReceiver();
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(networkReceiver, filter);
        }
    }

    private void unregisterNetworkReceiver() {
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
            networkReceiver = null;
        }
    }

    // 广播接收器类
    class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 在接收到网络状态变化的广播时更新 Wi-Fi 按钮图标
            if (intent.getAction() != null && intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                // MainActivity 中的 networkReceiver 已经是静态内部类，因此不需要额外处理
            }
        }
    }
}
