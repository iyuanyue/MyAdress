package com.iyuanyue.car;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private final List<String> mData; // 数据集合
    private final List<String> mIconTypes; // 图标类型集合
    private final LayoutInflater mInflater; // 布局加载器
    private final Context mContext; // 上下文
    private final Map<String, Map<String, Map<String, String>>> configData; // 配置数据
    private final boolean isAppRecyclerView; // 标识当前 RecyclerView 是否用于显示应用程序

    // 构造函数
    public MyAdapter(Context context, List<String> data, List<String> iconTypes, Map<String, Map<String, Map<String, String>>> configData, boolean isAppRecyclerView) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mIconTypes = iconTypes;
        this.configData = configData;
        this.isAppRecyclerView = isAppRecyclerView;
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
        // 绑定数据到 ViewHolder
        String itemName = mData.get(position);
        holder.text.setText(itemName);

        // 设置图标
        String iconType = mIconTypes.get(position);
        int iconResource = getIconResource(iconType);
        holder.button.setImageResource(iconResource);

        // 点击事件打开应用
        holder.button.setOnClickListener(v -> openApp(iconType));

        // 如果是应用程序的 RecyclerView，则设置长按监听器
        if (isAppRecyclerView) {
            holder.button.setOnLongClickListener(v -> {
                showAppSelectionDialog(iconType);
                return true;
            });
        }
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
    private void showAppSelectionDialog(String iconType) {
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
            String selectedPackageName = packageNames[which].toString();
            updateAppItem(iconType, selectedPackageName);
        });
        builder.show();
    }

    // 更新应用程序图标和包名
    private void updateAppItem(String iconType, String packageName) {
        if (configData != null && configData.containsKey("apps")) {
            Map<String, Map<String, String>> appConfig = configData.get("apps");
            if (appConfig != null && appConfig.containsKey(iconType)) {
                Map<String, String> appInfo = appConfig.get(iconType);
                appInfo.put("packageName", packageName);
                notifyDataSetChanged(); // 通知适配器数据已更改
            }
        }
    }

    public void setDataSet(List<String> data, List<String> iconTypes) {
        mData.clear();
        mIconTypes.clear();
        mData.addAll(data);
        mIconTypes.addAll(iconTypes);
        notifyDataSetChanged();
    }
}
