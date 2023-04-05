package com.mygdx.hattrick2023;

import com.badlogic.gdx.Gdx;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class MyClient extends GameClient {

    ExecutorService pool;
    String serverAddress = "192.168.50.54";
    AtomicBoolean isConnectedFlag = new AtomicBoolean(false);

    public MyClient(GameListener callback, String localAddress) {
        super(callback, localAddress, 45350);
    }

    @Override
    public void run() {
        // Remove pool-related code
        // Remove the first task.run() call
     //   Thread t = new Thread(new ReceiveThread());
   //     t.start();
        MainMenuScreen.debugText = "Looking for servers...";

        if (isConnected()) {
            Gdx.app.log("mygdxgame", "isConnected(): " + isConnected());
            MainMenuScreen.debugText = "Successfully connected to " + socket.getInetAddress();
          // Thread receiveThread = new Thread(new ReceiveThread());
          //  receiveThread.start();
        } else {
            // Create the ConnectThread and wait for it to finish
            ConnectThread connectThread = new ConnectThread(serverAddress);
            Thread connectThreadWrapper = new Thread(connectThread);
            connectThreadWrapper.start();

            // Wait for the ConnectThread to finish
            try {
                connectThreadWrapper.join();
            } catch (InterruptedException e) {
                // Log the interruption and handle it as needed
            }

            if (isConnected()) {
                MainMenuScreen.debugText = "Successfully connected to " + socket.getInetAddress();
            } else {
                callback.onConnectionFailed();
            }
        }
    }

    @Override
    public int getPlayerNumber() {
        return GameListener.PLAYER2;
    }

    @Override
    public void cancel() {
        if (isConnectedFlag.get()) {
            socket.close();
        }

        if (pool != null && pool.isShutdown() && !pool.isTerminated()) pool.shutdownNow();
    }

    private class ConnectThread implements Runnable {

        String address;

        public ConnectThread(String address) {
            this.address = address;
        }

        public void run() {
            try {
                InetAddress serverAddress = InetAddress.getByName(address);
                int port = 45351; // Replace this with your server's port number

                byte[] sendData = "Hello".getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);

                DatagramSocket tempSocket = new DatagramSocket();
                tempSocket.setReuseAddress(true);
                tempSocket.setSoTimeout(2000); // Set receive timeout to 2 seconds
                tempSocket.send(sendPacket);

                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                tempSocket.receive(receivePacket);

                String serverResponse = new String(receivePacket.getData(), 0, receivePacket.getLength());

                Gdx.app.log("MyClient", "Received response from server: " + serverResponse); // Add debug log for server response

                if (serverResponse.equals("Hello back")) {
                    socket = tempSocket;
                    isConnectedFlag.set(true);

                    Gdx.app.log("mygdxgame", "Successfully connected to " + address);
                    MainMenuScreen.debugText = "Successfully connected to " + address;
                    callback.onConnected();
                } else {
                    tempSocket.close();
                }
            } catch (SocketTimeoutException e) {
                Gdx.app.log("MyClient", "Connection timeout for address: " + address); // Add debug log for connection timeout
            } catch (IOException io) {
                MainMenuScreen.debugText += "\nConnection to " + address + " failed";
            }
        }
    }
    @Override
    public boolean isConnected() {
        return isConnectedFlag.get();
    }




}