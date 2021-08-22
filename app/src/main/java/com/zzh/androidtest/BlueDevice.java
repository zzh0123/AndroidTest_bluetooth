package com.zzh.androidtest;

/**
 * Author: zzhh
 * Date: 2021/8/22 21:37
 * Description:
 */
public class BlueDevice {
    public String name;
    public String address;
    public int state;

    public BlueDevice() {
        name = "";
        address = "";
        state = 0;
    }

    public BlueDevice(String name, String address, int state) {
        this.name = name;
        this.address = address;
        this.state = state;
    }
}