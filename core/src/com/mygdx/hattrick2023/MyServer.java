package com.mygdx.hattrick2023;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.utils.Timer;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

/**
 * Created by Administrator on 10-Nov-16.
 */
public class MyServer extends GameClient {
    private final Game game;
    private final DeviceAPI mController;
    private ArrayList<String> messagesToSend = new ArrayList<>();
    private int playerNumber;

    private boolean player1Connected = false;
    private boolean player2Connected = false;

    //  private static ServerSocket serverSocket, voiceServer;

    public MyServer(GameListener callback, String localAddress, int localPort, Game game, DeviceAPI mController, int PLAYERNUMBER) {
        super(callback, localAddress, localPort);
        this.game = game;
        this.mController = mController;
        this.messagesToSend = new ArrayList<>();
        this.playerNumber = PLAYERNUMBER;
        try {
            serverSocket = new DatagramSocket();
        } catch (SocketException e) {
            Gdx.app.log(DeviceAPI.TAG, "Failed to create DatagramSocket");
        }
    }


    @Override
    public void run() {
        MainMenuScreen.debugText = "Creating server on port no. " + GameClient.port + "\n";

        try {
            serverSocket.close();
        } catch (Exception e) {
        }
        try {
            //    voiceSocket.close();
        } catch (Exception e) {
        }
        try {
            serverSocket = new DatagramSocket(null); // Pass null to the constructor
            serverSocket.setReuseAddress(true); // Set the SO_REUSEADDR option
            serverSocket.bind(new InetSocketAddress(port)); // Bind the socket to the desired port
        } catch (Exception e) {
        }
        try {
            //   voiceServer.close();
        } catch (Exception e) {
        }

        // serverSocket = new DatagramSocket(port); // Remove this line
        // voiceServer = new DatagramSocket(voicePort);
        MainMenuScreen.debugText = "Created server at " + localAddress + " on port no. " + GameClient.port + "\n";

        Thread t1 = new Thread(new NormalServerThread(game, mController, MyServer.this));
        t1.start();

        // Thread t2 = new Thread(new VoiceServerThread());
        // t2.start();
    }

    public class NormalServerThread implements Runnable {
        private final Game game;
        private final DeviceAPI mController;
        private final GameClient gameClient;

        public NormalServerThread(Game game, DeviceAPI mController, GameClient gameClient) {
            this.game = game;
            this.mController = mController;
            this.gameClient = gameClient;

        }

        @Override
        public void run() {
            Thread receiveThread = new Thread(new ReceiveThread());
            receiveThread.start();

            try {
                while (!serverSocket.isClosed()) {
                    if (getPlayerNumber() == 2) {
                        gameClient.sendMessage2("Hello");
                    }
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);


                    String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    Gdx.app.log("MyServer", "Received message: " + receivedMessage); // Log received message
                    if (receivedMessage.equals("Hello")) {
                        byte[] sendData = "Hello back".getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                                receivePacket.getAddress(), receivePacket.getPort());
                        serverSocket.send(sendPacket);
                        Gdx.app.log("MyServer", "Sent 'Hello back' to the client"); // Log sent message
                        player2Connected = true;

                    }
                    callback.onConnected();
                        // Process other received packets here.
                        callback.onMessageReceived(receiveData, receivePacket.getLength());
                        Gdx.app.log("MyServer", "Packet sent to callback: " + receivedMessage);
                    }


            } catch (Exception e) {
                throw new RuntimeException(e);

            } finally {
                // The following lines should be outside the loop, but inside the run() method.
                callback.onDisconnected();
            }
        }



    }


    @Override
    public void cancel() {
        serverSocket.close();
        // voiceServer.close();
    }




    public void addMessageToSend(String message) {
        synchronized (messagesToSend) {
            messagesToSend.add(message);
        }
        Gdx.app.log("MyServer", "Added to send queue: " + message);
    }

    @Override
    public int getPlayerNumber() {
        return playerNumber;
    }

}
