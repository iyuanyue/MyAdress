package com.iyuanyue.car.Config;

import java.util.Map;

public class Config {
    private Map<String, AppInfo> apps;
    private Map<String, NearbyInfo> nearby;
    private Map<String, AddressInfo> address;

    public Map<String, AppInfo> getApps() {
        return apps;
    }

    public Map<String, NearbyInfo> getNearby() {
        return nearby;
    }

    public Map<String, AddressInfo> getAddress() {
        return address;
    }
}
