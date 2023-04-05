package com.mygdx.hattrick2023;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by Administrator on 10-Nov-16.
 */
public class MyClient2 extends GameClient implements GameClientInterface {


    private final Game game;
    private final DeviceAPI mController;
    private GameScreen gameScreen;
    private ArrayList<String> messagesToSend = new ArrayList<>();

    //  private static ServerSocket serverSocket, voiceServer;

    public MyClient2(GameListener callback, String localAddress, int localPort, Game game, DeviceAPI mController) {
        super(callback, localAddress, localPort);
        this.callback = callback; // Set the listener directly with the callback parameter
        this.game = game;
        this.mController = mController;
        this.messagesToSend = new ArrayList<>();
        try {
            serverSocket = new DatagramSocket();
        } catch (SocketException e) {
            Gdx.app.log(DeviceAPI.TAG, "Failed to create DatagramSocket");
        }
    }

    @Override
    public void run() {

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

        Thread t1 = new Thread(new NormalServerThread(game, mController, MyClient2.this, callback));
        t1.start();
        // Thread t2 = new Thread(new VoiceServerThread());
        // t2.start();
    }

    public class NormalServerThread implements Runnable {
        private final Game game;
        private final DeviceAPI mController;
        private final GameClient gameClient;

        GameListener callback;


        public NormalServerThread(Game game, DeviceAPI mController, GameClient gameClient, GameListener callback) {
            this.game = game;
            this.mController = mController;
            this.gameClient = gameClient;
            this.callback = callback;
        }

        @Override
        public void run() {
            Thread receiveThread = new Thread(new ReceiveThread());
            receiveThread.start();
            try {
                while (!serverSocket.isClosed()) {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);

                    String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    if (callback != null) {
                        callback.onMessageReceived(receivePacket.getData(), receivePacket.getLength());
                        Gdx.app.log("MyClient2", "Packet sent to callback: " + receivedMessage);
                    } else {
                        Gdx.app.log("MyClient2", "Received message but callback null"); // Log received message
                    }
                    Gdx.app.log("MyClient2", "Received message: " + receivedMessage); // Log received message
                    if (receivedMessage.equals("Hello")) {
                        callback.onConnected();
                        byte[] sendData = "Hello back".getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                                receivePacket.getAddress(), receivePacket.getPort());
                        serverSocket.send(sendPacket);
                        Gdx.app.log("MyClient2", "Sent 'Hello back' to the client"); // Log sent message

                    } else {

                    }
                }


            } catch (Exception e) {
                throw new RuntimeException(e);

            } finally {
                // The following lines should be outside the loop, but inside the run() method.
             //   callback.onDisconnected();
            }
        }



    }


    @Override
    public void cancel() {
        serverSocket.close();
        // voiceServer.close();
    }


    @Override
    public void setListener(GameListener callback) {
        this.callback = callback;
    }

    public void addMessageToSend(String message) {
        synchronized (messagesToSend) {
            messagesToSend.add(message);
        }
        Gdx.app.log("MyServer", "Added to send queue: " + message);
    }

    @Override
    public int getPlayerNumber() {
        return GameListener.PLAYER2;
    }


}
