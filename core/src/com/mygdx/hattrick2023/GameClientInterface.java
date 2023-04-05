package com.mygdx.hattrick2023;

import java.net.Socket;

/**
 * Created by Administrator on 10-Nov-16.
 */
public interface GameClientInterface extends Runnable {
    int port = 45351;
    int voicePort = 45365;

    boolean isConnected();
    void onConnected();
    void disconnect();
    void setListener(GameListener callback);
    void sendMessage(String message);

    void sendMessage2(String message);

    // void sendVoiceMessage(byte[] message);
    int getPlayerNumber();
    void cancel();

    @Override
    void run();
}
