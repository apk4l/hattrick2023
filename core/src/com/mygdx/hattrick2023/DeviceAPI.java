package com.mygdx.hattrick2023;

import java.util.List;

/**
 * Created by Administrator on 04-Nov-16.
 */
public interface DeviceAPI {

    String TAG = "mygdxgame";

    String getIpAddress();
    boolean isConnectedToLocalNetwork();

    void showNotification(String message);
    void setCallback(com.mygdx.hattrick2023.GameClientInterface callback);
//    List<Integer> getValidSampleRates();

    int getBufferSize();
}
