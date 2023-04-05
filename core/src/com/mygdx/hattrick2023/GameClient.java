package com.mygdx.hattrick2023;

import com.badlogic.gdx.Gdx;
import com.mygdx.hattrick2023.DeviceAPI;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

/**
 * Created by Administrator on 29-Nov-16.
 */
abstract public class GameClient implements GameClientInterface {

    static DatagramSocket socket, voiceSocket;
    InetAddress serverAddress;
    int serverPort;
    int localPort;
    DatagramSocket serverSocket;
    GameListener callback;
    String localAddress;

    public GameClient(GameListener callback, String localAddress, int localPort) {
        this.callback = callback;
        this.localAddress = localAddress;
        this.localPort = localPort;
        initializeSocket();
    }

    @Override
    abstract public void run();

    @Override
    abstract public int getPlayerNumber();

    @Override
    abstract public void cancel();

    public String getLocalSubnet() {
        String[] bytes = localAddress.split("\\.");
        return bytes[0] + "." + bytes[1] + "." + bytes[2] + ".";
    }

    @Override
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    @Override
    public void onConnected() {
        callback.onConnected();
    }

    @Override
    public void disconnect() {
        sendMessage("Disconnect");
        //    socket.close();
        // voiceSocket.close();
    }

    @Override
    public void setListener(GameListener callback) {
        this.callback = callback;
    }

    @Override
    public void sendMessage(String message) {
        try {
            byte[] sendData = message.getBytes();
            InetAddress serverAddress = InetAddress.getByName("192.168.50.5"); // Replace with the client's IP address
            int serverPort = 45351; // Replace with the client's port
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);

            // Add debug logs
            Gdx.app.log(DeviceAPI.TAG, "Sending message: " + message);
            Gdx.app.log(DeviceAPI.TAG, "Message length: " + sendData.length);
            Gdx.app.log(DeviceAPI.TAG, "Destination address: " + serverAddress.getHostAddress());
            Gdx.app.log(DeviceAPI.TAG, "Destination port: " + serverPort);

            socket.send(sendPacket);
        } catch (IOException io) {
            Gdx.app.log(DeviceAPI.TAG, "Failed to send message");
        }
    }

    @Override
    public void sendMessage2(String message) {
        try {
        //   if (isConnected()) {
                byte[] sendData = message.getBytes();
                InetAddress serverAddress = InetAddress.getByName("192.168.50.54"); // Replace with the client's IP address
                int serverPort = 45351; // Replace with the client's port
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);

                // Add debug logs
                Gdx.app.log(DeviceAPI.TAG, "Sending message: " + message);
                Gdx.app.log(DeviceAPI.TAG, "Message length: " + sendData.length);
                Gdx.app.log(DeviceAPI.TAG, "Destination address: " + serverAddress.getHostAddress());
                Gdx.app.log(DeviceAPI.TAG, "Destination port: " + serverPort);

                socket.send(sendPacket);
           // } else {
          //      Gdx.app.log(DeviceAPI.TAG, "Not connected, cannot send message");
           // }
        } catch (IOException io) {
            Gdx.app.log(DeviceAPI.TAG, "Failed to send message");
        }
    }

    //  @Override
    // public void sendVoiceMessage(byte[] message) {
    //   try {
    //      DatagramPacket sendPacket = new DatagramPacket(message, message.length, clientAddress, clientPort);
    //      voiceSocket.send(sendPacket);
    //   } catch (Exception e) {
    //        Gdx.app.log(DeviceAPI.TAG, "Failed to send voice: " + e.toString());
    //     }
    //   }


    class ReceiveThread implements Runnable {
        @Override
        public void run() {
            while (!socket.isClosed()) {
                try {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    Gdx.app.log("MyClient", "ReceiveThread started and listening for messages.");
                    socket.receive(receivePacket);

                    String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    Gdx.app.log("MyClient", "Received message: " + receivedMessage); // Log received message

                    // Process received packets here.
                    // Pass the received data and length to the onMessageReceived method.
                //    callback.onMessageReceived(receiveData, receivePacket.getLength());

                } catch (SocketTimeoutException e) {
                    Gdx.app.log("MyClient", "ReceiveThread timed out. Waiting for the next packet...");
                } catch (IOException e) {
                    if (socket.isClosed()) {
                        Gdx.app.log("MyClient", "Socket closed, stopping ReceiveThread.");
                        break;
                    } else {
                        Gdx.app.log("MyClient", "Error in ReceiveThread: " + e.getMessage());
                    }
                } catch (Exception e) {
                    Gdx.app.log("MyClient", "Error in ReceiveThread: " + e.getMessage());
                }
            }
        }
    }

    //  protected class VoiceReceiveThread implements Runnable {
    //    public void run() {
    //       Gdx.app.log(DeviceAPI.TAG, "voiceReceiveThread started");

    //        callback.getDeviceAPI().startRecording();

    //    while (isConnected()) {
    //        try {
    //            byte[] message = new byte[callback.getDeviceAPI().getBufferSize()];

    //           DatagramPacket receivePacket = new DatagramPacket(message, message.length);
    //          voiceSocket.receive(receivePacket);

    //        callback.getDeviceAPI().transmit(message);
    //     } catch (IOException io) {}
    //   }
    //   }
    //  }
    public void initializeSocket() {
        try {
            // Initialize the socket with the specific port number
            socket = new DatagramSocket(null); // Pass null to the constructor
            socket.setReuseAddress(true); // Set the SO_REUSEADDR option
            socket.bind(new InetSocketAddress(localPort)); // Bind the socket to the desired port
        } catch (SocketException se) {
            Gdx.app.log(DeviceAPI.TAG, "Failed to initialize socket: " + se.toString());
        }
    }
}