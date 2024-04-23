package com.iyuanyue.car;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Handler;
import com.iyuanyue.car.Navigation.AmapNavigation;
import com.iyuanyue.car.MainActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iyuanyue.car.Navigation.SpeechRecognitionHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private final List<String> mData; // 数据集合
    private final List<String> mIconTypes; // 图标类型集合
    private final LayoutInflater mInflater; // 布局加载器
    private final Context mContext; // 上下文
    private final Map<String, Map<String, Map<String, String>>> configData; // 配置数据

    public static final String FILE_NAME = "config.json";
    private final String recyclerViewType;
    private MainActivity mActivity;


    // 构造函数
    public MyAdapter(MainActivity activity,Context context, List<String> data, List<String> iconTypes, Map<String, Map<String, Map<String, String>>> configData, String recyclerViewType) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mIconTypes = iconTypes;
        this.configData = configData;
        this.recyclerViewType = recyclerViewType;
        this.mActivity = activity;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 创建 ViewHolder 实例
        View view = mInflater.inflate(R.layout.list_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // 创建 AmapNavigation 实例
        AmapNavigation amapNavigation = new AmapNavigation(mContext, this);

        // 绑定数据到 ViewHolder
        String itemName = mData.get(position);
        holder.text.setText(itemName);

        // 获取应用图标
        String iconType = mIconTypes.get(position);
        Drawable appIcon = null;
        String packageName;

        if (configData != null && configData.containsKey("apps")) {
            Map<String, Map<String, String>> appConfig = configData.get("apps");

            if (configData != null && configData.containsKey("apps")) {
                SearchResult searchResult = findKeyForItem(itemName);
                String itemKey = searchResult.getKey();
                if (itemKey != null && appConfig.containsKey(itemKey)) {
                    Map<String, String> appInfo = appConfig.get(itemKey);
                    packageName = appInfo.get("packageName");
                    String iconValue = appInfo.get("icon");
                    if (iconValue != null) {
                        int iconResource = getIconResource(iconValue);
                        holder.button.setImageResource(iconResource);
                        return;
                    }
                } else {
                    packageName = null;
                }
            } else {
                packageName = null;
            }


        } else {
            packageName = null;
        }
        // 如果配置文件中没有图标值，则尝试获取应用程序的图标
        if (packageName != null) {
            appIcon = getAppIcon(packageName);
        }
        if (appIcon != null) {
            holder.button.setImageDrawable(appIcon);
        } else {
            // 如果无法获取应用图标，则使用默认图标
            int iconResource = getIconResource(iconType);
            holder.button.setImageResource(iconResource);
        }


// 如果是AppRecyclerView，则设置点击和长按监听器
        if (isAppRecyclerView(recyclerViewType)) {
            // 点击事件打开应用
            holder.button.setOnClickListener(v -> openApp(packageName));
            // 查找itemName对应的键
            SearchResult searchResult = findKeyForItem(itemName);
            String itemKey = searchResult.getKey();
            //长按选择app
            holder.button.setOnLongClickListener(v -> {
                showAppSelectionDialog(itemKey); // 将键传递给方法
                return true;
            });
        }

// 如果是AddressRecyclerView，则设置点击和长按监听器
        if (isAddressRecyclerView(recyclerViewType)) {
            // 点击事件打开应用
            holder.button.setOnClickListener(v -> {
                amapNavigation.openLocationInMap(itemName);
            });
            // 长按事件显示对话框
            holder.button.setOnLongClickListener(v -> {
                // 显示对话框
                showEditLocationDialog(itemName);
                return true; // 表示已处理长按事件
            });
        }

        // NearbyRecyclerView，则设置点击和长按监听器
        if (isNearbyRecyclerView(recyclerViewType)) {
            // 点击事件打开应用
            holder.button.setOnClickListener(v -> {
                amapNavigation.openNearbyLocationInMap(itemName);
            });
            // 长按事件显示对话框
            holder.button.setOnLongClickListener(v -> {
                // 显示对话框
                showEditLocationDialog(itemName);
                return true; // 表示已处理长按事件
            });
        }
        // HomeworkRecyclerView，则设置点击和长按监听器
        if (isHomeworkRecyclerView(recyclerViewType)) {
            // 判断 itemName 是否为 "语音识别"
            if (!"语音识别".equals(itemName)) {
                // 点击事件打开应用
                holder.button.setOnClickListener(v -> {
                    amapNavigation.openLocationInMap(itemName);
                });
                // 长按事件显示对话框
                holder.button.setOnLongClickListener(v -> {
                    // 显示对话框
                    showEditLocationDialog(itemName);
                    return true; // 表示已处理长按事件
                });
            } else {
                // 点击事件
                holder.button.setOnClickListener(v -> {
                    mActivity.startSpeechRecognition();
                });
                // 长按事件显示对话框
                holder.button.setOnLongClickListener(v -> {
                    Toast.makeText(holder.itemView.getContext(), "当前长按项为语音识别", Toast.LENGTH_SHORT).show();
                    return true; // 表示已处理长按事件
                });

            }
        }
    }
    // 新添加的方法，用于检查 RecyclerView 类型是否为 AppRecyclerView
    public boolean isAppRecyclerView (String recyclerViewType){
        return recyclerViewType.equals("AppRecyclerView");
    }
    // 新添加的方法，用于检查 RecyclerView 类型是否为 AddressRecyclerView
    public boolean isAddressRecyclerView (String recyclerViewType){
        return recyclerViewType.equals("AddressRecyclerView");
    }
    // 新添加的方法，用于检查 RecyclerView 类型是否为 NearbyRecyclerView
    public boolean isNearbyRecyclerView (String recyclerViewType){
        return recyclerViewType.equals("NearbyRecyclerView");
    }
    // 新添加的方法，用于检查 RecyclerView 类型是否为 HomeworkRecyclerView
    public boolean isHomeworkRecyclerView (String recyclerViewType){
        return recyclerViewType.equals("HomeworkRecyclerView");
    }

    private void showEditLocationDialog(String itemName) {
        // 获取地点信息
        SearchResult searchResult = findKeyForItem(itemName);
        String sectionName = searchResult.getSectionName();
        String itemKey = searchResult.getKey();

        // 获取地点信息
        Map<String, String> poiInfo = getPoiInfo(itemKey, sectionName);
        if (poiInfo != null) {
            String name = poiInfo.get("name");
            String lat = poiInfo.get("lat");
            String lon = poiInfo.get("lon");
            String icon = poiInfo.get("icon");

            // 创建对话框视图
            View dialogView = mInflater.inflate(R.layout.dialog_edit_location, null);
            EditText nameEditText = dialogView.findViewById(R.id.editText_name);
            EditText latEditText = dialogView.findViewById(R.id.editText_lat);
            EditText lonEditText = dialogView.findViewById(R.id.editText_lon);
            Spinner iconSpinner = dialogView.findViewById(R.id.spinner_icon); // 改为 Spinner 控件

            // 设置初始值
            nameEditText.setText(name);
            latEditText.setText(lat);
            lonEditText.setText(lon);

            // 准备图标名称数组
            String[] iconNames = { "home", "home1", "home2", "home3", "work" , "work1", "school", "apartment", "house","storefront","24hstore","florist"}; // 替换为实际的图标名称数组

            // 创建适配器并设置给 Spinner
            ArrayAdapter<String> iconAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, iconNames);
            iconAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            iconSpinner.setAdapter(iconAdapter);

            // 设置默认选中项
            int position = Arrays.asList(iconNames).indexOf(icon);
            iconSpinner.setSelection(position);

            // 创建对话框
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setView(dialogView)
                    .setTitle("编辑地点信息")
                    .setPositiveButton("保存", (dialog, which) -> {
                        // 获取用户输入的值
                        String newName = nameEditText.getText().toString();
                        String newLat = latEditText.getText().toString();
                        String newLon = lonEditText.getText().toString();
                        String newIcon = iconSpinner.getSelectedItem().toString();

                        // 更新地点信息
                        updateLocationInfo(itemKey, newName, newIcon, newLat, newLon);
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            // 如果未找到地点信息，则显示 Toast 提示
            Toast.makeText(mContext, "未找到地点信息", Toast.LENGTH_SHORT).show();
        }
    }
    // 更新地点信息并保存到配置文件
    private void updateLocationInfo(String itemName, String newName, String newIcon,String newLat, String newLon) {
        // 读取当前的配置数据
        String configJson = readConfigFile();
        if (configJson != null) {
            // 解析 JSON 字符串为 Map 结构
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Map<String, Map<String, String>>>>() {
            }.getType();
            Map<String, Map<String, Map<String, String>>> configData = gson.fromJson(configJson, type);

            // 更新配置数据
            if (configData != null && configData.containsKey("address")) {
                Map<String, Map<String, String>> addressConfig = configData.get("address");
                if (addressConfig != null && itemName != null && addressConfig.containsKey(itemName)) {
                    Map<String, String> addressInfo = addressConfig.get(itemName);
                    addressInfo.put("name", newName);
                    addressInfo.put("icon", newIcon);
                    addressInfo.put("lat", newLat);
                    addressInfo.put("lon", newLon);
                }
            }
            // 将更新后的配置数据写入到 config.json 文件中
            String updatedConfigJson = gson.toJson(configData);
            writeConfigFile(updatedConfigJson);

            // 更新后重新设置适配器并刷新 RecyclerView
            // 重新设置适配器
            if (onConfigSavedListener != null) {
                onConfigSavedListener.onConfigSaved();
            }
            // 延迟一段时间后重新打开主页面
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);
            }, 1); // 延迟时间为 1000 毫秒 (1 秒)
        }
    }
    // 创建一个简单的类来包含找到的值和它所在部分的名称
    public class SearchResult {
        private String sectionName;
        private String key;

        SearchResult(String sectionName, String key) {
            this.sectionName = sectionName;
            this.key = key;
        }

        public String getSectionName() {
            return sectionName;
        }

        public String getKey() {
            return key;
        }
    }

    // 修改 findKeyForItem 方法以返回 SearchResult 类型
    public SearchResult findKeyForItem(String itemName) {
        // 遍历每个部分（例如："apps", "nearby", "address", "homework"）
        for (Map.Entry<String, Map<String, Map<String, String>>> sectionEntry : configData.entrySet()) {
            String sectionName = sectionEntry.getKey(); // 获取部分名称
            Map<String, Map<String, String>> subsectionData = sectionEntry.getValue();
            // 检查每个部分中是否包含目标 itemName
            for (Map.Entry<String, Map<String, String>> entry : subsectionData.entrySet()) {
                Map<String, String> itemInfo = entry.getValue();
                if (itemInfo.containsValue(itemName)) {
                    return new SearchResult(sectionName, entry.getKey()); // 返回部分名称和匹配的键
                }
            }
        }
        return null; // 如果未找到匹配的键，则返回 null
    }

    @Override
    public int getItemCount() {
        return mData.size(); // 返回数据集合大小
    }

    // ViewHolder 类
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        ImageButton button;

        ViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text); // 文本视图
            button = itemView.findViewById(R.id.imagebutton_icon); // 图标按钮
        }
    }

    // 根据图标类型获取相应的资源 ID
    private int getIconResource(String iconType) {
        // 默认图标资源ID
        int defaultIconResourceId = R.drawable.icon_place_holder;

        // 检查配置数据是否为空
        if (configData != null) {
            // 遍历每个部分的配置数据
            for (Map.Entry<String, Map<String, Map<String, String>>> sectionEntry : configData.entrySet()) {
                String section = sectionEntry.getKey();
                Map<String, Map<String, String>> sectionData = sectionEntry.getValue();
                if (sectionData != null) {
                    // 检查具体项中的图标类型
                    for (Map.Entry<String, Map<String, String>> entry : sectionData.entrySet()) {
                        String icon = entry.getValue().get("icon");
                        if (icon != null && icon.equals(iconType)) {
                            // 将图标类型转换为资源名称
                            String iconName = "icon_" + icon;
                            int resourceId = getDrawableResourceId(iconName);
                            return resourceId;
                        }
                    }
                }
            }
        }
        // 如果未找到对应的资源文件，则返回默认图标资源ID
        return defaultIconResourceId;
    }

    // 辅助方法：根据资源名称获取资源ID
    private int getDrawableResourceId(String resourceName) {
        return mContext.getResources().getIdentifier(resourceName, "drawable", mContext.getPackageName());
    }

    // 获取应用图标
    private Drawable getAppIcon(String packageName) {
        try {
            ApplicationInfo applicationInfo = mContext.getPackageManager().getApplicationInfo(packageName, 0);
            return applicationInfo.loadIcon(mContext.getPackageManager());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    // 打开应用
    private void openApp(String packageName) {
        PackageManager packageManager = mContext.getPackageManager();
        if (packageManager != null) {
            try {
                mContext.startActivity(packageManager.getLaunchIntentForPackage(packageName));
            } catch (Exception e) {
                Toast.makeText(mContext, "无法打开应用程序", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 显示应用选择对话框
    private void showAppSelectionDialog(String itemName) {
        PackageManager packageManager = mContext.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> appList = packageManager.queryIntentActivities(intent, 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("选择应用");
        CharSequence[] appNames = new CharSequence[appList.size()];
        CharSequence[] packageNames = new CharSequence[appList.size()];
        int i = 0;
        for (ResolveInfo resolveInfo : appList) {
            appNames[i] = resolveInfo.loadLabel(packageManager);
            packageNames[i] = resolveInfo.activityInfo.packageName;
            i++;
        }
        builder.setItems(appNames, (dialog, which) -> {
            String selectedAppName = appNames[which].toString(); // 获取选定应用程序的名称
            String selectedPackageName = packageNames[which].toString(); // 获取选定应用程序的包名
            updateAppItem(itemName, selectedAppName, selectedPackageName); // 更新应用程序名称和包名到配置文件
        });
        builder.show();
    }

    // 更新应用程序名称和包名到配置文件
    private void updateAppItem(String itemName, String appName, String packageName) {
        // 读取当前的配置数据
        String configJson = readConfigFile();

        if (configJson != null) {
            // 解析 JSON 字符串为 Map 结构
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Map<String, Map<String, String>>>>() {}.getType();
            Map<String, Map<String, Map<String, String>>> configData = gson.fromJson(configJson, type);

            // 更新配置数据
            if (configData != null && configData.containsKey("apps")) {
                Map<String, Map<String, String>> appConfig = configData.get("apps");
                if (appConfig != null && appConfig.containsKey(itemName)) {
                    Map<String, String> appInfo = appConfig.get(itemName);
                    appInfo.put("name", appName);
                    appInfo.put("packageName", packageName);
                }
            }

            // 将更新后的配置数据写入到 config.json 文件中
            String updatedConfigJson = gson.toJson(configData);
            writeConfigFile(updatedConfigJson);

            // 更新后重新设置适配器并刷新 RecyclerView
            // 重新设置适配器
            if (onConfigSavedListener != null) {
                onConfigSavedListener.onConfigSaved();
            }
            // 延迟一段时间后重新打开主页面
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);
            }, 1); // 延迟时间为 1000 毫秒 (1 秒)
        }

            }

    // 读取 config.json 文件内容
    private String readConfigFile() {
        FileInputStream fis = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try {
            fis = mContext.openFileInput(FILE_NAME); // 使用 mContext 调用 openFileInput 方法
            reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
                if (reader.ready()) {
                    content.append("\n"); // 添加换行符
                }
            }
            return content.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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

    public interface OnConfigSavedListener {
        void onConfigSaved();
    }
    private OnConfigSavedListener onConfigSavedListener;

    public void setOnConfigSavedListener(OnConfigSavedListener listener) {
        this.onConfigSavedListener = listener;
    }


    // 将更新后的配置数据写入到 config.json 文件中
    private void writeConfigFile(String configJson) {
        FileOutputStream fos = null;
        BufferedWriter writer = null;
        try {
            fos = mContext.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(configJson);


        } catch (IOException e) {
            e.printStackTrace();
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
    // 获取地点point信息
    public Map<String, String> getPoiInfo(String itemName, String itemType) {
        // 读取当前的配置数据
        String configJson = readConfigFile();

        if (configJson != null) {
            // 解析 JSON 数据
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Map<String, Map<String, String>>>>() {}.getType();
            Map<String, Map<String, Map<String, String>>> configData = gson.fromJson(configJson, type);

        // 检查配置数据是否为 null，并获取相应的信息
        if (configData != null && configData.containsKey(itemType)) {
            Map<String, Map<String, String>> itemConfig = configData.get(itemType);
            if (itemConfig != null && itemConfig.containsKey(itemName)) {
                return itemConfig.get(itemName);
            }
        }
        }
        return null; // 如果未找到匹配的 poi 信息，则返回 null
    }


    private double getLatitude(String itemName) {
        // 读取当前的配置数据
        String configJson = readConfigFile();
        if (configJson != null) {
            // 解析 JSON 字符串为 Map 结构
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Map<String, Map<String, String>>>>() {}.getType();
            Map<String, Map<String, Map<String, String>>> configData = gson.fromJson(configJson, type);

            // 从配置数据中获取纬度
            if (configData != null && configData.containsKey("locations")) {
                Map<String, Map<String, String>> locationConfig = configData.get("locations");
                if (locationConfig != null && locationConfig.containsKey(itemName)) {
                    Map<String, String> locationInfo = locationConfig.get(itemName);
                    return Double.parseDouble(locationInfo.get("lat"));
                }
            }
        }
        return 0.0;
    }

    private double getLongitude(String itemName) {
        // 读取当前的配置数据
        String configJson = readConfigFile();
        if (configJson != null) {
            // 解析 JSON 字符串为 Map 结构
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Map<String, Map<String, String>>>>() {}.getType();
            Map<String, Map<String, Map<String, String>>> configData = gson.fromJson(configJson, type);

            // 从配置数据中获取经度
            if (configData != null && configData.containsKey("locations")) {
                Map<String, Map<String, String>> locationConfig = configData.get("locations");
                if (locationConfig != null && locationConfig.containsKey(itemName)) {
                    Map<String, String> locationInfo = locationConfig.get(itemName);
                    return Double.parseDouble(locationInfo.get("lon"));
                }
            }
        }
        return 0.0;
    }


}
