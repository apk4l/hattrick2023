package com.mygdx.hattrick2023;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer implements Runnable {
    private int playerNumber = 1; // initialize playerNumber to 1
    private GameScreen gameScreen;
    private DatagramSocket serverSocket;
    private DatagramPacket receivePacket;
    private DatagramPacket sendPacket;
    private InetAddress clientAddress;
    private int clientPort;
    private boolean running;

    public UDPServer(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    public void start() {
        running = true;
        new Thread(this).start();
    }

    public void stop() {
        running = false;
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    @Override
    public void run() {
        try {
            serverSocket = new DatagramSocket(7777);
            byte[] receiveData = new byte[1024];
            byte[] sendData = new byte[1024];
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            sendPacket = new DatagramPacket(sendData, sendData.length);

            Gdx.app.log("UDPServer", "Server started");

            while (running) {
                serverSocket.receive(receivePacket);

                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                clientAddress = receivePacket.getAddress();
                clientPort = receivePacket.getPort();

                Gdx.app.log("UDPServer", "Received message from " + clientAddress + ":" + clientPort + " - " + message);

                // Check message type
                if (message.equals("join")) {
                    if (playerNumber == 1) {
                        // first player to join
                        // create myPlayer and send "1" to client
                        playerNumber = 2;
                        sendPacket.setData("1".getBytes());
                    } else {
                        // second player to join
                        // create otherPlayer and send "2" to client
                        playerNumber = 1;
                        sendPacket.setData("2".getBytes());
                    }
                    sendPacket.setAddress(clientAddress);
                    sendPacket.setPort(clientPort);
                    serverSocket.send(sendPacket);
                }
                if (message.startsWith("update")) {
                    String[] parts = message.split(",");
                    if (parts.length == 4) {
                        float x = Float.parseFloat(parts[1]);
                        float y = Float.parseFloat(parts[2]);
                        boolean isMyPlayer = Boolean.parseBoolean(parts[3]);
                        if (isMyPlayer) {
                            gameScreen.getMyPlayer(x, y);
                        } else {
                            gameScreen.getOtherPlayer(x, y);
                        }
                    }
                } else if (message.startsWith("puck")) {
                    String[] parts = message.split(",");
                    if (parts.length == 3) {
                        float x = Float.parseFloat(parts[1]);
                        float y = Float.parseFloat(parts[2]);
                        gameScreen.getPuckPosition(x, y);
                    }
                }
            }
        } catch (Exception e) {
            Gdx.app.error("UDPServer", "Error running server", e);
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }

    public void disconnect() {
    }

    public int getPlayerNumber() {
        return 0;
    }

}
