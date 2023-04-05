package com.mygdx.hattrick2023;

import java.net.Socket;

/**
 * Created by Administrator on 25-Nov-16.
 */
public interface GameListener {

    int PLAYER1 = 1;
    int PLAYER2 = 2;

    int GAME_WIDTH = 320;
    int GAME_HEIGHT = 480;

    void onConnected();
    void onDisconnected();

    void onConnectionFailed();

    void onMessageReceived(byte[] data, int length);

    com.mygdx.hattrick2023.DeviceAPI getDeviceAPI();
}
