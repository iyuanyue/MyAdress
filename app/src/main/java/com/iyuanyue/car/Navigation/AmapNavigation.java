package com.iyuanyue.car.Navigation;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.iyuanyue.car.MyAdapter;

import java.util.List;
import java.util.Map;

public class AmapNavigation {

    private Context mContext;
    private MyAdapter mAdapter;


    public AmapNavigation(Context context, MyAdapter adapter) {
        this.mContext = context;
        this.mAdapter = adapter;
    }

    // 打开附近项目地点
    public void openNearbyLocationInMap(String itemName) {
        // 查找itemName对应的键
        MyAdapter.SearchResult searchResult = mAdapter.findKeyForItem(itemName);
        String sectionName = searchResult.getSectionName();
        String itemKey = searchResult.getKey();
        Map<String, String> poiInfo = mAdapter.getPoiInfo(itemKey, sectionName);

        if (poiInfo != null) {
            String name = poiInfo.get("name");

            // 创建 Intent 对象
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("androidamap://keywordNavi?sourceApplication=softname&keyword=" + name + "&style=7"));
            intent.setPackage("com.autonavi.minimap");

            // 检查是否存在能够响应该 Intent 的应用
            PackageManager packageManager = mContext.getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
            boolean isIntentSafe = activities.size() > 0;
            Log.d("ItemName111", "itemName: " + activities.size()); // 添加此行以记录 itemName 的值

            // 如果存在能够响应该 Intent 的应用，则启动该 Intent
            if (isIntentSafe) {
                mContext.startActivity(intent);
            } else {
                // 如果没有应用能够响应该 Intent，则显示 Toast 提示
                Toast.makeText(mContext, "未安装高德地图应用", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 如果未找到地点信息，则显示 Toast 提示
            Toast.makeText(mContext, "未找到附近项目信息", Toast.LENGTH_SHORT).show();
        }
    }

    //打开高德进行导航
    public void openLocationInMap(String itemName) {
        // 查找itemName对应的键
        MyAdapter.SearchResult searchResult = mAdapter.findKeyForItem(itemName);
        String sectionName = searchResult.getSectionName();
        String itemKey = searchResult.getKey();
        Map<String, String> poiInfo = mAdapter.getPoiInfo(itemKey, sectionName);
        if (poiInfo != null) {
            String poiName = poiInfo.get("name");
            double latitude = Double.parseDouble(poiInfo.get("lat"));
            double longitude = Double.parseDouble(poiInfo.get("lon"));

            // 创建 Intent 对象
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("androidamap://navi?sourceApplication=app-name&poiname=" + poiName + "&lat=" + latitude + "&lon=" + longitude + "&dev=1&style=2"));
            intent.setPackage("com.autonavi.minimap");

            // 检查是否存在能够响应该 Intent 的应用
            PackageManager packageManager = mContext.getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
            boolean isIntentSafe = activities.size() > 0;

            // 如果存在能够响应该 Intent 的应用，则启动该 Intent
            if (isIntentSafe) {
                mContext.startActivity(intent);
            } else {
                // 如果没有应用能够响应该 Intent，则显示 Toast 提示
                Toast.makeText(mContext, "未安装高德地图应用", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 如果未找到地点信息，则显示 Toast 提示
            Toast.makeText(mContext, "未找到地点信息", Toast.LENGTH_SHORT).show();
        }
    }
}
