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

    String clientAdd = "";
    String serverAdd;

    //  private static ServerSocket serverSocket, voiceServer;

    public MyServer(GameListener callback, String localAddress, int localPort, Game game, DeviceAPI mController, int PLAYERNUMBER) {
        super(callback, localAddress, localPort);
        this.game = game;
        this.mController = mController;
        this.messagesToSend = new ArrayList<>();
        this.playerNumber = PLAYERNUMBER;
        serverAdd = "";
        clientAdd = "";
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

        private volatile boolean keepSendingHello = true;

        public NormalServerThread(Game game, DeviceAPI mController, GameClient gameClient) {
            this.game = game;
            this.mController = mController;
            this.gameClient = gameClient;

        }

        @Override
        public void run() {
            Thread receiveThread = new Thread(new ReceiveThread());
            receiveThread.start();

            // Start a new thread to send Hello messages using the new Runnable class
            Thread sendHelloThread = new Thread(new SendHelloRunnable(this));
            sendHelloThread.start();

            try {

                while (!serverSocket.isClosed()) {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    if (getPlayerNumber() == 2) {
                            if (receivedMessage.equals("Hello back")) {
                                Gdx.app.log("Client", "Received Hello Back from server"); // Log sent message
                                player2Connected = true;
                                callback.onConnected();
                                // Save the IP address for future communication
                                serverAdd = receivePacket.getAddress().getHostAddress();
                                gameClient.setServer(serverAdd);
                                Gdx.app.log("Client", "connected"); // Log sent message
                                keepSendingHello = false; // Stop sending "Hello" messages

                            }

                    } else if (getPlayerNumber() == 1) {

                        Gdx.app.log("MyServer", "Received message: " + receivedMessage); // Log received message

                        if (receivedMessage.equals("Hello")) {
                            byte[] sendData = "Hello back".getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                                    receivePacket.getAddress(), receivePacket.getPort());
                            clientAdd = receivePacket.getAddress().getHostAddress();
                            gameClient.setClient(clientAdd);
                            serverSocket.send(sendPacket);
                            Gdx.app.log("MyServer", "Sent 'Hello back' to the client"); // Log sent message
                            player1Connected = true;
                            callback.onConnected();

                        }


                    }
                    // Process other received packets here.
                    callback.onMessageReceived(receiveData, receivePacket.getLength());
                    Gdx.app.log("Packet", "Packet sent to callback: " + receivedMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                // The following lines should be outside the loop, but inside the run() method.
                callback.onDisconnected();
            }
        }
        public class SendHelloRunnable implements Runnable {
            private final NormalServerThread normalServerThread;

            public SendHelloRunnable(NormalServerThread normalServerThread) {
                this.normalServerThread = normalServerThread;
            }

            @Override
            public void run() {
                normalServerThread.sendHelloMessages();
            }
        }
        public void sendHelloMessages() {
            try {
                if (getPlayerNumber() == 2) {
                    // Get the local IP address
                    InetAddress localIP = InetAddress.getByName(localAddress);
                    // Extract the subnet from the IP address
                    String subnet = localIP.getHostAddress().substring(0, localIP.getHostAddress().lastIndexOf('.') + 1);

                    // Loop through all possible IPs on the local subnet
                    for (int i = 1; i <= 254 && keepSendingHello; i++) {
                        InetAddress targetIP = InetAddress.getByName(subnet + i);
                        byte[] sendData = "Hello".getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, targetIP, 45351);
                        serverSocket.send(sendPacket);
                        Gdx.app.log("Client", "Sent hello packet to " + targetIP); // Log received message
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
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
